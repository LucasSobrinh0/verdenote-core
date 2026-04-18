package com.sobrinholabs.verdenote_core.audit;

import com.sobrinholabs.verdenote_core.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_audit_events")
public class LoginAuditEvent {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(length = 255)
	private String identifier;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private LoginAuditEventType eventType;

	@Column(nullable = false, length = 45)
	private String ipAddress;

	@Column(length = 512)
	private String userAgent;

	@Column(length = 64)
	private String sessionIdHash;

	@Column(nullable = false)
	private boolean success;

	@Column(length = 255)
	private String reason;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected LoginAuditEvent() {
	}

	public LoginAuditEvent(User user, String identifier, LoginAuditEventType eventType, String ipAddress,
			String userAgent, String sessionIdHash, boolean success, String reason) {
		this.id = UUID.randomUUID();
		this.user = user;
		this.identifier = identifier;
		this.eventType = eventType;
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.sessionIdHash = sessionIdHash;
		this.success = success;
		this.reason = reason;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getIdentifier() {
		return identifier;
	}

	public LoginAuditEventType getEventType() {
		return eventType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getSessionIdHash() {
		return sessionIdHash;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getReason() {
		return reason;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
