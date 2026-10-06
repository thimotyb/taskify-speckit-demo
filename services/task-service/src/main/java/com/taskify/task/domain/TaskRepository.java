package com.taskify.task.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link Task}.
 */
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Lists a project's tasks, oldest first.
     *
     * @param projectId project id
     * @return tasks ordered by creation time, then id
     */
    List<Task> findByProjectIdOrderByCreatedAtAscIdAsc(UUID projectId);
}
