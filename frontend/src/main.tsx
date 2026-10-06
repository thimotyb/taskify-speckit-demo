import { CssBaseline, ThemeProvider } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { App } from './App';
import { ActingUserProvider } from './state/ActingUserContext';
import { theme } from './theme';

// Refetch when the window regains focus; no polling (real-time updates are out of scope).
const queryClient = new QueryClient({
  defaultOptions: { queries: { refetchOnWindowFocus: true, retry: 1 } },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        <ActingUserProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </ActingUserProvider>
      </QueryClientProvider>
    </ThemeProvider>
  </StrictMode>,
);
