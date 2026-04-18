package com.sobrinholabs.verdenote_core.document;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comment_threads")
public class CommentThread {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id")
	private Document document;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String anchorPayload;

	@Column(columnDefinition = "TEXT")
	private String selectedText;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommentThreadStatus status;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected CommentThread() {
	}

	public CommentThread(Document document, String anchorPayload, String selectedText, User createdBy) {
		this.id = UUID.randomUUID();
		this.document = document;
		this.anchorPayload = anchorPayload;
		this.selectedText = selectedText;
		this.status = CommentThreadStatus.OPEN;
		this.createdBy = createdBy;
	}

	@PrePersist
	void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public Document getDocument() {
		return document;
	}

	public String getAnchorPayload() {
		return anchorPayload;
	}

	public String getSelectedText() {
		return selectedText;
	}

	public CommentThreadStatus getStatus() {
		return status;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void updateStatus(CommentThreadStatus status) {
		this.status = status;
	}
}
