import { Box, Card, CardActionArea, CardContent, Container, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { ErrorView, LoadingView } from '../components/StateViews';
import { ROLE_LABELS } from '../services/types';
import { listUsers } from '../services/usersApi';
import { useActingUser } from '../state/ActingUserContext';

/**
 * Start screen: choose which of the five predefined users you are. No credentials are needed in this phase.
 * @returns the page element
 */
export function UserPicker() {
  const { select } = useActingUser();
  const navigate = useNavigate();
  const users = useQuery({ queryKey: ['users'], queryFn: listUsers });

  return (
    <Container maxWidth="sm" sx={{ py: 6 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Welcome to Taskify
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Choose who you are to continue.
      </Typography>
      {users.isPending && <LoadingView />}
      {users.isError && <ErrorView message={users.error.message} />}
      <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'grid', gap: 1.5 }} aria-label="Users">
        {users.data?.map((user) => (
          <li key={user.id}>
            <Card variant="outlined">
              <CardActionArea
                onClick={() => {
                  select(user.id);
                  navigate('/projects');
                }}
              >
                <CardContent>
                  <Typography variant="subtitle1">{user.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {ROLE_LABELS[user.role]}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </li>
        ))}
      </Box>
    </Container>
  );
}
