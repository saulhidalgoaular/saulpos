package com.saulpos.api.supplier;

import java.math.BigDecimal;

public record SupplierTermsResponse(
        Long id,
        Integer paymentTermDays,
        BigDecimal creditLimit,
        String notes
) {
}
