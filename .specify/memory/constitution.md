<!--
Sync Impact Report
- Version change: (unratified template) → 1.0.0
- Modified principles: none (initial adoption; all placeholders replaced)
- Added sections: Core Principles (I. Security-First, II. Validate All Inputs,
  III. Microservices Architecture, IV. Full Documentation), Architecture & Security
  Constraints, Development Workflow & Quality Gates, Governance
- Removed sections: none (template sections 4 and 5 unused; the user requested 4 principles)
- Follow-up TODOs: none. Technology stack, compliance standards and a runtime guidance
  file are intentionally not defined; add them by amendment when decided.
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
  internal service-to-service calls.

Rationale: Taskify handles user data and tasks; a breach or privilege escalation is costlier
than any delay caused by building security in from the start.

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
  or II are not permitted.

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

**Version**: 1.0.0 | **Ratified**: 2026-10-06 | **Last Amended**: 2026-10-06
