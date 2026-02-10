package com.saulpos.server.hardware;

import com.saulpos.core.hardware.scale.ScaleReadRequest;
import com.saulpos.core.hardware.scale.ScaleReadStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpScaleAdapterTest {

    private final NoOpScaleAdapter adapter = new NoOpScaleAdapter();

    @Test
    void readReturnsFailedWhenTargetMissing() {
        var result = adapter.read(new ScaleReadRequest("  "));

        assertThat(result.status()).isEqualTo(ScaleReadStatus.FAILED);
        assertThat(result.retryable()).isFalse();
        assertThat(result.message()).isEqualTo("scale target is required");
    }

    @Test
    void readReturnsUnsupportedWhenTargetProvided() {
        var result = adapter.read(new ScaleReadRequest("SCALE-1"));

        assertThat(result.status()).isEqualTo(ScaleReadStatus.UNSUPPORTED);
        assertThat(result.retryable()).isFalse();
        assertThat(result.weight()).isNull();
        assertThat(result.message()).isEqualTo("scale integration is not configured");
    }
}
