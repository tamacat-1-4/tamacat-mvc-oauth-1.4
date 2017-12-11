/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			SignedJWT jwt = config.getOAuth2CodeGenerator().parseSignedJWT(token);
			if (jwt != null) {
				//if (config.getOAuth2CodeGenerator().validateAccessToken(jwt)) {
				//}
				req.setAttribute("Authorized.JWT", jwt);
			}
			return;
		}
		throw new UnauthorizedException();
	}
	
	public void useOAuth2BearerAuthorization(boolean useOAuth2BearerAuthorization) {
		this.useOAuth2BearerAuthorization = useOAuth2BearerAuthorization;
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
