package com.saulpos.server.sale.service;

import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component
public class CartLinePolicyValidator {

    private static final int QUANTITY_SCALE = 3;
    private static final int MONEY_SCALE = 2;

    public BigDecimal normalizeQuantity(ProductEntity product, BigDecimal requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "quantity must be greater than zero");
        }

        BigDecimal stripped = requestedQuantity.stripTrailingZeros();
        int scale = Math.max(stripped.scale(), 0);
        ProductSaleMode saleMode = product.getSaleMode();

        if ((saleMode == ProductSaleMode.UNIT || saleMode == ProductSaleMode.OPEN_PRICE) && scale > 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    saleMode + " products require whole quantity values");
        }

        if (saleMode == ProductSaleMode.WEIGHT && scale > product.getQuantityPrecision()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "quantity precision exceeds product policy for productId=" + product.getId());
        }

        return requestedQuantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal normalizeUnitPrice(BigDecimal requestedUnitPrice) {
        if (requestedUnitPrice == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "unitPrice is required");
        }
        if (requestedUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "unitPrice must be non-negative");
        }
        return requestedUnitPrice.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public String normalizeLineKey(String lineKey) {
        if (lineKey == null) {
            return null;
        }

        String normalized = lineKey.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > 64) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lineKey must be at most 64 characters");
        }

        return normalized.toUpperCase(Locale.ROOT);
    }

    public String normalizeOpenPriceReason(String openPriceReason) {
        if (openPriceReason == null) {
            return null;
        }

        String normalized = openPriceReason.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > 255) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "openPriceReason must be at most 255 characters");
        }

        return normalized;
    }

    public BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount is required");
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
