# Task Service

Owns tasks and their comments (bounded context 2 of 2). Port **8082**.

## Endpoints (contract: `specs/001-taskify-kanban/contracts/task-service.openapi.yaml`)

| Method and path | Purpose |
|---|---|
| `GET /api/v1/projects/{projectId}/tasks` | A project's tasks with comment counts, oldest first |

More endpoints (create, edit, move, comment) arrive with later user stories. Every endpoint requires an
`X-User-Id` that project-service confirms as a predefined user (identification, not authentication).

## Fail closed

Every request verifies the acting user, and the project when one is named, with project-service. Nothing
is cached. If project-service cannot be reached the response is **503** and nothing changes.

## Configuration

| Variable | Meaning |
|---|---|
| `TASKIFY_DATA_DIR` | Directory for the H2 database file (default `./data`) |
| `TASKIFY_DB_USER`, `TASKIFY_DB_PASSWORD` | Database credentials (never commit values) |
| `TASKIFY_DB_FILE_KEY` | AES key for the database file, used by the `compose` profile |
| `TASKIFY_SERVICE_TOKEN` | Shared secret sent to project-service's `/internal/**` |
| `TASKIFY_PROJECT_SERVICE_URL` | project-service base URL (default `http://localhost:8081`) |

Storage is H2 in file mode; sample tasks and comments (`docs/seed-data.md`) load once on the first start.

## Run and test

```bash
mvn -pl services/task-service spring-boot:run     # from the repository root
mvn -pl services/task-service verify               # tests (incl. WireMock contract test) + Javadoc gate
```
