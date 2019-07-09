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

public class JsonApiActionHandler extends ActionHandler {

	static final Log LOG = LogFactory.getLog(JsonApiActionHandler.class);
	static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

	protected ApiActionProcessor processor;

	public JsonApiActionHandler() {
		this(new ApiActionProcessor());	
	}
	
	protected JsonApiActionHandler(ApiActionProcessor processor) {
		setApiActionProcessor(processor);
	}

	public void setApiActionProcessor(ApiActionProcessor processor) {
		this.processor = processor;
	}

	@Override
	protected void setDefaultContentType(HttpServletResponse resp) {
		resp.setContentType(JSON_CONTENT_TYPE);
	}

	@Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
		ActionDefine def = getActionDefine(req.getRequestURI());
		if (def != null) {
			processor.execute(def, req, resp);
		}
	}

	public void setUseOAuth2BearerAuthorization(boolean useOAuth2BearerAuthorization) {
		processor.setUseOAuth2BearerAuthorization(useOAuth2BearerAuthorization);
	}
	
	/**
	 * Allow trust Self-signed certification. (for Development/Test use)
	 * @since 1.4-20190702
	 */
	public void setAllowTrustSelfSignedCertificates(boolean allowTrustSelfSignedCertificates) {
		processor.setAllowTrustSelfSignedCertificates(allowTrustSelfSignedCertificates);
	}

	/**
	 * Disabled SSL hostname verifier. (for Development/Test use)
	 * @since 1.4-20190702
	 */
	public void setDisabledSSLHostnameVerifier(boolean disabledSSLHostnameVerifier) {
		processor.setDisabledSSLHostnameVerifier(disabledSSLHostnameVerifier);
	}
}
