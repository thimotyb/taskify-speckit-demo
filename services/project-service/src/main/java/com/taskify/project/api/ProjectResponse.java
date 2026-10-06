package com.taskify.project.api;

import com.taskify.project.domain.Project;
import java.time.Instant;
import java.util.UUID;

/**
 * Public view of a project.
 *
 * @param id          project id
 * @param name        project name
 * @param description optional description
 * @param createdBy   id of the user who created it
 * @param createdAt   creation time
 */
public record ProjectResponse(UUID id, String name, String description, UUID createdBy, Instant createdAt) {

    /**
     * Maps a project entity to its response.
     *
     * @param project the entity
     * @return the response
     */
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(),
                project.getCreatedBy(), project.getCreatedAt());
    }
}
