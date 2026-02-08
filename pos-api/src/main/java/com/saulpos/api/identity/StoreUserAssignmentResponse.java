package com.saulpos.api.identity;

public record StoreUserAssignmentResponse(
        Long id,
        Long userId,
        Long storeLocationId,
        Long roleId,
        boolean active
) {
}
