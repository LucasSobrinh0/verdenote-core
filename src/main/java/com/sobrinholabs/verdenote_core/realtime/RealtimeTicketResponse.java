package com.sobrinholabs.verdenote_core.realtime;

import com.sobrinholabs.verdenote_core.document.DocumentPermission;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RealtimeTicketResponse(
		String ticket,
		UUID documentId,
		UUID userId,
		List<DocumentPermission> permissions,
		OffsetDateTime expiresAt) {
}
