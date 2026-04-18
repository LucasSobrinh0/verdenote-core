package com.sobrinholabs.verdenote_core.realtime;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRealtimeTicketRequest(
		@NotNull UUID documentId) {
}
