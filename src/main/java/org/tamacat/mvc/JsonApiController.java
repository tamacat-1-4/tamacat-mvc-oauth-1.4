/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.mvc.Controller;
import org.tamacat.mvc.ExceptionHandler;
import org.tamacat.mvc.RequestHandler;
import org.tamacat.mvc.impl.StatusExceptionHandler;

public class JsonApiController implements Controller {
	
	static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

	protected RequestHandler requestHandler = new JsonApiActionHandler();
	protected ExceptionHandler exceptionHandler = new JsonStatusExceptionHandler();

	@Override
	public void setHandler(Object handler) {
		if (handler instanceof RequestHandler) {
			requestHandler = (RequestHandler) handler;
		}
		if (handler instanceof ExceptionHandler) {
			exceptionHandler = (ExceptionHandler) handler;
		}
	}

	@Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
		resp.setContentType(JSON_CONTENT_TYPE);
		requestHandler.handleRequest(req, resp);
	}

	@Override
	public void handleException(HttpServletRequest req,
			HttpServletResponse resp, Exception e) {
		resp.setContentType(JSON_CONTENT_TYPE);
		exceptionHandler.handleException(req, resp, e);
	}

	@Override
	public void dispatcher(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	
	static class JsonStatusExceptionHandler extends StatusExceptionHandler {
		public JsonStatusExceptionHandler() {
			errorPagePath = "/WEB-INF/jsp/error/json";
		}
	}
}
