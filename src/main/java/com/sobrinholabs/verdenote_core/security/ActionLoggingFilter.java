package com.sobrinholabs.verdenote_core.security;

import com.sobrinholabs.verdenote_core.audit.ActionLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ActionLoggingFilter extends OncePerRequestFilter {
	private final ActionLogService actionLogService;

	public ActionLoggingFilter(ActionLogService actionLogService) {
		this.actionLogService = actionLogService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (ServletException | IOException | RuntimeException exception) {
			if (shouldLog(request)) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				actionLogService.log(request, authentication, "HTTP_EXCEPTION", Map.of(
						"exception", exception.getClass().getSimpleName(),
						"message", exception.getMessage() != null ? exception.getMessage() : "-"));
			}
			throw exception;
		} finally {
			if (shouldLog(request)) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				actionLogService.log(request, authentication, "HTTP_ACTION", Map.of(
						"status", response.getStatus(),
						"session", request.getSession(false) != null ? "present" : "none"));
			}
		}
	}

	private boolean shouldLog(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.equals("/error")
				|| path.equals("/api/auth/me")
				|| path.equals("/api/auth/logout")
				|| path.startsWith("/api/admin/")
				|| path.startsWith("/api/user/");
	}
}
