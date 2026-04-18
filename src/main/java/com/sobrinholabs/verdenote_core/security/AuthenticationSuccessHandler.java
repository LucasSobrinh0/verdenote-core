package com.sobrinholabs.verdenote_core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobrinholabs.verdenote_core.auth.AuthUserResponse;
import com.sobrinholabs.verdenote_core.auth.LoginAttemptService;
import com.sobrinholabs.verdenote_core.audit.ActionLogService;
import com.sobrinholabs.verdenote_core.audit.LoginAuditEventType;
import com.sobrinholabs.verdenote_core.audit.LoginAuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {
	private final ObjectMapper objectMapper;
	private final LoginAttemptService loginAttemptService;
	private final LoginAuditService loginAuditService;
	private final ActionLogService actionLogService;

	public AuthenticationSuccessHandler(ObjectMapper objectMapper, LoginAttemptService loginAttemptService,
			LoginAuditService loginAuditService, ActionLogService actionLogService) {
		this.objectMapper = objectMapper;
		this.loginAttemptService = loginAttemptService;
		this.loginAuditService = loginAuditService;
		this.actionLogService = actionLogService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		loginAttemptService.loginSucceeded(request.getParameter("identifier"), request.getRemoteAddr());
		CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
		request.getSession().setAttribute(SessionIpTrackingFilter.SESSION_IP_ATTRIBUTE, loginAuditService.clientIp(request));
		loginAuditService.record(request, currentUser.getUser(), request.getParameter("identifier"),
				LoginAuditEventType.LOGIN_SUCCESS, true, null);
		actionLogService.log(request, authentication, "LOGIN_SUCCESS", Map.of(
				"username", currentUser.getUsername(),
				"groups", currentUser.getUser().getGroups().stream().map(group -> group.getName()).sorted().toList()));
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), AuthUserResponse.from(currentUser.getUser()));
	}
}
