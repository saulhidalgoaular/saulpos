package com.saulpos.api.identity;

public record StoreLocationResponse(
        Long id,
        Long merchantId,
        String code,
        String name,
        boolean active
) {
}
