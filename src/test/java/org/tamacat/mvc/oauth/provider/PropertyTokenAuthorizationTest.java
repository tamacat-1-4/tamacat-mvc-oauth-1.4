package org.tamacat.mvc.oauth.provider;

import static org.junit.Assert.*;

import org.junit.Test;

public class PropertyTokenAuthorizationTest {

	@Test
	public void testAuthorization() {
		PropertyTokenAuthorization auth = new PropertyTokenAuthorization();
		assertTrue(auth.authorization("TESTCLIENT12345", "ABCDEFG1234567890"));
		
		assertFalse(auth.authorization("TESTCLIENT12345", "ABCDEFG12345678901"));
		assertFalse(auth.authorization("TESTCLIENT12345", ""));
		assertFalse(auth.authorization("", "ABCDEFG12345678901"));
		assertFalse(auth.authorization("", ""));
	}

	@Test
	public void testGetJsonWebToken() {
		PropertyTokenAuthorization auth = new PropertyTokenAuthorization();
		assertEquals("TESTCLIENT12345", auth.getJsonWebToken("TESTCLIENT12345").getClientId());
	}
}
