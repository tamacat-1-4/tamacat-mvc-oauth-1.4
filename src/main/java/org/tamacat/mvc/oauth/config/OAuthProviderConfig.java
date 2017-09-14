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
import org.tamacat.mvc.oauth.token.JsonWebToken;
import org.tamacat.mvc.oauth.token.OAuthCodeGenerator;
import org.tamacat.mvc.oauth.util.PKIUtils;
import org.tamacat.util.PropertyUtils;
import org.tamacat.util.StringUtils;

import com.nimbusds.jose.JWSAlgorithm;

public class OAuthProviderConfig {
	static final Log LOG =LogFactory.getLog(OAuthProviderConfig.class);
	
	String issuer;					// localhost
	String tokenEndpoint;			// /oauth2/token
	String jwksUri;					// /oauth2/discovery/keys
	
	protected String tokenType = "Bearer";
	protected long accessTokenExpiresIn= 1L*60L*60L; //60min
	protected long refreshTokenExpiresIn= 14L * 24L*60L*60L; //14days
		
	protected JWSAlgorithm alg;

	protected RSAPublicKey publicKey;
	protected RSAPrivateKey privateKey;
	
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
		
		tokenEndpoint = props.getProperty("token_endpoint");
		jwksUri = props.getProperty("jwks_uri");
		
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
		this.tokenEndpoint = tokenEndpoint;
	}
		
	public String getJwksUri() {
		return jwksUri;
	}
	
	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
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
}
