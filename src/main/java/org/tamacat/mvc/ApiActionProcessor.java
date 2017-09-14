/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.action.Action;
import org.tamacat.mvc.action.ActionDefine;
import org.tamacat.mvc.action.ActionProcessor;
import org.tamacat.mvc.error.ForbiddenException;
import org.tamacat.mvc.error.HttpStatusException;
import org.tamacat.mvc.error.InternalServerErrorException;
import org.tamacat.mvc.error.InvalidRequestException;
import org.tamacat.mvc.error.NotFoundException;
import org.tamacat.mvc.oauth.TokenAuthorization;
import org.tamacat.mvc.oauth.OAuthRequestHandler;
import org.tamacat.mvc.oauth.error.MethodNotAllowedException;
import org.tamacat.mvc.util.ServletUtils;
import org.tamacat.util.ClassUtils;
import org.tamacat.util.IOUtils;

public class ApiActionProcessor extends ActionProcessor {
	static final Log LOG = LogFactory.getLog(ApiActionProcessor.class);
	
	static final String ACTION_KEY = "org.tamacat.mvc.Action";
	static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

	protected OAuthRequestHandler oauth;
	
	public ApiActionProcessor() {}
	
	public ApiActionProcessor(TokenAuthorization auth) {
		if (auth != null) {
			oauth = new OAuthRequestHandler(auth);
		}
	}
	
	@Override
	public void execute(ActionDefine actionDef, HttpServletRequest req, HttpServletResponse resp) {
		if (oauth != null) {
			if (oauth.handleOAuthRequest(req, resp)) {
				return;
			}
		}
		ServletUtils.setActionDefine(req, actionDef);
		Class<?> type = ClassUtils.forName(actionDef.getName());
		Action action = null;
		Method m = null;
		if (type != null) {
			m = ClassUtils.getMethod(type, actionDef.getAction(),
					HttpServletRequest.class, HttpServletResponse.class);
			if (m != null) {
				action = m.getAnnotation(Action.class);
				HttpMethodConstraint allowMethods = m.getAnnotation(HttpMethodConstraint.class);
				if (allowMethods != null) {
					if (req.getMethod().equals(allowMethods.value()) == false) {
						throw new MethodNotAllowedException();
					}
				}
			}
			if (action == null) {
				action = type.getAnnotation(Action.class);
				//throw new NotFoundException();
			}
			req.setAttribute(ACTION_KEY, action);
		} else {
			throw new NotFoundException();
		}
		//Access Control
		checkUserInRoles(action, req, resp);

		//Set Content-Type response header
		setContentType(action, req, resp);
		if (m != null) {
			try {
				//LOG.debug("actionName=" + actionDef.getActionName());
				Object o = ClassUtils.newInstance(type);
				try {
					m.invoke(o, req, resp);
				} finally {
					if (o instanceof AutoCloseable) {
						IOUtils.close(o);
					}
				}
			} catch (IllegalAccessException e) {
				throw new ForbiddenException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				// ignore
				//throw new NotFoundException();
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof InvalidRequestException) {
					throw (InvalidRequestException)e.getCause();
				}
				if (e.getCause() instanceof HttpStatusException) {
					throw (HttpStatusException) e.getCause();
				}
				throw new InternalServerErrorException(
					e.getMessage(), e.getCause());
			}
		} else {
			throw new NotFoundException();
		}
	}
	
	@Override
	protected void setContentType(Action action, HttpServletRequest req, HttpServletResponse resp) {
		resp.setContentType(JSON_CONTENT_TYPE);
	}
}
