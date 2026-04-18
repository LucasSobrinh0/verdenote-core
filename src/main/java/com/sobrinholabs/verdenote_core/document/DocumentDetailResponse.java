package com.sobrinholabs.verdenote_core.document;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public record DocumentDetailResponse(
		UUID id,
		String title,
		UUID ownerId,
		String ownerUsername,
		long currentVersion,
		String currentSnapshotBase64,
		DocumentRole role,
		List<DocumentPermission> permissions,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
	static DocumentDetailResponse from(Document document, DocumentRole role, List<DocumentPermission> permissions) {
		byte[] snapshot = document.getCurrentSnapshot();
		return new DocumentDetailResponse(
				document.getId(),
				document.getTitle(),
				document.getOwner().getId(),
				document.getOwner().getUsername(),
				document.getCurrentVersion(),
				snapshot == null ? null : Base64.getEncoder().encodeToString(snapshot),
				role,
				permissions,
				document.getCreatedAt(),
				document.getUpdatedAt());
	}
}
