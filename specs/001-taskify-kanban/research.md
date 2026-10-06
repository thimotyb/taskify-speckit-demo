# Research: Taskify Kanban Platform

All technical-context items were resolved from the user's stack choices and the constitution; there
were no open NEEDS CLARIFICATION items. Real-time updates and notifications are out of scope for this
phase and are not researched here.

## R1. H2 in file mode with Spring Data JPA

- **Decision**: Use H2 2.x (Spring Boot-managed version) in file mode, one database per service:
  `jdbc:h2:file:${TASKIFY_DATA_DIR}/<service>/<service>;DB_CLOSE_ON_EXIT=FALSE`. Hibernate uses its
  built-in H2 dialect. Flyway creates the schema and runs a versioned seed migration; because Flyway
  records applied migrations inside the database, the seed runs only on first start and restarts never
  re-seed, duplicate, or overwrite user changes (FR-013). Applied seed migrations are immutable.
- **Rationale**: H2 has first-class Spring Boot, Hibernate, and Flyway support. File mode gives restart
  durability (SC-005).
- **Hardening**: `spring.h2.console.enabled=false`; no `AUTO_SERVER` (it opens a TCP port); no `INIT` or
  `RUNSCRIPT` options built from external input; database user and password come from environment
  variables, never from source control; database files live on a volume mounted only into the owning
  service, which runs as a non-root user. In the Compose and production profiles files are encrypted at
  rest with `CIPHER=AES`, with the key from `TASKIFY_DB_FILE_KEY` (constitution security constraints).
- **Operational notes**: H2 file mode permits one owning process per file, so each service runs as a
  single instance. To reset to sample data, stop the stack and delete the volumes. Backup, export, and
  schema migration beyond Flyway are out of scope (spec Assumptions).
- **Alternatives considered**: SQLite (weaker Spring/Hibernate support); in-memory databases (data lost
  on restart, contradicts SC-005); PostgreSQL (durable and scalable, but adds a server for five users
  and was not requested).

## R2. Service decomposition

- **Decision**: `project-service` (users, projects) and `task-service` (tasks, comments), plus
  `gateway`.
- **Rationale**: Matches the entity ownership in the spec and the requested REST surface (projects and
  tasks). Users live with projects because both are small reference data. A notification service was
  dropped with the notifications requirement and can be added later without changing these services.
- **Alternatives considered**: separate user-service (extra deployable for five static records);
  separate comment service (too fine-grained); one service (violates the constitution).

## R3. Cross-service data and validation

- **Decision**: Services reference other services' data by id only; no foreign keys across services.
  task-service validates that `projectId` and user ids exist by calling project-service's internal
  endpoints (users cached for 60 s; projects checked on task creation). Failures to reach
  project-service fail closed (HTTP 503).
- **Rationale**: Constitution III forbids shared data stores and implicit trust.
- **Alternatives considered**: replicating the user list into each service (stale-data risk and
  duplicated seed data).

## R4. Keeping boards fresh without real-time updates

- **Decision**: The frontend fetches a board when opened, refetches when the window regains focus and
  after any change the user makes, and offers a manual refresh button. Card moves use an optimistic
  update that rolls back if the server rejects the move. No push channel, no polling.
- **Rationale**: The spec states other users see changes on their next refresh and the user confirmed
  real-time updates are not needed now. This is the simplest design that meets SC-003.
- **Alternatives considered**: periodic polling (extra load for no required benefit); Server-Sent Events
  or WebSocket (explicitly deferred; can be added later as a new endpoint without changing existing
  contracts).

## R5. Frontend stack

- **Decision**: React 18 + TypeScript + Vite; MUI v6 for UI; `@hello-pangea/dnd` for drag-and-drop;
  TanStack Query for server state.
- **Rationale**: `@hello-pangea/dnd` is purpose-built for Kanban lists, includes keyboard and screen
  reader support, and fits MUI Cards. TanStack Query gives caching, refetch-on-focus, and optimistic
  updates.
- **Alternatives considered**: dnd-kit (more flexible, more assembly for a simple board);
  Redux Toolkit (more boilerplate for server state).

## R6. Security design (no login)

- **Trust boundaries**: browser to gateway (untrusted); gateway to services (internal network);
  task-service to project-service (token-authenticated).
- **Decisions**:
  - Public endpoints require `X-User-Id` matching a seeded user; unknown or missing id returns 401.
    This is identification, not authentication, and is documented as such.
  - Internal endpoints (`/internal/**`) require `X-Service-Token`; compared in constant time;
    token injected via environment variable, never logged or committed. The gateway does not route
    `/internal/**`.
  - Bean Validation on every request DTO with allow-list limits; unknown JSON properties rejected;
    request body size limited; JPA parameterized queries only.
  - Output: React escapes by default, and `dangerouslySetInnerHTML` is banned by lint rule.
  - Gateway sets security headers (CSP, `X-Content-Type-Options`, `Referrer-Policy`) and a strict
    CORS allow-list.
  - Error responses use Problem Details without internals; security events (validation rejections,
    unknown user, bad service token) are logged without request bodies.
  - Basic per-user rate limiting at the gateway for write endpoints.
- **Abuse cases covered**: header spoofing (accepted risk this phase, documented), script injection in
  text fields, oversized payloads, forged internal calls, enumerating ids (UUIDs, 404 on unknown).

## R7. API style and versioning

- **Decision**: REST + JSON, URL-versioned (`/api/v1/...`), OpenAPI 3.1 contract per service kept in
  `specs/001-taskify-kanban/contracts/`, errors as RFC 9457 Problem Details.
- **Rationale**: Constitution III requires versioned contracts; Problem Details gives a uniform,
  safe error shape (no stack traces).
- **Alternatives considered**: GraphQL (not requested, more surface to secure and validate).

## R8. Testing strategy

- **Decision**: validation tests for every rule (valid, invalid, boundary, markup input); controller
  tests with MockMvc; WireMock contract tests for task-service's calls to project-service; Playwright
  end-to-end tests for each user story's independent test.
- **Rationale**: Constitution II requires tests per validation rule and III requires contract tests.

## R9. Documentation

- **Decision**: Javadoc on all public Java types and methods, TSDoc on exported TypeScript, springdoc
  OpenAPI generated per service and checked against the committed contracts, README per service and
  for the frontend; lint rules (Checkstyle `MissingJavadocMethod`, ESLint `jsdoc`) enforce it in CI.
- **Rationale**: Constitution IV requires full documentation updated with code.
