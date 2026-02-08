package com.saulpos.api.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerContactRequest(
        @NotNull(message = "contactType is required")
        CustomerContactType contactType,
        @NotBlank(message = "contactValue is required")
        @Size(max = 120, message = "contactValue must be at most 120 characters")
        String contactValue,
        Boolean primary
) {
}
