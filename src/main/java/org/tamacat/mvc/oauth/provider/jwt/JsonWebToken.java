/*
 * Copyright (c) 2016 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider.jwt;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.tamacat.mvc.oauth.error.AccessTokenException;
import org.tamacat.util.CollectionUtils;
import org.tamacat.util.EncryptionUtils;
import org.tamacat.util.StringUtils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

public class JsonWebToken {

	JsonObjectBuilder header = Json.createObjectBuilder();
	JsonObjectBuilder payload = Json.createObjectBuilder();

	String headerString;
	String payloadString;
	
	RSAPublicKey publicKey;
	RSAPrivateKey privateKey;
	
	JWSAlgorithm alg;
	boolean isBuilt;
	long exp;
	
	String clientId;
	String upn;
	String subject;
	String issuer;
	String nonce;
	String tid;
		
	public long getExp() {
		return exp;
	}

	public String getClientId() {
		return clientId;
	}

	public String getUpn() {
		return upn;
	}

	public String getSubject() {
		return subject;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getNonce() {
		return nonce;
	}

	public String getTid() {
		return tid;
	}
	
	/**
	 * @param alg JWSAlgorithm.RS256/HS256
	 * @return
	 */
	public JsonWebToken algorithm(JWSAlgorithm alg) {
		this.alg = alg;
		header.add("typ", "JWT").add("alg", alg.getName());
		return this;
	}
	
	public JsonWebToken algorithm(String alg) {
		return algorithm(JWSAlgorithm.parse(alg));
	}
	
	//"https://login.tamacat.org"
	public JsonWebToken issuer(String issuer) {
		this.issuer = issuer;
		payload.add("iss", issuer);
		return this;
	}
	
	public JsonWebToken upn(String upn) {
		this.upn = upn;
		payload.add("upn", upn); //OpenID
		return this;
	}
		
	public JsonWebToken tid(String tid) {
		this.tid = tid;
		payload.add("tid", tid); //Tenant ID
		return this;
	}
	
	public JsonWebToken subject(String subject) {
		this.subject = subject;
		payload.add("sub", subject); //User ID (Internal)
		return this;
	}
	
	public JsonWebToken audience(String audience) {
		this.clientId = audience;
		payload.add("aud", audience); //Client ID
		return this;
	}
	
	public JsonWebToken nonce(String nonce) {
		//if (nonce == null) nonce = UniqueCodeGenerator.generate();
		this.nonce = nonce;
		payload.add("nonce", nonce);
		return this;
	}
	
	//issue at
	public JsonWebToken issueAt(long iat) {
		payload.add("iat", iat); //new Date().getTime()/1000
		return this;
	}
	
	//auth_time (REQUIRED: request in max_age)
	public JsonWebToken authTime(long authTime) {
		payload.add("auth_time", authTime);
		return this;
	}
	
	//expiration
	public JsonWebToken expiration(long exp) {
		this.exp = exp;
		payload.add("exp", exp); //new Date().getTime()/1000+(1800)
		return this;
	}
	
	public boolean verifyExpiration() {
		if ((System.currentTimeMillis()/1000) > (exp)) {
			throw new AccessTokenException("Access Token time expired.");
		}
		return true;
	}
	
	public JsonWebToken set(String key, String value) {
		payload.add(key, value);
		return this;
	}
	
	public String getHeader() {
		build();
		return headerString;
	}
	
	public String getPayload() {
		build();
		return payloadString;
	}
	
	void build() {
		if (isBuilt == false) {
			headerString = header.build().toString();
			payloadString = payload.build().toString();
			isBuilt = true;
		}
	}
	
	public JsonWebToken publicKey(RSAPublicKey publicKey) {
		this.publicKey = publicKey;
		return this;
	}
	
	public JsonWebToken privateKey(RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
		String kid = getKid(privateKey.getEncoded());
		header.add("kid", kid);
		return this;
	}
	
	public String serialize() {
		JWSObject jwsObject = null;
		try {
			jwsObject = new JWSObject(JWSHeader.parse(getHeader()), new Payload(getPayload()));
			if (alg == JWSAlgorithm.HS256) {
				byte[] sharedKey = new byte[32];
				new SecureRandom().nextBytes(sharedKey);
				jwsObject.sign(new MACSigner(sharedKey));
			} else {
//				KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
//				keyGenerator.initialize(2048);
//				KeyPair kp = keyGenerator.genKeyPair();
//				publicKey = (RSAPublicKey)kp.getPublic();
//				privateKey = (RSAPrivateKey)kp.getPrivate();
//				System.out.println(publicKey.getModulus());
//				System.out.println(publicKey.getPublicExponent());
//				System.out.println(privateKey.getModulus());
//				System.out.println(privateKey.getPrivateExponent());
				jwsObject.sign(new RSASSASigner(privateKey));
			}
		} catch (Exception e) {
			throw new JsonWebTokenException(e);
		}
		return jwsObject.serialize();
	}
	
	public String getToken() {
		build();
		return Base64.getUrlEncoder().encodeToString(headerString.getBytes())
		  +"."+Base64.getUrlEncoder().encodeToString(payloadString.getBytes());
	}
	
	public static String getKid(byte[] key) {
		return EncryptionUtils.getMessageDigest(
			Base64.getUrlEncoder().encodeToString(key), "SHA-1").toLowerCase();
	}
	
	public static RSAPublicKey getRSAPublicKey(String x5c) {
		try {
			X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getUrlDecoder().decode(x5c));
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static RSAPublicKey getRSAPublicKey(String n, String e) {
		try {
			BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
			BigInteger publicExponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
			return (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static JWK getJWK(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
		try {
			String kid = getKid(privateKey.getEncoded());
			com.nimbusds.jose.util.Base64 b64x5c = com.nimbusds.jose.util.Base64.encode(publicKey.getEncoded());
			List<com.nimbusds.jose.util.Base64> x5c = CollectionUtils.newArrayList();
			x5c.add(b64x5c);
			
			//Convert to JWK format
			JWK jwk = new RSAKey.Builder(publicKey)
				.keyID(kid)
				.algorithm(JWSAlgorithm.RS256)
				.keyUse(KeyUse.SIGNATURE)
				.x509CertChain(x5c)
				.build();
			return jwk;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static RSAPublicKey getPublicKey(String jwks, String id) {
		if (StringUtils.isEmpty(id)) return null;
		String n = null;
		String e = null;
		try {
			JsonReader r = Json.createReader(new StringReader(jwks));
			JsonObject json = r.readObject();
			JsonArray keys = json.getJsonArray("keys");
			
			for (int i=0; i<keys.size(); i++) {
				JsonObject key = keys.getJsonObject(i);
				if (id.equals(key.getString("kid"))) {
					n = key.getString("n");
					e = key.getString("e");
					//System.out.println(n);
					//System.out.println(e);
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		if (StringUtils.isNotEmpty(n) && StringUtils.isNotEmpty(e)) {
			return JsonWebToken.getRSAPublicKey(n, e);
		} else {
			return null;
		}
	}
}
