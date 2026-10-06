package com.taskify.project.api;

import com.taskify.project.service.ProjectService;
import com.taskify.project.service.UserService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoints used by other Taskify services to check that users and projects exist.
 * Protected by the service token and never routed by the gateway.
 */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final ProjectService projects;
    private final UserService users;

    /**
     * Creates the controller.
     *
     * @param projects project service
     * @param users    user service
     */
    public InternalController(ProjectService projects, UserService users) {
        this.projects = projects;
        this.users = users;
    }

    /**
     * Existence check for a project.
     *
     * @param projectId project id
     * @return the project
     */
    @GetMapping("/projects/{projectId}")
    public ProjectResponse project(@PathVariable UUID projectId) {
        return ProjectResponse.from(projects.get(projectId));
    }

    /**
     * Existence check for a user.
     *
     * @param userId user id
     * @return the user
     */
    @GetMapping("/users/{userId}")
    public UserResponse user(@PathVariable UUID userId) {
        return UserResponse.from(users.get(userId));
    }
}
