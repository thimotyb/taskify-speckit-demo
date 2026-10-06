package com.taskify.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A project: a named workspace displayed as a Kanban board. Name is required, 1-100 characters
 * after trimming, and unique ignoring case; description is optional, up to 1,000 characters.
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Required by JPA. */
    protected Project() {
    }

    /**
     * Creates a project.
     *
     * @param id          unique identifier
     * @param name        trimmed name, 1-100 characters
     * @param description optional description, up to 1,000 characters
     * @param createdBy   id of the acting user
     * @param createdAt   creation time
     */
    public Project(UUID id, String name, String description, UUID createdBy, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
