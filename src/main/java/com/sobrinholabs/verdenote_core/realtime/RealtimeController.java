package com.sobrinholabs.verdenote_core.realtime;

import com.sobrinholabs.verdenote_core.document.DocumentService;
import com.sobrinholabs.verdenote_core.document.PersistYjsUpdateRequest;
import com.sobrinholabs.verdenote_core.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeController {
	private final RealtimeTicketService realtimeTicketService;
	private final DocumentService documentService;
	private final RealtimeServiceAuthenticator realtimeServiceAuthenticator;

	public RealtimeController(RealtimeTicketService realtimeTicketService, DocumentService documentService,
			RealtimeServiceAuthenticator realtimeServiceAuthenticator) {
		this.realtimeTicketService = realtimeTicketService;
		this.documentService = documentService;
		this.realtimeServiceAuthenticator = realtimeServiceAuthenticator;
	}

	@PostMapping("/tickets")
	public RealtimeTicketResponse createTicket(
			@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody CreateRealtimeTicketRequest request,
			HttpServletRequest servletRequest) {
		return realtimeTicketService.issue(currentUser.getUser(), request, servletRequest);
	}

	@PostMapping("/tickets/validate")
	public ValidateRealtimeTicketResponse validateTicket(@Valid @RequestBody ValidateRealtimeTicketRequest request,
			HttpServletRequest servletRequest) {
		realtimeServiceAuthenticator.requireValidSecret(servletRequest);
		return realtimeTicketService.validateForSocket(request);
	}

	@PostMapping("/documents/{documentId}/updates")
	public RealtimePersistUpdateResponse persistUpdate(
			@PathVariable UUID documentId,
			@Valid @RequestBody RealtimePersistUpdateRequest request,
			HttpServletRequest servletRequest) {
		realtimeServiceAuthenticator.requireValidSecret(servletRequest);
		var user = realtimeTicketService.validateForPersist(request.ticket(), documentId);
		long version = documentService.persistYjsUpdate(
				user,
				documentId,
				new PersistYjsUpdateRequest(request.updatePayloadBase64(), request.snapshotPayloadBase64()),
				servletRequest);
		return new RealtimePersistUpdateResponse(version);
	}
}
