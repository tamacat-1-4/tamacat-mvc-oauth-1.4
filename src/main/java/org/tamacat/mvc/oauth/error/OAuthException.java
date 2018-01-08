/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

public class OAuthException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	protected String description;
	protected String errorUri;
	
	public OAuthException(String message, String description, String errorUri) {
		super(message);
		this.description = description;
		this.errorUri = errorUri;
	}
	
	public OAuthException() {
	}

	public OAuthException(String message) {
		super(message);
	}

	public OAuthException(Throwable cause) {
		super(cause);
	}

	public OAuthException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getErrorUri() {
		return errorUri;
	}

	public void setErrorUri(String errorUri) {
		this.errorUri = errorUri;
	}
}
