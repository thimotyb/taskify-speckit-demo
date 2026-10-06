package com.taskify.project.service;

/**
 * Raised when a requested record does not exist. Mapped to HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message safe, user-facing message
     */
    public NotFoundException(String message) {
        super(message);
    }
}
