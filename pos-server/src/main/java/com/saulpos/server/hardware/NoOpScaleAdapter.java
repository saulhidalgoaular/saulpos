package com.saulpos.server.hardware;

import com.saulpos.core.hardware.scale.ScaleAdapter;
import com.saulpos.core.hardware.scale.ScaleReadRequest;
import com.saulpos.core.hardware.scale.ScaleReadResult;
import org.springframework.stereotype.Component;

@Component
public class NoOpScaleAdapter implements ScaleAdapter {

    @Override
    public ScaleReadResult read(ScaleReadRequest request) {
        if (request == null || request.target() == null || request.target().isBlank()) {
            return ScaleReadResult.failed(false, "scale target is required");
        }
        return ScaleReadResult.unsupported("scale integration is not configured");
    }
}
