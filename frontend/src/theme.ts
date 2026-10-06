import { createTheme } from '@mui/material/styles';

/** MUI theme for Taskify. */
export const theme = createTheme({
  palette: { mode: 'light', primary: { main: '#3f51b5' }, background: { default: '#f4f5f7' } },
  shape: { borderRadius: 8 },
});
