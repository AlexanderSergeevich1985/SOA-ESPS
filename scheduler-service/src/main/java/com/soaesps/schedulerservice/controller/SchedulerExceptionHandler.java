package com.soaesps.schedulerservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Converts controller-layer exceptions into RFC 7807 ProblemDetail responses.
 * Applies to every @RestController in the package.
 */
@RestControllerAdvice(assignableTypes = SchedulerController.class)
public class SchedulerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SchedulerExceptionHandler.class);

    /**
     * @Valid failed on @RequestBody (field-level constraint violations).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, errors);
        problem.setTitle("Validation failed");
        problem.setType(URI.create("https://soa-esps.local/errors/validation"));
        return problem;
    }

    /**
     * Request parameter could not be converted (e.g. bad date format).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' has invalid value: " + ex.getValue());
        problem.setTitle("Invalid parameter");
        problem.setType(URI.create("https://soa-esps.local/errors/parameter"));
        return problem;
    }

    /**
     * Catch-all for unexpected failures inside scheduler operations.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error in scheduler controller", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal scheduler error");
        problem.setTitle("Scheduler error");
        problem.setType(URI.create("https://soa-esps.local/errors/internal"));
        return problem;
    }
}