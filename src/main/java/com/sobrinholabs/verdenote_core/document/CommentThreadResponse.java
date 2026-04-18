package com.sobrinholabs.verdenote_core.document;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CommentThreadResponse(
		UUID id,
		String anchorPayload,
		String selectedText,
		CommentThreadStatus status,
		String warning,
		UUID createdBy,
		String createdByUsername,
		List<CommentResponse> comments,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
	static CommentThreadResponse from(CommentThread thread, List<CommentResponse> comments) {
		return new CommentThreadResponse(
				thread.getId(),
				thread.getAnchorPayload(),
				thread.getSelectedText(),
				thread.getStatus(),
				thread.getStatus() == CommentThreadStatus.ORPHANED ? "Texto original removido" : null,
				thread.getCreatedBy().getId(),
				thread.getCreatedBy().getUsername(),
				comments,
				thread.getCreatedAt(),
				thread.getUpdatedAt());
	}
}
