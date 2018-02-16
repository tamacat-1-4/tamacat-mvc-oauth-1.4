/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

import org.tamacat.mvc.error.ClientSideException;
import org.tamacat.mvc.error.HttpStatusException;

public class TooManyRequestsException extends HttpStatusException implements ClientSideException {

	private static final long serialVersionUID = 1L;

	static final String MESSAGE = "Too Many Requests";
	
	public TooManyRequestsException() {
		super(429, MESSAGE, null, null);
	}
	
	public TooManyRequestsException(String message) {
		super(429, MESSAGE, message, null);
	}
	
	public TooManyRequestsException(String message, Throwable cause) {
		super(429, MESSAGE , message, cause);
	}
}
