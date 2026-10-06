# Tasks: Taskify Kanban Platform

**Input**: Design documents from `/specs/001-taskify-kanban/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/ (all present)

**Tests**: Included. The spec does not ask for TDD, but the constitution requires them: Principle II
(automated tests for every validation rule: valid, invalid, boundary) and Principle III (contract tests
for every interface). Write each story's tests first and confirm they fail before implementing.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested alone.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story the task belongs to (US1 to US5); Setup, Foundational, and Polish have none
- Every task names exact file paths

## Path Conventions

Paths follow plan.md. Java base packages: `com.taskify.project`, `com.taskify.task`,
`com.taskify.gateway`, so `<svc>/src/main/java/com/taskify/<name>/...`. Abbreviations used below:

- `PS` = `services/project-service`, `TS` = `services/task-service`, `GW` = `services/gateway`
- `FE` = `frontend`
- Java source: `PS/src/main/java/com/taskify/project`, tests: `PS/src/test/java/com/taskify/project`
  (same pattern for `TS` with `task` and `GW` with `gateway`)

**Documentation rule (constitution IV)**: every public Java type and method gets Javadoc, every exported
TypeScript symbol gets TSDoc, and each task that adds code also updates the relevant README.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Repository, build, and tooling skeleton

- [ ] T001 Create the directory skeleton from plan.md: `services/gateway/`, `services/project-service/`, `services/task-service/`, `frontend/`, `docs/`, `config/`
- [ ] T002 Create the Maven parent `pom.xml` at the repo root: Java 21, Spring Boot 3.3.x BOM, modules `services/gateway`, `services/project-service`, `services/task-service`, Checkstyle plugin bound to `verify`, Surefire/Failsafe
- [ ] T003 [P] Create `config/checkstyle.xml` enforcing `MissingJavadocType` and `MissingJavadocMethod` on public APIs, and `JavadocMethod` tag checks
- [ ] T004 [P] Create `PS/pom.xml` with dependencies: spring-boot-starter-web, -validation, -data-jpa, -security, -actuator, flyway-core, `com.h2database:h2` (runtime), springdoc-openapi-starter-webmvc-api, and test dependencies (spring-boot-starter-test, spring-security-test)
- [ ] T005 [P] Create `TS/pom.xml` with the same dependencies as the project service plus WireMock (test scope) for contract tests
- [ ] T006 [P] Create `GW/pom.xml` with spring-cloud-starter-gateway, spring-boot-starter-security, spring-boot-starter-actuator, and test dependencies
- [ ] T007 [P] Initialize `FE/package.json`, `FE/vite.config.ts`, `FE/tsconfig.json` with React 18, TypeScript 5, MUI v6, `@hello-pangea/dnd`, `@tanstack/react-query`, `react-router-dom`, and dev dependencies Vitest, React Testing Library, Playwright
- [ ] T008 [P] Configure `FE/eslint.config.js` with the `jsdoc` plugin (require TSDoc on exported symbols) and a `no-restricted-syntax` rule banning `dangerouslySetInnerHTML`; add `FE/.prettierrc`
- [ ] T009 [P] Create root `.gitignore` (target/, node_modules/, data/, `.env`) and `.env.example` listing, without values, `TASKIFY_SERVICE_TOKEN`, `TASKIFY_DB_USER`, `TASKIFY_DB_PASSWORD`, `TASKIFY_DB_FILE_KEY`, `TASKIFY_DATA_DIR`
- [ ] T010 [P] Create non-root multi-stage Dockerfiles: `PS/Dockerfile`, `TS/Dockerfile`, `GW/Dockerfile`, `FE/Dockerfile`
- [ ] T011 Create `docker-compose.yml`: gateway, project-service, task-service, frontend; one named data volume per service mounted at `/data/<service>` and only into that service; secrets passed via environment variables from `.env`; internal ports not published except gateway (8080) and frontend (3000)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Persistence, security, error handling, seeding, and frontend shell shared by every story

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Persistence and seed data

- [ ] T012 Document fixed seed data in `docs/seed-data.md`: users `00000000-0000-0000-0000-000000000001` to `...005` (Priya Shah, PRODUCT_MANAGER; Marco Rossi, Lena Fischer, Tom Nguyen, Sara Okafor, ENGINEER); projects `10000000-0000-0000-0000-000000000001` to `...003` ("Website Redesign", "Mobile App Launch", "Internal Tooling"); 8 tasks per project (2 in each status) with ids `20000000-0000-0000-0000-0000000000NN`, several assigned, 5 comments in total
- [ ] T013 [P] Create `PS/src/main/resources/application.yml`: datasource `jdbc:h2:file:${TASKIFY_DATA_DIR}/project-service/project-service;DB_CLOSE_ON_EXIT=FALSE` (plus `CIPHER=AES` in the `compose` and `prod` profiles with file key from `TASKIFY_DB_FILE_KEY`), user and password from environment, `spring.h2.console.enabled=false`, no `AUTO_SERVER`, Flyway enabled, `spring.jackson.deserialization.fail-on-unknown-properties=true`, max request size limit, server port 8081
- [ ] T014 [P] Create `TS/src/main/resources/application.yml` with the same settings for task-service (file `${TASKIFY_DATA_DIR}/task-service/task-service`), port 8082, and `taskify.project-service.base-url` and `taskify.service-token` properties
- [ ] T015 [P] Create Flyway migration `PS/src/main/resources/db/migration/V1__schema.sql`: table `users` (id UUID PK, name VARCHAR(100) NOT NULL, role with CHECK in ('PRODUCT_MANAGER','ENGINEER')) and `projects` (id UUID PK, name VARCHAR(100) NOT NULL, description VARCHAR(1000), created_by UUID NOT NULL, created_at TIMESTAMP NOT NULL) with a unique index on `LOWER(name)`
- [ ] T016 Create Flyway seed migration `PS/src/main/resources/db/migration/V2__seed.sql` inserting the 5 users and 3 projects from `docs/seed-data.md`; the seed runs once because Flyway records applied versions, and applied migrations are never edited
- [ ] T017 [P] Create Flyway migration `TS/src/main/resources/db/migration/V1__schema.sql`: table `tasks` (id UUID PK, project_id UUID NOT NULL, title VARCHAR(150) NOT NULL, description VARCHAR(2000), status CHECK in ('TODO','IN_PROGRESS','IN_REVIEW','DONE') DEFAULT 'TODO', assignee_id UUID NULL, created_by, created_at, updated_at, updated_by) and `comments` (id UUID PK, task_id UUID NOT NULL FK to tasks, author_id UUID NOT NULL, text VARCHAR(1000) NOT NULL, created_at TIMESTAMP NOT NULL); index on `tasks(project_id, status, created_at)` and `comments(task_id, created_at)`
- [ ] T018 Create Flyway seed migration `TS/src/main/resources/db/migration/V2__seed.sql` inserting the sample tasks and comments from `docs/seed-data.md`

### Security, errors, and logging (both services)

- [ ] T019 [P] Implement `ProblemDetailsAdvice` in `PS/src/main/java/com/taskify/project/config/ProblemDetailsAdvice.java`: RFC 9457 responses for validation errors (400 with `errors[{field,message}]`), malformed/unknown-property JSON (400), not found (404), conflict (409), unauthorized (401), generic failure (500 with no stack trace or internals); Javadoc on every handler
- [ ] T020 [P] Implement `ProblemDetailsAdvice` in `TS/src/main/java/com/taskify/task/config/ProblemDetailsAdvice.java` with the same behavior plus 503 for an unreachable project-service
- [ ] T021 Implement `ServiceTokenFilter` in `PS/src/main/java/com/taskify/project/config/ServiceTokenFilter.java` protecting `/internal/**`: constant-time comparison (`MessageDigest.isEqual`) of `X-Service-Token` against `taskify.service-token`; 401 on missing or wrong token; never log the token
- [ ] T022 Implement `UserIdentityFilter` in `PS/src/main/java/com/taskify/project/config/UserIdentityFilter.java` for `/api/**`: require `X-User-Id` as a UUID matching a seeded user, otherwise 401 Problem Details; place the acting user id in a request-scoped holder (`ActingUser.java`)
- [ ] T023 Implement `SecurityConfig` in `PS/src/main/java/com/taskify/project/config/SecurityConfig.java`: stateless, CSRF disabled for the header-based API, security headers, filters registered in order, springdoc paths enabled only in the `local` profile
- [ ] T024 Implement `ProjectServiceClient` in `TS/src/main/java/com/taskify/task/client/ProjectServiceClient.java`: calls `GET /internal/projects/{id}` and `GET /internal/users/{id}` with `X-Service-Token`, 2 s connect and read timeouts, users cached 60 s, maps unreachable/5xx to a fail-closed `UpstreamUnavailableException` (HTTP 503) and 404 to not-found; Javadoc
- [ ] T025 Implement `UserIdentityFilter` and `ActingUser` in `TS/src/main/java/com/taskify/task/config/` for `/api/**`: require `X-User-Id` UUID, verify it via `ProjectServiceClient`, otherwise 401
- [ ] T026 Implement `SecurityConfig` in `TS/src/main/java/com/taskify/task/config/SecurityConfig.java` mirroring the project service (stateless, headers, no console)
- [ ] T027 [P] Configure logging in `PS/src/main/resources/logback-spring.xml` and `TS/src/main/resources/logback-spring.xml`: structured fields, never log request bodies or the service token; log validation rejections, unknown-user attempts, and bad service tokens at WARN

### Gateway

- [ ] T028 Implement gateway routes in `GW/src/main/java/com/taskify/gateway/config/RouteConfig.java`: `/api/v1/users/**` and `/api/v1/projects` (exact and `/{id}`) to project-service; `/api/v1/projects/{id}/tasks` and `/api/v1/tasks/**` to task-service; explicit deny (404) for `/internal/**`
- [ ] T029 [P] Implement `SecurityHeadersFilter` in `GW/src/main/java/com/taskify/gateway/config/SecurityHeadersFilter.java` adding CSP, `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, and `CorsConfig.java` with a strict allow-list from configuration
- [ ] T030 [P] Implement `WriteRateLimitFilter` in `GW/src/main/java/com/taskify/gateway/config/WriteRateLimitFilter.java`: per-`X-User-Id` limit on POST/PUT/PATCH (default 60 requests per minute, configurable), 429 Problem Details when exceeded
- [ ] T031 [P] Create `GW/src/main/resources/application.yml` (port 8080, upstream URLs, request size limit, allowed origin) and `GW/README.md`

### Frontend shell

- [ ] T032 [P] Implement the API client in `FE/src/services/apiClient.ts`: base `/api/v1`, injects `X-User-Id` from the acting-user context, parses Problem Details into a typed `ApiError` with safe field messages; TSDoc
- [ ] T033 [P] Implement `FE/src/state/ActingUserContext.tsx`: holds the selected user id, persists it in `localStorage` (wrapped in try/catch), exposes `useActingUser`
- [ ] T034 [P] Create `FE/src/components/StateViews.tsx` (loading, empty, error, not-found views) and `FE/src/theme.ts` (MUI theme)
- [ ] T035 Create `FE/src/main.tsx` and `FE/src/App.tsx` with `QueryClientProvider` (refetch on window focus enabled, no polling), router routes `/`, `/projects`, `/projects/:projectId`, and a guard that redirects to the user picker when no user is selected

**Checkpoint**: Foundation ready; user stories can now begin

---

## Phase 3: User Story 1 - Choose a user and view a project's Kanban board (Priority: P1) 🎯 MVP

**Goal**: Pick one of five predefined users, see three sample projects, open a project to see the four-column board with seeded tasks.

**Independent Test**: Open the app, select any user, open each sample project, confirm the four columns show the seeded tasks in their columns, and that the selection survives a reload.

### Tests for User Story 1 ⚠️ (write first, confirm they fail)

- [ ] T036 [P] [US1] Controller tests for `GET /api/v1/users`, `GET /api/v1/projects`, `GET /api/v1/projects/{projectId}` (200, 401 without or with unknown `X-User-Id`, 404 unknown project) in `PS/src/test/java/com/taskify/project/api/ReadEndpointsTest.java`, asserting exactly 5 users (1 PRODUCT_MANAGER, 4 ENGINEER) and 3 seeded projects
- [ ] T037 [P] [US1] Controller tests for the internal endpoints `GET /internal/projects/{projectId}` and `GET /internal/users/{userId}` (200, 404, 401 without or with wrong `X-Service-Token`) in `PS/src/test/java/com/taskify/project/api/InternalEndpointsTest.java`
- [ ] T038 [P] [US1] Controller test for `GET /api/v1/projects/{projectId}/tasks` (200 with `commentCount`, ordered by `createdAt` ascending within status, 404 unknown project, 401 unknown user) in `TS/src/test/java/com/taskify/task/api/ListTasksTest.java`
- [ ] T039 [P] [US1] WireMock contract test for `ProjectServiceClient` (project exists, project 404, user exists, upstream 500/timeout fails closed with 503, token header sent) in `TS/src/test/java/com/taskify/task/client/ProjectServiceClientContractTest.java`
- [ ] T040 [P] [US1] Component tests for `UserPicker`, `ProjectList`, `Board` rendering four columns in order with empty-state message in `FE/tests/unit/board.test.tsx`
- [ ] T041 [P] [US1] Playwright scenario for quickstart scenario 1 (5 users, 3 projects, four columns, selection survives reload) in `FE/tests/e2e/us1-view-board.spec.ts`

### Implementation for User Story 1

- [ ] T042 [P] [US1] Create `User` entity (role enum `PRODUCT_MANAGER`/`ENGINEER`; name 1-100 chars) and `UserRepository` in `PS/src/main/java/com/taskify/project/domain/`
- [ ] T043 [P] [US1] Create `Project` entity (name required, 1-100 chars after trim, unique ignoring case; description optional up to 1,000 chars; createdBy, createdAt) and `ProjectRepository` in `PS/src/main/java/com/taskify/project/domain/`
- [ ] T044 [US1] Implement `UserService` and `ProjectService` (list, get, not-found handling) in `PS/src/main/java/com/taskify/project/service/`
- [ ] T045 [US1] Implement `UserController`, `ProjectController` (GET endpoints), `InternalController` (`/internal/projects/{id}`, `/internal/users/{id}`), and DTOs `UserResponse`, `ProjectResponse` in `PS/src/main/java/com/taskify/project/api/` per `contracts/project-service.openapi.yaml`
- [ ] T046 [P] [US1] Create `TaskStatus` enum (`TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`), `Task` entity (title required 1-150 chars after trim; description optional up to 2,000 chars; status default `TODO`; assigneeId optional, at most one; createdBy; createdAt; updatedAt; updatedBy) and `TaskRepository` in `TS/src/main/java/com/taskify/task/domain/`
- [ ] T047 [P] [US1] Create `Comment` entity (text required 1-1,000 chars after trim; authorId; createdAt) and `CommentRepository` with a count-by-task query in `TS/src/main/java/com/taskify/task/domain/`
- [ ] T048 [US1] Implement `TaskService.listByProject` (verifies the project via `ProjectServiceClient`, returns tasks with comment counts ordered by `createdAt`) in `TS/src/main/java/com/taskify/task/service/TaskService.java`
- [ ] T049 [US1] Implement `TaskController` `GET /api/v1/projects/{projectId}/tasks` and `TaskResponse` DTO in `TS/src/main/java/com/taskify/task/api/` per `contracts/task-service.openapi.yaml`
- [ ] T050 [P] [US1] Create typed API functions and models for users, projects, and tasks in `FE/src/services/usersApi.ts`, `FE/src/services/projectsApi.ts`, `FE/src/services/tasksApi.ts`, and types in `FE/src/services/types.ts`
- [ ] T051 [P] [US1] Implement `FE/src/pages/UserPicker.tsx` (five users with role labels, no credentials, selection stored in context and survives reload)
- [ ] T052 [P] [US1] Implement `FE/src/pages/ProjectList.tsx` (list of projects, link to board, user switcher in the app bar)
- [ ] T053 [US1] Implement `FE/src/components/Column.tsx`, `FE/src/components/TaskCard.tsx` (title, assignee name, comment count per FR-015; text rendered as plain text), and `FE/src/pages/ProjectBoard.tsx` (four columns in order, empty-state message, scrollable columns, manual refresh button, not-found view)
- [ ] T054 [US1] Update `PS/README.md` and `TS/README.md` and `FE/README.md` with purpose, configuration, run and test instructions, and the endpoints delivered in this story

**Checkpoint**: User Story 1 is fully functional and testable on its own (MVP)

---

## Phase 4: User Story 2 - Move tasks across Kanban columns (Priority: P1)

**Goal**: Any user moves any task to any column; the new status is saved and visible to everyone on refresh.

**Independent Test**: On a sample project drag a task from To Do through each column to Done, reload, and confirm it stays in Done.

### Tests for User Story 2 ⚠️

- [ ] T055 [P] [US2] Controller and validation tests for `PUT /api/v1/tasks/{taskId}/status` in `TS/src/test/java/com/taskify/task/api/SetStatusTest.java`: every status to every other status (forward and backward) succeeds; same-status move returns 200 and changes nothing; invalid status value, missing body, unknown extra property each return 400; unknown task returns 404; unknown user returns 401; `updatedAt` and `updatedBy` change only on a real move
- [ ] T056 [P] [US2] Component test for drag-and-drop behavior in `FE/tests/unit/dnd.test.tsx`: optimistic move, rollback with a visible error when the server rejects, keyboard move
- [ ] T057 [P] [US2] Playwright scenario for quickstart scenarios 2 and 3 (drag through all columns and back, reload keeps position; second window shows the move after refocus) in `FE/tests/e2e/us2-move-task.spec.ts`

### Implementation for User Story 2

- [ ] T058 [US2] Add `SetStatusRequest` DTO (`status` required, enum only, unknown properties rejected) in `TS/src/main/java/com/taskify/task/api/SetStatusRequest.java`
- [ ] T059 [US2] Implement `TaskService.setStatus` (any transition allowed, no-op when unchanged, sets `updatedAt`/`updatedBy` from `ActingUser`) in `TS/src/main/java/com/taskify/task/service/TaskService.java`
- [ ] T060 [US2] Implement `PUT /api/v1/tasks/{taskId}/status` in `TS/src/main/java/com/taskify/task/api/TaskController.java`
- [ ] T061 [US2] Add the `setTaskStatus` call to `FE/src/services/tasksApi.ts` and an optimistic `useMoveTask` mutation hook (rollback and error message on failure, board refetch on settle) in `FE/src/services/useMoveTask.ts`
- [ ] T062 [US2] Integrate `@hello-pangea/dnd` into `FE/src/pages/ProjectBoard.tsx`, `FE/src/components/Column.tsx`, and `FE/src/components/TaskCard.tsx` (drag between any columns, keyboard and screen-reader support, same-column drop is a no-op)
- [ ] T063 [US2] Update `TS/README.md` and `FE/README.md` with the status endpoint and drag-and-drop behavior

**Checkpoint**: User Stories 1 and 2 both work independently

---

## Phase 5: User Story 3 - Create, edit, and assign tasks (Priority: P2)

**Goal**: Create tasks (start in To Do), edit title and description, and assign, reassign, or clear the single assignee.

**Independent Test**: Create a task, assign it to an engineer, reassign it, clear the assignee, edit its title, and confirm invalid input is rejected.

### Tests for User Story 3 ⚠️

- [ ] T064 [P] [US3] Validation and controller tests for `POST /api/v1/projects/{projectId}/tasks` in `TS/src/test/java/com/taskify/task/api/CreateTaskTest.java`: valid; title empty, whitespace only, 150 chars (accepted), 151 chars (rejected); description 2,000 (accepted) and 2,001 (rejected); unknown `assigneeId` rejected; unknown project 404; unknown extra property 400; markup in title stored and returned verbatim as text; new task has status `TODO`; project-service down returns 503
- [ ] T065 [P] [US3] Validation and controller tests for `PATCH /api/v1/tasks/{taskId}` in `TS/src/test/java/com/taskify/task/api/UpdateTaskTest.java`: partial updates; `assigneeId: null` clears; absent `assigneeId` leaves it unchanged; empty body 400; empty or 151-char title rejected and task unchanged; description set to null; unknown task 404; second assignee impossible by schema
- [ ] T066 [P] [US3] Component tests for `CreateTaskDialog` and `EditTaskDialog` field validation messages in `FE/tests/unit/task-dialogs.test.tsx`
- [ ] T067 [P] [US3] Playwright scenarios for quickstart scenarios 4, 5, 6, and 9 (the task parts) in `FE/tests/e2e/us3-create-edit-assign.spec.ts`

### Implementation for User Story 3

- [ ] T068 [US3] Add `CreateTaskRequest` (`title` required, `@Size(min=1,max=150)` after trim; `description` optional `@Size(max=2000)`; optional `assigneeId`) and `UpdateTaskRequest` (title 1-150, description up to 2,000 or null, `assigneeId` UUID or null; at least one property; distinguishes absent from null) in `TS/src/main/java/com/taskify/task/api/`
- [ ] T069 [US3] Implement `TaskService.create` (verifies project and assignee via `ProjectServiceClient`, status `TODO`, `createdBy` from `ActingUser`, trims text) and `TaskService.update` (applies only supplied fields, verifies a non-null assignee, rejected requests leave the task unchanged, sets `updatedAt`/`updatedBy`) in `TS/src/main/java/com/taskify/task/service/TaskService.java`
- [ ] T070 [US3] Implement `POST /api/v1/projects/{projectId}/tasks`, `GET /api/v1/tasks/{taskId}`, and `PATCH /api/v1/tasks/{taskId}` in `TS/src/main/java/com/taskify/task/api/TaskController.java`
- [ ] T071 [P] [US3] Add `createTask`, `getTask`, `updateTask` calls to `FE/src/services/tasksApi.ts`
- [ ] T072 [P] [US3] Implement `FE/src/components/CreateTaskDialog.tsx` (title 150 and description 2,000 character limits with counters, assignee select including "Unassigned", field-level server error display)
- [ ] T073 [US3] Implement `FE/src/components/EditTaskDialog.tsx` (edit title, description, assignee; rejected edit keeps previous values and shows the message) and wire both dialogs into `FE/src/pages/ProjectBoard.tsx` and `FE/src/components/TaskCard.tsx`
- [ ] T074 [US3] Update `TS/README.md` and `FE/README.md` with the create, edit, and assign behavior and validation limits

**Checkpoint**: User Stories 1 to 3 work independently

---

## Phase 6: User Story 4 - Comment on tasks (Priority: P2)

**Goal**: Any user comments on any task; comments show author and time, oldest first.

**Independent Test**: Open a task, comment as one user, switch user, and confirm the comment appears with the first user's name and time.

### Tests for User Story 4 ⚠️

- [ ] T075 [P] [US4] Validation and controller tests for `POST` and `GET /api/v1/tasks/{taskId}/comments` in `TS/src/test/java/com/taskify/task/api/CommentsTest.java`: valid comment; empty and whitespace-only rejected; 1,000 chars accepted and 1,001 rejected; unknown extra property 400; markup stored verbatim; unknown task 404; list is oldest first; any user can comment on a task in any column and any assignee; `commentCount` on the task increases
- [ ] T076 [P] [US4] Component test for `CommentsPanel` (order, author name and time, validation messages) in `FE/tests/unit/comments.test.tsx`
- [ ] T077 [P] [US4] Playwright scenarios for quickstart scenarios 7 and 9 (the comment parts) in `FE/tests/e2e/us4-comments.spec.ts`

### Implementation for User Story 4

- [ ] T078 [US4] Add `CreateCommentRequest` (`text` required, `@Size(min=1,max=1000)` after trim, unknown properties rejected) and `CommentResponse` in `TS/src/main/java/com/taskify/task/api/`
- [ ] T079 [US4] Implement `CommentService` (add with `authorId` from `ActingUser`, list oldest first) in `TS/src/main/java/com/taskify/task/service/CommentService.java`
- [ ] T080 [US4] Implement `GET` and `POST /api/v1/tasks/{taskId}/comments` in `TS/src/main/java/com/taskify/task/api/CommentController.java`
- [ ] T081 [P] [US4] Add `listComments` and `addComment` calls to `FE/src/services/tasksApi.ts`
- [ ] T082 [US4] Implement `FE/src/components/CommentsPanel.tsx` (list with author name and timestamp, comment form with 1,000 character limit and server error display) and open it from a task detail dialog `FE/src/components/TaskDetailDialog.tsx` reached from `TaskCard.tsx`; refresh the board's comment count after posting
- [ ] T083 [US4] Update `TS/README.md` and `FE/README.md` with the comment endpoints and limits

**Checkpoint**: User Stories 1 to 4 work independently

---

## Phase 7: User Story 5 - Create projects (Priority: P3)

**Goal**: Create a project with a unique name; it opens with an empty four-column board.

**Independent Test**: Create a project and confirm it appears in the list with an empty board; a duplicate name is rejected.

### Tests for User Story 5 ⚠️

- [ ] T084 [P] [US5] Validation and controller tests for `POST /api/v1/projects` in `PS/src/test/java/com/taskify/project/api/CreateProjectTest.java`: valid; name empty, whitespace only, 100 chars (accepted), 101 chars (rejected); description 1,000 accepted and 1,001 rejected; duplicate name differing only by case returns 409; unknown extra property 400; unknown user 401; markup stored verbatim
- [ ] T085 [P] [US5] Component test for `CreateProjectDialog` validation in `FE/tests/unit/project-dialog.test.tsx`
- [ ] T086 [P] [US5] Playwright scenario for quickstart scenario 8 in `FE/tests/e2e/us5-create-project.spec.ts`

### Implementation for User Story 5

- [ ] T087 [US5] Add `CreateProjectRequest` (`name` required `@Size(min=1,max=100)` after trim; `description` optional `@Size(max=1000)`; unknown properties rejected) in `PS/src/main/java/com/taskify/project/api/CreateProjectRequest.java`
- [ ] T088 [US5] Implement `ProjectService.create` (trim, case-insensitive uniqueness check backed by the unique index, `createdBy` from `ActingUser`, map constraint violation to 409) in `PS/src/main/java/com/taskify/project/service/ProjectService.java`
- [ ] T089 [US5] Implement `POST /api/v1/projects` in `PS/src/main/java/com/taskify/project/api/ProjectController.java`
- [ ] T090 [P] [US5] Add `createProject` to `FE/src/services/projectsApi.ts` and implement `FE/src/components/CreateProjectDialog.tsx` (100 and 1,000 character limits, duplicate-name message), wired into `FE/src/pages/ProjectList.tsx`
- [ ] T091 [US5] Update `PS/README.md` and `FE/README.md` with the project creation endpoint and limits

**Checkpoint**: All five user stories work independently

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Constitution compliance, hardening, and end-to-end validation

- [ ] T092 [P] Add an OpenAPI drift test in `PS/src/test/java/com/taskify/project/ContractDriftTest.java` and `TS/src/test/java/com/taskify/task/ContractDriftTest.java` failing when the generated springdoc document differs from `specs/001-taskify-kanban/contracts/*.openapi.yaml`
- [ ] T093 [P] Add security tests in `PS/src/test/java/com/taskify/project/SecurityTest.java` and `TS/src/test/java/com/taskify/task/SecurityTest.java`: every `/api/**` endpoint returns 401 without a known `X-User-Id`; `/internal/**` returns 401 without the token; no stack traces or internals in error bodies; `/h2-console` returns 404; security headers present
- [ ] T094 [P] Add gateway tests in `GW/src/test/java/com/taskify/gateway/GatewayTest.java`: routing table, `/internal/**` not reachable, CORS allow-list, security headers, write rate limit returns 429
- [ ] T095 [P] Add a persistence test in `TS/src/test/java/com/taskify/task/PersistenceRestartTest.java` (and the project equivalent): start against a temporary H2 file, create data, restart the context, assert data remains and the seed is not duplicated (FR-013, SC-005)
- [ ] T096 [P] Add a plain-text rendering test in `FE/tests/unit/xss.test.tsx` proving titles, descriptions, comments, and project names containing markup render as text (FR-012, SC-006)
- [ ] T097 [P] Add a load/performance check in `TS/src/test/java/com/taskify/task/BoardLoadTest.java`: a project with 100 tasks lists in under 3 seconds (SC-007)
- [ ] T098 [P] Add a dependency vulnerability scan to the build: OWASP dependency-check in the Maven parent `pom.xml` and `npm audit --audit-level=high` script in `FE/package.json`; document the policy in the root `README.md`
- [ ] T099 [P] Verify documentation completeness: run Checkstyle and ESLint jsdoc rules across all modules, fix every missing Javadoc/TSDoc, and ensure each of `GW/README.md`, `PS/README.md`, `TS/README.md`, `FE/README.md` covers purpose, configuration, run, test, and contracts
- [ ] T100 [P] Write the root `README.md` with architecture overview, environment variables, Docker Compose usage, reset-to-sample-data steps, and the identification-not-authentication caveat
- [ ] T101 Review keyboard and screen-reader behavior of the board and dialogs (focus management, labels, drag-and-drop keyboard flow) and fix issues in `FE/src/components/`
- [ ] T102 Run the full `quickstart.md` validation scenarios 1 to 14 against `docker compose up --build` and record results in `specs/001-taskify-kanban/quickstart.md` notes

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies
- **Foundational (Phase 2)**: depends on Setup; blocks all user stories
- **User Stories (Phases 3 to 7)**: all depend on Foundational
  - US1 and US2 are both P1; US2 needs the board and task list from US1
  - US3 and US4 (P2) build on the board from US1 (and the drag-and-drop work in US2 touches the same board files, so do US2 before US3 if working alone)
  - US5 (P3) only needs Foundational plus the project list from US1
- **Polish (Phase 8)**: depends on all desired stories

### User Story Dependencies

- **US1 (P1)**: starts after Foundational; no dependency on other stories
- **US2 (P1)**: starts after US1 backend and board components exist
- **US3 (P2)**: starts after US1; independent of US2 except shared frontend files
- **US4 (P2)**: starts after US1; uses `TaskDetailDialog`, which US3's edit dialog can later reuse
- **US5 (P3)**: starts after US1's project list exists; independent of US2 to US4

### Within Each User Story

- Tests first and failing, then entities, then services, then endpoints, then frontend
- Backend and frontend tasks marked [P] in the same story can run in parallel once the contract is fixed
- README task last in each story

### Parallel Opportunities

- Setup: all [P] build and tooling tasks
- Foundational: persistence files for the two services, the two `ProblemDetailsAdvice` classes, gateway filters, and frontend shell files
- Per story: all tests marked [P]; entity tasks for different services; frontend API and component tasks in different files
- After US1: US3, US4, and US5 can proceed in parallel with different developers (US2 first if sharing board files)

---

## Parallel Example: User Story 1

```text
# Tests together:
Task: "Controller tests for users and projects in PS/src/test/.../ReadEndpointsTest.java"
Task: "Controller test for listing tasks in TS/src/test/.../ListTasksTest.java"
Task: "WireMock contract test in TS/src/test/.../ProjectServiceClientContractTest.java"
Task: "Component tests in FE/tests/unit/board.test.tsx"

# Entities together:
Task: "Create User entity in PS/.../domain/User.java"
Task: "Create Project entity in PS/.../domain/Project.java"
Task: "Create Task entity in TS/.../domain/Task.java"
Task: "Create Comment entity in TS/.../domain/Comment.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (blocks all stories)
3. Complete Phase 3: User Story 1
4. **Stop and validate**: run quickstart scenario 1
5. Demo; the app is read-only at this point

For a more useful first release, add User Story 2 (also P1) before demoing, since moving cards is the core Kanban value.

### Incremental Delivery

1. Setup + Foundational → foundation ready
2. US1 → validate → demo (read-only board)
3. US2 → validate → demo (movable cards)
4. US3 → validate → demo (create, edit, assign)
5. US4 → validate → demo (comments)
6. US5 → validate → demo (new projects)
7. Polish → full quickstart run

### Parallel Team Strategy

1. Team completes Setup + Foundational together
2. Then: Developer A on US1 then US2 (shared board files); Developer B on US3 and US4 after US1's board exists; Developer C on US5; one person on gateway and Compose polish

---

## Notes

- [P] tasks touch different files and have no dependency on incomplete tasks
- Real-time updates and notifications are out of scope (plan.md); do not add push or polling
- Never commit secrets; `.env` stays untracked
- Applied Flyway migrations are immutable; schema changes after first release need a new version
- Commit after each task or logical group; stop at any checkpoint to validate a story on its own
