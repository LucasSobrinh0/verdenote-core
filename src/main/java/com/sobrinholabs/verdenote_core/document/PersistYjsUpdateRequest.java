package com.sobrinholabs.verdenote_core.document;

import jakarta.validation.constraints.NotBlank;

public record PersistYjsUpdateRequest(
		@NotBlank String updatePayloadBase64,
		String snapshotPayloadBase64) {
}
