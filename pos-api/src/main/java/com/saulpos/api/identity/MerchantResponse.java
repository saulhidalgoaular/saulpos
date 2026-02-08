package com.saulpos.api.identity;

public record MerchantResponse(
        Long id,
        String code,
        String name,
        boolean active
) {
}
