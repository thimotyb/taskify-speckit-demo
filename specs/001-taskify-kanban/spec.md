# Feature Specification: Taskify Kanban Platform

**Feature Branch**: `001-taskify-kanban`

**Created**: 2026-10-06

**Status**: Draft

**Input**: User description: "Develop Taskify, a team productivity platform where predefined users create projects, assign tasks, comment, and move tasks across Kanban columns (To Do, In Progress, In Review, Done). Five users (one product manager, four engineers), three sample projects, no login for this first phase."

## Clarifications

### Session 2026-10-06

- Q: Who should be allowed to move a task to Done? → A: Any user can move any task to any column, including Done.
- Q: Who should be allowed to add comments to a task? → A: Any selected user can comment on any task in any project.
- Q: Can a task have more than one assignee? → A: One assignee at most per task, or unassigned.
- Q: Should a task's title and description be editable after creation? → A: Yes, any user can edit any task's title and description, under the same validation rules as creation.
- Q: To make data survive an app restart, should each service store its database in a file on disk instead of in memory? → A: Yes, each service keeps its database in a file on a persistent volume; seed data loads only when the database is empty.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Choose a user and view a project's Kanban board (Priority: P1)

A team member opens Taskify and sees the list of five predefined users. They pick who they are, then see the three sample projects. Opening a project shows a board with four columns (To Do, In Progress, In Review, Done), each listing that project's tasks.

**Why this priority**: Without a way to identify the acting user and see the board, no other capability has value. It is the foundation for all other stories.

**Independent Test**: Open the app, select any user, open each sample project, and confirm the four columns appear with the sample tasks in their columns.

**Acceptance Scenarios**:

1. **Given** the app is opened for the first time, **When** the user lands on the start screen, **Then** they see exactly five users (one product manager, four engineers) and can select one without entering any credentials.
2. **Given** a user is selected, **When** they view the project list, **Then** they see the three sample projects.
3. **Given** a project is opened, **When** the board loads, **Then** it shows the columns To Do, In Progress, In Review, and Done in that order, with each task shown in the column matching its status.
4. **Given** a user is selected, **When** they reload the page, **Then** the same user remains selected.

---

### User Story 2 - Move tasks across Kanban columns (Priority: P1)

A user moves a task from one column to another (for example, from To Do to In Progress) to reflect progress. The new status is saved and visible to everyone.

**Why this priority**: Moving tasks across columns is the core value of a Kanban tool.

**Independent Test**: On a sample project, move a task from To Do through each column to Done and reload the page; the task stays in its last column.

**Acceptance Scenarios**:

1. **Given** a task in To Do, **When** the user moves it to In Progress, **Then** the task appears in In Progress and no longer in To Do.
2. **Given** a task in any column, **When** the user moves it to any other column (forward or backward), **Then** the move is accepted and saved.
3. **Given** a task was moved, **When** a different user opens the same project, **Then** they see the task in its new column.

---

### User Story 3 - Create and assign tasks (Priority: P2)

A user creates a task in a project with a title and optional description, and assigns it to one of the five users. New tasks begin in To Do. Users can change the assignee later.

**Why this priority**: Tasks are the unit of work; the board is only useful once users can add their own and assign ownership.

**Independent Test**: Create a task in a project, assign it to an engineer, and confirm it appears in To Do showing the assignee; reassign it and confirm the change.

**Acceptance Scenarios**:

1. **Given** a project board, **When** a user creates a task with a valid title, **Then** the task appears in To Do with the chosen assignee (or unassigned if none was chosen).
2. **Given** a task, **When** a user changes its assignee to another of the five users, **Then** the board shows the new assignee.
3. **Given** the task creation form, **When** a user submits an empty or over-length title, **Then** the task is not created and a clear message explains what to fix.
4. **Given** an existing task, **When** any user edits its title or description with valid values, **Then** the task card shows the updated values to everyone.
5. **Given** an existing task, **When** a user edits its title to an empty or over-length value, **Then** the change is rejected, the previous title is kept, and a clear message explains what to fix.

---

### User Story 4 - Comment on tasks (Priority: P2)

A user opens a task and adds a comment. Comments show the author and time, and are listed oldest to newest.

**Why this priority**: Discussion on tasks is a key collaboration feature but depends on tasks existing.

**Independent Test**: Open a task, add a comment as one user, switch to another user, and confirm the comment appears with the first user's name and time.

**Acceptance Scenarios**:

1. **Given** a task, **When** a user submits a non-empty comment, **Then** it is appended to the task's comment list with the author's name and timestamp.
2. **Given** a task with comments, **When** any user opens it, **Then** all comments are shown in chronological order.
3. **Given** the comment form, **When** a user submits an empty or over-length comment, **Then** it is rejected with a clear message.

---

### User Story 5 - Create projects (Priority: P3)

A user creates a new project with a name and optional description. It appears alongside the sample projects with an empty four-column board.

**Why this priority**: Three sample projects are enough to use the product; creating more extends it.

**Independent Test**: Create a project and confirm it appears in the project list with an empty board.

**Acceptance Scenarios**:

1. **Given** the project list, **When** a user creates a project with a valid name, **Then** it appears in the list and opens to an empty board with all four columns.
2. **Given** the project creation form, **When** a user submits an empty, over-length, or duplicate name, **Then** the project is not created and a clear message explains why.

---

### Edge Cases

- A task is moved to the column it is already in: nothing changes and no error is shown.
- Two users move or edit the same task at nearly the same time: the last change wins and both users see the final state after refreshing.
- A task's assignee is cleared: the task is shown as unassigned.
- A column has no tasks: it is still shown, with an empty-state message.
- A column has many tasks: the column remains usable and scrollable.
- Input containing markup or script-like text (titles, descriptions, comments, project names) is stored and displayed as plain text and never executed.
- A project or task that no longer exists is requested (for example, via a stale link): the user sees a clear "not found" message.
- No user is selected when attempting to create, move, or comment: the user is asked to select one first.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide exactly five predefined users (one product manager, four engineers) and MUST NOT require a login, password, or registration.
- **FR-002**: Users MUST be able to select which predefined user they are acting as, and the system MUST remember the selection across page reloads.
- **FR-003**: System MUST provide three predefined sample projects, each with sample tasks, available on first use.
- **FR-004**: Users MUST be able to view the list of all projects.
- **FR-005**: Users MUST be able to create a project with a name (required, unique, up to 100 characters) and an optional description (up to 1,000 characters).
- **FR-006**: System MUST display each project as a Kanban board with the four columns To Do, In Progress, In Review, and Done, in that order.
- **FR-007**: Users MUST be able to create a task in a project with a title (required, up to 150 characters) and an optional description (up to 2,000 characters); new tasks MUST start in To Do.
- **FR-007a**: Any user MUST be able to edit the title and description of any task after creation, subject to the same validation limits as FR-007; rejected edits MUST leave the task unchanged.
- **FR-008**: Users MUST be able to assign a task to at most one of the five users, change the assignee, or leave it unassigned; a task MUST NOT have more than one assignee.
- **FR-009**: Any user MUST be able to move any task to any of the four columns, including Done, regardless of who the task is assigned to, and the system MUST persist the new status.
- **FR-010**: Users MUST be able to add comments (required, up to 1,000 characters) to any task in any project, regardless of its assignee or column; each comment MUST record its author and creation time.
- **FR-011**: System MUST show a task's comments in chronological order, oldest first.
- **FR-012**: System MUST validate all user-provided input (names, titles, descriptions, comments, selections) on the server side, reject invalid input with a clear, non-technical message, and treat all text as plain text when displayed.
- **FR-013**: System MUST show every change (new task, move, assignment, comment, new project) to all users after refresh and MUST NOT lose data on page reload or when the application is restarted. Sample data MUST be loaded only the first time, when no data exists, so that restarts never overwrite or duplicate user changes.
- **FR-014**: All five users MUST have the same permissions in this phase; the product manager role is a label only.
- **FR-015**: System MUST show each task's assignee and comment count on the board card.
- **FR-016**: System MUST record the acting user for every create, edit, assign, move, and comment action.
- **FR-017**: System MUST reject every request that does not identify one of the five predefined users, with a clear message, and MUST NOT return or change any data for such a request.

### Key Entities

- **User**: One of five predefined people; has a name and a role (product manager or engineer). Not created or edited by users in this phase.
- **Project**: A named workspace with an optional description; contains tasks and is displayed as a board.
- **Task**: A unit of work within one project; has an editable title, optional editable description, status (To Do, In Progress, In Review, or Done), optional assignee (a User), and a creation time.
- **Comment**: A message on one task; has text, an author (a User), and a creation time.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A first-time user can select a user and open a project board in under 30 seconds with no instructions.
- **SC-002**: A user can create a task and assign it in under 1 minute.
- **SC-003**: A user can move a task to another column in under 5 seconds, and the change is visible to other users on their next refresh.
- **SC-004**: A user can add a comment to a task in under 30 seconds.
- **SC-005**: 100% of tasks, comments, projects, and assignments remain after a page reload and after a full restart of the application, with no sample data duplicated or restored over user changes.
- **SC-006**: 100% of submitted invalid inputs (empty, over-length, or markup-containing) are rejected or neutralized without errors visible to the user and without executing any submitted content.
- **SC-007**: The board for a project with 100 tasks loads in under 3 seconds.
- **SC-008**: In a usability check, at least 90% of the five team members complete all primary tasks (select user, open board, create task, move task, comment) on their first attempt.

## Security Considerations

This feature is Security-First. The points below record what needs protecting and what is knowingly
accepted in this phase.

- **Trust boundaries**: people using Taskify in a browser are untrusted, so everything they submit
  is checked before it is used (FR-012, FR-017). The parts of Taskify that talk to each other do not
  trust one another blindly either; each request between them is also verified.
- **Sensitive data**: project names, task titles and descriptions, comments, and the five user names.
  This phase holds only sample or non-sensitive data. Taskify collects no passwords, payment details,
  or personal contact information.
- **Abuse cases to defend against**: submitting harmful text such as scripts or markup; submitting
  oversized or malformed input; using the system's internal interfaces directly instead of through the
  app; flooding the system with changes; guessing identifiers to reach items that do not exist.
- **Knowingly accepted risk (no login)**: because there is no login, anyone who can reach the app can
  pretend to be any of the five users, and the recorded "who did it" is only as trustworthy as the
  user selected. This is allowed by the constitution's Early-Phase Identification Exception on these
  conditions: the system holds only sample data, is used only by the five predefined users on a
  trusted network, still rejects unknown or missing users (FR-017), and is replaced by real
  authentication before any release to real users or an untrusted network.
- **Visibility by design**: all five users can see and change every project and task (FR-014), so
  there is no per-user privacy to protect in this phase.

## Assumptions

- This phase is for a small trusted team (five users); there is no login, so selecting a user is an identity convenience and not a security boundary. Authentication is deferred to a later phase, and the design should not prevent adding it.
- The product manager and engineers have identical permissions; role-based permissions are out of scope.
- Any user may create tasks in any project and move, assign, or comment on any task.
- Deleting tasks, comments, and projects is out of scope for this phase, as is editing comments and projects; editing a task's title and description, assignee, and status is in scope.
- Task priorities, due dates, labels, attachments, notifications, search, and filtering are out of scope.
- Column set and order are fixed and not configurable.
- Task order within a column is by creation time; manual ordering within a column is out of scope.
- Real-time live updates are not required; other users see changes on their next refresh.
- The primary client is a desktop web browser; native mobile apps are out of scope.
- Data is stored durably on disk so it survives restarts; backup, export, and migration of that data are out of scope for this phase.
- Sample users, projects, and tasks are provided as seed data, loaded only on first start, with names and contents chosen at planning time.
- Per the project constitution, the feature must be built security-first with server-side input validation, a microservices architecture, and complete documentation; these are planning and implementation concerns, not detailed here.
