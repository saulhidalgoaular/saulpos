package com.saulpos.api.supplier;

import java.util.List;

public record SupplierResponse(
        Long id,
        Long merchantId,
        String code,
        String name,
        String taxIdentifier,
        boolean active,
        List<SupplierContactResponse> contacts,
        SupplierTermsResponse terms
) {
}
