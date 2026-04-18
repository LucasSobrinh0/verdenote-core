package com.sobrinholabs.verdenote_core.document;

import com.sobrinholabs.verdenote_core.user.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DocumentAuditService {
	private final DocumentAuditEventRepository documentAuditEventRepository;

	public DocumentAuditService(DocumentAuditEventRepository documentAuditEventRepository) {
		this.documentAuditEventRepository = documentAuditEventRepository;
	}

	public void record(HttpServletRequest request, Document document, User actor, String eventType, Map<String, ?> metadata) {
		documentAuditEventRepository.save(new DocumentAuditEvent(
				document,
				actor,
				eventType,
				clientIp(request),
				truncate(request.getHeader("User-Agent"), 512),
				metadata == null || metadata.isEmpty() ? null : metadata.toString()));
	}

	private String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
