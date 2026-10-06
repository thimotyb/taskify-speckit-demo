# Seed Data

Sample data loaded once, on the first start of each service (Flyway versioned migration `V2__seed.sql`).
Restarts never reload or duplicate it. Ids are fixed so services can refer to each other's records.
Applied seed migrations are immutable.

## Users (project-service)

| Id | Name | Role |
|---|---|---|
| `00000000-0000-0000-0000-000000000001` | Priya Shah | PRODUCT_MANAGER |
| `00000000-0000-0000-0000-000000000002` | Marco Rossi | ENGINEER |
| `00000000-0000-0000-0000-000000000003` | Lena Fischer | ENGINEER |
| `00000000-0000-0000-0000-000000000004` | Tom Nguyen | ENGINEER |
| `00000000-0000-0000-0000-000000000005` | Sara Okafor | ENGINEER |

## Projects (project-service)

| Id | Name |
|---|---|
| `10000000-0000-0000-0000-000000000001` | Website Redesign |
| `10000000-0000-0000-0000-000000000002` | Mobile App Launch |
| `10000000-0000-0000-0000-000000000003` | Internal Tooling |

## Tasks (task-service)

Ids are `20000000-0000-0000-0000-0000000000NN`. Tasks 01-08 belong to project 1, 09-16 to project 2,
17-24 to project 3. Each project has two tasks in every status. Creation times are one minute apart
starting 2026-10-01 09:01 UTC, so the order within a column follows the number.

| NN | Project | Status | Title | Assignee |
|---|---|---|---|---|
| 01 | 1 | TODO | Audit current site content | Marco |
| 02 | 1 | TODO | Collect brand guidelines | unassigned |
| 03 | 1 | IN_PROGRESS | Design new homepage mockups | Lena |
| 04 | 1 | IN_PROGRESS | Set up component library | Tom |
| 05 | 1 | IN_REVIEW | Write navigation copy | Sara |
| 06 | 1 | IN_REVIEW | Accessibility review of mockups | Marco |
| 07 | 1 | DONE | Define sitemap | Lena |
| 08 | 1 | DONE | Choose hosting provider | Tom |
| 09 | 2 | TODO | Draft app store listing | Priya |
| 10 | 2 | TODO | Plan beta tester recruitment | unassigned |
| 11 | 2 | IN_PROGRESS | Implement onboarding flow | Marco |
| 12 | 2 | IN_PROGRESS | Set up crash reporting | Sara |
| 13 | 2 | IN_REVIEW | Review payment screen design | Lena |
| 14 | 2 | IN_REVIEW | Security review of login flow | Tom |
| 15 | 2 | DONE | Create app icon set | Sara |
| 16 | 2 | DONE | Register developer accounts | Priya |
| 17 | 3 | TODO | Evaluate CI pipeline options | Tom |
| 18 | 3 | TODO | Document deployment steps | unassigned |
| 19 | 3 | IN_PROGRESS | Build team dashboard prototype | Marco |
| 20 | 3 | IN_PROGRESS | Automate weekly report | Lena |
| 21 | 3 | IN_REVIEW | Migrate scripts to shared repo | Sara |
| 22 | 3 | IN_REVIEW | Review access policy draft | Priya |
| 23 | 3 | DONE | Inventory internal tools | Tom |
| 24 | 3 | DONE | Retire legacy spreadsheet | Marco |

## Comments (task-service)

Ids are `30000000-0000-0000-0000-0000000000NN`.

| NN | Task | Author | Text |
|---|---|---|---|
| 01 | 03 | Priya | Please include a mobile layout. |
| 02 | 03 | Lena | Will do, first draft tomorrow. |
| 03 | 05 | Marco | Copy reads well, minor wording tweaks left. |
| 04 | 11 | Priya | Keep onboarding under three screens. |
| 05 | 14 | Sara | Found one issue with password reset, details in the doc. |
