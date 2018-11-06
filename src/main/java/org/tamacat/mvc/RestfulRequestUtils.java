package org.tamacat.mvc;

import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

import org.tamacat.util.StringUtils;

public class RestfulRequestUtils {

	public static String getResource(Path path, int index) {
		int count = path.getNameCount();
		if (count <= index) {
			return null;
		}
		return path.getName(index).normalize().toString();
	}

	public static void setResourceId(HttpServletRequest req, String id) {
		if (StringUtils.isNotEmpty(id)) {
			req.setAttribute("RESTfulRequest.resourceId", id);
		}
	}

	public static String getResourceId(HttpServletRequest req) {
		return (String) req.getAttribute("RESTfulRequest.resourceId");
	}

}
