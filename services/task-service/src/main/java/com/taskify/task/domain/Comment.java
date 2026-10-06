package com.taskify.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A comment on a task. Text is required, 1-1,000 characters after trimming. Comments are immutable
 * in this phase.
 */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Required by JPA. */
    protected Comment() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
