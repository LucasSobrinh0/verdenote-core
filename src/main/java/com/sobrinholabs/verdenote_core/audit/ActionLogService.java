package com.sobrinholabs.verdenote_core.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ActionLogService {
	private final boolean enabled;
	private final Path logPath;

	public ActionLogService(@Value("${DEBUG:false}") String enabled,
			@Value("${verdenote.debug.action-log-path:logs/actions.log}") String actionLogPath) {
		this.enabled = "true".equalsIgnoreCase(enabled.trim());
		this.logPath = Path.of(actionLogPath);
	}

	public void log(HttpServletRequest request, Authentication authentication, String action, Map<String, ?> details) {
		if (!enabled) {
			return;
		}
		String line = "%s action=%s method=%s path=%s query=%s ip=%s principal=%s authorities=%s details=%s%n"
				.formatted(
						OffsetDateTime.now(),
						sanitize(action),
						sanitize(request.getMethod()),
						sanitize(request.getRequestURI()),
						sanitize(request.getQueryString()),
						sanitize(clientIp(request)),
						sanitize(principal(authentication)),
						sanitize(authorities(authentication)),
						sanitize(String.valueOf(details)));
		write(line);
	}

	public void logSystem(String action, Map<String, ?> details) {
		if (!enabled) {
			return;
		}
		String line = "%s action=%s details=%s%n"
				.formatted(OffsetDateTime.now(), sanitize(action), sanitize(String.valueOf(details)));
		write(line);
	}

	public void logFrontend(FrontendLogRequest request) {
		if (!enabled) {
			return;
		}
		String line = "%s action=FRONTEND_DEBUG level=%s context=%s status=%s url=%s message=%s body=%s%n"
				.formatted(
						OffsetDateTime.now(),
						sanitize(request.level()),
						sanitize(request.context()),
						sanitize(request.status()),
						sanitize(request.url()),
						sanitize(request.message()),
						sanitize(request.body()));
		write(line);
	}

	private synchronized void write(String line) {
		try {
			Files.createDirectories(logPath.getParent());
			Files.writeString(logPath, line, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException exception) {
			throw new IllegalStateException("Não foi possível escrever o action log.", exception);
		}
	}

	private String clientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(forwardedFor)) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private String principal(Authentication authentication) {
		if (authentication == null) {
			return "anonymous";
		}
		return authentication.getName();
	}

	private String authorities(Authentication authentication) {
		if (authentication == null) {
			return "[]";
		}
		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.sorted()
				.collect(Collectors.joining(",", "[", "]"));
	}

	private String sanitize(String value) {
		if (value == null) {
			return "-";
		}
		return value.replace('\n', ' ').replace('\r', ' ');
	}
}
