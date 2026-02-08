package com.saulpos.server.error;

import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String CORRELATION_ID_LOG_VAR = "correlationId";

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException ex, WebRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), ex.getMessage());
        problemDetail.setTitle(errorCode.getMessage());
        problemDetail.setProperty("code", errorCode.getCode());
        addCorrelationId(problemDetail);

        return createResponseEntity(problemDetail, null, errorCode.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setTitle(ErrorCode.VALIDATION_ERROR.getMessage());
        problemDetail.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        org.springframework.validation.FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing));

        problemDetail.setProperty("errors", errors);
        addCorrelationId(problemDetail);

        return createResponseEntity(problemDetail, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        problemDetail.setProperty("code", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        addCorrelationId(problemDetail);

        // Log the exception with correlation ID
        logger.error("Unhandled exception [CorrelationID: " + MDC.get(CORRELATION_ID_LOG_VAR) + "]", ex);

        return createResponseEntity(problemDetail, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                ErrorCode.AUTH_FORBIDDEN.getStatus(),
                ErrorCode.AUTH_FORBIDDEN.getMessage());
        problemDetail.setTitle(ErrorCode.AUTH_FORBIDDEN.getMessage());
        problemDetail.setProperty("code", ErrorCode.AUTH_FORBIDDEN.getCode());
        addCorrelationId(problemDetail);
        return createResponseEntity(problemDetail, null, ErrorCode.AUTH_FORBIDDEN.getStatus(), request);
    }

    private void addCorrelationId(ProblemDetail problemDetail) {
        String correlationId = MDC.get(CORRELATION_ID_LOG_VAR);
        if (correlationId != null) {
            problemDetail.setProperty("correlationId", correlationId);
        }
    }
}
