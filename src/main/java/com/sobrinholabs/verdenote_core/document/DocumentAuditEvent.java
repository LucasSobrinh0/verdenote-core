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
@Table(name = "document_audit_events")
public class DocumentAuditEvent {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id")
	private Document document;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_id")
	private User actor;

	@Column(nullable = false, length = 60)
	private String eventType;

	@Column(nullable = false, length = 45)
	private String ipAddress;

	@Column(length = 512)
	private String userAgent;

	@Column(columnDefinition = "TEXT")
	private String metadata;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected DocumentAuditEvent() {
	}

	public DocumentAuditEvent(Document document, User actor, String eventType, String ipAddress, String userAgent, String metadata) {
		this.id = UUID.randomUUID();
		this.document = document;
		this.actor = actor;
		this.eventType = eventType;
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.metadata = metadata;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}
}
