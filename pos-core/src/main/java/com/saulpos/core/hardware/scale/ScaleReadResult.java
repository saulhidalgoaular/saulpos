package com.saulpos.core.hardware.scale;

import java.math.BigDecimal;

public record ScaleReadResult(
        ScaleReadStatus status,
        BigDecimal weight,
        String unit,
        boolean stable,
        boolean retryable,
        String message
) {

    public static ScaleReadResult success(BigDecimal weight, String unit, boolean stable) {
        return new ScaleReadResult(ScaleReadStatus.SUCCESS, weight, unit, stable, false, "scale read successful");
    }

    public static ScaleReadResult failed(boolean retryable, String message) {
        return new ScaleReadResult(ScaleReadStatus.FAILED, null, null, false, retryable, message);
    }

    public static ScaleReadResult unsupported(String message) {
        return new ScaleReadResult(ScaleReadStatus.UNSUPPORTED, null, null, false, false, message);
    }
}
