package com.taskify.project.config;

import com.taskify.project.domain.UserRepository;
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
 * <p>The {@code X-User-Id} header must be a UUID that matches one of the seeded users; otherwise the
 * request is rejected with 401 and no data is returned or changed. The one exception is
 * {@code GET /api/v1/users}, the list of the five predefined users, which a client needs in order to
 * choose an identity in the first place. This is identification, not authentication.
 */
public class UserIdentityFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(UserIdentityFilter.class);

    private final UserRepository users;
    private final ProblemWriter problems;

    /**
     * Creates the filter.
     *
     * @param users    user repository used to verify the identity
     * @param problems problem response writer
     */
    public UserIdentityFilter(UserRepository users, ProblemWriter problems) {
        this.users = users;
        this.problems = problems;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean userList = "GET".equals(request.getMethod()) && "/api/v1/users".equals(path);
        return !path.startsWith("/api/") || userList;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        UUID userId = parse(request.getHeader("X-User-Id"));
        if (userId == null || !users.existsById(userId)) {
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
