package com.sobrinholabs.verdenote_core.document;

import jakarta.validation.constraints.NotNull;

public record UpdateCommentThreadRequest(
		@NotNull CommentThreadStatus status) {
}
