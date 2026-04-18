package com.sobrinholabs.verdenote_core.document;

import com.sobrinholabs.verdenote_core.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_yjs_updates")
public class DocumentYjsUpdate {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id")
	private Document document;

	@Column(nullable = false)
	private long version;

	@Column(nullable = false)
	private byte[] updatePayload;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected DocumentYjsUpdate() {
	}

	public DocumentYjsUpdate(Document document, long version, byte[] updatePayload, User createdBy) {
		this.id = UUID.randomUUID();
		this.document = document;
		this.version = version;
		this.updatePayload = updatePayload;
		this.createdBy = createdBy;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}
}
