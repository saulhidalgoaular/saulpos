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
    INTERNAL_SERVER_ERROR("POS-5000", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
