/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.action.ActionDefine;
import org.tamacat.mvc.impl.ActionHandler;
import org.tamacat.mvc.oauth.provider.TokenAuthorization;

public class JsonApiActionHandler extends ActionHandler {

	static final Log LOG = LogFactory.getLog(JsonApiActionHandler.class);
	static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	
	protected TokenAuthorization auth;
	
	public JsonApiActionHandler() {
	}
	
	public void setTokenAuthorization(TokenAuthorization auth) {
		this.auth = auth;
	}
	
	@Override
	protected void setDefaultContentType(HttpServletResponse resp) {
		resp.setContentType(JSON_CONTENT_TYPE);
	}
	
	@Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
		ApiActionProcessor processor = new ApiActionProcessor();
		ActionDefine def = getActionDefine(req.getRequestURI());
		if (def != null) {
			processor.execute(def, req, resp);
		}
	}
}
