package com.saulpos.api.catalog;

import java.math.BigDecimal;

public record OpenPriceEntryValidationResponse(
        Long productId,
        String sku,
        BigDecimal enteredPrice,
        BigDecimal minAllowedPrice,
        BigDecimal maxAllowedPrice,
        boolean reasonRequired
) {
}
