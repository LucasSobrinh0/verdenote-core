package com.sobrinholabs.verdenote_core.realtime;

import com.sobrinholabs.verdenote_core.document.DocumentPermission;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ValidateRealtimeTicketResponse(
		UUID documentId,
		UUID userId,
		String username,
		List<DocumentPermission> permissions,
		String currentSnapshotBase64,
		OffsetDateTime expiresAt) {
}
