package com.saulpos.server.catalog.service;

import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class CategoryHierarchyValidator {

    public void validateReparent(Long categoryId, Long newParentId, Map<Long, Long> parentByCategoryId) {
        if (newParentId == null) {
            return;
        }
        if (categoryId.equals(newParentId)) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "category cannot be parent of itself: " + categoryId);
        }

        Long cursor = newParentId;
        Set<Long> visited = new HashSet<>();
        while (cursor != null) {
            if (!visited.add(cursor)) {
                throw new BaseException(ErrorCode.CONFLICT, "category hierarchy contains an existing cycle");
            }
            if (categoryId.equals(cursor)) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "reparent would create a cycle for categoryId=%d parentId=%d".formatted(categoryId, newParentId));
            }
            cursor = parentByCategoryId.get(cursor);
        }
    }
}
