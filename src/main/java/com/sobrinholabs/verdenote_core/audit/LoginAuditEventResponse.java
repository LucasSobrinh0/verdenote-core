package com.sobrinholabs.verdenote_core.audit;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LoginAuditEventResponse(
		UUID id,
		UUID userId,
		String username,
		String identifier,
		String eventType,
		String ipAddress,
		String userAgent,
		String sessionIdHash,
		boolean success,
		String reason,
		OffsetDateTime createdAt) {
	public static LoginAuditEventResponse from(LoginAuditEvent event) {
		return new LoginAuditEventResponse(
				event.getId(),
				event.getUser() != null ? event.getUser().getId() : null,
				event.getUser() != null ? event.getUser().getUsername() : null,
				event.getIdentifier(),
				event.getEventType().name(),
				event.getIpAddress(),
				event.getUserAgent(),
				event.getSessionIdHash(),
				event.isSuccess(),
				event.getReason(),
				event.getCreatedAt());
	}
}
