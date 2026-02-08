package com.saulpos.api.tax;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TaxPreviewResponse(
        Long storeLocationId,
        Instant at,
        List<TaxPreviewLineResponse> lines,
        BigDecimal subtotalNet,
        BigDecimal totalTax,
        BigDecimal totalGross
) {
}
