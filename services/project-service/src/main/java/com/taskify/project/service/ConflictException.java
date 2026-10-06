package com.taskify.project.service;

/**
 * Raised when a request conflicts with existing data, such as a duplicate project name.
 * Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message safe, user-facing message
     */
    public ConflictException(String message) {
        super(message);
    }
}
