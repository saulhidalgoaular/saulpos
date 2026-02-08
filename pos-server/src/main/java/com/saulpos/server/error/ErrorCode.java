package com.saulpos.server.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_ERROR("POS-4001", "Validation failed for one or more fields", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("POS-4004", "The requested resource was not found", HttpStatus.NOT_FOUND),
    CONFLICT("POS-4009", "A conflict occurred with the current state of the resource", HttpStatus.CONFLICT),
    AUTHENTICATION_REQUIRED("POS-4010", "Authentication is required to access this resource", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_CREDENTIALS("POS-4011", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_LOCKED("POS-4012", "Account is temporarily locked", HttpStatus.LOCKED),
    AUTH_ACCOUNT_DISABLED("POS-4013", "Account is disabled", HttpStatus.FORBIDDEN),
    AUTH_TOKEN_INVALID("POS-4014", "Authentication token is invalid", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED("POS-4015", "Authentication token has expired", HttpStatus.UNAUTHORIZED),
    AUTH_FORBIDDEN("POS-4030", "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR("POS-5000", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
