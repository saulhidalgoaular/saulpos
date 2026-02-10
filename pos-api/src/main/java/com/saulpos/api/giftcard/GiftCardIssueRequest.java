package com.saulpos.api.giftcard;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GiftCardIssueRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        @NotNull(message = "customerId is required")
        Long customerId,
        @NotBlank(message = "cardNumber is required")
        @Size(max = 40, message = "cardNumber must be at most 40 characters")
        String cardNumber,
        @NotNull(message = "issuedAmount is required")
        @DecimalMin(value = "0.01", message = "issuedAmount must be greater than 0")
        BigDecimal issuedAmount,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
