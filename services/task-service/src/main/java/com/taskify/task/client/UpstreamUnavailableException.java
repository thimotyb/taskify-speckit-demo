package com.taskify.task.client;

/**
 * Raised when project-service cannot be reached or answers unexpectedly. Task-service fails closed:
 * the request is rejected with HTTP 503 and nothing changes.
 */
public class UpstreamUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message internal description (not shown to users)
     * @param cause   the underlying failure, or null
     */
    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
