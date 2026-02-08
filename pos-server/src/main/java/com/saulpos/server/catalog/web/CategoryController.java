package com.saulpos.server.catalog.web;

import com.saulpos.api.catalog.CategoryReparentRequest;
import com.saulpos.api.catalog.CategoryTreeResponse;
import com.saulpos.server.catalog.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public List<CategoryTreeResponse> tree(
            @RequestParam("merchantId") @NotNull(message = "merchantId is required") Long merchantId) {
        return categoryService.getCategoryTree(merchantId);
    }

    @PostMapping("/{id}/reparent")
    public CategoryTreeResponse reparent(@PathVariable("id") Long id,
                                         @Valid @RequestBody CategoryReparentRequest request) {
        return categoryService.reparentCategory(id, request);
    }
}
