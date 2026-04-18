# Verde Note Core

Spring Boot backend for Verde Note. This service is the source of truth for users, authentication, permissions, documents, comments, history, audit and persistence.

## Responsibilities

- HTTP session authentication with Spring Security.
- CSRF protection for browser state-changing requests.
- BCrypt password hashing.
- User, group and permission management.
- Admin dashboard APIs.
- Document CRUD.
- Document ACL with `OWNER`, `EDITOR` and `VIEWER`.
- Document sharing by user, email or group.
- Yjs update and snapshot persistence.
- Comment threads and comment records.
- Login and document audit data.
- Short-lived realtime tickets for `verdenote-node`.

## Architecture

Main packages:

- `auth`: registration, login helper DTOs, CSRF and current user endpoints.
- `security`: Spring Security config, session handling, remember-me and filters.
- `user`, `group`, `permission`: identity and RBAC model.
- `admin`: admin-only account, group and audit APIs.
- `document`: documents, ACL, versions, comments and document audit.
- `realtime`: ticket issuing and internal Node-to-Core endpoints.
- `audit`: login/action logging and frontend debug logging.
- `common`: API error handling and shared configuration.

The backend never returns JPA entities directly from public controllers. Controllers return DTOs and services enforce business rules.

## Database

PostgreSQL is managed by Flyway migrations in `src/main/resources/db/migration`.

Core tables include:

- `users`
- `groups`
- `permissions`
- `user_groups`
- `group_permissions`
- `login_audit_events`
- `documents`
- `document_acl`
- `document_yjs_updates`
- `document_versions`
- `comment_threads`
- `document_comments`
- `document_audit_events`
- `realtime_tickets`

## Security Notes

- Browser login uses HTTP sessions, not JWT.
- CSRF remains enabled for browser endpoints.
- Passwords are stored only as BCrypt hashes.
- Realtime internal endpoints require `X-VerdeNote-Realtime-Secret`.
- Realtime ticket values are stored hashed.
- Cookies should be `Secure=true` in production.
- Real `.env` files must never be committed.

## Local Run

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## Tests

```bash
./gradlew test
```

## Docker

Build:

```bash
docker build -t verdenote-core .
```

Run via root compose:

```bash
docker compose up --build
```

Run this module alone, with its own PostgreSQL:

```bash
docker compose up --build
```

This module compose exposes:

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

## Environment

Use `.env.example` as a template. Keep `.env` local only. Production must provide strong values for:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `VERDENOTE_REMEMBER_ME_KEY`
- `VERDENOTE_REALTIME_SERVICE_SECRET`
- `VERDENOTE_CORS_ALLOWED_ORIGINS`
- `VERDENOTE_COOKIE_SECURE=true`
