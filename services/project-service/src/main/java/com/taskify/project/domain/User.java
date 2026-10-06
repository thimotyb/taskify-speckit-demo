package com.taskify.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * One of the five predefined users. Seeded at first start and read-only afterwards.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    /** Display name, 1-100 characters. */
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /** Required by JPA. */
    protected User() {
    }

    /**
     * Creates a user.
     *
     * @param id   unique identifier
     * @param name display name, 1-100 characters
     * @param role role label
     */
    public User(UUID id, String name, UserRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }
}
