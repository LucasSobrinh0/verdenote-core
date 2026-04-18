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
@Table(name = "document_versions")
public class DocumentVersion {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id")
	private Document document;

	@Column(nullable = false)
	private long version;

	@Column(nullable = false)
	private byte[] snapshotPayload;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(nullable = false, length = 120)
	private String reason;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected DocumentVersion() {
	}

	public DocumentVersion(Document document, long version, byte[] snapshotPayload, User createdBy, String reason) {
		this.id = UUID.randomUUID();
		this.document = document;
		this.version = version;
		this.snapshotPayload = snapshotPayload;
		this.createdBy = createdBy;
		this.reason = reason;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public long getVersion() {
		return version;
	}

	public byte[] getSnapshotPayload() {
		return snapshotPayload;
	}

	public String getReason() {
		return reason;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
