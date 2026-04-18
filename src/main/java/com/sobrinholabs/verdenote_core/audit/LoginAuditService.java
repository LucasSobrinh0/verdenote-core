package com.sobrinholabs.verdenote_core.audit;

import com.sobrinholabs.verdenote_core.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LoginAuditService {
	private final LoginAuditEventRepository loginAuditEventRepository;

	public LoginAuditService(LoginAuditEventRepository loginAuditEventRepository) {
		this.loginAuditEventRepository = loginAuditEventRepository;
	}

	@Transactional
	public void record(HttpServletRequest request, User user, String identifier, LoginAuditEventType eventType,
			boolean success, String reason) {
		loginAuditEventRepository.save(new LoginAuditEvent(
				user,
				normalize(identifier),
				eventType,
				clientIp(request),
				truncate(request.getHeader("User-Agent"), 512),
				sessionHash(request.getSession(false)),
				success,
				truncate(reason, 255)));
	}

	@Transactional(readOnly = true)
	public Page<LoginAuditEventResponse> search(String search, Pageable pageable) {
		if (!StringUtils.hasText(search)) {
			return loginAuditEventRepository.findAll(pageable).map(LoginAuditEventResponse::from);
		}
		return loginAuditEventRepository.search(search.trim(), pageable).map(LoginAuditEventResponse::from);
	}

	public String clientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(forwardedFor)) {
			return truncate(forwardedFor.split(",")[0].trim(), 45);
		}
		return truncate(request.getRemoteAddr(), 45);
	}

	private String sessionHash(HttpSession session) {
		if (session == null) {
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(session.getId().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 indisponível.", exception);
		}
	}

	private String normalize(String value) {
		return StringUtils.hasText(value) ? truncate(value.trim().toLowerCase(), 255) : null;
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
