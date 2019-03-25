/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PKIUtils {

	static final PKIUtils SELF = new PKIUtils();
	
	RSAPublicKey publicKey;
	RSAPrivateKey privateKey;
	
	private PKIUtils() {
		generate();
	}
	
	public static PKIUtils getInstance() {
		return SELF;
	}
	
	void generate() {
		try {
			KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
			keyGenerator.initialize(2048);
			KeyPair kp = keyGenerator.genKeyPair();
			publicKey = (RSAPublicKey)kp.getPublic();
			privateKey = (RSAPrivateKey)kp.getPrivate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static RSAPublicKey getPublicKey() {
		return getInstance().publicKey;
	}
	
	public static RSAPrivateKey getPrivateKey() {
		return getInstance().privateKey;
	}
	
	public static RSAPublicKey getRSAPublicKey(String n) {
		try {
			X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(n));
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static RSAPrivateKey getRSAPrivateKey(String n) {
		try {
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(n));
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
