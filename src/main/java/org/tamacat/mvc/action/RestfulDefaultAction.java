package org.tamacat.mvc.action;

import javax.servlet.http.HttpServletRequest;

import org.tamacat.mvc.RestfulRequestUtils;

public class RestfulDefaultAction {

	public String getResourceId(HttpServletRequest req) {
		return RestfulRequestUtils.getResourceId(req);
	}
}
