package com.saulpos.api.sale;

import com.saulpos.api.catalog.ProductSaleMode;

import java.math.BigDecimal;

public record SaleCartLineResponse(
        Long lineId,
        String lineKey,
        Long productId,
        String sku,
        String productName,
        ProductSaleMode saleMode,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount,
        String openPriceReason
) {
}
