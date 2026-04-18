package com.sobrinholabs.verdenote_core.security;

import com.sobrinholabs.verdenote_core.audit.LoginAuditEventType;
import com.sobrinholabs.verdenote_core.audit.LoginAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionIpTrackingFilter extends OncePerRequestFilter {
	public static final String SESSION_IP_ATTRIBUTE = "VERDENOTE_SESSION_IP";

	private final LoginAuditService loginAuditService;

	public SessionIpTrackingFilter(LoginAuditService loginAuditService) {
		this.loginAuditService = loginAuditService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		HttpSession session = request.getSession(false);

		if (session != null && authentication != null && authentication.isAuthenticated()
				&& authentication.getPrincipal() instanceof CurrentUser currentUser) {
			String currentIp = loginAuditService.clientIp(request);
			String previousIp = (String) session.getAttribute(SESSION_IP_ATTRIBUTE);
			if (previousIp == null) {
				session.setAttribute(SESSION_IP_ATTRIBUTE, currentIp);
			} else if (!previousIp.equals(currentIp)) {
				loginAuditService.record(request, currentUser.getUser(), currentUser.getUsername(),
						LoginAuditEventType.SESSION_IP_CHANGED, true, "IP anterior: " + previousIp);
				session.setAttribute(SESSION_IP_ATTRIBUTE, currentIp);
			}
		}

		filterChain.doFilter(request, response);
	}
}
