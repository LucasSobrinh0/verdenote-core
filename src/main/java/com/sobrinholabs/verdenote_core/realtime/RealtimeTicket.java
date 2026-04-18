package com.sobrinholabs.verdenote_core.realtime;

import com.sobrinholabs.verdenote_core.document.Document;
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
@Table(name = "realtime_tickets")
public class RealtimeTicket {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id")
	private Document document;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, length = 64)
	private String ticketHash;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String permissions;

	@Column(nullable = false)
	private OffsetDateTime expiresAt;

	private OffsetDateTime usedAt;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected RealtimeTicket() {
	}

	public RealtimeTicket(Document document, User user, String ticketHash, String permissions, OffsetDateTime expiresAt) {
		this.id = UUID.randomUUID();
		this.document = document;
		this.user = user;
		this.ticketHash = ticketHash;
		this.permissions = permissions;
		this.expiresAt = expiresAt;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public Document getDocument() {
		return document;
	}

	public User getUser() {
		return user;
	}

	public String getPermissions() {
		return permissions;
	}

	public OffsetDateTime getExpiresAt() {
		return expiresAt;
	}

	public OffsetDateTime getUsedAt() {
		return usedAt;
	}

	public void markUsed() {
		this.usedAt = OffsetDateTime.now();
	}
}
