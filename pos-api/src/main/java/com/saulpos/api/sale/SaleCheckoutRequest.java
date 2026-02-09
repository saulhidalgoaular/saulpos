package com.saulpos.api.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaleCheckoutRequest(
        @NotNull(message = "cartId is required")
        Long cartId,
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @NotEmpty(message = "payments is required")
        List<@Valid SaleCheckoutPaymentRequest> payments,
        Long customerId
) {

    public SaleCheckoutRequest(Long cartId,
                               Long cashierUserId,
                               Long terminalDeviceId,
                               List<@Valid SaleCheckoutPaymentRequest> payments) {
        this(cartId, cashierUserId, terminalDeviceId, payments, null);
    }
}
