package com.taskify.task.service;

import com.taskify.task.client.ProjectServiceClient;
import com.taskify.task.domain.CommentRepository;
import com.taskify.task.domain.Task;
import com.taskify.task.domain.TaskRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Business logic for tasks. Every operation that names a project first checks it with
 * project-service (no caching), so an outage fails closed.
 */
@Service
public class TaskService {

    /**
     * A task together with its number of comments.
     *
     * @param task         the task
     * @param commentCount number of comments on it
     */
    public record TaskWithCount(Task task, long commentCount) {
    }

    private final TaskRepository tasks;
    private final CommentRepository comments;
    private final ProjectServiceClient projectService;

    /**
     * Creates the service.
     *
     * @param tasks          task repository
     * @param comments       comment repository
     * @param projectService client for project-service
     */
    public TaskService(TaskRepository tasks, CommentRepository comments, ProjectServiceClient projectService) {
        this.tasks = tasks;
        this.comments = comments;
        this.projectService = projectService;
    }

    /**
     * Lists a project's tasks with comment counts, oldest first.
     *
     * @param projectId project id
     * @return the project's tasks
     * @throws NotFoundException                               if the project does not exist
     * @throws com.taskify.task.client.UpstreamUnavailableException if project-service cannot answer
     */
    public List<TaskWithCount> listByProject(UUID projectId) {
        if (!projectService.projectExists(projectId)) {
            throw new NotFoundException("Project not found");
        }
        List<Task> found = tasks.findByProjectIdOrderByCreatedAtAscIdAsc(projectId);
        Map<UUID, Long> counts = new HashMap<>();
        if (!found.isEmpty()) {
            comments.countByTaskIds(found.stream().map(Task::getId).toList())
                    .forEach(c -> counts.put(c.getTaskId(), c.getTotal()));
        }
        return found.stream().map(t -> new TaskWithCount(t, counts.getOrDefault(t.getId(), 0L))).toList();
    }
}
