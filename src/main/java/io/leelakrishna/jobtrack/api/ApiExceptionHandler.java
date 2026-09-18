package io.leelakrishna.jobtrack.api;

import io.leelakrishna.jobtrack.service.ApplicationNotFoundException;
import io.leelakrishna.jobtrack.service.DuplicateApplicationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain failures to RFC 7807 problem responses.
 *
 * <p>Deliberately explicit: an invalid status transition is a 409, not a 500, and the message
 * says which transition was refused. A caller should never have to read server logs to find
 * out why a request failed.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ProblemDetail onNotFound(ApplicationNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Application not found", exception.getMessage());
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ProblemDetail onDuplicate(DuplicateApplicationException exception) {
        return problem(HttpStatus.CONFLICT, "Duplicate application", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail onIllegalTransition(IllegalStateException exception) {
        return problem(HttpStatus.CONFLICT, "Invalid status transition", exception.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail onConcurrentUpdate(ObjectOptimisticLockingFailureException exception) {
        log.warn("concurrent update rejected: {}", exception.getMessage());
        return problem(
                HttpStatus.CONFLICT,
                "Concurrent update",
                "this application was changed by someone else; re-read it and retry");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidationFailure(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Validation failed", "one or more fields are invalid");
        detail.setProperty("fields", fields);
        return detail;
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
