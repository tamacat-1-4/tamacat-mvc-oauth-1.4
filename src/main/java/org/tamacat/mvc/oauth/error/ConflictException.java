/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

import org.tamacat.mvc.error.ClientSideException;
import org.tamacat.mvc.error.HttpStatusException;

public class ConflictException extends HttpStatusException implements ClientSideException {

	private static final long serialVersionUID = 1L;

	public ConflictException() {
		super(409, "Conflict", null, null);
	}
	
	public ConflictException(String message) {
		super(409, "Conflict", message, null);
	}
	
	public ConflictException(String message, Throwable cause) {
		super(409, "Conflict", message, cause);
	}
}
