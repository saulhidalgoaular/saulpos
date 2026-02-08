package com.saulpos.api.customer;

public record CustomerContactResponse(
        Long id,
        CustomerContactType contactType,
        String contactValue,
        boolean primary,
        boolean active
) {
}
