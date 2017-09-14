/*
 * Copyright (c) 2016 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.util;

import javax.servlet.http.HttpServletRequest;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.StringUtils;

public class AuthorizationUtils {
	static final Log LOG = LogFactory.getLog(AuthorizationUtils.class);
	static final String AUTHORIZATION = "Authorization";
	
	public static String getBasicAuthAccessToken(HttpServletRequest req) {
		return getAuthorizationAccessToken(req, "Basic");
	}
	
	public static String getBearerAccessToken(HttpServletRequest req) {
		return getAuthorizationAccessToken(req, "Bearer");
	}
	
	public static String getAuthorizationAccessToken(HttpServletRequest req, String type) {
		String authHeader = req.getHeader(AUTHORIZATION);
		if (StringUtils.isNotEmpty(authHeader) && authHeader.startsWith(type+" ")) {
			String accessToken = req.getHeader(AUTHORIZATION).replace(type+" ","");
			if (StringUtils.isNotEmpty(accessToken)) {
				return accessToken;
			}
		}
		return null;
	}
	
	public static String getAuthorizationAccessToken(HttpServletRequest req) {
		String authHeader = req.getHeader(AUTHORIZATION);
		if (StringUtils.isNotEmpty(authHeader)) {
			if (authHeader.startsWith("Bearer ")) {
				return authHeader.replaceFirst("^Bearer ","");
			} else if (authHeader.startsWith("Basic ")) {
				return authHeader.replaceFirst("^Basic ","");
			}
		}
		return null;
	}
}
