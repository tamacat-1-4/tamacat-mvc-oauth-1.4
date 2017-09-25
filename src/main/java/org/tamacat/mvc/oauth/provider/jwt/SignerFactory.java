/*
 * Copyright (c) 2016 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider.jwt;

import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SignerFactory {
	public static final SecureRandom DEFAULT_SECURE_RANDOM;
	
	static {
		DEFAULT_SECURE_RANDOM = new SecureRandom();
		DEFAULT_SECURE_RANDOM.nextBytes(new byte[64]);
	}
	
	public static SecretKey generateKey(SignatureAlgorithm alg, SecureRandom random) {
		if (alg.isHmac() == false) {
			throw new IllegalArgumentException("SignatureAlgorithm argument must represent an HMAC algorithm.");
		}
		byte[] bytes;
		switch (alg) {
			case HS256:
				bytes = new byte[32];
				break;
			case HS384:
				bytes = new byte[48];
				break;
			default:
				bytes = new byte[64];
		}
		random.nextBytes(bytes);
		return new SecretKeySpec(bytes, alg.getJcaName());
	}
}
