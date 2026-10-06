import CommentOutlined from '@mui/icons-material/CommentOutlined';
import { Card, CardContent, Chip, Stack, Typography } from '@mui/material';
import type { Task, User } from '../services/types';

/**
 * A task on the board: title, assignee, and comment count (FR-015). All text is rendered as plain text.
 * @param props the task and the user directory used to resolve the assignee name
 * @returns the card element
 */
export function TaskCard({ task, users }: { task: Task; users: User[] }) {
  const assignee = users.find((u) => u.id === task.assigneeId);
  return (
    <Card variant="outlined" data-testid="task-card" aria-label={task.title}>
      <CardContent sx={{ '&:last-child': { pb: 2 } }}>
        <Typography variant="subtitle2" sx={{ wordBreak: 'break-word' }}>
          {task.title}
        </Typography>
        <Stack direction="row" spacing={1} sx={{ mt: 1, alignItems: 'center' }}>
          <Chip size="small" label={assignee ? assignee.name : 'Unassigned'} variant={assignee ? 'filled' : 'outlined'} />
          <Chip
            size="small"
            variant="outlined"
            icon={<CommentOutlined />}
            label={task.commentCount}
            aria-label={`${task.commentCount} comments`}
          />
        </Stack>
      </CardContent>
    </Card>
  );
}
