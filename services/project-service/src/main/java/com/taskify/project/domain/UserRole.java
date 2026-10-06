package com.taskify.project.domain;

/**
 * Role label of a predefined user. Roles are labels only: all users have the same permissions
 * in this phase (FR-014).
 */
public enum UserRole {
    /** The product manager. */
    PRODUCT_MANAGER,
    /** An engineer. */
    ENGINEER
}
