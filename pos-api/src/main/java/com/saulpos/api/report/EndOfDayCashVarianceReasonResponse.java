package com.saulpos.api.report;

public record EndOfDayCashVarianceReasonResponse(
        String reason,
        long count
) {
}
