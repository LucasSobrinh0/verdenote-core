package com.sobrinholabs.verdenote_core.audit;

import jakarta.validation.constraints.Size;

public record FrontendLogRequest(
		@Size(max = 20) String level,
		@Size(max = 255) String context,
		@Size(max = 10) String status,
		@Size(max = 500) String url,
		@Size(max = 1000) String message,
		@Size(max = 4000) String body) {
}
