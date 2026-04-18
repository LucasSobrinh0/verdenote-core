package com.sobrinholabs.verdenote_core.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentVersionResponse(
		UUID id,
		long version,
		String reason,
		OffsetDateTime createdAt) {
	static DocumentVersionResponse from(DocumentVersion version) {
		return new DocumentVersionResponse(version.getId(), version.getVersion(), version.getReason(), version.getCreatedAt());
	}
}
