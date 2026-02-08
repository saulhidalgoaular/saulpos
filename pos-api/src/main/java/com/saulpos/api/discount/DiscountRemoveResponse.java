package com.saulpos.api.discount;

import java.time.Instant;

public record DiscountRemoveResponse(
        Long id,
        boolean removed,
        String removedByUsername,
        Instant removedAt
) {
}
