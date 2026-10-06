import { AppBar, Box, Button, Chip, Container, Toolbar, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import { listUsers } from '../services/usersApi';
import { useActingUser } from '../state/ActingUserContext';

/**
 * Page frame with the app bar, the acting user, and the user switcher.
 * @returns the layout element
 */
export function AppShell() {
  const { userId, clear } = useActingUser();
  const navigate = useNavigate();
  const users = useQuery({ queryKey: ['users'], queryFn: listUsers });
  const current = users.data?.find((u) => u.id === userId);

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" color="primary" elevation={0}>
        <Toolbar>
          <Typography variant="h6" component={RouterLink} to="/projects" sx={{ color: 'inherit', textDecoration: 'none', flexGrow: 1 }}>
            Taskify
          </Typography>
          {current && <Chip label={`Acting as ${current.name}`} sx={{ mr: 2, bgcolor: 'rgba(255,255,255,0.2)', color: 'inherit' }} />}
          <Button
            color="inherit"
            onClick={() => {
              clear();
              navigate('/');
            }}
          >
            Switch user
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth={false} sx={{ py: 3 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
