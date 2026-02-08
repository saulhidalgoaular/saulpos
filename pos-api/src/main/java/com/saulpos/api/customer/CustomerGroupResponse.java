package com.saulpos.api.customer;

public record CustomerGroupResponse(
        Long id,
        Long merchantId,
        String code,
        String name,
        boolean active
) {
}
