/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth;

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
import org.tamacat.mvc.oauth.util.AuthorizationUtils;
import org.tamacat.mvc.util.ServletUtils;
import org.tamacat.util.StringUtils;

public class OAuthRequestHandler {

	static final Log LOG = LogFactory.getLog(OAuthRequestHandler.class);
	static final String TOKEN_ENDPOINT_SUFFIX = "/token";
	
	protected OAuthProviderConfig config = new OAuthProviderConfig();
	
	protected TokenAuthorization auth;
		
	public OAuthRequestHandler(TokenAuthorization auth) {
		this.auth = auth;
	}
	
	public boolean handleOAuthRequest(HttpServletRequest req, HttpServletResponse resp) {
		if (isTokenAccessRequest(req)) {
			//POST /token HTTP/1.1
			//grant_type=client_credentials&client_id=xxxx&client_secret=xxxx
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
					ServletUtils.println(resp, accessToken);
					return true;
				}
			}
			throw new BadRequestException();
		} else {
			String token = getBearerToken(req, resp);
			LOG.trace(token);
			if (StringUtils.isNotEmpty(token)) {
				validateAccessToken(token);
			} else {
				throw new UnauthorizedException();
			}
		}
		return false;
	}
	
	public boolean isTokenAccessRequest(HttpServletRequest req) {
		return req.getRequestURI().endsWith(TOKEN_ENDPOINT_SUFFIX);
	}
	
	public String generateAccessToken(String clientId, String clientSecret) {
		if (StringUtils.isNotEmpty(clientId)) {
			String accessToken = config.generateAccessToken(
					clientId, auth.getJsonWebToken(clientId));
			
			JsonObjectBuilder json = Json.createObjectBuilder();
			json.add("access_token", accessToken)
			.add("token_type", config.getTokenType())
			.add("expires_in", config.getAccessTokenExpiresIn());
			return json.build().toString();
		} else {
			throw new BadRequestException();
		}
	}
	
	public void validateAccessToken(String token) {
		config.getOAuth2CodeGenerator().validateAccessToken(token);
	}
	
	public String getBearerToken(HttpServletRequest req, HttpServletResponse res) {
		String authHeader = req.getHeader("Authorization");
		if (StringUtils.isNotEmpty(authHeader)) {
			String[] tokens = StringUtils.split(authHeader, " ");
			if (tokens.length == 2) {
				return tokens[1];
			}
		}
		return null;
	}
}
