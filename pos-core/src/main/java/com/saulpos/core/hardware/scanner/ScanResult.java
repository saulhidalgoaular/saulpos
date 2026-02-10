package com.saulpos.core.hardware.scanner;

public record ScanResult(
        ScanStatus status,
        String value,
        String symbology,
        boolean retryable,
        String message
) {

    public static ScanResult success(String value, String symbology) {
        return new ScanResult(ScanStatus.SUCCESS, value, symbology, false, "scan successful");
    }

    public static ScanResult failed(boolean retryable, String message) {
        return new ScanResult(ScanStatus.FAILED, null, null, retryable, message);
    }

    public static ScanResult unsupported(String message) {
        return new ScanResult(ScanStatus.UNSUPPORTED, null, null, false, message);
    }
}
