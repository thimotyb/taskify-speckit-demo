package com.taskify.project.service;

import com.taskify.project.domain.Project;
import com.taskify.project.domain.ProjectRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for projects.
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projects;

    /**
     * Creates the service.
     *
     * @param projects project repository
     */
    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    /**
     * Lists all projects, oldest first.
     *
     * @return all projects
     */
    public List<Project> list() {
        return projects.findAllByOrderByCreatedAtAscIdAsc();
    }

    /**
     * Finds one project.
     *
     * @param id project id
     * @return the project
     * @throws NotFoundException if the project does not exist
     */
    public Project get(UUID id) {
        return projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
    }
}
