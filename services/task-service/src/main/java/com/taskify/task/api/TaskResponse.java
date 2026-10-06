package com.taskify.task.api;

import com.taskify.task.domain.Task;
import com.taskify.task.domain.TaskStatus;
import com.taskify.task.service.TaskService.TaskWithCount;
import java.time.Instant;
import java.util.UUID;

/**
 * Public view of a task, including its comment count (FR-015).
 *
 * @param id           task id
 * @param projectId    owning project
 * @param title        title
 * @param description  optional description
 * @param status       Kanban column
 * @param assigneeId   optional assignee (a user id)
 * @param commentCount number of comments
 * @param createdBy    user who created it
 * @param createdAt    creation time
 * @param updatedAt    time of the last change
 * @param updatedBy    user who made the last change
 */
public record TaskResponse(UUID id, UUID projectId, String title, String description, TaskStatus status,
        UUID assigneeId, long commentCount, UUID createdBy, Instant createdAt, Instant updatedAt, UUID updatedBy) {

    /**
     * Maps a task and its comment count to a response.
     *
     * @param value the task with its comment count
     * @return the response
     */
    public static TaskResponse from(TaskWithCount value) {
        Task t = value.task();
        return new TaskResponse(t.getId(), t.getProjectId(), t.getTitle(), t.getDescription(), t.getStatus(),
                t.getAssigneeId(), value.commentCount(), t.getCreatedBy(), t.getCreatedAt(), t.getUpdatedAt(),
                t.getUpdatedBy());
    }
}
