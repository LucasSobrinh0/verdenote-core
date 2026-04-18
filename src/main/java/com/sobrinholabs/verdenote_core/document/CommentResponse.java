package com.sobrinholabs.verdenote_core.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
		UUID id,
		UUID threadId,
		String body,
		UUID createdBy,
		String createdByUsername,
		boolean edited,
		boolean deleted,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
	static CommentResponse from(DocumentComment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getThread().getId(),
				comment.getBody(),
				comment.getCreatedBy().getId(),
				comment.getCreatedBy().getUsername(),
				comment.isEdited(),
				comment.isDeleted(),
				comment.getCreatedAt(),
				comment.getUpdatedAt());
	}
}
