package com.saulpos.core.printing;

public record PrintResult(
        PrintStatus status,
        boolean retryable,
        String message
) {

    public static PrintResult success(String message) {
        return new PrintResult(PrintStatus.SUCCESS, false, message);
    }

    public static PrintResult failed(boolean retryable, String message) {
        return new PrintResult(PrintStatus.FAILED, retryable, message);
    }
}
