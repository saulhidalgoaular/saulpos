package com.saulpos.api.identity;

import jakarta.validation.constraints.NotNull;

public record StoreUserAssignmentRequest(
        @NotNull(message = "userId is required")
        Long userId,
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotNull(message = "roleId is required")
        Long roleId
) {
}
