package com.sobrinholabs.verdenote_core.document;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

public record DocumentVersionDetailResponse(
		UUID id,
		long version,
		String snapshotPayloadBase64,
		String reason,
		OffsetDateTime createdAt) {
	static DocumentVersionDetailResponse from(DocumentVersion version) {
		return new DocumentVersionDetailResponse(
				version.getId(),
				version.getVersion(),
				Base64.getEncoder().encodeToString(version.getSnapshotPayload()),
				version.getReason(),
				version.getCreatedAt());
	}
}
