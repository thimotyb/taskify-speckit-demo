package com.taskify.project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protects {@code /internal/**}: the {@code X-Service-Token} header must match the configured
 * shared secret. Fails closed when no secret is configured. The token is never logged.
 */
public class ServiceTokenFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceTokenFilter.class);

    private final byte[] expected;
    private final ProblemWriter problems;

    /**
     * Creates the filter.
     *
     * @param token    configured shared secret; empty rejects every internal call
     * @param problems problem response writer
     */
    public ServiceTokenFilter(String token, ProblemWriter problems) {
        this.expected = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
        this.problems = problems;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String supplied = request.getHeader("X-Service-Token");
        boolean valid = expected.length > 0 && supplied != null
                && MessageDigest.isEqual(expected, supplied.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            LOG.warn("Rejected internal request with missing or invalid service token");
            problems.write(response, HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized",
                    "A valid service token is required.");
            return;
        }
        chain.doFilter(request, response);
    }
}
