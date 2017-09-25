/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider.jwt;

import java.util.Date;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.error.ForbiddenException;
import org.tamacat.mvc.oauth.config.OAuthProviderConfig;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

import net.minidev.json.JSONObject;

public class OAuthCodeGenerator {
	static final Log LOG = LogFactory.getLog(OAuthCodeGenerator.class);
	
	protected OAuthProviderConfig provider;
	
	public OAuthCodeGenerator(OAuthProviderConfig provider) {
		this.provider = provider;
	}
	
	public String generateAccessToken(String clientId, JsonWebToken jwt) {
		//return EncryptSessionUtils.encryptSession(uid+":"+otp.generate(issuer+"/access_token"));
		String issuer = provider.getIssuer(clientId);
		long now = new Date().getTime()/1000; //sec.
		String alg = provider.getJWSAlgorithm().getName();
		jwt.algorithm(alg).issuer(issuer).audience(clientId)
			.privateKey(provider.getRSAPrivateKey())
			.issueAt(now).expiration(now + provider.getAccessTokenExpiresIn());
		return jwt.serialize();
	}
	
	public boolean validateAuthorizationCode(String clientId, String callbackUri, String code) {
		if (code == null) return false;
		try {
			SignedJWT jwt = parseSignedJWT(code);
			JWSVerifier verifier = new DefaultJWSVerifierFactory().createJWSVerifier(jwt.getHeader(), provider.getRSAPublicKey());
			if (jwt.verify(verifier)) {
				//check clientID and callbackUri
				JSONObject json = jwt.getPayload().toJSONObject();
				LOG.debug("validateAuthorizationCode: "+json);
				Number exp = json.getAsNumber("exp");
				if (exp != null) {
					long expired = exp.longValue();
					if ((System.currentTimeMillis()/1000) > expired) {
						return false;
					}
				}
				String callback = (String) json.get("callback_uri");
				if (callback != null && callback.equals(callbackUri)) {
					return true;
				}
			}
		} catch (Exception e) { //JOSEException
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean validateAccessToken(SignedJWT jwt) {
		try {
			JWSVerifier verifier = new DefaultJWSVerifierFactory()
				.createJWSVerifier(jwt.getHeader(), provider.getRSAPublicKey());
			if (jwt.verify(verifier)) {
				JSONObject json = jwt.getPayload().toJSONObject();
				LOG.debug("validateAccessToken: "+json);
				Number exp = json.getAsNumber("exp");
				if (exp != null) {
					long expired = exp.longValue();
					if ((System.currentTimeMillis()/1000) > expired) {
						return false;
					}
				} else {
					LOG.warn("access_token exp is empty.");
					return false;
				}
				System.out.println(json.getAsString("aud"));
				return true;
			} else {
				return false;
			}
		} catch (JOSEException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public SignedJWT parseSignedJWT(String tokenString) {
		try {
			return (SignedJWT) JWTParser.parse(tokenString);
		} catch (java.text.ParseException e) {
			throw new ForbiddenException(e.getMessage(), e); //TODO
		}
	}
}
