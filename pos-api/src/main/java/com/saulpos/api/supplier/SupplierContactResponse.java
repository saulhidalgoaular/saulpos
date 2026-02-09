package com.saulpos.api.supplier;

public record SupplierContactResponse(
        Long id,
        SupplierContactType contactType,
        String contactValue,
        boolean primary,
        boolean active
) {
}
