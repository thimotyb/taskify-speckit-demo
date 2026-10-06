import { apiFetch } from './apiClient';
import type { Task } from './types';

/**
 * Lists a project's tasks, ordered by creation time.
 * @param projectId project id
 * @returns the project's tasks
 */
export function listTasks(projectId: string): Promise<Task[]> {
  return apiFetch<Task[]>(`/projects/${encodeURIComponent(projectId)}/tasks`);
}
