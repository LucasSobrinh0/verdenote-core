package com.sobrinholabs.verdenote_core.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentThreadRequest(
		@NotBlank String anchorPayload,
		@Size(max = 1000) String selectedText,
		@NotBlank @Size(max = 4000) String body) {
}
