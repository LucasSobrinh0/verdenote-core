package com.sobrinholabs.verdenote_core.document;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentAclRepository extends JpaRepository<DocumentAcl, UUID> {
	@EntityGraph(attributePaths = {"user", "group"})
	List<DocumentAcl> findByDocumentId(UUID documentId);

	Optional<DocumentAcl> findByDocumentIdAndUserId(UUID documentId, UUID userId);

	Optional<DocumentAcl> findByDocumentIdAndGroupId(UUID documentId, UUID groupId);

	@Query("""
			select acl from DocumentAcl acl
			where acl.document.id = :documentId
				and (acl.user.id = :userId or acl.group.id in :groupIds)
			""")
	List<DocumentAcl> findApplicable(@Param("documentId") UUID documentId, @Param("userId") UUID userId,
			@Param("groupIds") Collection<UUID> groupIds);
}
