import { Box, Paper, Stack, Typography } from '@mui/material';
import type { Task, TaskStatus, User } from '../services/types';
import { EmptyView } from './StateViews';
import { TaskCard } from './TaskCard';

/**
 * One Kanban column with its tasks; shows an empty-state message when it has none and scrolls when
 * it has many.
 * @param props the column status, label, tasks, and the user directory
 * @returns the column element
 */
export function Column({
  status,
  label,
  tasks,
  users,
}: {
  status: TaskStatus;
  label: string;
  tasks: Task[];
  users: User[];
}) {
  return (
    <Paper
      variant="outlined"
      component="section"
      aria-label={label}
      data-testid={`column-${status}`}
      sx={{ flex: '1 1 0', minWidth: 240, display: 'flex', flexDirection: 'column', maxHeight: '75vh' }}
    >
      <Box sx={{ p: 2, pb: 1 }}>
        <Typography variant="subtitle1" component="h2">
          {label} ({tasks.length})
        </Typography>
      </Box>
      <Stack spacing={1} sx={{ p: 2, pt: 1, overflowY: 'auto' }}>
        {tasks.length === 0 ? (
          <EmptyView message="No tasks" />
        ) : (
          tasks.map((task) => <TaskCard key={task.id} task={task} users={users} />)
        )}
      </Stack>
    </Paper>
  );
}
