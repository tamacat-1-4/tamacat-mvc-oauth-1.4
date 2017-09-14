/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

public class AccessTokenException extends OAuthException {

	private static final long serialVersionUID = 1L;

	public AccessTokenException(String message, String description, String errorUri) {
		super(message, description, errorUri);
	}
	
	public AccessTokenException() {
		super();
	}

	public AccessTokenException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessTokenException(String message) {
		super(message);
	}

	public AccessTokenException(Throwable cause) {
		super(cause);
	}

}
