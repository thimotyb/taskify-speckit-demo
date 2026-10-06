import { apiFetch } from './apiClient';
import type { Project } from './types';

/**
 * Lists all projects, oldest first.
 * @returns all projects
 */
export function listProjects(): Promise<Project[]> {
  return apiFetch<Project[]>('/projects');
}

/**
 * Gets one project.
 * @param projectId project id
 * @returns the project
 */
export function getProject(projectId: string): Promise<Project> {
  return apiFetch<Project>(`/projects/${encodeURIComponent(projectId)}`);
}
