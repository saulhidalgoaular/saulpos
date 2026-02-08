package com.saulpos.api.catalog;

import jakarta.validation.constraints.NotNull;

public record CategoryReparentRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        Long parentId
) {
}
