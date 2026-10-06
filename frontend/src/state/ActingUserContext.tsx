import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { setActingUserId, setUnauthorizedHandler } from '../services/apiClient';

const STORAGE_KEY = 'taskify.actingUserId';

interface ActingUserValue {
  /** The selected user id, or null when nobody is selected. */
  userId: string | null;
  /** Selects the acting user and remembers the choice across reloads. */
  select: (id: string) => void;
  /** Forgets the selected user. */
  clear: () => void;
}

const ActingUserContext = createContext<ActingUserValue | null>(null);

function readStored(): string | null {
  try {
    return window.localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function writeStored(id: string | null): void {
  try {
    if (id) window.localStorage.setItem(STORAGE_KEY, id);
    else window.localStorage.removeItem(STORAGE_KEY);
  } catch {
    // storage may be unavailable; the selection then lasts for this page view only
  }
}

/**
 * Holds the selected user (FR-002), persists it in localStorage, and keeps the API client in sync.
 * @param props the provider children
 * @returns the provider element
 */
export function ActingUserProvider({ children }: { children: ReactNode }) {
  const [userId, setUserId] = useState<string | null>(() => {
    const stored = readStored();
    setActingUserId(stored);
    return stored;
  });

  const select = useCallback((id: string) => {
    setActingUserId(id);
    writeStored(id);
    setUserId(id);
  }, []);

  const clear = useCallback(() => {
    setActingUserId(null);
    writeStored(null);
    setUserId(null);
  }, []);

  // A rejected identity (for example after the data was reset) sends the user back to the picker.
  useEffect(() => {
    setUnauthorizedHandler(clear);
    return () => setUnauthorizedHandler(null);
  }, [clear]);

  const value = useMemo(() => ({ userId, select, clear }), [userId, select, clear]);
  return <ActingUserContext.Provider value={value}>{children}</ActingUserContext.Provider>;
}

/**
 * Reads the acting-user state.
 * @returns the selected user id and the actions to change it
 */
export function useActingUser(): ActingUserValue {
  const value = useContext(ActingUserContext);
  if (!value) throw new Error('useActingUser must be used inside ActingUserProvider');
  return value;
}
