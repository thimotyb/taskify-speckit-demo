/** Role label of a predefined user. Roles are labels only; permissions are identical. */
export type Role = 'PRODUCT_MANAGER' | 'ENGINEER';

/** One of the five predefined users. */
export interface User {
  id: string;
  name: string;
  role: Role;
}

/** A project, shown as a Kanban board. */
export interface Project {
  id: string;
  name: string;
  description: string | null;
  createdBy: string;
  createdAt: string;
}

/** The four Kanban columns. */
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';

/** A task on a project's board. */
export interface Task {
  id: string;
  projectId: string;
  title: string;
  description: string | null;
  status: TaskStatus;
  assigneeId: string | null;
  commentCount: number;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  updatedBy: string;
}

/** The board columns in display order. */
export const COLUMNS: ReadonlyArray<{ status: TaskStatus; label: string }> = [
  { status: 'TODO', label: 'To Do' },
  { status: 'IN_PROGRESS', label: 'In Progress' },
  { status: 'IN_REVIEW', label: 'In Review' },
  { status: 'DONE', label: 'Done' },
];

/** Human-readable label of a role. */
export const ROLE_LABELS: Record<Role, string> = {
  PRODUCT_MANAGER: 'Product manager',
  ENGINEER: 'Engineer',
};
