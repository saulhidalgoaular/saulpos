package com.saulpos.server.catalog;

import com.saulpos.server.catalog.service.CategoryHierarchyValidator;
import com.saulpos.server.error.BaseException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryHierarchyValidatorTest {

    private final CategoryHierarchyValidator validator = new CategoryHierarchyValidator();

    @Test
    void allowsMovingCategoryToRoot() {
        assertThatCode(() -> validator.validateReparent(10L, null, Map.of(10L, 2L, 2L, 1L)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsCategoryBeingItsOwnParent() {
        assertThatThrownBy(() -> validator.validateReparent(5L, 5L, Map.of(5L, 1L)))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("category cannot be parent of itself");
    }

    @Test
    void rejectsCycleWhenParentIsADescendant() {
        Map<Long, Long> parentByCategoryId = new HashMap<>();
        parentByCategoryId.put(1L, null);
        parentByCategoryId.put(2L, 1L);
        parentByCategoryId.put(3L, 2L);

        assertThatThrownBy(() -> validator.validateReparent(1L, 3L, parentByCategoryId))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("reparent would create a cycle");
    }

    @Test
    void allowsValidReparentAcrossBranches() {
        Map<Long, Long> parentByCategoryId = new HashMap<>();
        parentByCategoryId.put(1L, null);
        parentByCategoryId.put(2L, 1L);
        parentByCategoryId.put(3L, null);
        parentByCategoryId.put(4L, 3L);

        assertThatCode(() -> validator.validateReparent(2L, 4L, parentByCategoryId))
                .doesNotThrowAnyException();
    }
}
