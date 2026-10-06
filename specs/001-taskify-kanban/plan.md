# Implementation Plan: Taskify Kanban Platform

**Branch**: `001-taskify-kanban` | **Date**: 2026-10-06 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-taskify-kanban/spec.md`

**Note**: Real-time updates and notifications are out of scope for this phase (user decision, and
consistent with the spec's Assumptions). Other users see changes when they refresh or refocus the page.

## Summary

Taskify is a Kanban web app for a five-person team (no login). Users pick one of five predefined
identities, open one of three seeded projects, and create, edit, assign, comment on, and drag
tasks across To Do, In Progress, In Review, and Done.

The technical approach follows the user-provided stack and the project constitution:

- **Backend**: two Spring Boot 3 / Java 21 microservices behind a Spring Cloud Gateway.
  - `project-service` owns users and projects.
  - `task-service` owns tasks and comments.
- **Persistence**: each service owns a private H2 database in file mode on its own persistent volume
  (constitution III), so data survives restarts. Flyway seeds sample data on first start only.
- **Freshness without real-time**: the board is fetched on load and refetched on window focus and
  after the user's own changes. There is no push channel, no polling, and no notifications.
- **Frontend**: React 18 + TypeScript + Material UI, with a drag-and-drop board.
- **Validation and security**: all input is validated server-side at the first service that
  receives it (constitution I, II). Every public type and endpoint is documented (constitution IV).

## Technical Context

**Language/Version**: Java 21 (LTS) for services; TypeScript 5.x on Node 20 LTS for the frontend

**Primary Dependencies**:
- Backend: Spring Boot 3.3.x (Web, Validation, Data JPA, Security, Actuator), Spring Cloud Gateway,
  Flyway, H2 2.x (`com.h2database:h2`, runtime scope, version managed by Spring Boot), springdoc-openapi
- Frontend: React 18, Vite, MUI (Material UI) v6, `@hello-pangea/dnd`, TanStack Query, React Router

**Storage**: H2 in file mode, one database file per service on a dedicated volume mounted at
`/data/<service>`. Each service runs as a single instance (H2 file mode allows one owning process).
Data survives restarts (FR-013, SC-005). The H2 web console and TCP server are disabled.

**Testing**: JUnit 5, Spring Boot Test + MockMvc, WireMock (contract tests for task-service's calls to
project-service); Vitest + React Testing Library; Playwright for end-to-end

**Target Platform**: Linux containers (Docker Compose for local run); desktop browsers (evergreen
Chrome, Firefox, Safari, Edge)

**Project Type**: Web application, microservices backend plus single-page frontend

**Performance Goals**: Board of 100 tasks loads in under 3 s (SC-007); a card move completes in under
5 s from drop to saved (SC-003)

**Constraints**:
- No login in this phase: the acting user is sent as an `X-User-Id` header and is not a security
  boundary (spec Assumptions).
- Service-to-service calls are authenticated with a shared service token from environment
  configuration; no secrets are committed.
- Services have no shared database and no shared code library.
- Concurrent edits: last write wins (spec edge case).

**Scale/Scope**: 5 users, a handful of projects, hundreds of tasks; about 5 screens/views (user
picker, project list, board, task detail dialog, create/edit dialogs)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Pre-research | Post-design |
|---|---|---|---|
| I. Security-First | Trust boundaries and abuse cases identified; least privilege; fail closed; no secrets in repo/logs; authN/Z on every endpoint including internal calls | PASS (see note 1) | PASS: internal endpoints require the service token; public endpoints require a valid known user id; abuse cases listed in research.md R6 |
| II. Validate All User Inputs | Server-side allow-list schema validation at first receiving service; encoded output; tests per rule | PASS | PASS: Bean Validation DTOs with explicit limits (data-model.md), ProblemDetail errors, parameterized queries only, React escapes output, validation tests required in tasks |
| III. Microservices | Bounded context per service, own data store, versioned contracts, contract tests, no implicit trust | PASS | PASS: 2 services plus gateway, `/v1`-versioned OpenAPI contracts in `contracts/`, WireMock contract tests, internal calls re-validated |
| IV. Full Documentation | Doc comments on all public code, README per service, docs updated with code | PASS | PASS: Javadoc/TSDoc required, springdoc OpenAPI served per service, README per service listed in structure |

Note 1: the spec defers authentication (no login). This is covered by the **Early-Phase
Identification Exception** in constitution v1.1.0 Principle I, not by reinterpreting the rule. Its
conditions are met as follows:

| Condition | How the plan meets it |
|---|---|
| Spec defers authentication, records risk acceptance | spec.md Assumptions and Security Considerations |
| Server validates identity on every request, fails closed | `X-User-Id` must match a seeded user, otherwise 401 (FR-017; research R6) |
| Service-to-service calls stay authenticated | `X-Service-Token` on `/internal/**`; `/internal/**` not routed by the gateway |
| Only sample or non-sensitive data; not exposed to untrusted users | Seed data only; local and Compose use on a trusted network |
| Identity handling concentrated in one place | `UserIdentityFilter` per service and the gateway are the only places that read `X-User-Id` |
| Plan tracks replacement and sunset | See Deferred Work below |

## Project Structure

### Documentation (this feature)

```text
specs/001-taskify-kanban/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── project-service.openapi.yaml
│   └── task-service.openapi.yaml
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
pom.xml                          # Maven parent (Java 21, dependency management)
docker-compose.yml               # gateway, 2 services, frontend, one data volume per service

services/
├── gateway/
│   ├── README.md
│   ├── pom.xml
│   └── src/main/java/.../gateway/        # routes, CORS, security headers, request size limits
├── project-service/
│   ├── README.md
│   ├── pom.xml
│   ├── src/main/java/.../project/
│   │   ├── api/                          # controllers, request/response DTOs
│   │   ├── domain/                       # User, Project entities, repositories
│   │   ├── service/                      # business logic
│   │   └── config/                       # H2 datasource, security, error handling
│   ├── src/main/resources/db/migration/  # Flyway schema + one-time seed (5 users, 3 projects)
│   └── src/test/java/.../project/        # unit, controller, validation tests
└── task-service/
    ├── README.md
    ├── pom.xml
    ├── src/main/java/.../task/
    │   ├── api/
    │   ├── domain/                       # Task, Comment, TaskStatus
    │   ├── service/
    │   ├── client/                       # project-service client
    │   └── config/
    ├── src/main/resources/db/migration/  # schema + one-time seed tasks and comments
    └── src/test/java/.../task/           # includes contract tests (WireMock)

frontend/
├── README.md
├── package.json
├── vite.config.ts
├── src/
│   ├── components/                       # Board, Column, TaskCard, dialogs
│   ├── pages/                            # UserPicker, ProjectList, ProjectBoard
│   ├── services/                         # typed API clients
│   └── state/                            # acting-user context, query cache wiring
└── tests/
    ├── unit/
    └── e2e/                              # Playwright
```

**Structure Decision**: Web application with a microservices backend (constitution III). Two
services map to the two bounded contexts in the spec: projects (with the fixed user directory) and
tasks (with comments). A gateway gives the browser a single origin and a single place for CORS,
headers, and future authentication. No shared library module is used so services stay independently
deployable. A notification service can be added later as a third service without changing these two.

## Deferred Work

- **Replace identification with authentication.** Required before the first release to real users or
  any untrusted network, and the exception ends then at the latest (constitution Principle I).
  Approach: add authentication at the gateway (for example, token-based login issuing a signed user
  token), have `UserIdentityFilter` in each service read the verified identity instead of the raw
  `X-User-Id` header, and add authorization rules per role if the spec introduces them. Tracked as the
  first item of the next feature after this one; it must not ship to real users before it is done.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Three deployables (gateway + 2 services) for 5 users | Constitution III mandates microservices; two services match the two data-owning contexts | A single service violates the constitution |
| Each service limited to one running instance | H2 file mode allows only one process per database file | A networked database server would allow scaling out but is unnecessary for 5 users; revisit if load grows |
