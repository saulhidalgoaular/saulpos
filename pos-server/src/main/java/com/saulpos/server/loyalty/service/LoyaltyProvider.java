package com.saulpos.server.loyalty.service;

public interface LoyaltyProvider {

    String providerCode();

    LoyaltyProviderResult earn(LoyaltyProviderEarnCommand command);

    LoyaltyProviderResult redeem(LoyaltyProviderRedeemCommand command);
}
