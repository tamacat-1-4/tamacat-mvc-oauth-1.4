/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider.endpoint;

import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.error.UnauthorizedException;
import org.tamacat.mvc.oauth.config.OAuthProviderConfig;
import org.tamacat.mvc.oauth.error.BadRequestException;
import org.tamacat.mvc.oauth.provider.TokenAuthorization;
import org.tamacat.mvc.oauth.util.AuthorizationUtils;
import org.tamacat.mvc.util.ServletUtils;
import org.tamacat.util.StringUtils;

public class TokenEndpoint implements Endpoint {

	static final Log LOG = LogFactory.getLog(TokenEndpoint.class);
	protected TokenAuthorization auth;
	protected OAuthProviderConfig config;
	
	public TokenEndpoint(OAuthProviderConfig config) {
		this.config = config;
	}
	
	public void setTokenAuthorization(TokenAuthorization auth) {
		this.auth = auth;
	}
	
	@Override
	public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp) {
		//Client Credentials Grant
		//https://tools.ietf.org/html/rfc6749#section-4.4
		String grantType = req.getParameter("grant_type");
		LOG.trace("grant_type="+grantType);
		if ("POST".equalsIgnoreCase(req.getMethod()) && "client_credentials".equals(grantType)) {
			String b64Encoded = AuthorizationUtils.getBasicAuthAccessToken(req);
			String clientId = null;
			String clientSecret = null;
			if (StringUtils.isNotEmpty(b64Encoded)) {
				String token = new String(Base64.getUrlDecoder().decode(b64Encoded));
				if (StringUtils.isNotEmpty(token)) {
					String[] clientIdSecret = StringUtils.split(token, ":");
					if (clientIdSecret.length == 2) {
						clientId = clientIdSecret[0];
						clientSecret = clientIdSecret[1];
					}
				}
			} else {
				clientId = req.getParameter("client_id");
				clientSecret = req.getParameter("client_secret");
			}
			LOG.trace("client_id="+clientId);
			LOG.trace("client_secret="+clientSecret);
			
			if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
				throw new UnauthorizedException();
			}
			if (auth.authorization(clientId, clientSecret) == false) {
				throw new UnauthorizedException();
			}
			String accessToken = generateAccessToken(clientId, clientSecret);
			LOG.trace("access_token="+accessToken);
			if (StringUtils.isNotEmpty(accessToken)) {
				resp.setContentType("application/json");
				resp.setHeader("Cache-Control", "no-store");
				resp.setHeader("Pragma", "no-cache");
				String accessControlAllowOrigin = config.getAccessControlAllowOrigin();
				if (StringUtils.isNotEmpty(accessControlAllowOrigin)) {
					resp.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
				}
				ServletUtils.println(resp, accessToken);
				return true;
			}
		}
		throw new BadRequestException(
			"The grant_type parameter must not be empty for the token endpoint.");
	}
	
	public String generateAccessToken(String clientId, String clientSecret) {
		if (StringUtils.isNotEmpty(clientId)) {
			String accessToken = config.generateAccessToken(clientId, auth.getJsonWebToken(clientId));
			
			JsonObjectBuilder json = Json.createObjectBuilder();
			json.add("access_token", accessToken)
			.add("token_type", config.getTokenType())
			.add("expires_in", config.getAccessTokenExpiresIn());
			return json.build().toString();
		} else {
			throw new BadRequestException();
		}
	}
}
