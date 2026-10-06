# Specification Analysis Report: Taskify Kanban Platform

**Command**: `/speckit-analyze` (read-only) | **Feature**: `001-taskify-kanban` | **Date**: 2026-10-06

**Artifacts analyzed**: `spec.md`, `plan.md`, `tasks.md` (102 tasks), plus `research.md`,
`data-model.md`, `contracts/`, `quickstart.md`, and constitution v1.0.0 at analysis time.

**Status after follow-up (same day)**: findings C1 and C2 were resolved by amending the constitution
to v1.1.0 (Early-Phase Identification Exception), adding a Security Considerations section and FR-017
to the spec, and updating the plan's Constitution Check and Deferred Work. All other findings remain
open. Because the spec gained FR-017, task coverage for it is noted below.

## Findings

| ID | Category | Severity | Status | Location(s) | Summary | Recommendation |
|----|----------|----------|--------|-------------|---------|----------------|
| C1 | Constitution | CRITICAL | **Resolved** | constitution Principle I and Workflow; plan.md Constitution Check Note 1; spec Assumptions; T022, T025 | Principle I required authentication on every endpoint, but the spec has no login and the plan passed the check by reinterpreting the rule; deviations from Principle I were not permitted. | Resolved by constitution v1.1.0 (bounded Early-Phase Identification Exception); plan Note 1 now cites it condition by condition and the plan tracks replacing it (Deferred Work). |
| C2 | Constitution | CRITICAL | **Resolved** | constitution Principle I; spec.md; research.md R6 | Every feature spec MUST identify trust boundaries, sensitive data, and abuse cases; only research.md R6 did. | Resolved: spec.md now has a Security Considerations section and FR-017. |
| I1 | Inconsistency | HIGH | Open | T022 (Foundational) vs T042 (US1) | T022 checks `X-User-Id` against the `User` entity and `UserRepository`, which are created later in US1 (T042), so Foundational cannot finish as written. | Move T042 into Foundational before T022. |
| I2 | Inconsistency | HIGH | Open | quickstart scenario 11; T025; T048; task-service contract | Scenario 11 expects a card move to work while project-service is down, but T025 verifies `X-User-Id` through project-service on every request (60 s cache). After expiry or on a cold start the move fails with 503 or 401. The status endpoint has no 503 in the contract. | Choose one behavior: serve stale cached users when project-service is down, or change scenario 11 and the contract to say moves also fail closed. |
| G1 | Coverage gap | MEDIUM | Open | plan.md R9; T098, T099; constitution Workflow | Quality gates are described as "in CI", but no task creates a pipeline. | Add a task for `.github/workflows/ci.yml` running `mvn verify`, `npm ci && npm run lint && npm test`, and the vulnerability scans. |
| G2 | Coverage gap | MEDIUM | Open | constitution IV; T031, T054, T063, T074, T083, T091 | Every service needs a README, but only the gateway README is created (T031). The project-service, task-service, and frontend READMEs are only "updated". The plan tree omits `docs/` and `config/`. | Create all READMEs in Setup; add `docs/` and `config/` to the plan structure. |
| I3 | Inconsistency | MEDIUM | Open | tasks.md Dependencies; T061, T071, T081; T053, T062, T072, T073, T082; README tasks | US3, US4, and US5 are said to run in parallel but edit the same files (`tasksApi.ts`, `TaskCard.tsx`, `ProjectBoard.tsx`, `TaskService.java`, the READMEs). T071 and T081 are both `[P]` on the same file. | Narrow the parallel claim, or split the shared files. |
| I4 | Inconsistency | MEDIUM | Open | research.md R3; T048; T038 | R3 says projects are checked "on task creation"; T048 checks the project on every board load, so a project-service outage breaks viewing. | Align R3 with T048, or cache project existence. |
| U1 | Underspecified | MEDIUM | Open | FR-016; T065, T075; data-model.md | FR-016 requires recording the acting user for every action. The model keeps `createdBy` and one `updatedBy` that the last change overwrites, and no test asserts attribution for edits, assignments, or comments. | Add attribution assertions to the create, update, comment, and project tests. |
| U2 | Ambiguity | LOW | Open | FR-005; T043, T084 | The spec says only "unique"; case-insensitive, trimmed uniqueness is defined only in the plan and tasks. | State it in FR-005. |
| U3 | Coverage gap | LOW | Open | SC-001, SC-002, SC-004, SC-008 | Usability timings and the 90% first-attempt target have no measurement task. | Add a manual usability step to T102. |
| U4 | Coverage gap | LOW | Open | Edge cases; FR-014 | No test for last-write-wins on concurrent edits, and none that the product manager and engineers have equal permissions. | Add one small test for each. |
| T1 | Task ordering | LOW | Open | T039 vs T024 | The test-first contract test (T039) targets a client that Foundational already built (T024). | Move T024 into US1 after T039, or note the exception. |
| T2 | Inconsistency | LOW | Open | T023 vs T092 | T023 enables springdoc only in the local profile, but the drift test (T092) needs it in the test profile. | State it in T023. |
| T3 | Terminology | LOW | Open | spec.md Clarifications | One bullet puts storage detail into a technology-agnostic spec. | Reword it behaviorally ("data survives restarts"). |
| N1 | Coverage gap | LOW | Open (new) | FR-017 (added after analysis) | FR-017 (reject requests from unknown or missing users) is covered by T022, T025, and T093, but no task states FR-017 explicitly. | Reference FR-017 in T093 when the tasks are next revised. |

## Coverage Summary

| Requirement Key | Has Task? | Task IDs | Notes |
|---|---|---|---|
| FR-001, FR-003 | Yes | T012, T016, T018, T036 | |
| FR-002 | Yes | T033, T051, T041 | |
| FR-004, FR-006, FR-015 | Yes | T052, T053 | |
| FR-005 | Yes | T084 to T091 | Uniqueness rule is not in the spec (U2) |
| FR-007, FR-007a, FR-008 | Yes | T064 to T074 | |
| FR-009 | Yes | T055 to T063 | |
| FR-010, FR-011 | Yes | T075 to T083 | |
| FR-012 | Yes | T019, T020, per-story validation tests, T093, T096 | |
| FR-013 | Yes | T016, T018, T095 | |
| FR-014 | Partial | none direct | No parity test (U4) |
| FR-016 | Partial | T059, T069, T079 | No attribution tests (U1) |
| FR-017 (added later) | Yes | T022, T025, T093 | Not referenced by ID (N1) |
| SC-003, SC-005, SC-006, SC-007 | Yes | T055 to T063, T095, T096, T097 | |

## Constitution Alignment Issues

- C1 and C2: resolved (see above). Principles II, III, and IV were satisfied at analysis time.
- Under constitution v1.1.0, the Early-Phase Identification Exception requires the plan to track
  replacing identification with authentication. The plan's Deferred Work section does this.

## Unmapped Tasks

Setup tasks T001 to T011 and the accessibility review T101. They are infrastructure or
constitution-driven; listed for information only.

## Metrics (at analysis time, before FR-017)

- Total requirements: 21 (17 functional, 4 buildable success criteria)
- Total tasks: 102
- Coverage: 100% have at least one task; 2 only partially (FR-014, FR-016)
- Ambiguity count: 1
- Duplication count: 0
- Critical issues: 2 (both now resolved)

## Next Actions

- Fix the HIGH items (I1, I2) in `tasks.md` before `/speckit-implement`. I1 is a reorder; I2 needs a
  decision between serving stale cached users and failing closed.
- Fold in G1 and G2 (CI workflow task, README creation task) at the same time; they are small.
- MEDIUM and LOW items do not block implementation.
