/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.token;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import org.tamacat.mvc.oauth.config.OAuthProviderConfig;
import org.tamacat.mvc.oauth.provider.jwt.JsonWebToken;

public class JsonWebTokenTest {
	@Test
	public void testJsonWebToken() {
		OAuthProviderConfig config = new OAuthProviderConfig();
		String tid = "tamacat.org";
		String clientId = "TESTCLIENT123";
		JsonWebToken jwt = new JsonWebToken().algorithm(config.getJWSAlgorithm())
				.issuer(config.getIssuer())
				.privateKey(config.getRSAPrivateKey())
				.publicKey(config.getRSAPublicKey())
				.tid(tid)
				.audience(clientId)
				.nonce("12345");
		
		//String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjE4YmM3MDI0NjQ3MmIyNmFmMTFiYjg5OWY3MGVlOTVlN2Y1NmZiYjEifQ==.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpLyIsInRpZCI6InRhbWFjYXQub3JnIiwiYXVkIjoiVEVTVENMSUVOVDEyMyIsIm5vbmNlIjoiMTIzNDUifQ==";
		//System.out.println(jwt.getToken());
		//assertEquals(token,  jwt.getToken());
		
		assertEquals(clientId, jwt.getClientId());
		assertEquals(tid, jwt.getTid());
	}

	@Test
	public void testVerifyExpiration() {
		OAuthProviderConfig config = new OAuthProviderConfig();
		JsonWebToken jwt = new JsonWebToken().algorithm(config.getJWSAlgorithm())
				.expiration(1553590587L);
		try {
			System.out.println(jwt.verifyExpiration());
		} catch (Exception e) {
			//e.printStackTrace();
			assertEquals("Access Token time expired.", e.getMessage());
		}
		System.out.println(System.currentTimeMillis()/1000);
		System.out.println(new Date().getTime()/1000);
	}

}
