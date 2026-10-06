package com.taskify.project.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link Project}.
 */
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Lists all projects, oldest first.
     *
     * @return projects ordered by creation time, then id
     */
    List<Project> findAllByOrderByCreatedAtAscIdAsc();
}
