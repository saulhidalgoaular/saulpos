package com.saulpos.server.catalog.service;

import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.catalog.ProductUnitOfMeasure;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Set;

@Component
public class ProductSaleModePolicyValidator {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final Set<ProductUnitOfMeasure> WEIGHT_UOMS = EnumSet.of(
            ProductUnitOfMeasure.KILOGRAM,
            ProductUnitOfMeasure.GRAM,
            ProductUnitOfMeasure.POUND);

    public NormalizedPolicy normalizePolicy(ProductSaleMode requestedSaleMode,
                                            ProductUnitOfMeasure requestedUom,
                                            Integer requestedPrecision,
                                            BigDecimal requestedOpenPriceMin,
                                            BigDecimal requestedOpenPriceMax,
                                            Boolean requestedOpenPriceRequiresReason) {
        ProductSaleMode saleMode = requestedSaleMode == null ? ProductSaleMode.UNIT : requestedSaleMode;
        ProductUnitOfMeasure quantityUom = requestedUom == null ? ProductUnitOfMeasure.UNIT : requestedUom;
        int quantityPrecision = resolvePrecision(saleMode, requestedPrecision);
        BigDecimal openPriceMin = normalizeMoney(requestedOpenPriceMin);
        BigDecimal openPriceMax = normalizeMoney(requestedOpenPriceMax);
        boolean openPriceRequiresReason = requestedOpenPriceRequiresReason != null && requestedOpenPriceRequiresReason;

        switch (saleMode) {
            case UNIT -> validateUnitPolicy(quantityUom, quantityPrecision, openPriceMin, openPriceMax, openPriceRequiresReason);
            case WEIGHT -> validateWeightPolicy(quantityUom, quantityPrecision, openPriceMin, openPriceMax, openPriceRequiresReason);
            case OPEN_PRICE -> validateOpenPricePolicy(quantityUom, quantityPrecision, openPriceMin, openPriceMax);
            default -> throw new BaseException(ErrorCode.VALIDATION_ERROR, "unsupported saleMode: " + saleMode);
        }

        return new NormalizedPolicy(saleMode, quantityUom, quantityPrecision, openPriceMin, openPriceMax, openPriceRequiresReason);
    }

    public BigDecimal validateOpenPriceEntry(BigDecimal enteredPrice,
                                             BigDecimal openPriceMin,
                                             BigDecimal openPriceMax,
                                             boolean openPriceRequiresReason,
                                             String reason) {
        if (enteredPrice == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "enteredPrice is required");
        }
        BigDecimal normalized = normalizeMoney(enteredPrice);
        if (normalized == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "enteredPrice is required");
        }
        if (openPriceMin != null && normalized.compareTo(openPriceMin) < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "enteredPrice must be greater than or equal to openPriceMin");
        }
        if (openPriceMax != null && normalized.compareTo(openPriceMax) > 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "enteredPrice must be less than or equal to openPriceMax");
        }
        if (openPriceRequiresReason && (reason == null || reason.trim().isEmpty())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "reason is required for this product");
        }
        return normalized;
    }

    private int resolvePrecision(ProductSaleMode saleMode, Integer requestedPrecision) {
        if (requestedPrecision == null) {
            return saleMode == ProductSaleMode.WEIGHT ? 3 : 0;
        }
        return requestedPrecision;
    }

    private void validateUnitPolicy(ProductUnitOfMeasure quantityUom,
                                    int quantityPrecision,
                                    BigDecimal openPriceMin,
                                    BigDecimal openPriceMax,
                                    boolean openPriceRequiresReason) {
        if (quantityUom != ProductUnitOfMeasure.UNIT) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "UNIT saleMode requires quantityUom=UNIT");
        }
        if (quantityPrecision != 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "UNIT saleMode requires quantityPrecision=0");
        }
        if (openPriceMin != null || openPriceMax != null || openPriceRequiresReason) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "UNIT saleMode does not allow open-price policy fields");
        }
    }

    private void validateWeightPolicy(ProductUnitOfMeasure quantityUom,
                                      int quantityPrecision,
                                      BigDecimal openPriceMin,
                                      BigDecimal openPriceMax,
                                      boolean openPriceRequiresReason) {
        if (!WEIGHT_UOMS.contains(quantityUom)) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "WEIGHT saleMode requires quantityUom in [KILOGRAM, GRAM, POUND]");
        }
        if (quantityPrecision < 1 || quantityPrecision > 3) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "WEIGHT saleMode requires quantityPrecision between 1 and 3");
        }
        if (openPriceMin != null || openPriceMax != null || openPriceRequiresReason) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "WEIGHT saleMode does not allow open-price policy fields");
        }
    }

    private void validateOpenPricePolicy(ProductUnitOfMeasure quantityUom,
                                         int quantityPrecision,
                                         BigDecimal openPriceMin,
                                         BigDecimal openPriceMax) {
        if (quantityUom != ProductUnitOfMeasure.UNIT) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "OPEN_PRICE saleMode requires quantityUom=UNIT");
        }
        if (quantityPrecision != 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "OPEN_PRICE saleMode requires quantityPrecision=0");
        }
        if (openPriceMin == null || openPriceMax == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "OPEN_PRICE saleMode requires openPriceMin and openPriceMax");
        }
        if (openPriceMin.compareTo(openPriceMax) > 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "openPriceMin must be less than or equal to openPriceMax");
        }
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(ZERO) < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount must be non-negative");
        }
        return normalized;
    }

    public record NormalizedPolicy(
            ProductSaleMode saleMode,
            ProductUnitOfMeasure quantityUom,
            int quantityPrecision,
            BigDecimal openPriceMin,
            BigDecimal openPriceMax,
            boolean openPriceRequiresReason
    ) {
    }
}
