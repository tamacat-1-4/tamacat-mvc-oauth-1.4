/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.error.NotFoundException;
import org.tamacat.mvc.impl.ActionHandler;
import org.tamacat.mvc.oauth.config.OAuthProviderConfig;
import org.tamacat.mvc.oauth.provider.endpoint.DiscoveryKeysEndpoint;
import org.tamacat.mvc.oauth.provider.endpoint.TokenEndpoint;

/**
 *  Action Handler for OAuth 2.0 Provider.
 */
public class OAuthProviderActionHandler extends ActionHandler {

	static final Log LOG = LogFactory.getLog(OAuthProviderActionHandler.class);
	static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	
	static final String TOKEN_ENDPOINT_SUFFIX = "/token";
	static final String DISCOVERY_KEYS_ENDPOINT_SUFFIX = "/discovery/keys";
	static final String DEFAULT_PROVIDER_CONFIG = "oauth2-provider.properties";
	
	protected OAuthProviderConfig config;
	protected TokenEndpoint tokenEndpoint;
	protected DiscoveryKeysEndpoint discoveryKeysEndpoint;
	protected TokenAuthorization auth;
	
	public OAuthProviderActionHandler() {
		config = new OAuthProviderConfig(DEFAULT_PROVIDER_CONFIG);
		discoveryKeysEndpoint = new DiscoveryKeysEndpoint(config);
		tokenEndpoint = new TokenEndpoint(config);
	}
	
	public void setTokenAuthorization(TokenAuthorization auth) {
		this.auth = auth;
		tokenEndpoint.setTokenAuthorization(auth);
	}
	
	@Override
	protected void setDefaultContentType(HttpServletResponse resp) {
		resp.setContentType(JSON_CONTENT_TYPE);
	}
	
	@Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
		//POST /token HTTP/1.1
		if (isRequestEndpoint(req, TOKEN_ENDPOINT_SUFFIX)) {
			tokenEndpoint.handleRequest(req, resp);
		}
		//GET /discovery/keys HTTP/1.1
		else if (isRequestEndpoint(req, DISCOVERY_KEYS_ENDPOINT_SUFFIX)) {
			discoveryKeysEndpoint.handleRequest(req, resp);
		}
		else {
			/* CHECK ACCESS TOKEN
			String token = AuthorizationUtils.getBearerAccessToken(req);
			LOG.trace(token);
			if (StringUtils.isNotEmpty(token)) {
				SignedJWT jwt = config.getOAuth2CodeGenerator().parseSignedJWT(token);
				if (config.getOAuth2CodeGenerator().validateAccessToken(jwt)) {
					req.setAttribute("Authorized.JWT", jwt);
					return;
				}
			}
			*/
			throw new NotFoundException();
		}
	}
	
	static boolean isRequestEndpoint(HttpServletRequest req, String endpoint) {
		return req.getRequestURI().endsWith(endpoint);
	}
}
