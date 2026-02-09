package com.saulpos.server.sale.web;

import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.server.sale.service.SaleCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Validated
public class SaleCheckoutController {

    private final SaleCheckoutService saleCheckoutService;

    @PostMapping("/checkout")
    public SaleCheckoutResponse checkout(@Valid @RequestBody SaleCheckoutRequest request) {
        return saleCheckoutService.checkout(request);
    }
}
