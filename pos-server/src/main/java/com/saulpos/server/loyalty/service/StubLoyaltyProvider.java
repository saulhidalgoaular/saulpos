package com.saulpos.server.loyalty.service;

import com.saulpos.api.loyalty.LoyaltyOperationStatus;

import java.math.RoundingMode;

public class StubLoyaltyProvider implements LoyaltyProvider {

    private static final int STUB_REDEEM_LIMIT = 500;

    @Override
    public String providerCode() {
        return "STUB";
    }

    @Override
    public LoyaltyProviderResult earn(LoyaltyProviderEarnCommand command) {
        int points = command.saleGrossAmount()
                .setScale(0, RoundingMode.DOWN)
                .intValue();
        if (points <= 0) {
            points = 1;
        }
        return new LoyaltyProviderResult(
                LoyaltyOperationStatus.APPLIED,
                points,
                command.reference(),
                "stub loyalty earn applied");
    }

    @Override
    public LoyaltyProviderResult redeem(LoyaltyProviderRedeemCommand command) {
        if (command.requestedPoints() > STUB_REDEEM_LIMIT) {
            return new LoyaltyProviderResult(
                    LoyaltyOperationStatus.REJECTED,
                    0,
                    null,
                    "requested points exceed stub limit");
        }
        return new LoyaltyProviderResult(
                LoyaltyOperationStatus.APPLIED,
                -command.requestedPoints(),
                command.reference(),
                "stub loyalty redeem applied");
    }
}
