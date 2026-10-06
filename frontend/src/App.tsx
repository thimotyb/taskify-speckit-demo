import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { ProjectBoard } from './pages/ProjectBoard';
import { ProjectList } from './pages/ProjectList';
import { UserPicker } from './pages/UserPicker';
import { useActingUser } from './state/ActingUserContext';

/**
 * Route table. Pages other than the user picker require a selected user and redirect to the picker
 * otherwise.
 * @returns the routes element
 */
export function App() {
  const { userId } = useActingUser();
  return (
    <Routes>
      <Route path="/" element={<UserPicker />} />
      <Route element={userId ? <AppShell /> : <Navigate to="/" replace />}>
        <Route path="/projects" element={<ProjectList />} />
        <Route path="/projects/:projectId" element={<ProjectBoard />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
