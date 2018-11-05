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

public class RESTfulApiActionHandler extends JsonApiActionHandler {
	
	static final Log LOG = LogFactory.getLog(RESTfulApiActionHandler.class);
	
	protected ApiActionProcessor processor = new ApiActionProcessor();

	public void useOAuth2BearerAuthorization(boolean use) {
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
		String action = req.getMethod().toLowerCase(); //get/post/put/patch/delete
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
					return new ActionDefine(className, action);
				}
			} catch (Exception e) { //java.nio.file.InvalidPathException
				throw new NotFoundException("uri="+uri, e);
			}
		}
		return null;
	}

	@Override
	protected String getClassName(Path path) {
		int count = path.getNameCount();
		LOG.trace("path=" + path + ", count=" + count);
		if (count <= 2) {
			return null;
		}
		String className = path.getName(count - 1).normalize().toString();
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
}
