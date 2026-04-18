package com.sobrinholabs.verdenote_core.realtime;

import jakarta.validation.constraints.NotBlank;

public record RealtimePersistUpdateRequest(
		@NotBlank String ticket,
		@NotBlank String updatePayloadBase64,
		String snapshotPayloadBase64) {
}
