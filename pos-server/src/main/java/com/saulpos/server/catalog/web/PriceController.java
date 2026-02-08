package com.saulpos.server.catalog.web;

import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.server.catalog.service.PricingService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/catalog/prices")
@RequiredArgsConstructor
@Validated
public class PriceController {

    private final PricingService pricingService;

    @GetMapping("/resolve")
    public PriceResolutionResponse resolve(
            @RequestParam("storeLocationId")
            @NotNull(message = "storeLocationId is required")
            Long storeLocationId,
            @RequestParam("productId")
            @NotNull(message = "productId is required")
            Long productId,
            @RequestParam(value = "at", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant at) {
        return pricingService.resolvePrice(storeLocationId, productId, at);
    }
}
