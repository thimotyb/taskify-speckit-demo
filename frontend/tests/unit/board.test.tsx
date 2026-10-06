import { ThemeProvider } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ProjectBoard } from '../../src/pages/ProjectBoard';
import { ProjectList } from '../../src/pages/ProjectList';
import { UserPicker } from '../../src/pages/UserPicker';
import type { Project, Task, User } from '../../src/services/types';
import { ActingUserProvider } from '../../src/state/ActingUserContext';
import { theme } from '../../src/theme';

const users: User[] = [
  { id: 'u1', name: 'Priya Shah', role: 'PRODUCT_MANAGER' },
  { id: 'u2', name: 'Marco Rossi', role: 'ENGINEER' },
  { id: 'u3', name: 'Lena Fischer', role: 'ENGINEER' },
  { id: 'u4', name: 'Tom Nguyen', role: 'ENGINEER' },
  { id: 'u5', name: 'Sara Okafor', role: 'ENGINEER' },
];
const projects: Project[] = [
  { id: 'p1', name: 'Website Redesign', description: 'Refresh', createdBy: 'u1', createdAt: '2026-10-01T09:00:00Z' },
  { id: 'p2', name: 'Mobile App Launch', description: null, createdBy: 'u1', createdAt: '2026-10-01T09:00:01Z' },
  { id: 'p3', name: 'Internal Tooling', description: null, createdBy: 'u1', createdAt: '2026-10-01T09:00:02Z' },
];

function task(id: string, title: string, status: Task['status'], assigneeId: string | null, commentCount = 0): Task {
  return { id, projectId: 'p1', title, description: null, status, assigneeId, commentCount, createdBy: 'u1',
    createdAt: '2026-10-01T09:00:00Z', updatedAt: '2026-10-01T09:00:00Z', updatedBy: 'u1' };
}

function json(body: unknown, status = 200) {
  return Promise.resolve(new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }));
}

function mockApi(tasks: Task[]) {
  vi.stubGlobal('fetch', vi.fn((url: string) => {
    if (url.endsWith('/users')) return json(users);
    if (url.endsWith('/projects')) return json(projects);
    if (url.endsWith('/projects/p1')) return json(projects[0]);
    if (url.endsWith('/projects/p1/tasks')) return json(tasks);
    return json({ detail: 'Not found' }, 404);
  }));
}

function renderAt(path: string) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <ThemeProvider theme={theme}>
      <QueryClientProvider client={client}>
        <ActingUserProvider>
          <MemoryRouter initialEntries={[path]}>
            <Routes>
              <Route path="/" element={<UserPicker />} />
              <Route path="/projects" element={<ProjectList />} />
              <Route path="/projects/:projectId" element={<ProjectBoard />} />
            </Routes>
          </MemoryRouter>
        </ActingUserProvider>
      </QueryClientProvider>
    </ThemeProvider>,
  );
}

beforeEach(() => window.localStorage.clear());
afterEach(() => vi.unstubAllGlobals());

describe('UserPicker', () => {
  it('shows the five predefined users with roles and no credentials', async () => {
    mockApi([]);
    renderAt('/');
    await screen.findByText('Priya Shah');
    const list = screen.getByRole('list', { name: 'Users' });
    expect(within(list).getAllByRole('listitem')).toHaveLength(5);
    expect(within(list).getByText('Product manager')).toBeInTheDocument();
    expect(within(list).getAllByText('Engineer')).toHaveLength(4);
    expect(screen.queryByLabelText(/password/i)).not.toBeInTheDocument();
  });

  it('remembers the selected user across reloads', async () => {
    mockApi([]);
    renderAt('/');
    await userEvent.click(await screen.findByText('Marco Rossi'));
    expect(window.localStorage.getItem('taskify.actingUserId')).toBe('u2');
  });
});

describe('ProjectList', () => {
  it('lists the projects', async () => {
    mockApi([]);
    renderAt('/projects');
    await screen.findByText('Website Redesign');
    const list = screen.getByRole('list', { name: 'Projects' });
    expect(within(list).getAllByRole('listitem')).toHaveLength(3);
    expect(screen.getByText('Mobile App Launch')).toBeInTheDocument();
  });
});

describe('Board', () => {
  it('renders four columns in order with tasks in their status column', async () => {
    mockApi([
      task('t1', 'Audit content', 'TODO', 'u2', 2),
      task('t2', 'Choose hosting', 'DONE', null),
    ]);
    renderAt('/projects/p1');
    await screen.findByText('Audit content');
    const headings = screen.getAllByRole('heading', { level: 2 }).map((h) => h.textContent);
    expect(headings).toEqual(['To Do (1)', 'In Progress (0)', 'In Review (0)', 'Done (1)']);
    expect(within(screen.getByTestId('column-TODO')).getByText('Audit content')).toBeInTheDocument();
    expect(within(screen.getByTestId('column-DONE')).getByText('Choose hosting')).toBeInTheDocument();
  });

  it('shows assignee and comment count on the card', async () => {
    mockApi([task('t1', 'Audit content', 'TODO', 'u2', 2)]);
    renderAt('/projects/p1');
    await screen.findByText('Audit content');
    expect(screen.getByText('Marco Rossi')).toBeInTheDocument();
    expect(screen.getByLabelText('2 comments')).toBeInTheDocument();
  });

  it('shows an empty-state message in columns without tasks', async () => {
    mockApi([task('t1', 'Audit content', 'TODO', null)]);
    renderAt('/projects/p1');
    await screen.findByText('Audit content');
    expect(within(screen.getByTestId('column-IN_REVIEW')).getByText('No tasks')).toBeInTheDocument();
  });

  it('shows a not-found view for an unknown project', async () => {
    mockApi([]);
    renderAt('/projects/missing');
    expect(await screen.findByText(/was not found/i)).toBeInTheDocument();
  });

  it('renders markup in a title as plain text', async () => {
    mockApi([task('t1', '<script>alert(1)</script>', 'TODO', null)]);
    renderAt('/projects/p1');
    expect(await screen.findByText('<script>alert(1)</script>')).toBeInTheDocument();
    expect(document.querySelector('script')).toBeNull();
  });
});
