package org.tamacat.mvc;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.mvc.ApiActionProcessor;
import org.tamacat.mvc.JsonApiActionHandler;
import org.tamacat.mvc.action.ActionDefine;
import org.tamacat.mvc.error.NotFoundException;
import org.tamacat.util.ClassUtils;
import org.tamacat.util.StringUtils;

public class RestfulActionHandler extends JsonApiActionHandler {

	static final Log LOG = LogFactory.getLog(RestfulActionHandler.class);

	protected int resourceNameIndex = 2;
	protected int resourceIdIndex = 3;

	protected ApiActionProcessor processor = new ApiActionProcessor();

	public void setResourceNameIndex(int resourceNameIndex) {
		this.resourceNameIndex = resourceNameIndex;
	}

	public void setResourceIdIndex(int resourceIdIndex) {
		this.resourceIdIndex = resourceIdIndex;
	}
	
	public void setUseOAuth2BearerAuthorization(boolean use) {
		processor.setUseOAuth2BearerAuthorization(use);
	}

	@Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
		ActionDefine def = getActionDefine(req);
		if (def != null) {
			processor.execute(def, req, resp);
		}
	}

	protected ActionDefine getActionDefine(HttpServletRequest req) {
		String action = req.getMethod().toLowerCase(); // get/post/put/patch/delete
		String uri = req.getRequestURI();
		if (uri != null) {
			for (String ch : actionNotFoundPath) {
				if (uri.indexOf(ch) >= 0) {
					throw new NotFoundException();
				}
			}
			try {
				Path path = Paths.get(uri);
				String className = getClassName(path);
				if (StringUtils.isNotEmpty(className) && StringUtils.isNotEmpty(action)) {
					RestfulRequestUtils.setResourceId(req, getResourceId(path));
					return new ActionDefine(className, action);
				}
			} catch (Exception e) { // java.nio.file.InvalidPathException
				throw new NotFoundException("uri=" + uri, e);
			}
		}
		return null;
	}

	@Override
	protected String getClassName(Path path) {
		String className = getResourceName(path);
		if (StringUtils.isEmpty(className)) {
			return null;
		}
		if (className.indexOf('_') >= 0) {
			String[] sep = className.split("_");
			StringBuilder names = new StringBuilder();
			for (String val : sep) {
				names.append(ClassUtils.getCamelCaseName(val));
			}
			className = names.toString();
		} else {
			className = ClassUtils.getCamelCaseName(className);
		}
		return packageName + "." + className + "Action";
	}
	
	protected String getResourceName(Path path) {
		return RestfulRequestUtils.getResource(path, resourceNameIndex);
	}
	
	protected String getResourceId(Path path) {
		return RestfulRequestUtils.getResource(path, resourceIdIndex);
	}
}
