package com.sobrinholabs.verdenote_core.document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentThreadRepository extends JpaRepository<CommentThread, UUID> {
	@EntityGraph(attributePaths = "createdBy")
	List<CommentThread> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
