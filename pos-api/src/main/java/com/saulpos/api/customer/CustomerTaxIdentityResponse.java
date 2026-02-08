package com.saulpos.api.customer;

public record CustomerTaxIdentityResponse(
        Long id,
        String documentType,
        String documentValue,
        boolean active
) {
}
