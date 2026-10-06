package com.taskify.project.api;

import com.taskify.project.service.ProjectService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for projects.
 */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projects;

    /**
     * Creates the controller.
     *
     * @param projects project service
     */
    public ProjectController(ProjectService projects) {
        this.projects = projects;
    }

    /**
     * Lists all projects, oldest first.
     *
     * @return all projects
     */
    @GetMapping
    public List<ProjectResponse> list() {
        return projects.list().stream().map(ProjectResponse::from).toList();
    }

    /**
     * Gets one project.
     *
     * @param projectId project id
     * @return the project
     */
    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable UUID projectId) {
        return ProjectResponse.from(projects.get(projectId));
    }
}
