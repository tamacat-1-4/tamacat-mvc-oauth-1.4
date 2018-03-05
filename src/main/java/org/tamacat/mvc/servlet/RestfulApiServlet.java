/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.tamacat.mvc.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.mvc.servlet.StandardServlet;
import org.tamacat.util.StringUtils;

public class RestfulApiServlet extends StandardServlet {

	private static final long serialVersionUID = 1L;

	protected String accessControlAllowOrigin;
	protected String accessControlAllowMethods;
	protected String accessControlAllowHeaders;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		String accessControlAllowOrigin = config.getInitParameter("Access-Control-Allow-Origin");
		if (StringUtils.isNotEmpty(accessControlAllowOrigin)) {
			this.accessControlAllowOrigin = accessControlAllowOrigin;
		}
		String accessControlAllowMethods = config.getInitParameter("Access-Control-Allow-Methods");
		if (StringUtils.isNotEmpty(accessControlAllowMethods)) {
			this.accessControlAllowMethods = accessControlAllowMethods;
		}
		
		String accessControlAllowHeaders = config.getInitParameter("Access-Control-Allow-Headers");
		if (StringUtils.isNotEmpty(accessControlAllowHeaders)) {
			this.accessControlAllowHeaders = accessControlAllowHeaders;
		}
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doOptions(req, resp);
		
		if (StringUtils.isNotEmpty(accessControlAllowOrigin)) {
			resp.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
		}
		if (StringUtils.isNotEmpty(accessControlAllowMethods)) {
			resp.setHeader("Access-Control-Allow-Methods", accessControlAllowMethods);
		}
		if (StringUtils.isNotEmpty(accessControlAllowHeaders)) {
			resp.setHeader("Access-Control-Allow-Headers", accessControlAllowHeaders);
		}
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}
}
