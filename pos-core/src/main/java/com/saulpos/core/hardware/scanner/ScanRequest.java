package com.saulpos.core.hardware.scanner;

public record ScanRequest(
        String target,
        String payload
) {
}
