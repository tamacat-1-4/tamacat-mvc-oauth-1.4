/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.token;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import com.nimbusds.jose.util.BigIntegerUtils;

public class GeneratePKI {

	public static void main(String[] args) throws Exception {
		generate();
	}
	
	static void generate() throws Exception {
		//RSAPublicKey publicKey = PKIUtils.getPublicKey();
		//RSAPrivateKey privateKey = PKIUtils.getPrivateKey();
		//System.out.println(publicKey);
		//System.out.println(privateKey);
		
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		keyGenerator.initialize(2048);
		KeyPair kp = keyGenerator.genKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey)kp.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();
		
		System.out.println("RSA_public_key="+Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		System.out.println("RSA_private_key="+Base64.getEncoder().encodeToString(privateKey.getEncoded()));
		System.out.println("RSA_public_key_exponent="+ Base64.getEncoder().encodeToString(BigIntegerUtils.toBytesUnsigned(publicKey.getPublicExponent())));
		System.out.println("RSA_private_key_exponent="+Base64.getEncoder().encodeToString(BigIntegerUtils.toBytesUnsigned(privateKey.getPrivateExponent())));
	}
}
