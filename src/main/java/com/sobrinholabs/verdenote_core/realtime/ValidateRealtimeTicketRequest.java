package com.sobrinholabs.verdenote_core.realtime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ValidateRealtimeTicketRequest(
		@NotBlank String ticket,
		@NotNull UUID documentId) {
}
