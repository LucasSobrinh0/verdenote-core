package com.sobrinholabs.verdenote_core.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobrinholabs.verdenote_core.document.Document;
import com.sobrinholabs.verdenote_core.document.DocumentAccessService;
import com.sobrinholabs.verdenote_core.document.DocumentPermission;
import com.sobrinholabs.verdenote_core.document.DocumentService;
import com.sobrinholabs.verdenote_core.user.User;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RealtimeTicketService {
	private static final int TICKET_BYTES = 32;

	private final RealtimeTicketRepository realtimeTicketRepository;
	private final DocumentService documentService;
	private final DocumentAccessService documentAccessService;
	private final ObjectMapper objectMapper;
	private final SecureRandom secureRandom = new SecureRandom();

	public RealtimeTicketService(
			RealtimeTicketRepository realtimeTicketRepository,
			DocumentService documentService,
			DocumentAccessService documentAccessService,
			ObjectMapper objectMapper) {
		this.realtimeTicketRepository = realtimeTicketRepository;
		this.documentService = documentService;
		this.documentAccessService = documentAccessService;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public RealtimeTicketResponse issue(User user, CreateRealtimeTicketRequest request, HttpServletRequest servletRequest) {
		Document document = documentService.findActiveDocument(request.documentId());
		var role = documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_READ);
		List<DocumentPermission> permissions = documentAccessService.permissionsFor(role);
		String token = newToken();
		OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(3);
		realtimeTicketRepository.save(new RealtimeTicket(document, user, hash(token), permissionsJson(permissions), expiresAt));
		return new RealtimeTicketResponse(token, document.getId(), user.getId(), permissions, expiresAt);
	}

	@Transactional
	public ValidateRealtimeTicketResponse validateForSocket(ValidateRealtimeTicketRequest request) {
		RealtimeTicket ticket = findValidTicket(request.ticket(), request.documentId(), false);
		documentAccessService.requireRole(ticket.getDocument(), ticket.getUser(), DocumentPermission.DOCUMENT_READ);
		ticket.markUsed();
		return response(ticket);
	}

	@Transactional(readOnly = true)
	public User validateForPersist(String token, java.util.UUID documentId) {
		RealtimeTicket ticket = findValidTicket(token, documentId, true);
		if (!permissions(ticket).contains(DocumentPermission.DOCUMENT_EDIT)) {
			throw new SecurityException("Acesso negado.");
		}
		documentAccessService.requireRole(ticket.getDocument(), ticket.getUser(), DocumentPermission.DOCUMENT_EDIT);
		return ticket.getUser();
	}

	private RealtimeTicket findValidTicket(String token, java.util.UUID documentId, boolean allowUsed) {
		RealtimeTicket ticket = realtimeTicketRepository.findByTicketHash(hash(token))
				.filter(item -> item.getDocument().getId().equals(documentId))
				.orElseThrow(() -> new SecurityException("Ticket inválido."));
		if (ticket.getExpiresAt().isBefore(OffsetDateTime.now())) {
			throw new SecurityException("Ticket expirado.");
		}
		if (!allowUsed && ticket.getUsedAt() != null) {
			throw new SecurityException("Ticket já utilizado.");
		}
		return ticket;
	}

	private ValidateRealtimeTicketResponse response(RealtimeTicket ticket) {
		var currentRole = documentAccessService.roleFor(ticket.getDocument(), ticket.getUser());
		return new ValidateRealtimeTicketResponse(
				ticket.getDocument().getId(),
				ticket.getUser().getId(),
				ticket.getUser().getUsername(),
				documentAccessService.permissionsFor(currentRole),
				ticket.getDocument().getCurrentSnapshot() == null ? null : Base64.getEncoder().encodeToString(ticket.getDocument().getCurrentSnapshot()),
				ticket.getExpiresAt());
	}

	private List<DocumentPermission> permissions(RealtimeTicket ticket) {
		try {
			return objectMapper.readValue(ticket.getPermissions(), new TypeReference<>() {
			});
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Permissões inválidas no ticket.");
		}
	}

	private String permissionsJson(List<DocumentPermission> permissions) {
		try {
			return objectMapper.writeValueAsString(permissions);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Não foi possível criar ticket realtime.");
		}
	}

	private String newToken() {
		byte[] bytes = new byte[TICKET_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String token) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 não disponível.");
		}
	}
}
