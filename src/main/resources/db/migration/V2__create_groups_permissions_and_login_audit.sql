CREATE TABLE groups (
    id UUID PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(255) NOT NULL,
    system_group BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_groups_name UNIQUE (name),
    CONSTRAINT ck_groups_name_format CHECK (name = UPPER(name))
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_permissions_name UNIQUE (name),
    CONSTRAINT ck_permissions_name_format CHECK (name = UPPER(name))
);

CREATE TABLE user_groups (
    user_id UUID NOT NULL,
    group_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_user_groups_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_groups_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE RESTRICT
);

CREATE TABLE group_permissions (
    group_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, permission_id),
    CONSTRAINT fk_group_permissions_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_group_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE RESTRICT
);

CREATE TABLE login_audit_events (
    id UUID PRIMARY KEY,
    user_id UUID NULL,
    identifier VARCHAR(255) NULL,
    event_type VARCHAR(40) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512) NULL,
    session_id_hash VARCHAR(64) NULL,
    success BOOLEAN NOT NULL,
    reason VARCHAR(255) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_login_audit_events_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_login_audit_events_created_at ON login_audit_events (created_at DESC);
CREATE INDEX idx_login_audit_events_user_id ON login_audit_events (user_id);
CREATE INDEX idx_login_audit_events_identifier ON login_audit_events (identifier);
CREATE INDEX idx_login_audit_events_ip_address ON login_audit_events (ip_address);

INSERT INTO groups (id, name, description, system_group) VALUES
    ('11111111-1111-1111-1111-111111111111', 'USER', 'Acesso ao app normal e ao próprio perfil.', TRUE),
    ('22222222-2222-2222-2222-222222222222', 'ADMIN', 'Acesso ao painel administrativo e gestão do sistema.', TRUE);

INSERT INTO permissions (id, name, description) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'APP_ACCESS', 'Acessar o app normal.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'PROFILE_READ', 'Ver o próprio perfil.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'ADMIN_PANEL_ACCESS', 'Acessar o painel admin.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'USER_CREATE', 'Criar usuários.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'ADMIN_CREATE', 'Criar administradores.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'USER_READ', 'Listar usuários e administradores.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 'USER_UPDATE', 'Ativar, desativar e alterar grupos de usuários.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'GROUP_READ', 'Listar grupos.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', 'GROUP_MANAGE', 'Gerenciar grupos.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 'AUDIT_READ', 'Ver auditoria de login.');

INSERT INTO group_permissions (group_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM permissions
WHERE name IN ('APP_ACCESS', 'PROFILE_READ');

INSERT INTO group_permissions (group_id, permission_id)
SELECT '22222222-2222-2222-2222-222222222222', id
FROM permissions
WHERE name IN (
    'APP_ACCESS',
    'PROFILE_READ',
    'ADMIN_PANEL_ACCESS',
    'USER_CREATE',
    'ADMIN_CREATE',
    'USER_READ',
    'USER_UPDATE',
    'GROUP_READ',
    'GROUP_MANAGE',
    'AUDIT_READ'
);

INSERT INTO user_groups (user_id, group_id)
SELECT id, '11111111-1111-1111-1111-111111111111'
FROM users
WHERE role IN ('USER', 'ADMIN');

INSERT INTO user_groups (user_id, group_id)
SELECT id, '22222222-2222-2222-2222-222222222222'
FROM users
WHERE role = 'ADMIN';

ALTER TABLE users DROP COLUMN role;
