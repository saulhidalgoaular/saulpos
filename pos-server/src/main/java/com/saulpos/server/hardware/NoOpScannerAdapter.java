package com.saulpos.server.hardware;

import com.saulpos.core.hardware.scanner.ScanRequest;
import com.saulpos.core.hardware.scanner.ScanResult;
import com.saulpos.core.hardware.scanner.ScannerAdapter;
import org.springframework.stereotype.Component;

@Component
public class NoOpScannerAdapter implements ScannerAdapter {

    @Override
    public ScanResult scan(ScanRequest request) {
        if (request == null || request.payload() == null || request.payload().isBlank()) {
            return ScanResult.failed(false, "scan payload is required");
        }
        return ScanResult.unsupported("scanner integration is not configured");
    }
}
