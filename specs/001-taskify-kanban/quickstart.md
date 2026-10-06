# Quickstart: Taskify Kanban Platform

Validation guide for running the system and proving each user story. Entities and fields are in
[data-model.md](data-model.md); API shapes are in [contracts/](contracts/).

## Prerequisites

- JDK 21, Maven 3.9+
- Node.js 20 LTS and npm
- Docker with Compose (optional, runs everything together)
- Environment variable `TASKIFY_SERVICE_TOKEN` set to a random value of at least 32 characters
  (for example `export TASKIFY_SERVICE_TOKEN=$(openssl rand -hex 32)`). It is never committed.
- Environment variables `TASKIFY_DB_USER`, `TASKIFY_DB_PASSWORD`, and (for encrypted files)
  `TASKIFY_DB_FILE_KEY`, set to random values and never committed.
- `TASKIFY_DATA_DIR`, a writable directory for the H2 database files (local runs; Compose uses one
  named volume per service).

## Run

Option 1, Docker Compose (from the repository root):

```bash
docker compose up --build
```

Option 2, locally (each in its own terminal; service ports are fixed, see each service README):

```bash
mvn -pl services/project-service spring-boot:run
mvn -pl services/task-service spring-boot:run
mvn -pl services/gateway spring-boot:run
cd frontend && npm install && npm run dev
```

Open the frontend URL printed by the dev server (Compose: `http://localhost:3000`). The gateway
listens on `http://localhost:8080`. Sample data is loaded on the first start only. To reset to sample data, stop everything and delete
`TASKIFY_DATA_DIR` (or run `docker compose down -v`).

## Automated checks

```bash
mvn verify                   # unit, validation, controller, contract tests, Javadoc/Checkstyle
cd frontend && npm test      # Vitest component tests
cd frontend && npm run e2e   # Playwright, runs the scenarios below against a running stack
```

## Validation scenarios

| # | Story | Steps | Expected |
|---|---|---|---|
| 1 | US1 | Open the app; pick a user; open each sample project | Exactly 5 users (1 product manager, 4 engineers), 3 projects, 4 columns in order with seeded tasks; selected user survives a reload |
| 2 | US2 | Drag a task from To Do through each column to Done; reload | Card stays in Done; dragging backward also works |
| 3 | US2 multi-user | Open the same project in two browser windows as different users; drag a card in one; then refresh or refocus the other window | The other window shows the move after refresh or refocus (no live push is expected) |
| 4 | US3 | Create a task with a valid title; assign it; reassign it; clear the assignee | Appears in To Do with assignee shown, then updates, then shows unassigned |
| 5 | US3 validation | Submit an empty title and a 151-character title | Both rejected with a clear message; no task created |
| 6 | US3 edit | Edit a title and description; then edit the title to empty | Valid edit visible to all; invalid edit rejected and the old title kept |
| 7 | US4 | As user A comment on a task; switch to user B and open the task | Comment shows author and time, oldest first; the card's comment count increased |
| 8 | US5 | Create a project, then repeat with a duplicate name | New empty board with 4 columns; duplicate rejected |
| 9 | Security | Submit `<script>alert(1)</script>` as a title, comment, and project name | Stored and shown as plain text; nothing executes |
| 10 | Security | Call `/api/v1/projects` without `X-User-Id`, then with an unknown id (only `GET /api/v1/users` is public); call an `/internal/**` path directly on a service without the token | 401 in each case, with a safe error body; `/internal/**` is not reachable through the gateway |
| 11 | Resilience | Stop project-service; try to open a board, create a task, and drag a card | Each fails closed with a clear error (503). The dragged card returns to its original column and the message "The task cannot be moved right now. Please try again later." is shown. After project-service restarts, the same actions succeed |
| 12 | Restart | Create a project, a task, and a comment; restart every service (`docker compose restart`); reload | All three remain, assignments and column positions unchanged (SC-005) |
| 13 | First-start seeding | After scenario 12, count projects, tasks, and users | Counts are unchanged: 5 users, no duplicated sample projects or tasks (FR-013) |
| 14 | Hardening | Request the H2 console path on any service | Not available (404) |

## API documentation

Each service serves its OpenAPI document and Swagger UI at `/swagger-ui.html` in local development
(disabled in production profiles). The committed contracts in `contracts/` are the source of truth;
`mvn verify` fails if a service's generated document drifts from its contract.
