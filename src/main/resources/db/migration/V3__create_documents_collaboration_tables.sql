CREATE TABLE documents (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    current_snapshot BYTEA NULL,
    current_version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documents_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_documents_owner_id ON documents (owner_id);
CREATE INDEX idx_documents_updated_at ON documents (updated_at DESC);

CREATE TABLE document_acl (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    user_id UUID NULL,
    group_id UUID NULL,
    role VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_acl_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_acl_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_acl_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_acl_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_document_acl_role CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER')),
    CONSTRAINT ck_document_acl_subject CHECK (
        (user_id IS NOT NULL AND group_id IS NULL)
        OR (user_id IS NULL AND group_id IS NOT NULL)
    ),
    CONSTRAINT uk_document_acl_user UNIQUE (document_id, user_id),
    CONSTRAINT uk_document_acl_group UNIQUE (document_id, group_id)
);

CREATE INDEX idx_document_acl_document_id ON document_acl (document_id);
CREATE INDEX idx_document_acl_user_id ON document_acl (user_id);
CREATE INDEX idx_document_acl_group_id ON document_acl (group_id);

CREATE TABLE document_yjs_updates (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    version BIGINT NOT NULL,
    update_payload BYTEA NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_yjs_updates_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_yjs_updates_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uk_document_yjs_updates_version UNIQUE (document_id, version)
);

CREATE INDEX idx_document_yjs_updates_document_version ON document_yjs_updates (document_id, version);

CREATE TABLE document_versions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    version BIGINT NOT NULL,
    snapshot_payload BYTEA NOT NULL,
    created_by UUID NOT NULL,
    reason VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_versions_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_versions_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uk_document_versions_version UNIQUE (document_id, version)
);

CREATE INDEX idx_document_versions_document_version ON document_versions (document_id, version DESC);

CREATE TABLE comment_threads (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    anchor_payload TEXT NOT NULL,
    selected_text TEXT NULL,
    status VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_threads_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_threads_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_comment_threads_status CHECK (status IN ('OPEN', 'RESOLVED', 'ORPHANED'))
);

CREATE INDEX idx_comment_threads_document_id ON comment_threads (document_id);

CREATE TABLE document_comments (
    id UUID PRIMARY KEY,
    thread_id UUID NOT NULL,
    body TEXT NOT NULL,
    created_by UUID NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_comments_thread FOREIGN KEY (thread_id) REFERENCES comment_threads (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_comments_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_document_comments_thread_id ON document_comments (thread_id);

CREATE TABLE document_audit_events (
    id UUID PRIMARY KEY,
    document_id UUID NULL,
    actor_id UUID NULL,
    event_type VARCHAR(60) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512) NULL,
    metadata TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_audit_events_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE SET NULL,
    CONSTRAINT fk_document_audit_events_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_document_audit_events_document_id ON document_audit_events (document_id);
CREATE INDEX idx_document_audit_events_actor_id ON document_audit_events (actor_id);
CREATE INDEX idx_document_audit_events_created_at ON document_audit_events (created_at DESC);

CREATE TABLE realtime_tickets (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    user_id UUID NOT NULL,
    ticket_hash VARCHAR(64) NOT NULL,
    permissions TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_realtime_tickets_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_realtime_tickets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_realtime_tickets_hash UNIQUE (ticket_hash)
);

CREATE INDEX idx_realtime_tickets_hash ON realtime_tickets (ticket_hash);
CREATE INDEX idx_realtime_tickets_expires_at ON realtime_tickets (expires_at);
