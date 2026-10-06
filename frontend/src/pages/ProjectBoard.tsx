import RefreshIcon from '@mui/icons-material/Refresh';
import { Box, Button, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink, useParams } from 'react-router-dom';
import { Column } from '../components/Column';
import { ErrorView, LoadingView, NotFoundView } from '../components/StateViews';
import { ApiError } from '../services/apiClient';
import { getProject } from '../services/projectsApi';
import { listTasks } from '../services/tasksApi';
import { COLUMNS } from '../services/types';
import { listUsers } from '../services/usersApi';

/**
 * A project's Kanban board: four columns in order. Data is fetched on open, on window focus, and on
 * the refresh button; there is no live push in this phase.
 * @returns the page element
 */
export function ProjectBoard() {
  const { projectId = '' } = useParams();
  const project = useQuery({ queryKey: ['project', projectId], queryFn: () => getProject(projectId), retry: false });
  const tasks = useQuery({ queryKey: ['tasks', projectId], queryFn: () => listTasks(projectId), retry: false });
  const users = useQuery({ queryKey: ['users'], queryFn: listUsers });

  const notFound = [project.error, tasks.error].some((e) => e instanceof ApiError && e.status === 404);
  if (notFound) return <NotFoundView what="This project" />;
  const error = project.error ?? tasks.error;
  if (error) return <ErrorView message={error.message} />;
  if (!project.data || !tasks.data) return <LoadingView />;

  const refreshing = project.isFetching || tasks.isFetching;
  return (
    <Box>
      <Stack direction="row" sx={{ alignItems: 'center', mb: 2 }} spacing={2}>
        <Button component={RouterLink} to="/projects" size="small">
          All projects
        </Button>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          {project.data.name}
        </Typography>
        <Button
          startIcon={<RefreshIcon />}
          onClick={() => {
            void project.refetch();
            void tasks.refetch();
          }}
          disabled={refreshing}
        >
          Refresh
        </Button>
      </Stack>
      <Box sx={{ display: 'flex', gap: 2, overflowX: 'auto', alignItems: 'flex-start' }}>
        {COLUMNS.map((column) => (
          <Column
            key={column.status}
            status={column.status}
            label={column.label}
            tasks={tasks.data.filter((t) => t.status === column.status)}
            users={users.data ?? []}
          />
        ))}
      </Box>
    </Box>
  );
}
