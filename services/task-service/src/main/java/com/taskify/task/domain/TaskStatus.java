package com.taskify.task.domain;

/**
 * The four Kanban columns, in board order. A task may move from any status to any other (FR-009).
 */
public enum TaskStatus {
    /** Not started; the status of every new task. */
    TODO,
    /** Being worked on. */
    IN_PROGRESS,
    /** Waiting for review. */
    IN_REVIEW,
    /** Finished. */
    DONE
}
