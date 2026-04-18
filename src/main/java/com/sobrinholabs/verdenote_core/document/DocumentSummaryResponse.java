package com.sobrinholabs.verdenote_core.document;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentSummaryResponse(
		UUID id,
		String title,
		UUID ownerId,
		String ownerUsername,
		long currentVersion,
		DocumentRole role,
		List<DocumentPermission> permissions,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
	static DocumentSummaryResponse from(Document document, DocumentRole role, List<DocumentPermission> permissions) {
		return new DocumentSummaryResponse(
				document.getId(),
				document.getTitle(),
				document.getOwner().getId(),
				document.getOwner().getUsername(),
				document.getCurrentVersion(),
				role,
				permissions,
				document.getCreatedAt(),
				document.getUpdatedAt());
	}
}
