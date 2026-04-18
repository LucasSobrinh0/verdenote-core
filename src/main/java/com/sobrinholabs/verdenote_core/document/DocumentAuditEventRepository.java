package com.sobrinholabs.verdenote_core.document;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAuditEventRepository extends JpaRepository<DocumentAuditEvent, UUID> {
}
