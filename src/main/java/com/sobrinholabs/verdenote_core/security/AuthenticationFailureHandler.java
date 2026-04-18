package com.sobrinholabs.verdenote_core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureHandler implements org.springframework.security.web.authentication.AuthenticationFailureHandler {
	private final ObjectMapper objectMapper;
	private final LoginAttemptService loginAttemptService;
	private final LoginAuditService loginAuditService;
	private final ActionLogService actionLogService;

	public AuthenticationFailureHandler(ObjectMapper objectMapper, LoginAttemptService loginAttemptService,
			LoginAuditService loginAuditService, ActionLogService actionLogService) {
		this.objectMapper = objectMapper;
		this.loginAttemptService = loginAttemptService;
		this.loginAuditService = loginAuditService;
		this.actionLogService = actionLogService;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {
		loginAttemptService.loginFailed(request.getParameter("identifier"), request.getRemoteAddr());
		loginAuditService.record(request, null, request.getParameter("identifier"), LoginAuditEventType.LOGIN_FAILURE,
				false, "Credenciais inválidas.");
		actionLogService.log(request, null, "LOGIN_FAILURE", Map.of(
				"identifier", request.getParameter("identifier") != null ? request.getParameter("identifier") : "-",
				"reason", "Credenciais inválidas."));
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), Map.of("message", "Credenciais inválidas."));
	}
}
