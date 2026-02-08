package com.saulpos.api.catalog;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        Long merchantId,
        String code,
        String name,
        boolean active,
        Long parentId,
        List<CategoryTreeResponse> children
) {
}
