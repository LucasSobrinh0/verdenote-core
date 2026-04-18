package com.sobrinholabs.verdenote_core.document;

import com.sobrinholabs.verdenote_core.group.Group;
import com.sobrinholabs.verdenote_core.group.GroupRepository;
import com.sobrinholabs.verdenote_core.user.User;
import com.sobrinholabs.verdenote_core.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DocumentService {
	private final DocumentRepository documentRepository;
	private final DocumentAclRepository documentAclRepository;
	private final DocumentYjsUpdateRepository documentYjsUpdateRepository;
	private final DocumentVersionRepository documentVersionRepository;
	private final CommentThreadRepository commentThreadRepository;
	private final DocumentCommentRepository documentCommentRepository;
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final DocumentAccessService documentAccessService;
	private final DocumentAuditService documentAuditService;

	public DocumentService(
			DocumentRepository documentRepository,
			DocumentAclRepository documentAclRepository,
			DocumentYjsUpdateRepository documentYjsUpdateRepository,
			DocumentVersionRepository documentVersionRepository,
			CommentThreadRepository commentThreadRepository,
			DocumentCommentRepository documentCommentRepository,
			UserRepository userRepository,
			GroupRepository groupRepository,
			DocumentAccessService documentAccessService,
			DocumentAuditService documentAuditService) {
		this.documentRepository = documentRepository;
		this.documentAclRepository = documentAclRepository;
		this.documentYjsUpdateRepository = documentYjsUpdateRepository;
		this.documentVersionRepository = documentVersionRepository;
		this.commentThreadRepository = commentThreadRepository;
		this.documentCommentRepository = documentCommentRepository;
		this.userRepository = userRepository;
		this.groupRepository = groupRepository;
		this.documentAccessService = documentAccessService;
		this.documentAuditService = documentAuditService;
	}

	@Transactional(readOnly = true)
	public Page<DocumentSummaryResponse> list(User user, String search, Pageable pageable) {
		List<UUID> groupIds = user.getGroups().stream().map(Group::getId).toList();
		Page<Document> documents = StringUtils.hasText(search)
				? documentRepository.searchVisibleForUser(user.getId(), groupIds, search.trim(), pageable)
				: documentRepository.findVisibleForUser(user.getId(), groupIds, pageable);
		return documents.map(document -> {
			DocumentRole role = documentAccessService.roleFor(document, user);
			return DocumentSummaryResponse.from(document, role, documentAccessService.permissionsFor(role));
		});
	}

	@Transactional
	public DocumentDetailResponse create(User user, CreateDocumentRequest request, HttpServletRequest servletRequest) {
		Document document = documentRepository.save(new Document(user, request.title().trim()));
		documentAclRepository.save(DocumentAcl.forUser(document, user, DocumentRole.OWNER, user));
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_CREATED", Map.of("title", document.getTitle()));
		return DocumentDetailResponse.from(document, DocumentRole.OWNER, documentAccessService.permissionsFor(DocumentRole.OWNER));
	}

	@Transactional(readOnly = true)
	public DocumentDetailResponse get(User user, UUID documentId) {
		Document document = findActiveDocument(documentId);
		DocumentRole role = documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_READ);
		return DocumentDetailResponse.from(document, role, documentAccessService.permissionsFor(role));
	}

	@Transactional
	public DocumentDetailResponse rename(User user, UUID documentId, UpdateDocumentRequest request, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		DocumentRole role = documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_RENAME);
		document.rename(request.title().trim());
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_RENAMED", Map.of("title", document.getTitle()));
		return DocumentDetailResponse.from(document, role, documentAccessService.permissionsFor(role));
	}

	@Transactional
	public void delete(User user, UUID documentId, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_DELETE);
		document.markDeleted();
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_DELETED", Map.of());
	}

	@Transactional(readOnly = true)
	public List<DocumentAclResponse> listAcl(User user, UUID documentId) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_SHARE);
		return documentAclRepository.findByDocumentId(documentId).stream().map(DocumentAclResponse::from).toList();
	}

	@Transactional
	public DocumentAclResponse share(User user, UUID documentId, DocumentAclRequest request, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_SHARE);
		DocumentAcl acl = buildAcl(document, user, request);
		DocumentAcl saved = documentAclRepository.save(acl);
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_SHARED", Map.of("role", request.role().name()));
		return DocumentAclResponse.from(saved);
	}

	@Transactional
	public DocumentAclResponse updateAcl(User user, UUID documentId, UUID aclId, DocumentAclRequest request, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_SHARE);
		DocumentAcl acl = documentAclRepository.findById(aclId)
				.filter(item -> item.getDocument().getId().equals(documentId))
				.orElseThrow(() -> new IllegalArgumentException("Compartilhamento não encontrado."));
		if (acl.getRole() == DocumentRole.OWNER) {
			throw new IllegalArgumentException("O dono do documento não pode ser alterado por esta rota.");
		}
		acl.updateRole(request.role());
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_ACL_UPDATED", Map.of("role", request.role().name()));
		return DocumentAclResponse.from(acl);
	}

	@Transactional
	public void deleteAcl(User user, UUID documentId, UUID aclId, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_SHARE);
		DocumentAcl acl = documentAclRepository.findById(aclId)
				.filter(item -> item.getDocument().getId().equals(documentId))
				.orElseThrow(() -> new IllegalArgumentException("Compartilhamento não encontrado."));
		if (acl.getRole() == DocumentRole.OWNER) {
			throw new IllegalArgumentException("O dono do documento não pode ser removido por esta rota.");
		}
		documentAclRepository.delete(acl);
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_ACL_DELETED", Map.of("aclId", aclId));
	}

	@Transactional(readOnly = true)
	public List<DocumentVersionResponse> versions(User user, UUID documentId) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_HISTORY);
		return documentVersionRepository.findByDocumentIdOrderByVersionDesc(documentId).stream()
				.map(DocumentVersionResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DocumentVersionDetailResponse version(User user, UUID documentId, long version) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_HISTORY);
		return documentVersionRepository.findByDocumentIdAndVersion(document.getId(), version)
				.map(DocumentVersionDetailResponse::from)
				.orElseThrow(() -> new IllegalArgumentException("Versão não encontrada."));
	}

	@Transactional
	public DocumentDetailResponse restore(User user, UUID documentId, long version, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		DocumentRole role = documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_EDIT);
		DocumentVersion documentVersion = documentVersionRepository.findByDocumentIdAndVersion(documentId, version)
				.orElseThrow(() -> new IllegalArgumentException("Versão não encontrada."));
		document.restore(version, documentVersion.getSnapshotPayload());
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_RESTORED", Map.of("version", version));
		return DocumentDetailResponse.from(document, role, documentAccessService.permissionsFor(role));
	}

	@Transactional(readOnly = true)
	public List<CommentThreadResponse> comments(User user, UUID documentId) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_READ);
		List<CommentThread> threads = commentThreadRepository.findByDocumentIdOrderByCreatedAtAsc(document.getId());
		Map<UUID, List<CommentResponse>> commentsByThread = documentCommentRepository
				.findByThreadIdInOrderByCreatedAtAsc(threads.stream().map(CommentThread::getId).toList())
				.stream()
				.map(CommentResponse::from)
				.collect(java.util.stream.Collectors.groupingBy(CommentResponse::threadId));
		return threads.stream()
				.map(thread -> CommentThreadResponse.from(thread, commentsByThread.getOrDefault(thread.getId(), List.of())))
				.toList();
	}

	@Transactional
	public CommentThreadResponse createThread(User user, UUID documentId, CreateCommentThreadRequest request, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_COMMENT);
		CommentThread thread = commentThreadRepository.save(new CommentThread(
				document,
				request.anchorPayload(),
				StringUtils.hasText(request.selectedText()) ? request.selectedText().trim() : null,
				user));
		DocumentComment comment = documentCommentRepository.save(new DocumentComment(thread, request.body().trim(), user));
		documentAuditService.record(servletRequest, document, user, "COMMENT_THREAD_CREATED", Map.of("threadId", thread.getId()));
		return CommentThreadResponse.from(thread, List.of(CommentResponse.from(comment)));
	}

	@Transactional
	public CommentResponse addComment(User user, UUID documentId, UUID threadId, CreateCommentRequest request, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_COMMENT);
		CommentThread thread = commentThreadRepository.findById(threadId)
				.filter(item -> item.getDocument().getId().equals(documentId))
				.orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado."));
		DocumentComment comment = documentCommentRepository.save(new DocumentComment(thread, request.body().trim(), user));
		documentAuditService.record(servletRequest, document, user, "COMMENT_CREATED", Map.of("threadId", threadId));
		return CommentResponse.from(comment);
	}

	@Transactional
	public CommentThreadResponse updateThread(User user, UUID documentId, UUID threadId, UpdateCommentThreadRequest request,
			HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_COMMENT);
		CommentThread thread = commentThreadRepository.findById(threadId)
				.filter(item -> item.getDocument().getId().equals(documentId))
				.orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado."));
		thread.updateStatus(request.status());
		documentAuditService.record(servletRequest, document, user, "COMMENT_THREAD_UPDATED", Map.of("status", request.status().name()));
		List<CommentResponse> comments = documentCommentRepository.findByThreadIdInOrderByCreatedAtAsc(List.of(threadId)).stream()
				.map(CommentResponse::from)
				.toList();
		return CommentThreadResponse.from(thread, comments);
	}

	@Transactional
	public CommentResponse updateComment(User user, UUID documentId, UUID commentId, UpdateCommentRequest request, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_COMMENT);
		DocumentComment comment = documentCommentRepository.findById(commentId)
				.filter(item -> item.getThread().getDocument().getId().equals(documentId))
				.orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado."));
		if (!comment.getCreatedBy().getId().equals(user.getId())) {
			throw new SecurityException("Acesso negado.");
		}
		comment.updateBody(request.body().trim());
		documentAuditService.record(servletRequest, document, user, "COMMENT_UPDATED", Map.of("commentId", commentId));
		return CommentResponse.from(comment);
	}

	@Transactional
	public long persistYjsUpdate(User user, UUID documentId, PersistYjsUpdateRequest request, HttpServletRequest servletRequest) {
		Document document = findActiveDocument(documentId);
		documentAccessService.requireRole(document, user, DocumentPermission.DOCUMENT_EDIT);
		byte[] updatePayload = Base64.getDecoder().decode(request.updatePayloadBase64());
		byte[] snapshotPayload = StringUtils.hasText(request.snapshotPayloadBase64())
				? Base64.getDecoder().decode(request.snapshotPayloadBase64())
				: updatePayload;
		long version = document.nextVersion(snapshotPayload);
		documentYjsUpdateRepository.save(new DocumentYjsUpdate(document, version, updatePayload, user));
		if (version == 1 || version % 25 == 0) {
			documentVersionRepository.save(new DocumentVersion(document, version, snapshotPayload, user, version == 1 ? "initial" : "snapshot"));
		}
		documentAuditService.record(servletRequest, document, user, "DOCUMENT_UPDATE_PERSISTED", Map.of("version", version));
		return version;
	}

	public Document findActiveDocument(UUID documentId) {
		return documentRepository.findById(documentId)
				.filter(document -> !document.isDeleted())
				.orElseThrow(() -> new IllegalArgumentException("Documento não encontrado."));
	}

	private DocumentAcl buildAcl(Document document, User actor, DocumentAclRequest request) {
		boolean hasUserIdentifier = StringUtils.hasText(request.userIdentifier());
		int subjectCount = (request.userId() == null ? 0 : 1) + (hasUserIdentifier ? 1 : 0) + (request.groupId() == null ? 0 : 1);
		if (subjectCount != 1) {
			throw new IllegalArgumentException("Informe um usuário ou grupo para compartilhar.");
		}
		if (request.role() == DocumentRole.OWNER) {
			throw new IllegalArgumentException("Compartilhamento OWNER só é criado para o dono do documento.");
		}
		if (request.userId() != null || hasUserIdentifier) {
			User targetUser = request.userId() != null
					? userRepository.findById(request.userId()).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."))
					: userRepository.findByUsernameIgnoreCase(request.userIdentifier().trim())
							.or(() -> userRepository.findByEmailIgnoreCase(request.userIdentifier().trim()))
							.orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
			documentAclRepository.findByDocumentIdAndUserId(document.getId(), targetUser.getId())
					.ifPresent(existing -> {
						throw new IllegalArgumentException("Documento já compartilhado com este usuário.");
					});
			return DocumentAcl.forUser(document, targetUser, request.role(), actor);
		}
		Group group = groupRepository.findById(request.groupId())
				.orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado."));
		documentAclRepository.findByDocumentIdAndGroupId(document.getId(), group.getId())
				.ifPresent(existing -> {
					throw new IllegalArgumentException("Documento já compartilhado com este grupo.");
				});
		return DocumentAcl.forGroup(document, group, request.role(), actor);
	}

}
