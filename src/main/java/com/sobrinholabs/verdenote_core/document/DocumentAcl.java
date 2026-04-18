package com.sobrinholabs.verdenote_core.document;

import com.sobrinholabs.verdenote_core.group.Group;
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
@Table(name = "document_acl")
public class DocumentAcl {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id")
	private Document document;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DocumentRole role;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected DocumentAcl() {
	}

	private DocumentAcl(Document document, User user, Group group, DocumentRole role, User createdBy) {
		this.id = UUID.randomUUID();
		this.document = document;
		this.user = user;
		this.group = group;
		this.role = role;
		this.createdBy = createdBy;
	}

	public static DocumentAcl forUser(Document document, User user, DocumentRole role, User createdBy) {
		return new DocumentAcl(document, user, null, role, createdBy);
	}

	public static DocumentAcl forGroup(Document document, Group group, DocumentRole role, User createdBy) {
		return new DocumentAcl(document, null, group, role, createdBy);
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

	public Group getGroup() {
		return group;
	}

	public DocumentRole getRole() {
		return role;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void updateRole(DocumentRole role) {
		this.role = role;
	}
}
