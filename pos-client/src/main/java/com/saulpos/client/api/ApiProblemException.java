package com.saulpos.client.api;

public final class ApiProblemException extends RuntimeException {

    private final int status;
    private final String code;

    public ApiProblemException(int status, String code, String detail) {
        super(detail == null || detail.isBlank() ? "Request failed" : detail);
        this.status = status;
        this.code = code;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }
}
