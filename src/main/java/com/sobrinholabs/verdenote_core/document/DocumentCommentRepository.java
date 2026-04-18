package com.sobrinholabs.verdenote_core.document;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, UUID> {
	@EntityGraph(attributePaths = {"thread", "createdBy"})
	List<DocumentComment> findByThreadIdInOrderByCreatedAtAsc(Collection<UUID> threadIds);
}
