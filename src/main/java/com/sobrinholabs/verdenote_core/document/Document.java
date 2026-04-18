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
@Table(name = "documents")
public class Document {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private User owner;

	@Column(nullable = false, length = 180)
	private String title;

	private byte[] currentSnapshot;

	@Column(nullable = false)
	private long currentVersion;

	@Column(nullable = false)
	private boolean deleted;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected Document() {
	}

	public Document(User owner, String title) {
		this.id = UUID.randomUUID();
		this.owner = owner;
		this.title = title;
		this.currentVersion = 0;
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

	public User getOwner() {
		return owner;
	}

	public String getTitle() {
		return title;
	}

	public byte[] getCurrentSnapshot() {
		return currentSnapshot;
	}

	public long getCurrentVersion() {
		return currentVersion;
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

	public void rename(String title) {
		this.title = title;
	}

	public void markDeleted() {
		this.deleted = true;
	}

	public long nextVersion(byte[] snapshot) {
		this.currentVersion++;
		this.currentSnapshot = snapshot;
		return currentVersion;
	}

	public void restore(long version, byte[] snapshot) {
		this.currentVersion = version;
		this.currentSnapshot = snapshot;
	}
}
