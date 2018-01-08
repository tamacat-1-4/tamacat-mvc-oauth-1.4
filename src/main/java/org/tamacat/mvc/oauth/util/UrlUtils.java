/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class UrlUtils {

	public static void errorJsonResponse(HttpServletResponse response, String error, int status) {
		//400 Bad Request
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().println(error);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
