package com.sobrinholabs.verdenote_core.security;

import com.sobrinholabs.verdenote_core.audit.ActionLogService;
import com.sobrinholabs.verdenote_core.audit.LoginAuditEventType;
import com.sobrinholabs.verdenote_core.audit.LoginAuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {
	private final String rememberMeCookieName;
	private final boolean secureCookie;
	private final LoginAuditService loginAuditService;
	private final ActionLogService actionLogService;

	public LogoutSuccessHandler(
			@Value("${verdenote.remember-me.cookie-name:VERDENOTE_REMEMBER_ME}") String rememberMeCookieName,
			@Value("${verdenote.cookies.secure:true}") boolean secureCookie,
			LoginAuditService loginAuditService,
			ActionLogService actionLogService) {
		this.rememberMeCookieName = rememberMeCookieName;
		this.secureCookie = secureCookie;
		this.loginAuditService = loginAuditService;
		this.actionLogService = actionLogService;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.Authentication authentication) throws IOException, ServletException {
		if (authentication != null && authentication.getPrincipal() instanceof CurrentUser currentUser) {
			loginAuditService.record(request, currentUser.getUser(), currentUser.getUsername(),
					LoginAuditEventType.LOGOUT, true, null);
			actionLogService.log(request, authentication, "LOGOUT", Map.of("username", currentUser.getUsername()));
		}
		clearCookie(response, "VERDENOTE_SESSION");
		clearCookie(response, rememberMeCookieName);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	private void clearCookie(HttpServletResponse response, String name) {
		Cookie cookie = new Cookie(name, "");
		cookie.setHttpOnly(true);
		cookie.setSecure(secureCookie);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}
