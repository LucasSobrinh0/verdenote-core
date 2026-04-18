package com.sobrinholabs.verdenote_core.document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
	List<DocumentVersion> findByDocumentIdOrderByVersionDesc(UUID documentId);

	Optional<DocumentVersion> findByDocumentIdAndVersion(UUID documentId, long version);
}
