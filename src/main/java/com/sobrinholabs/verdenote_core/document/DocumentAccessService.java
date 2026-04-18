package com.sobrinholabs.verdenote_core.document;

import com.sobrinholabs.verdenote_core.user.User;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentAccessService {
	private final DocumentAclRepository documentAclRepository;

	public DocumentAccessService(DocumentAclRepository documentAclRepository) {
		this.documentAclRepository = documentAclRepository;
	}

	public DocumentRole requireRole(Document document, User user, DocumentPermission permission) {
		DocumentRole role = roleFor(document, user);
		if (!permissionsFor(role).contains(permission)) {
			throw new SecurityException("Acesso negado.");
		}
		return role;
	}

	public DocumentRole roleFor(Document document, User user) {
		if (document.getOwner().getId().equals(user.getId())) {
			return DocumentRole.OWNER;
		}
		Set<UUID> groupIds = user.getGroups().stream()
				.map(group -> group.getId())
				.collect(java.util.stream.Collectors.toSet());
		return documentAclRepository.findApplicable(document.getId(), user.getId(), groupIds).stream()
				.map(DocumentAcl::getRole)
				.min(Comparator.comparingInt(this::rank))
				.orElseThrow(() -> new SecurityException("Acesso negado."));
	}

	public List<DocumentPermission> permissionsFor(DocumentRole role) {
		return switch (role) {
			case OWNER -> List.of(
					DocumentPermission.DOCUMENT_READ,
					DocumentPermission.DOCUMENT_EDIT,
					DocumentPermission.DOCUMENT_COMMENT,
					DocumentPermission.DOCUMENT_SHARE,
					DocumentPermission.DOCUMENT_RENAME,
					DocumentPermission.DOCUMENT_DELETE,
					DocumentPermission.DOCUMENT_HISTORY);
			case EDITOR -> List.of(
					DocumentPermission.DOCUMENT_READ,
					DocumentPermission.DOCUMENT_EDIT,
					DocumentPermission.DOCUMENT_COMMENT,
					DocumentPermission.DOCUMENT_HISTORY);
			case VIEWER -> List.of(DocumentPermission.DOCUMENT_READ);
		};
	}

	private int rank(DocumentRole role) {
		return switch (role) {
			case OWNER -> 0;
			case EDITOR -> 1;
			case VIEWER -> 2;
		};
	}
}
