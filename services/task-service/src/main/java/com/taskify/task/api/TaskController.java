package com.taskify.task.api;

import com.taskify.task.service.TaskService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for tasks.
 */
@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private final TaskService tasks;

    /**
     * Creates the controller.
     *
     * @param tasks task service
     */
    public TaskController(TaskService tasks) {
        this.tasks = tasks;
    }

    /**
     * Lists a project's tasks for the board, ordered by creation time within each status.
     *
     * @param projectId project id
     * @return the project's tasks with comment counts
     */
    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> list(@PathVariable UUID projectId) {
        return tasks.listByProject(projectId).stream().map(TaskResponse::from).toList();
    }
}
