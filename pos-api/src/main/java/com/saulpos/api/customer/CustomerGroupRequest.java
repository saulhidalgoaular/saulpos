package com.saulpos.api.customer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerGroupRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        @NotNull(message = "code is required")
        @Size(max = 80, message = "code must be at most 80 characters")
        String code,
        @NotNull(message = "name is required")
        @Size(max = 160, message = "name must be at most 160 characters")
        String name,
        Boolean active
) {
}
