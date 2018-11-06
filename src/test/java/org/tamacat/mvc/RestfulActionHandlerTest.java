package org.tamacat.mvc;

import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.Test;

public class RestfulActionHandlerTest {

	@Test
	public void testGetClassName() {
		RestfulActionHandler handler = new RestfulActionHandler();
		handler.setPackageName("org.tamacat.test.action");

		assertEquals("org.tamacat.test.action.UsersAction", handler.getClassName(Paths.get("/scim/v1/Users/123")));
		assertEquals("org.tamacat.test.action.UsersAction", handler.getClassName(Paths.get("/scim/v1/Users")));

		assertNull(handler.getClassName(Paths.get("/scim/v1/")));
	}

	@Test
	public void testGetResourceId() {
		RestfulActionHandler handler = new RestfulActionHandler();
		handler.setPackageName("org.tamacat.test.action");

		assertEquals("123", handler.getResourceId(Paths.get("/scim/v1/Users/123")));
		assertNull(handler.getResourceId(Paths.get("/scim/v1/Users")));
	}
}
