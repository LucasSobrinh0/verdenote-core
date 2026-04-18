package com.sobrinholabs.verdenote_core.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDocumentRequest(
		@NotBlank @Size(max = 180) String title) {
}
