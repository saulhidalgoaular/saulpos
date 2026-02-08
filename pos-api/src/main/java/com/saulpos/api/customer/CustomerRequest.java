package com.saulpos.api.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CustomerRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        @Size(max = 160, message = "displayName must be at most 160 characters")
        String displayName,
        Boolean invoiceRequired,
        Boolean creditEnabled,
        List<@Valid CustomerTaxIdentityRequest> taxIdentities,
        List<@Valid CustomerContactRequest> contacts
) {
}
