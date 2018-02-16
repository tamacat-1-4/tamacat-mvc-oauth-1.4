/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

import org.tamacat.mvc.error.ClientSideException;
import org.tamacat.mvc.error.HttpStatusException;

public class MethodNotAllowedException extends HttpStatusException implements ClientSideException {

	private static final long serialVersionUID = 1L;

	public MethodNotAllowedException() {
		super(405, "Method Not Allowed", null, null);
	}
	
	public MethodNotAllowedException(String message) {
		super(405, "Method Not Allowed", message, null);
	}
	
	public MethodNotAllowedException(String message, Throwable cause) {
		super(405, "Method Not Allowed", message, cause);
	}
}
