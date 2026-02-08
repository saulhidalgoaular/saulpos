package com.saulpos.server.discount.web;

import com.saulpos.api.discount.DiscountApplyRequest;
import com.saulpos.api.discount.DiscountApplyResponse;
import com.saulpos.api.discount.DiscountPreviewRequest;
import com.saulpos.api.discount.DiscountPreviewResponse;
import com.saulpos.api.discount.DiscountRemoveResponse;
import com.saulpos.server.discount.service.DiscountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Validated
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public DiscountApplyResponse apply(@Valid @RequestBody DiscountApplyRequest request) {
        return discountService.apply(request);
    }

    @PostMapping("/{id}/remove")
    public DiscountRemoveResponse remove(@PathVariable("id") Long id,
                                         @RequestParam("storeLocationId")
                                         @NotNull(message = "storeLocationId is required") Long storeLocationId,
                                         @RequestParam("contextKey")
                                         @NotBlank(message = "contextKey is required") String contextKey) {
        return discountService.remove(id, storeLocationId, contextKey);
    }

    @PostMapping("/preview")
    public DiscountPreviewResponse preview(@Valid @RequestBody DiscountPreviewRequest request) {
        return discountService.preview(request);
    }
}
