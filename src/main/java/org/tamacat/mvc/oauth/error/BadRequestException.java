/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

import org.tamacat.mvc.error.HttpStatusException;

public class BadRequestException extends HttpStatusException {

	private static final long serialVersionUID = 1L;

	public BadRequestException() {
		super(400, "Bad Request", null, null);
	}
	
	public BadRequestException(String message) {
		super(400, "Bad Request", message, null);
	}
	
	public BadRequestException(String message, Throwable cause) {
		super(400, "Bad Request", message, cause);
	}
}
