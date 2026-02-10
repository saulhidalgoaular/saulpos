package com.saulpos.api.giftcard;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GiftCardRedeemRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,
        Long saleId,
        Long saleReturnId,
        @Size(max = 80, message = "reference must be at most 80 characters")
        String reference,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {

    @AssertTrue(message = "exactly one of saleId or saleReturnId is required")
    public boolean hasExactlyOneContext() {
        return (saleId != null) ^ (saleReturnId != null);
    }
}
