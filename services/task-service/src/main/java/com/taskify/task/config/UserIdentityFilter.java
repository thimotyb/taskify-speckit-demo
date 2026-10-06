package com.taskify.task.config;

import com.taskify.task.client.ProjectServiceClient;
import com.taskify.task.client.UpstreamUnavailableException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Identifies the acting user for {@code /api/**} requests (FR-017).
 *
 * <p>The {@code X-User-Id} header must be a UUID that project-service confirms as a predefined user,
 * checked on every request without caching. Unknown or missing users get 401; if project-service
 * cannot answer the request fails closed with 503. This is identification, not authentication.
 */
public class UserIdentityFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(UserIdentityFilter.class);

    private final ProjectServiceClient projectService;
    private final ProblemWriter problems;

    /**
     * Creates the filter.
     *
     * @param projectService client used to verify the identity
     * @param problems       problem response writer
     */
    public UserIdentityFilter(ProjectServiceClient projectService, ProblemWriter problems) {
        this.projectService = projectService;
        this.problems = problems;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        UUID userId = parse(request.getHeader("X-User-Id"));
        boolean known;
        try {
            known = userId != null && projectService.userExists(userId);
        } catch (UpstreamUnavailableException e) {
            LOG.warn("Could not verify user identity: {}", e.getMessage());
            problems.write(response, HttpStatus.SERVICE_UNAVAILABLE, "upstream-unavailable", "Service unavailable",
                    "The service cannot process this request right now. Please try again later.");
            return;
        }
        if (!known) {
            LOG.warn("Rejected request with missing or unknown user identity");
            problems.write(response, HttpStatus.UNAUTHORIZED, "unknown-user", "Unknown user",
                    "Select one of the predefined users to continue.");
            return;
        }
        ActingUser.set(request, userId);
        chain.doFilter(request, response);
    }

    private static UUID parse(String header) {
        if (header == null) {
            return null;
        }
        try {
            return UUID.fromString(header.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
