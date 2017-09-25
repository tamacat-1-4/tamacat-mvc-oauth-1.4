/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider.jwt;

public class JsonWebTokenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JsonWebTokenException() {}

	public JsonWebTokenException(String message) {
		super(message);
	}

	public JsonWebTokenException(Throwable cause) {
		super(cause);
	}

	public JsonWebTokenException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonWebTokenException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
