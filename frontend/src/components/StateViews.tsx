import { Alert, Box, CircularProgress, Typography } from '@mui/material';

/**
 * Loading indicator.
 * @returns the loading view
 */
export function LoadingView() {
  return (
    <Box role="status" aria-label="Loading" sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
      <CircularProgress />
    </Box>
  );
}

/**
 * Message shown when a list has no entries.
 * @param props the message to show
 * @returns the empty view
 */
export function EmptyView({ message }: { message: string }) {
  return (
    <Typography variant="body2" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
      {message}
    </Typography>
  );
}

/**
 * Error message with a safe, user-facing text.
 * @param props the message to show
 * @returns the error view
 */
export function ErrorView({ message }: { message: string }) {
  return (
    <Alert severity="error" sx={{ m: 2 }}>
      {message}
    </Alert>
  );
}

/**
 * Message for a project or task that does not exist.
 * @param props what was not found
 * @returns the not-found view
 */
export function NotFoundView({ what }: { what: string }) {
  return (
    <Alert severity="warning" sx={{ m: 2 }}>
      {what} was not found. It may have been removed.
    </Alert>
  );
}
