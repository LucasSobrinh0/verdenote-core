package com.sobrinholabs.verdenote_core.document;

import com.sobrinholabs.verdenote_core.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "document_comments")
public class DocumentComment {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "thread_id")
	private CommentThread thread;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String body;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(nullable = false)
	private boolean edited;

	@Column(nullable = false)
	private boolean deleted;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected DocumentComment() {
	}

	public DocumentComment(CommentThread thread, String body, User createdBy) {
		this.id = UUID.randomUUID();
		this.thread = thread;
		this.body = body;
		this.createdBy = createdBy;
		this.edited = false;
		this.deleted = false;
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

	public CommentThread getThread() {
		return thread;
	}

	public String getBody() {
		return body;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public boolean isEdited() {
		return edited;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void updateBody(String body) {
		this.body = body;
		this.edited = true;
	}
}
