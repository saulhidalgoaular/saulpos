package com.saulpos.api.customer;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomerGroupAssignmentRequest(
        @NotNull(message = "customerGroupIds is required")
        List<@NotNull(message = "customerGroupIds cannot contain null values") Long> customerGroupIds
) {
}
