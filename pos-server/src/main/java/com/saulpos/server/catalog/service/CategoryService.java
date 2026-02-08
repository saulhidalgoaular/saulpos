package com.saulpos.server.catalog.service;

import com.saulpos.api.catalog.CategoryReparentRequest;
import com.saulpos.api.catalog.CategoryTreeResponse;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Comparator<CategoryEntity> CATEGORY_ORDER = Comparator
            .comparing(CategoryEntity::getName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CategoryEntity::getCode, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CategoryEntity::getId);

    private final CategoryRepository categoryRepository;
    private final MerchantRepository merchantRepository;
    private final CategoryHierarchyValidator categoryHierarchyValidator;

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree(Long merchantId) {
        requireMerchant(merchantId);
        List<CategoryEntity> categories = loadMerchantCategories(merchantId);
        return buildTree(categories);
    }

    @Transactional
    public CategoryTreeResponse reparentCategory(Long categoryId, CategoryReparentRequest request) {
        Long merchantId = request.merchantId();
        requireMerchant(merchantId);

        CategoryEntity category = requireCategory(categoryId, merchantId);
        CategoryEntity newParent = null;
        if (request.parentId() != null) {
            newParent = requireCategory(request.parentId(), merchantId);
        }

        List<CategoryEntity> categories = loadMerchantCategories(merchantId);
        Map<Long, Long> parentByCategoryId = toParentLookup(categories);
        categoryHierarchyValidator.validateReparent(category.getId(), request.parentId(), parentByCategoryId);

        category.setParent(newParent);
        categoryRepository.save(category);

        List<CategoryEntity> refreshedCategories = loadMerchantCategories(merchantId);
        Map<Long, List<CategoryEntity>> childrenByParentId = indexChildrenByParentId(refreshedCategories);
        Map<Long, CategoryEntity> categoriesById = toCategoryIndex(refreshedCategories);
        return buildNode(requireFromMap(categoriesById, categoryId), childrenByParentId, new ArrayDeque<>());
    }

    private List<CategoryEntity> loadMerchantCategories(Long merchantId) {
        return categoryRepository.findByMerchantId(merchantId).stream()
                .sorted(CATEGORY_ORDER)
                .toList();
    }

    private List<CategoryTreeResponse> buildTree(List<CategoryEntity> categories) {
        Map<Long, List<CategoryEntity>> childrenByParentId = indexChildrenByParentId(categories);
        List<CategoryEntity> roots = childrenByParentId.getOrDefault(null, List.of());
        return roots.stream()
                .map(root -> buildNode(root, childrenByParentId, new ArrayDeque<>()))
                .toList();
    }

    private Map<Long, List<CategoryEntity>> indexChildrenByParentId(List<CategoryEntity> categories) {
        Map<Long, List<CategoryEntity>> indexed = new LinkedHashMap<>();
        for (CategoryEntity category : categories) {
            Long parentId = category.getParent() != null ? category.getParent().getId() : null;
            indexed.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(category);
        }
        for (List<CategoryEntity> children : indexed.values()) {
            children.sort(CATEGORY_ORDER);
        }
        return indexed;
    }

    private CategoryTreeResponse buildNode(CategoryEntity category,
                                           Map<Long, List<CategoryEntity>> childrenByParentId,
                                           Deque<Long> path) {
        if (path.contains(category.getId())) {
            throw new BaseException(ErrorCode.CONFLICT, "category hierarchy contains an existing cycle");
        }

        path.push(category.getId());
        List<CategoryTreeResponse> children = childrenByParentId.getOrDefault(category.getId(), List.of()).stream()
                .map(child -> buildNode(child, childrenByParentId, path))
                .toList();
        path.pop();

        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        return new CategoryTreeResponse(
                category.getId(),
                category.getMerchant().getId(),
                category.getCode(),
                category.getName(),
                category.isActive(),
                parentId,
                children);
    }

    private Map<Long, Long> toParentLookup(List<CategoryEntity> categories) {
        Map<Long, Long> lookup = new HashMap<>();
        for (CategoryEntity category : categories) {
            lookup.put(category.getId(), category.getParent() != null ? category.getParent().getId() : null);
        }
        return lookup;
    }

    private Map<Long, CategoryEntity> toCategoryIndex(List<CategoryEntity> categories) {
        Map<Long, CategoryEntity> indexed = new LinkedHashMap<>();
        for (CategoryEntity category : categories) {
            indexed.put(category.getId(), category);
        }
        return indexed;
    }

    private CategoryEntity requireFromMap(Map<Long, CategoryEntity> categoriesById, Long categoryId) {
        CategoryEntity category = categoriesById.get(categoryId);
        if (category == null) {
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "category not found: " + categoryId);
        }
        return category;
    }

    private CategoryEntity requireCategory(Long categoryId, Long merchantId) {
        return categoryRepository.findByIdAndMerchantId(categoryId, merchantId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "category not found for merchantId=%d categoryId=%d".formatted(merchantId, categoryId)));
    }

    private void requireMerchant(Long merchantId) {
        merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "merchant not found: " + merchantId));
    }
}
