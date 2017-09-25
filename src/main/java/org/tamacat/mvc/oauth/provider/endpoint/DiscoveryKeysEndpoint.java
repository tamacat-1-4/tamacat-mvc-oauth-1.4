/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider.endpoint;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.oauth.config.OAuthProviderConfig;
import org.tamacat.mvc.oauth.provider.jwt.JsonWebToken;
import org.tamacat.mvc.oauth.util.UrlUtils;
import org.tamacat.util.StringUtils;

/**
 * https://openid.net/specs/openid-connect-discovery-1_0.html
 * 3.  OpenID Provider Metadata - jwks_uri
 * 
 * URL of the OP's JSON Web Key Set [JWK] document. 
 * This contains the signing key(s) the RP uses to validate
 * signatures from the OP. 
 * The JWK Set MAY also contain the Server's encryption key(s), 
 * which are used by RPs to encrypt requests to the Server. 
 * When both signing and encryption keys are made available, 
 * a use (Key Use) parameter value is REQUIRED for all keys in
 * the referenced JWK Set to indicate each key's intended usage.
 * Although some algorithms allow the same key to be used for 
 * both signatures and encryption, doing so is NOT RECOMMENDED, 
 * as it is less secure. The JWK x5c parameter MAY be used to 
 * provide X.509 representations of keys provided. When used, 
 * the bare key values MUST still be present and MUST match 
 * those in the certificate.
 */
public class DiscoveryKeysEndpoint implements Endpoint {
	
	static final Log LOG = LogFactory.getLog(DiscoveryKeysEndpoint.class);
	
	protected OAuthProviderConfig provider;
	protected String jwks;
	
	public DiscoveryKeysEndpoint(OAuthProviderConfig provider) {
		this.provider = provider;
		init();
	}
	
	@Override
	public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp) {
		try {
			resp.getWriter().println(getJwksJsonString());
			resp.setContentType("application/json");
			resp.setHeader("Cache-Control", "no-store");
			resp.setHeader("Pragma", "no-cache");
			resp.setStatus(200);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			JsonObjectBuilder error = Json.createObjectBuilder().add("error", "invalid_request");
			UrlUtils.errorJsonResponse(resp, error.build().toString(), 400);
		}
		return true;
	}
	
	protected void setJwksJsonString(String jwks) {
		this.jwks = jwks;
	}
	
	protected String getJwksJsonString() {
		return jwks;
	}
	
	protected void init() {
		//TODO: Multiple JWK keys
		//http://openid.net/specs/openid-connect-core-1_0.html
		//10.1.1.  Rotation of Asymmetric Signing Keys
		
		String jwk = JsonWebToken.getJWK(provider.getRSAPublicKey(), provider.getRSAPrivateKey()).toJSONString();
		JsonArrayBuilder keys = Json.createArrayBuilder();
		keys.add(Json.createReader(new StringReader(jwk)).readObject());
		setJwksJsonString(StringUtils.formatJson(Json.createObjectBuilder().add("keys", keys).build().toString()));
	}
}
