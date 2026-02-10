package com.saulpos.server.hardware;

import com.saulpos.core.hardware.scanner.ScanRequest;
import com.saulpos.core.hardware.scanner.ScanStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpScannerAdapterTest {

    private final NoOpScannerAdapter adapter = new NoOpScannerAdapter();

    @Test
    void scanReturnsFailedWhenPayloadMissing() {
        var result = adapter.scan(new ScanRequest("TERM-1", "   "));

        assertThat(result.status()).isEqualTo(ScanStatus.FAILED);
        assertThat(result.retryable()).isFalse();
        assertThat(result.message()).isEqualTo("scan payload is required");
    }

    @Test
    void scanReturnsUnsupportedWhenPayloadProvided() {
        var result = adapter.scan(new ScanRequest("TERM-1", "0123456789012"));

        assertThat(result.status()).isEqualTo(ScanStatus.UNSUPPORTED);
        assertThat(result.retryable()).isFalse();
        assertThat(result.message()).isEqualTo("scanner integration is not configured");
    }
}
