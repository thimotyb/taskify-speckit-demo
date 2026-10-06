package com.taskify.project.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Holder for the identity of the user acting in the current request.
 *
 * <p>The identity comes from the {@code X-User-Id} header and is verified by
 * {@link UserIdentityFilter}. This is identification, not authentication (see the constitution's
 * Early-Phase Identification Exception). This class and the filter are the only places that deal
 * with the raw identity, so real authentication can replace them later.
 */
public final class ActingUser {

    private static final String ATTRIBUTE = "taskify.actingUser";

    private ActingUser() {
    }

    /**
     * Records the verified acting user on the request.
     *
     * @param request the current request
     * @param userId  the verified user id
     */
    static void set(HttpServletRequest request, UUID userId) {
        request.setAttribute(ATTRIBUTE, userId);
    }

    /**
     * Returns the acting user of the current request.
     *
     * @return the verified user id
     * @throws IllegalStateException if called outside a verified request
     */
    public static UUID current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        Object value = attributes == null ? null : attributes.getAttribute(ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (value instanceof UUID id) {
            return id;
        }
        throw new IllegalStateException("No verified acting user on this request");
    }
}
