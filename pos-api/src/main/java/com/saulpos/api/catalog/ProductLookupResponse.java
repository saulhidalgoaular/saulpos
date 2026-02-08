package com.saulpos.api.catalog;

public record ProductLookupResponse(
        Long productId,
        Long variantId,
        Long merchantId,
        String sku,
        String productName,
        String variantCode,
        String variantName,
        String barcode,
        ProductSaleMode saleMode,
        ProductUnitOfMeasure quantityUom,
        int quantityPrecision
) {
}
