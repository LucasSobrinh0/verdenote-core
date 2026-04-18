package com.sobrinholabs.verdenote_core.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentAclResponse(
		UUID id,
		UUID userId,
		String username,
		UUID groupId,
		String groupName,
		DocumentRole role,
		OffsetDateTime createdAt) {
	static DocumentAclResponse from(DocumentAcl acl) {
		return new DocumentAclResponse(
				acl.getId(),
				acl.getUser() == null ? null : acl.getUser().getId(),
				acl.getUser() == null ? null : acl.getUser().getUsername(),
				acl.getGroup() == null ? null : acl.getGroup().getId(),
				acl.getGroup() == null ? null : acl.getGroup().getName(),
				acl.getRole(),
				acl.getCreatedAt());
	}
}
