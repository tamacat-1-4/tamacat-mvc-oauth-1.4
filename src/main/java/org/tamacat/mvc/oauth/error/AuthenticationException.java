/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.error;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.tamacat.util.StringUtils;

public class AuthenticationException extends OAuthException {

	private static final long serialVersionUID = 1L;

	public static enum ErrorCode {
		INTERACTION_REQUIRED,
		LOGIN_REQUIRED,
		ACCOUNT_SELECTION_REQUIRED,
		CONSENT_REQUIRED,
		INVALID_REQUEST_URI,
		INVALID_REQUEST,
		INVALID_REQUEST_OBJECT, //UNUSED?
		REQUEST_NOT_SUPPORTED,
		REQUEST_URI_NOT_SUPPORTED,
		REGISTRATION_NOT_SUPPORTED;
		
		public String getName() {
			return name().toLowerCase();
		}
	}
	
	protected ErrorCode errorCode;
	
	public AuthenticationException() {}
	
	public AuthenticationException(ErrorCode errorCode, String description) {
		this.errorCode = errorCode;
		this.description = description;
	}
	
	public AuthenticationException(ErrorCode errorCode, String description, String errorUri) {
		this.errorCode = errorCode;
		this.description = description;
		this.errorUri = errorUri;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorString() {
		StringBuilder message = new StringBuilder();
		if (StringUtils.isNotEmpty(errorCode)) {
			message.append("error_code="+errorCode.getName());
		}
		if (StringUtils.isNotEmpty(description)) {
			message.append("error_description="+description);
		}
		if (StringUtils.isNotEmpty(errorUri)) {
			message.append("error_uri="+errorUri);
		}
		return message.toString();
	}
	
	public String getErrorJSON() {
		JsonObjectBuilder json = Json.createObjectBuilder()
				.add("error", "invalid_request");
		if (StringUtils.isNotEmpty(errorCode)) {
			json.add("error_code", errorCode.getName());
		}
		if (StringUtils.isNotEmpty(description)) {
			json.add("error_description", description);
		}
		if (StringUtils.isNotEmpty(errorUri)) {
			json.add("error_uri", errorUri);
		}
		return json.build().toString();
	}
}
