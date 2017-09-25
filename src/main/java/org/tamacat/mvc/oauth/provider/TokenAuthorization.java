/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.mvc.oauth.provider.jwt.JsonWebToken;

/**
 * Interface for OAuth 2.0 Authorization Framework.
 */
public interface TokenAuthorization {
	
	/**
	 * Check registered client_id and client_secret
	 */
	boolean authorization(String clientId, String clientSecret);
	
	/**
	 * Generate Json Web Token
	 * @param clientId
	 */
	JsonWebToken getJsonWebToken(String clientId);
	
	void activate(HttpServletRequest req, HttpServletResponse resp, JsonWebToken jwt);
}
