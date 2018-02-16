/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

import org.tamacat.mvc.error.ClientSideException;
import org.tamacat.mvc.error.HttpStatusException;

public class RequestEntityTooLargeException extends HttpStatusException implements ClientSideException {

	private static final long serialVersionUID = 1L;

	static final String MESSAGE = "Request Entity Too Large";
	
	public RequestEntityTooLargeException() {
		super(413, MESSAGE, null, null);
	}
	
	public RequestEntityTooLargeException(String message) {
		super(413, MESSAGE, message, null);
	}
	
	public RequestEntityTooLargeException(String message, Throwable cause) {
		super(413, MESSAGE , message, cause);
	}
}
