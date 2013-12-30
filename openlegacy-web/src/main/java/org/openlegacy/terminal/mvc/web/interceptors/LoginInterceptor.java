package org.openlegacy.terminal.mvc.web.interceptors;

import org.openlegacy.mvc.web.MvcConstants;
import org.openlegacy.terminal.TerminalSession;
import org.openlegacy.terminal.modules.login.LoginMetadata;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor extends HandlerInterceptorAdapter {

	@Inject
	private TerminalSession terminalSession;

	@Inject
	private LoginMetadata loginMetadata;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String requestUri = request.getRequestURI().substring(1);
		if (!request.getRequestURI().contains(".") && !requestUri.toLowerCase().endsWith(MvcConstants.LOGIN_VIEW)
				&& !requestUri.endsWith("logoff") && loginMetadata.getLoginScreenDefinition() != null
				&& !terminalSession.isConnected()) {
			String requestedPage = requestUri.substring(requestUri.indexOf("/") + 1);
			@SuppressWarnings("unchecked")
			Set<String> paramNames = request.getParameterMap().keySet();
			for (String paramName : paramNames) {
				requestedPage = MessageFormat.format("{0}&{1}={2}", requestedPage, paramName, request.getParameter(paramName));
			}
			requestedPage = requestedPage.replaceFirst("&", "?");
			response.sendRedirect(request.getContextPath() + "/login?requestedUrl=" + URLEncoder.encode(requestedPage, "UTF-8"));
			return false;
		}
		return true;
	}
}
