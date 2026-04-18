package com.sobrinholabs.verdenote_core.group;

import com.sobrinholabs.verdenote_core.permission.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class Group {
	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 60)
	private String name;

	@Column(nullable = false, length = 255)
	private String description;

	@Column(nullable = false)
	private boolean systemGroup;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "group_permissions",
			joinColumns = @JoinColumn(name = "group_id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private Set<Permission> permissions = new LinkedHashSet<>();

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected Group() {
	}

	public Group(String name, String description, boolean systemGroup) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.description = description;
		this.systemGroup = systemGroup;
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

	public boolean isSystemGroup() {
		return systemGroup;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void update(String name, String description, Set<Permission> permissions) {
		if (!systemGroup) {
			this.name = name;
		}
		this.description = description;
		this.permissions.clear();
		this.permissions.addAll(permissions);
	}
}
