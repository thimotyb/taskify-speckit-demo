import { Box, Card, CardActionArea, CardContent, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink } from 'react-router-dom';
import { EmptyView, ErrorView, LoadingView } from '../components/StateViews';
import { listProjects } from '../services/projectsApi';

/**
 * Lists all projects; each links to its board (FR-004).
 * @returns the page element
 */
export function ProjectList() {
  const projects = useQuery({ queryKey: ['projects'], queryFn: listProjects });

  return (
    <Box>
      <Typography variant="h5" component="h1" gutterBottom>
        Projects
      </Typography>
      {projects.isPending && <LoadingView />}
      {projects.isError && <ErrorView message={projects.error.message} />}
      {projects.data?.length === 0 && <EmptyView message="No projects yet" />}
      <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'grid', gap: 2, gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))' }} aria-label="Projects">
        {projects.data?.map((project) => (
          <li key={project.id}>
            <Card variant="outlined">
              <CardActionArea component={RouterLink} to={`/projects/${project.id}`}>
                <CardContent>
                  <Typography variant="h6">{project.name}</Typography>
                  {project.description && (
                    <Typography variant="body2" color="text.secondary">
                      {project.description}
                    </Typography>
                  )}
                </CardContent>
              </CardActionArea>
            </Card>
          </li>
        ))}
      </Box>
    </Box>
  );
}
