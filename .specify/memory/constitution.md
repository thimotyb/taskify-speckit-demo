<!--
Sync Impact Report
- Version change: 1.0.0 → 1.1.0
- Modified principles: I. Security-First (authentication bullet now refers to a bounded
  exception; Early-Phase Identification Exception added; rationale extended). Title unchanged.
- Added sections: none (new exception paragraph inside Principle I)
- Removed sections: none
- Other edits: Development Workflow clarifies the exception is not a deviation.
- Bump rationale: MINOR. Materially expanded guidance with a bounded exception; no principle
  was removed, and all other Principle I rules are unchanged.
- Follow-up TODOs: none in this file. Dependent artifacts to refresh: plan.md Constitution
  Check Note 1 and the spec's Security Considerations (see analysis findings C1 and C2).
-->
# Taskify Constitution

## Core Principles

### I. Security-First (NON-NEGOTIABLE)
Taskify is a Security-First application. Security requirements MUST be considered in every
spec, plan, and task, and take precedence over convenience, delivery speed, and feature scope.
- Every feature spec MUST identify its trust boundaries, sensitive data, and abuse cases.
- Every service MUST apply least privilege, deny by default, and fail closed.
- Secrets MUST NOT be committed to source control or written to logs.
- Authentication and authorization MUST be enforced on every service endpoint, including
  internal service-to-service calls, except where the Early-Phase Identification Exception
  below applies.

**Early-Phase Identification Exception**: In early phases of implementation, user-facing
endpoints MAY use simple identification without login (for example, choosing one of a fixed set
of predefined users) instead of authentication, but only while ALL of these conditions hold:
- The feature spec explicitly defers authentication, records the risk acceptance, and states
  that identification is not a security boundary.
- The server MUST still validate the claimed identity against the known user set on every
  request and fail closed when it is missing or unknown; it MUST NOT trust the client.
- Service-to-service calls MUST remain authenticated; the exception covers end-user endpoints
  only.
- The system MUST hold only sample or non-sensitive data and MUST NOT be exposed to untrusted
  users or networks.
- Identity handling MUST be concentrated in one place so real authentication can replace it
  without changing business logic.
- The plan MUST track the work to replace identification with authentication, and the
  exception ends no later than the first release to real users or an untrusted network.

All other requirements of this constitution, including Principle II, still apply during the
exception. Using the exception is a defined rule, not a deviation.

Rationale: Taskify handles user data and tasks; a breach or privilege escalation is costlier
than any delay caused by building security in from the start. The bounded exception lets early
phases validate the product without a login flow while keeping the path to real authentication
open.

### II. Validate All User Inputs
All user-supplied input MUST be validated before it is processed, stored, or forwarded.
- Validation MUST happen server-side at the service boundary that first receives the input;
  client-side validation is a convenience only and never a substitute.
- Inputs MUST be checked against an explicit schema (type, length, format, allowed values)
  using an allow-list approach; invalid input MUST be rejected with a safe error message.
- Input MUST be encoded or parameterized for its destination (e.g., queries, HTML, shell,
  logs) to prevent injection.
- Every validation rule MUST have automated tests covering valid, invalid, and boundary cases.

Rationale: Unvalidated input is the root cause of most injection and data-integrity
vulnerabilities.

### III. Microservices Architecture
Taskify MUST be built as a set of small, independently deployable services.
- Each service MUST own a single bounded context and its own data store; no service may read
  or write another service's data store directly.
- Services MUST communicate only through documented, versioned contracts (APIs or events).
- Contract changes MUST be backward compatible or released as a new version with a migration
  plan.
- Each service MUST be independently testable, with contract tests for every interface it
  exposes or consumes.
- Services MUST NOT trust other services implicitly; Principles I and II apply to inter-service
  traffic.

Rationale: Clear boundaries limit the blast radius of failures and security incidents and
allow teams to change services independently.

### IV. Full Documentation
Code MUST be fully documented. A change is not complete until its documentation is.
- Every public module, class, function, and API endpoint MUST have a doc comment describing
  its purpose, parameters, return values, errors raised, and security considerations.
- Every service MUST have a README covering its purpose, configuration, how to run and test
  it, and its API or event contracts.
- Non-obvious logic MUST include comments explaining why, not only what.
- Documentation MUST be updated in the same change as the code it describes.

Rationale: Documented code is reviewable for security flaws and lets teams work across
service boundaries without reverse engineering.

## Architecture & Security Constraints

- Data MUST be encrypted in transit between all clients and services; sensitive data MUST be
  encrypted at rest.
- Dependencies MUST be reviewed and kept free of known critical vulnerabilities before release.
- Error responses MUST NOT leak internal details (stack traces, queries, infrastructure).
- Security-relevant events (authentication failures, authorization denials, validation
  rejections) MUST be logged without recording secrets or sensitive personal data.

## Development Workflow & Quality Gates

- Every change MUST go through code review, and the reviewer MUST verify compliance with this
  constitution.
- A change MUST NOT merge unless tests pass, input validation is covered by tests, and
  documentation is updated.
- Any deviation from a principle MUST be recorded with its justification in the plan's
  complexity or exceptions section and approved by a reviewer. Deviations from Principle I
  or II are not permitted; the Early-Phase Identification Exception in Principle I is a
  defined rule and does not count as a deviation.

## Governance

This constitution supersedes all other practices and guidance in the project.
- **Amendments**: Proposed in writing with the rationale and impact on existing specs, plans,
  and code; MUST be approved by the project maintainers and include a migration plan for any
  non-compliant existing work.
- **Versioning**: Semantic versioning. MAJOR for backward-incompatible principle removals or
  redefinitions; MINOR for new principles or materially expanded guidance; PATCH for
  clarifications and wording fixes.
- **Compliance review**: Every spec, plan, and pull request MUST be checked against these
  principles, and the constitution MUST be reviewed at least once per quarter.

**Version**: 1.1.0 | **Ratified**: 2026-10-06 | **Last Amended**: 2026-10-06
