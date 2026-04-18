package com.sobrinholabs.verdenote_core.document;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DocumentAclRequest(
		UUID userId,
		String userIdentifier,
		UUID groupId,
		@NotNull DocumentRole role) {
}
