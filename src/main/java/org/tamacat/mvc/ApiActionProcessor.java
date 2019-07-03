/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.action.Action;
import org.tamacat.mvc.action.ActionDefine;
import org.tamacat.mvc.action.ActionProcessor;
import org.tamacat.mvc.error.ForbiddenException;
import org.tamacat.mvc.error.HttpStatusException;
import org.tamacat.mvc.error.InternalServerErrorException;
import org.tamacat.mvc.error.InvalidRequestException;
import org.tamacat.mvc.error.NotFoundException;
import org.tamacat.mvc.error.UnauthorizedException;
import org.tamacat.mvc.oauth.config.OAuthProviderConfig;
import org.tamacat.mvc.oauth.error.MethodNotAllowedException;
import org.tamacat.mvc.oauth.provider.jwt.JsonWebToken;
import org.tamacat.mvc.oauth.util.AuthorizationUtils;
import org.tamacat.mvc.util.ServletUtils;
import org.tamacat.util.ClassUtils;
import org.tamacat.util.IOUtils;
import org.tamacat.util.StringUtils;

import com.nimbusds.jwt.SignedJWT;

public class ApiActionProcessor extends ActionProcessor {
	static final Log LOG = LogFactory.getLog(ApiActionProcessor.class);
	
	static final String ACTION_KEY = "org.tamacat.mvc.Action";
	static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	static final String DEFAULT_OAUTH_API_PROPS = "oauth2-provider.properties";
	
	protected OAuthProviderConfig config;
	protected boolean useOAuth2BearerAuthorization = true;
	
	protected boolean allowTrustSelfSignedCertificates;
	protected boolean disabledSSLHostnameVerifier;
	
	public ApiActionProcessor() {
		this(new OAuthProviderConfig(DEFAULT_OAUTH_API_PROPS));
	}
	
	public ApiActionProcessor(OAuthProviderConfig config) {
		this.config = config;
	}
	
	@Override
	public void execute(ActionDefine actionDef, HttpServletRequest req, HttpServletResponse resp) {
		if (useOAuth2BearerAuthorization) {
			validateAccessToken(req, resp);
		}
		setAccessControlAllowResponse(resp);
		
		ServletUtils.setActionDefine(req, actionDef);
		Class<?> type = ClassUtils.forName(actionDef.getName());
		Action action = null;
		Method m = null;
		if (type != null) {
			m = ClassUtils.getMethod(type, actionDef.getAction(),
					HttpServletRequest.class, HttpServletResponse.class);
			if (m != null) {
				action = m.getAnnotation(Action.class);
				HttpMethodConstraint allowMethods = m.getAnnotation(HttpMethodConstraint.class);
				if (allowMethods != null) {
					if (req.getMethod().equals(allowMethods.value()) == false) {
						throw new MethodNotAllowedException();
					}
				}
			}
			if (action == null) {
				action = type.getAnnotation(Action.class);
				//throw new NotFoundException();
			}
			req.setAttribute(ACTION_KEY, action);
		} else {
			throw new NotFoundException();
		}
		//Access Control
		checkUserInRoles(action, req, resp);

		//Set Content-Type response header
		setContentType(action, req, resp);
		if (m != null) {
			try {
				//LOG.debug("actionName=" + actionDef.getActionName());
				Object o = ClassUtils.newInstance(type);
				try {
					m.invoke(o, req, resp);
				} finally {
					if (o instanceof AutoCloseable) {
						IOUtils.close(o);
					}
				}
			} catch (IllegalAccessException e) {
				throw new ForbiddenException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				// ignore
				//throw new NotFoundException();
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof InvalidRequestException) {
					throw (InvalidRequestException)e.getCause();
				}
				if (e.getCause() instanceof HttpStatusException) {
					throw (HttpStatusException) e.getCause();
				}
				throw new InternalServerErrorException(
					e.getMessage(), e.getCause());
			}
		} else {
			throw new NotFoundException();
		}
	}
	
	@Override
	protected void setContentType(Action action, HttpServletRequest req, HttpServletResponse resp) {
		resp.setContentType(JSON_CONTENT_TYPE);
	}

	public void validateAccessToken(HttpServletRequest req, HttpServletResponse resp) {
		//Check access_token
		String token = AuthorizationUtils.getBearerAccessToken(req);
		LOG.trace("validate access_token="+token);
		if (StringUtils.isNotEmpty(token)) {
			if (StringUtils.isNotEmpty(config.getIntrospectEndpoint())) {
				JsonWebToken jwt = introspect(token);
				req.setAttribute("Authorized.JWT", jwt);
			} else {
				SignedJWT jwt = config.getOAuth2CodeGenerator().parseSignedJWT(token);
				if (jwt != null) {
					//if (config.getOAuth2CodeGenerator().validateAccessToken(jwt))
					req.setAttribute("Authorized.JWT", jwt);
				}
			}
			return;
		}
		throw new UnauthorizedException();
	}
	
	protected JsonWebToken introspect(String accessToken) {
		JsonWebToken jwt = new JsonWebToken();
		try {
			String endpoint = config.getIntrospectEndpoint();
			LOG.debug(endpoint);
			HttpPost request = new HttpPost(endpoint);
			request.setHeader("Content-Type", "application/x-www-form-urlencoded");
			request.setHeader("Authorization", "Bearer "+accessToken);
			
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("token", accessToken));
			request.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			
			LOG.debug(request);
			HttpClientBuilder builder = HttpClients.custom();
			if (allowTrustSelfSignedCertificates) {
				builder.setSSLContext(SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build());
			}
			if (disabledSSLHostnameVerifier) {
				builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
			}
			
			HttpClient client = builder.build();
			HttpResponse response = client.execute(request);
			LOG.debug(response.getStatusLine());
			if (response.getStatusLine().getStatusCode() < 300) {
				JsonObject json = Json.createReader(response.getEntity().getContent()).readObject();
				//LOG.debug(StringUtils.formatJson(json.toString()));
				boolean active = false;
				if (json.containsKey("active")) {
					active = json.getBoolean("active");
				}
				if (active) {
					if (json.containsKey("tid")) {
						jwt.tid(json.getString("tid"));
					}
					if (json.containsKey("upn")) {
						jwt.upn(json.getString("upn"));
					}
					if (json.containsKey("client_id")) {
						jwt.audience(json.getString("client_id"));
					}
					if (json.containsKey("app_id")) {
						jwt.set("app_id", json.getString("app_id"));
					}
					if (json.containsKey("app_name")) {
						jwt.set("app_name", json.getString("app_name"));
					}
					if (json.containsKey("iss")) {
						jwt.issuer(json.getString("iss"));
					}
					if (json.containsKey("scopes")) {
						jwt.set("scopes", json.getString("scopes"));
					}
					if (json.containsKey("iat")) {
						jwt.issueAt(json.getJsonNumber("iat").longValue());
					}
					if (json.containsKey("exp")) {
						jwt.expiration(json.getJsonNumber("exp").longValue());
					}
				}
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
		return jwt;
	}
	
	public void setUseOAuth2BearerAuthorization(boolean useOAuth2BearerAuthorization) {
		this.useOAuth2BearerAuthorization = useOAuth2BearerAuthorization;
	}
	
	/**
	 * Allow trust Self-signed certification. (for Development/Test use)
	 * @since 1.4-20190702
	 */
	public void setAllowTrustSelfSignedCertificates(boolean allowTrustSelfSignedCertificates) {
		this.allowTrustSelfSignedCertificates = allowTrustSelfSignedCertificates;
	}
	
	/**
	 * Disabled SSL hostname verifier. (for Development/Test use)
	 * @since 1.4-20190702
	 */
	public void setDisabledSSLHostnameVerifier(boolean disabledSSLHostnameVerifier) {
		this.disabledSSLHostnameVerifier = disabledSSLHostnameVerifier;
	}
	
	protected void setAccessControlAllowResponse(HttpServletResponse resp) {
		String accessControlAllowOrigin = config.getAccessControlAllowOrigin();
		if (StringUtils.isNotEmpty(accessControlAllowOrigin)) {
			resp.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
		}
		String accessControlAllowMethods = config.getAccessControlAllowMethods();
		if (StringUtils.isNotEmpty(accessControlAllowMethods)) {
			resp.setHeader("Access-Control-Allow-Methods", accessControlAllowMethods);
		}
		String accessControlAllowHeaders = config.getAccessControlAllowHeaders();
		if (StringUtils.isNotEmpty(accessControlAllowHeaders)) {
			resp.setHeader("Access-Control-Allow-Headers", accessControlAllowHeaders);
		}
	}
}
