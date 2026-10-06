/** A field-level validation message returned by the API. */
export interface FieldError {
  field: string;
  message: string;
}

/** Error raised for any non-success API answer; messages are safe to show to users. */
export class ApiError extends Error {
  readonly status: number;
  readonly fields: FieldError[];

  /**
   * Creates the error.
   * @param status HTTP status code
   * @param message safe, user-facing message
   * @param fields field-level validation messages, if any
   */
  constructor(status: number, message: string, fields: FieldError[] = []) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fields = fields;
  }
}

const BASE = '/api/v1';

let actingUserId: string | null = null;
let onUnauthorized: (() => void) | null = null;

/**
 * Sets the identity sent as the X-User-Id header. This is identification, not authentication.
 * @param id the selected user id, or null when nobody is selected
 */
export function setActingUserId(id: string | null): void {
  actingUserId = id;
}

/**
 * Registers a callback invoked when the server rejects the identity (401).
 * @param callback the callback, or null to remove it
 */
export function setUnauthorizedHandler(callback: (() => void) | null): void {
  onUnauthorized = callback;
}

/**
 * Calls the API and parses the JSON answer. Problem Details answers become {@link ApiError}s.
 * @param path path below /api/v1, starting with a slash
 * @param init optional fetch options (method, JSON body)
 * @returns the parsed JSON body
 */
export async function apiFetch<T>(path: string, init: { method?: string; body?: unknown } = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (actingUserId) headers['X-User-Id'] = actingUserId;
  if (init.body !== undefined) headers['Content-Type'] = 'application/json';
  let response: Response;
  try {
    response = await fetch(BASE + path, {
      method: init.method ?? 'GET',
      headers,
      body: init.body === undefined ? undefined : JSON.stringify(init.body),
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Please check your connection and try again.');
  }
  if (response.ok) {
    return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
  }
  if (response.status === 401 && onUnauthorized) onUnauthorized();
  let message = 'Something went wrong. Please try again later.';
  let fields: FieldError[] = [];
  try {
    const problem = (await response.json()) as { detail?: string; errors?: FieldError[] };
    if (problem.detail) message = problem.detail;
    if (Array.isArray(problem.errors)) fields = problem.errors;
  } catch {
    // keep the generic message
  }
  throw new ApiError(response.status, message, fields);
}
