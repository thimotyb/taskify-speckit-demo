# Project Service

Owns the five predefined users and the projects (bounded context 1 of 2). Port **8081**.

## Endpoints (contract: `specs/001-taskify-kanban/contracts/project-service.openapi.yaml`)

| Method and path | Purpose | Who may call |
|---|---|---|
| `GET /api/v1/users` | The five predefined users (names and roles) | Anyone: the one public endpoint, needed to choose an identity |
| `GET /api/v1/projects` | List projects, oldest first | Identified user (`X-User-Id`) |
| `GET /api/v1/projects/{projectId}` | One project | Identified user |
| `GET /internal/projects/{projectId}` | Existence check for other services | Services (`X-Service-Token`) |
| `GET /internal/users/{userId}` | Existence check for other services | Services |

`X-User-Id` identifies the acting user and is **not authentication** (see the constitution's
Early-Phase Identification Exception). Unknown or missing users get 401. `/internal/**` is protected by
the shared service token and is never routed by the gateway. Errors are RFC 9457 problem responses with
no internals.

## Configuration

| Variable | Meaning |
|---|---|
| `TASKIFY_DATA_DIR` | Directory for the H2 database file (default `./data`) |
| `TASKIFY_DB_USER`, `TASKIFY_DB_PASSWORD` | Database credentials (never commit values) |
| `TASKIFY_DB_FILE_KEY` | AES key for the database file, used by the `compose` profile |
| `TASKIFY_SERVICE_TOKEN` | Shared secret for `/internal/**`; empty rejects every internal call |

Storage is H2 in file mode: data survives restarts. Flyway creates the schema and loads the sample data
(`docs/seed-data.md`) once, on the first start. The H2 console and TCP server are disabled. The `local`
profile enables Swagger UI.

## Run and test

```bash
mvn -pl services/project-service spring-boot:run     # from the repository root
mvn -pl services/project-service verify               # tests + Javadoc checkstyle gate
```
