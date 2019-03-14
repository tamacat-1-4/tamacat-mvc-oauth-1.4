/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Properties;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.oauth.provider.jwt.JsonWebToken;
import org.tamacat.mvc.oauth.provider.jwt.OAuthCodeGenerator;
import org.tamacat.mvc.oauth.util.PKIUtils;
import org.tamacat.util.PropertyUtils;
import org.tamacat.util.StringUtils;

import com.nimbusds.jose.JWSAlgorithm;

public class OAuthProviderConfig {
	static final Log LOG =LogFactory.getLog(OAuthProviderConfig.class);
	
	String issuer;					// localhost
	String tokenEndpoint;			// /oauth2/token
	String introspectEndpoint;			// /oauth2/introspect
	String userinfoEndpoint;			// /oauth2/userinfo
	String jwksUri;					// /oauth2/discovery/keys
	
	protected String tokenType = "Bearer";
	protected long accessTokenExpiresIn= 1L*60L*60L; //60min
	protected long refreshTokenExpiresIn= 14L * 24L*60L*60L; //14days
		
	protected JWSAlgorithm alg;

	protected RSAPublicKey publicKey;
	protected RSAPrivateKey privateKey;
	protected String accessControlAllowOrigin;
	protected String accessControlAllowMethods;
	protected String accessControlAllowHeaders;
	
	protected OAuthCodeGenerator generator;

	public OAuthCodeGenerator getOAuth2CodeGenerator() {
		generator = new OAuthCodeGenerator(this);
		return generator;
	}
	
	public JWSAlgorithm getJWSAlgorithm() {
		if (alg == null) alg = JWSAlgorithm.RS256;
		return alg;
	}
	
	public OAuthProviderConfig() {
		this("oauth2-provider.properties");
	}
	
	public OAuthProviderConfig(String propFile) {
		Properties props = PropertyUtils.getProperties(propFile);
		setIssuer(props.getProperty("issuer"));
		
		setTokenEndpoint(props.getProperty("token_endpoint"));
		setIntrospectEndpoint(props.getProperty("introspect_endpoint"));
		setUserInfoEndpoint(props.getProperty("userinfo_endpoint"));
		setJwksUri(props.getProperty("jwks_uri"));
		
		accessTokenExpiresIn = StringUtils.parse(props.getProperty("access_token_expired_in"), accessTokenExpiresIn);
		refreshTokenExpiresIn = StringUtils.parse(props.getProperty("refresh_token_expired_in"), refreshTokenExpiresIn);
		
		try {
			String alg = props.getProperty("jws_algorithm");
			if (StringUtils.isNotEmpty(alg)) {
				this.alg = JWSAlgorithm.parse(alg);
			}
		} catch (Exception e) {
			this.alg = JWSAlgorithm.RS256;
		}
		String b64PublicKey = props.getProperty("RSA_public_key");
		String b64PrivateKey = props.getProperty("RSA_private_key");
		if (StringUtils.isNotEmpty(b64PublicKey)) {
			publicKey = PKIUtils.getRSAPublicKey(b64PublicKey);
		}
		if (StringUtils.isNotEmpty(b64PrivateKey)) {
			privateKey = PKIUtils.getRSAPrivateKey(b64PrivateKey);
		}

		LOG.trace(getRSAPublicKey());
		LOG.trace(getRSAPrivateKey());
		
		String accessControlAllowOrigin = props.getProperty("Access-Control-Allow-Origin");
		if (StringUtils.isNotEmpty(accessControlAllowOrigin)) {
			this.accessControlAllowOrigin = accessControlAllowOrigin;
		}
		
		String accessControlAllowMethods = props.getProperty("Access-Control-Allow-Methods");
		if (StringUtils.isNotEmpty(accessControlAllowMethods)) {
			this.accessControlAllowMethods = accessControlAllowMethods;
		}
		
		String accessControlAllowHeaders = props.getProperty("Access-Control-Allow-Headers");
		if (StringUtils.isNotEmpty(accessControlAllowHeaders)) {
			this.accessControlAllowHeaders = accessControlAllowHeaders;
		}
	}
	
	public String generateAccessToken(String clientId, JsonWebToken jwt) {
		return generator.generateAccessToken(clientId, jwt);
	}
	
	public String getIssuer() {
		return issuer;
	}
	
	public String getIssuer(String clientId) {
		return issuer.replace("${client_id}", clientId);
	}
	
	public void setIssuer(String issuer) {
		this.issuer = issuer;
		generator = new OAuthCodeGenerator(this);
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}
	
	public void setTokenEndpoint(String tokenEndpoint) {
		if (StringUtils.isNotEmpty(tokenEndpoint)) {
			this.tokenEndpoint = tokenEndpoint.replace("${issuer}", getIssuer());
		}
	}
	
	public String getIntrospectEndpoint() {
		return introspectEndpoint;
	}
	
	public void setIntrospectEndpoint(String introspectEndpoint) {
		if (StringUtils.isNotEmpty(introspectEndpoint)) {
			this.introspectEndpoint = introspectEndpoint.replace("${issuer}", getIssuer());
		}
	}
	
	public String getUserInfoEndpoint() {
		return userinfoEndpoint;
	}
	
	public void setUserInfoEndpoint(String userinfoEndpoint) {
		if (StringUtils.isNotEmpty(userinfoEndpoint)) {
			this.userinfoEndpoint = userinfoEndpoint.replace("${issuer}", getIssuer());
		}
	}
	
	public String getJwksUri() {
		return jwksUri;
	}
	
	public void setJwksUri(String jwksUri) {
		if (StringUtils.isNotEmpty(jwksUri)) {
			this.jwksUri = jwksUri.replace("${issuer}", getIssuer());
		}
	}
	
	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
		
	public long getAccessTokenExpiresIn() {
		return accessTokenExpiresIn;
	}
	
	public void setAccessTokenExpiresIn(long expiresIn) {
		this.accessTokenExpiresIn = expiresIn;
	}
	
	public long getRefreshTokenExpiresIn() {
		return refreshTokenExpiresIn;
	}
	
	public void setRefreshTokenExpiresIn(long expiresIn) {
		this.refreshTokenExpiresIn = expiresIn;
	}
	
	public RSAPublicKey getRSAPublicKey() {
		return publicKey;
	}
	
	public RSAPrivateKey getRSAPrivateKey() {
		return privateKey;
	}
	
	public String getAccessControlAllowOrigin() {
		return accessControlAllowOrigin;
	}
	
	public String getAccessControlAllowMethods() {
		return accessControlAllowMethods;
	}
	
	public String getAccessControlAllowHeaders() {
		return accessControlAllowHeaders;
	}
}
