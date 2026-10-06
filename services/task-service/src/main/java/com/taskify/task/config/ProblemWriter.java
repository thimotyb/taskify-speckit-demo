package com.taskify.task.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Writes RFC 9457 problem responses from servlet filters, where controller advice does not apply.
 * Bodies never contain stack traces or internal details.
 */
@Component
public class ProblemWriter {

    private final ObjectMapper mapper;

    /**
     * Creates the writer.
     *
     * @param mapper JSON mapper
     */
    public ProblemWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Writes a problem response.
     *
     * @param response the response to write to
     * @param status   HTTP status
     * @param code     short machine-readable problem code
     * @param title    short human-readable title
     * @param detail   safe, non-technical detail
     * @throws IOException if writing fails
     */
    public void write(HttpServletResponse response, HttpStatus status, String code, String title, String detail)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:taskify:problem:" + code));
        problem.setTitle(title);
        response.setStatus(status.value());
        response.setContentType("application/problem+json");
        mapper.writeValue(response.getOutputStream(), problem);
    }
}
