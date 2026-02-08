package com.saulpos.server.promotion.web;

import com.saulpos.api.promotion.PromotionEvaluateRequest;
import com.saulpos.api.promotion.PromotionEvaluateResponse;
import com.saulpos.server.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Validated
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/evaluate")
    public PromotionEvaluateResponse evaluate(@Valid @RequestBody PromotionEvaluateRequest request) {
        return promotionService.evaluate(request);
    }
}
