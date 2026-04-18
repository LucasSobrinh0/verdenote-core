package com.sobrinholabs.verdenote_core.document;

import com.sobrinholabs.verdenote_core.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@GetMapping
	public Page<DocumentSummaryResponse> list(
			@AuthenticationPrincipal CurrentUser currentUser,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return documentService.list(currentUser.getUser(), search, pageable);
	}

	@PostMapping
	public ResponseEntity<DocumentDetailResponse> create(
			@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody CreateDocumentRequest request,
			HttpServletRequest servletRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(documentService.create(currentUser.getUser(), request, servletRequest));
	}

	@GetMapping("/{documentId}")
	public DocumentDetailResponse get(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable UUID documentId) {
		return documentService.get(currentUser.getUser(), documentId);
	}

	@PatchMapping("/{documentId}")
	public DocumentDetailResponse update(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@Valid @RequestBody UpdateDocumentRequest request,
			HttpServletRequest servletRequest) {
		return documentService.rename(currentUser.getUser(), documentId, request, servletRequest);
	}

	@DeleteMapping("/{documentId}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			HttpServletRequest servletRequest) {
		documentService.delete(currentUser.getUser(), documentId, servletRequest);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{documentId}/acl")
	public List<DocumentAclResponse> acl(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable UUID documentId) {
		return documentService.listAcl(currentUser.getUser(), documentId);
	}

	@PostMapping("/{documentId}/acl")
	public ResponseEntity<DocumentAclResponse> share(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@Valid @RequestBody DocumentAclRequest request,
			HttpServletRequest servletRequest) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(documentService.share(currentUser.getUser(), documentId, request, servletRequest));
	}

	@PatchMapping("/{documentId}/acl/{aclId}")
	public DocumentAclResponse updateAcl(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@PathVariable UUID aclId,
			@Valid @RequestBody DocumentAclRequest request,
			HttpServletRequest servletRequest) {
		return documentService.updateAcl(currentUser.getUser(), documentId, aclId, request, servletRequest);
	}

	@DeleteMapping("/{documentId}/acl/{aclId}")
	public ResponseEntity<Void> deleteAcl(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@PathVariable UUID aclId,
			HttpServletRequest servletRequest) {
		documentService.deleteAcl(currentUser.getUser(), documentId, aclId, servletRequest);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{documentId}/versions")
	public List<DocumentVersionResponse> versions(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable UUID documentId) {
		return documentService.versions(currentUser.getUser(), documentId);
	}

	@GetMapping("/{documentId}/versions/{version}")
	public DocumentVersionDetailResponse version(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@PathVariable long version) {
		return documentService.version(currentUser.getUser(), documentId, version);
	}

	@PostMapping("/{documentId}/restore/{version}")
	public DocumentDetailResponse restore(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@PathVariable long version,
			HttpServletRequest servletRequest) {
		return documentService.restore(currentUser.getUser(), documentId, version, servletRequest);
	}

	@PostMapping("/{documentId}/updates")
	public java.util.Map<String, Long> persistUpdate(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@Valid @RequestBody PersistYjsUpdateRequest request,
			HttpServletRequest servletRequest) {
		long version = documentService.persistYjsUpdate(currentUser.getUser(), documentId, request, servletRequest);
		return java.util.Map.of("version", version);
	}

	@GetMapping("/{documentId}/comments")
	public List<CommentThreadResponse> comments(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable UUID documentId) {
		return documentService.comments(currentUser.getUser(), documentId);
	}

	@PostMapping("/{documentId}/comments/threads")
	public ResponseEntity<CommentThreadResponse> createThread(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@Valid @RequestBody CreateCommentThreadRequest request,
			HttpServletRequest servletRequest) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(documentService.createThread(currentUser.getUser(), documentId, request, servletRequest));
	}

	@PostMapping("/{documentId}/comments/threads/{threadId}/comments")
	public ResponseEntity<CommentResponse> addComment(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@PathVariable UUID threadId,
			@Valid @RequestBody CreateCommentRequest request,
			HttpServletRequest servletRequest) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(documentService.addComment(currentUser.getUser(), documentId, threadId, request, servletRequest));
	}

	@PatchMapping("/{documentId}/comments/threads/{threadId}")
	public CommentThreadResponse updateThread(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@PathVariable UUID threadId,
			@Valid @RequestBody UpdateCommentThreadRequest request,
			HttpServletRequest servletRequest) {
		return documentService.updateThread(currentUser.getUser(), documentId, threadId, request, servletRequest);
	}

	@PatchMapping("/{documentId}/comments/{commentId}")
	public CommentResponse updateComment(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID documentId,
			@PathVariable UUID commentId,
			@Valid @RequestBody UpdateCommentRequest request,
			HttpServletRequest servletRequest) {
		return documentService.updateComment(currentUser.getUser(), documentId, commentId, request, servletRequest);
	}
}
