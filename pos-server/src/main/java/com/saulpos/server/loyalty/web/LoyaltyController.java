package com.saulpos.server.loyalty.web;

import com.saulpos.api.loyalty.LoyaltyEarnRequest;
import com.saulpos.api.loyalty.LoyaltyOperationResponse;
import com.saulpos.api.loyalty.LoyaltyRedeemRequest;
import com.saulpos.server.loyalty.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Validated
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @PostMapping("/earn")
    public LoyaltyOperationResponse earn(@Valid @RequestBody LoyaltyEarnRequest request) {
        return loyaltyService.earn(request);
    }

    @PostMapping("/redeem")
    public LoyaltyOperationResponse redeem(@Valid @RequestBody LoyaltyRedeemRequest request) {
        return loyaltyService.redeem(request);
    }
}
