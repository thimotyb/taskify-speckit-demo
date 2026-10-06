# Data Model: Taskify Kanban Platform

Each service owns its tables. Cross-service references are ids only (no foreign keys). Ids are UUID
strings. Timestamps are UTC ISO-8601.

## Persistence

Each service stores its tables in its own H2 database file (file mode) on its own volume. Schema and
seed data are applied by versioned Flyway migrations; the seed migration runs once, on the first start
of an empty database. Data survives restarts (FR-013). Seed ids are fixed so services can reference
each other's seeded records.

## project-service

### User (seeded, read-only)

| Field | Type | Rules |
|---|---|---|
| id | UUID | primary key, fixed in seed data |
| name | string | 1-100 chars |
| role | enum | `PRODUCT_MANAGER` or `ENGINEER` |

Seed: 1 product manager and 4 engineers. Roles are labels only; permissions are identical (FR-014).

### Project

| Field | Type | Rules |
|---|---|---|
| id | UUID | primary key |
| name | string | required, 1-100 chars after trim, unique ignoring case (FR-005) |
| description | string | optional, up to 1,000 chars |
| createdBy | UUID | a User id; the acting user (FR-016) |
| createdAt | timestamp | set by server |

Seed: 3 sample projects with fixed ids.

## task-service

### Task

| Field | Type | Rules |
|---|---|---|
| id | UUID | primary key |
| projectId | UUID | required; must exist in project-service at creation |
| title | string | required, 1-150 chars after trim (FR-007) |
| description | string | optional, up to 2,000 chars |
| status | enum | `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`; default `TODO` |
| assigneeId | UUID | optional, at most one; must be a known User id (FR-008) |
| createdBy | UUID | acting user |
| createdAt | timestamp | server-set; defines order within a column |
| updatedAt | timestamp | server-set on every change |
| updatedBy | UUID | acting user of the last change |

**State transitions**: any status to any other status is allowed, forward or backward (FR-009). A move
to the current status is a no-op that returns success.

**Concurrency**: last write wins (spec edge case). No optimistic locking is enforced.

### Comment

| Field | Type | Rules |
|---|---|---|
| id | UUID | primary key |
| taskId | UUID | required, references Task in the same service |
| authorId | UUID | acting user (FR-010) |
| text | string | required, 1-1,000 chars after trim |
| createdAt | timestamp | server-set; list order is oldest first (FR-011) |

Comments are immutable in this phase (no edit or delete).

Seed: sample tasks across all four statuses in each sample project, a few with assignees and comments.
