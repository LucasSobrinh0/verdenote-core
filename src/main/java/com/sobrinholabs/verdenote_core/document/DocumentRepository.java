package com.sobrinholabs.verdenote_core.document;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
	@EntityGraph(attributePaths = "owner")
	@Query("""
			select distinct document from Document document
			left join DocumentAcl acl on acl.document = document
			where document.deleted = false
				and (document.owner.id = :userId
					or acl.user.id = :userId
					or acl.group.id in :groupIds)
			order by document.updatedAt desc
			""")
	Page<Document> findVisibleForUser(@Param("userId") UUID userId, @Param("groupIds") Collection<UUID> groupIds, Pageable pageable);

	@EntityGraph(attributePaths = "owner")
	@Query("""
			select distinct document from Document document
			left join DocumentAcl acl on acl.document = document
			where document.deleted = false
				and (document.owner.id = :userId
					or acl.user.id = :userId
					or acl.group.id in :groupIds)
				and lower(document.title) like lower(concat('%', :search, '%'))
			order by document.updatedAt desc
			""")
	Page<Document> searchVisibleForUser(@Param("userId") UUID userId, @Param("groupIds") Collection<UUID> groupIds,
			@Param("search") String search, Pageable pageable);
}
