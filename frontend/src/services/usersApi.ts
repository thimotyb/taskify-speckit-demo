import { apiFetch } from './apiClient';
import type { User } from './types';

/**
 * Lists the five predefined users. This is the one endpoint that needs no identity.
 * @returns all users
 */
export function listUsers(): Promise<User[]> {
  return apiFetch<User[]>('/users');
}
