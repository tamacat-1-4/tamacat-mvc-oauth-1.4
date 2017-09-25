/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.provider;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.mvc.oauth.provider.jwt.JsonWebToken;
import org.tamacat.util.PropertyUtils;
import org.tamacat.util.StringUtils;

public class PropertyTokenAuthorization implements TokenAuthorization {

	static final String DEFAULT_ACCESS_KEYS = "access_keys.properties";
	
	String tid;
	Properties props;
	
	public PropertyTokenAuthorization() {
		this(DEFAULT_ACCESS_KEYS);
	}
	
	public PropertyTokenAuthorization(String file) {
		props = PropertyUtils.getProperties(file);
	}

	public void setTid(String tid) {
		this.tid = tid;
	}
	
	public String getClientSecret(String clientId) {
		return props.getProperty(clientId);
	}
	
	@Override
	public boolean authorization(String clientId, String clientSecret) {
		if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(clientSecret)) {
			if (clientSecret.equals(getClientSecret(clientId))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public JsonWebToken getJsonWebToken(String clientId) {
		JsonWebToken jwt = new JsonWebToken();
		jwt.audience(clientId);
		if (StringUtils.isNotEmpty(tid)) jwt.tid(tid);
		return jwt;
	}
	
	@Override
	public void activate(HttpServletRequest req, HttpServletResponse resp, JsonWebToken jwt) {
		// TODO Auto-generated method stub

	}
}
