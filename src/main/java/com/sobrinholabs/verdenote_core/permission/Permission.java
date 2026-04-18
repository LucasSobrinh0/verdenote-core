package com.sobrinholabs.verdenote_core.permission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "permissions")
public class Permission {
	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 80)
	private String name;

	@Column(nullable = false, length = 255)
	private String description;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected Permission() {
	}

	public Permission(String name, String description) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.description = description;
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

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
