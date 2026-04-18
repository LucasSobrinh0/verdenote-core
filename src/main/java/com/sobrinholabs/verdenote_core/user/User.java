package com.sobrinholabs.verdenote_core.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sobrinholabs.verdenote_core.group.Group;
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
@Table(name = "users")
public class User {
	@Id
	private UUID id;

	@Column(nullable = false, length = 80)
	private String firstName;

	@Column(nullable = false, length = 80)
	private String lastName;

	@Column(nullable = false, length = 50)
	private String username;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, length = 100)
	private String passwordHash;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "user_groups",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "group_id"))
	private Set<Group> groups = new LinkedHashSet<>();

	@Column(nullable = false)
	private boolean enabled;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected User() {
	}

	public User(String firstName, String lastName, String username, String email, String passwordHash, Set<Group> groups) {
		this.id = UUID.randomUUID();
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.groups.addAll(groups);
		this.enabled = true;
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

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	@JsonIgnore
	public String getPasswordHash() {
		return passwordHash;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void replaceGroups(Set<Group> groups) {
		this.groups.clear();
		this.groups.addAll(groups);
	}
}
