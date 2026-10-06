package com.taskify.project.config;

import com.taskify.project.service.ConflictException;
import com.taskify.project.service.NotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps every failure to an RFC 9457 problem response with a safe, non-technical message. Stack
 * traces and internal details are only logged, never returned.
 */
@RestControllerAdvice
public class ProblemDetailsAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemDetailsAdvice.class);

    private static ProblemDetail problem(HttpStatus status, String code, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:taskify:problem:" + code));
        problem.setTitle(title);
        return problem;
    }

    /**
     * Handles records that do not exist.
     *
     * @param e the exception
     * @return a 404 problem
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem(HttpStatus.NOT_FOUND, "not-found", "Not found", e.getMessage()));
    }

    /**
     * Handles conflicts with existing data.
     *
     * @param e the exception
     * @return a 409 problem
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> conflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem(HttpStatus.CONFLICT, "conflict", "Conflict", e.getMessage()));
    }

    /**
     * Handles malformed identifiers in the URL, which can never match a record.
     *
     * @param e the exception
     * @return a 404 problem
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> badIdentifier(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem(HttpStatus.NOT_FOUND, "not-found", "Not found", "The requested item was not found."));
    }

    /**
     * Last-resort handler: logs the failure and returns a generic 500 without internals.
     *
     * @param e the exception
     * @return a 500 problem
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(Exception e) {
        LOG.error("Unexpected failure", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Unexpected error",
                        "Something went wrong. Please try again later."));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map((FieldError f) -> Map.of("field", f.getField(),
                        "message", f.getDefaultMessage() == null ? "Invalid value" : f.getDefaultMessage()))
                .toList();
        LOG.warn("Validation rejected {} field(s)", errors.size());
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation", "Invalid input",
                "Some fields are not valid.");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        LOG.warn("Rejected malformed or unexpected request body");
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "malformed-body", "Invalid input",
                "The request body is missing, malformed, or contains unexpected fields."));
    }
}
