package com.saulpos.api.discount;

import com.saulpos.api.tax.TaxPreviewResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DiscountPreviewResponse(
        Long storeLocationId,
        String contextKey,
        Instant at,
        List<DiscountPreviewLineResponse> lines,
        List<DiscountPreviewAppliedResponse> appliedDiscounts,
        BigDecimal subtotalBeforeDiscount,
        BigDecimal totalDiscount,
        BigDecimal subtotalAfterDiscount,
        TaxPreviewResponse taxPreview
) {
}
