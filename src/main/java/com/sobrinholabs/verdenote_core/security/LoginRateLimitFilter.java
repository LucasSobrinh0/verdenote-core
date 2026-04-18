package com.sobrinholabs.verdenote_core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobrinholabs.verdenote_core.auth.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRateLimitFilter extends OncePerRequestFilter {
	private final LoginAttemptService loginAttemptService;
	private final ObjectMapper objectMapper;

	public LoginRateLimitFilter(LoginAttemptService loginAttemptService, ObjectMapper objectMapper) {
		this.loginAttemptService = loginAttemptService;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (isLoginRequest(request) && loginAttemptService.isBlocked(request.getParameter("identifier"), request.getRemoteAddr())) {
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(response.getWriter(), Map.of("message", "Muitas tentativas de login. Tente novamente mais tarde."));
			return;
		}
		filterChain.doFilter(request, response);
	}

	private boolean isLoginRequest(HttpServletRequest request) {
		return "POST".equalsIgnoreCase(request.getMethod()) && "/api/auth/login".equals(request.getRequestURI());
	}
}
