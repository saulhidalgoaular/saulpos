package com.saulpos.server.inventory.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CostingCalculator {

    public BigDecimal normalizeCostScale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal weightedAverageCost(BigDecimal currentOnHand,
                                          BigDecimal currentWeightedAverageCost,
                                          BigDecimal receivedQuantity,
                                          BigDecimal receivedUnitCost) {
        BigDecimal normalizedCurrentOnHand = normalizeQuantity(currentOnHand);
        BigDecimal normalizedCurrentAverageCost = normalizeCostScale(currentWeightedAverageCost);
        BigDecimal normalizedReceivedQuantity = normalizeQuantity(receivedQuantity);
        BigDecimal normalizedReceivedUnitCost = normalizeCostScale(receivedUnitCost);

        if (normalizedReceivedQuantity.signum() <= 0) {
            throw new IllegalArgumentException("receivedQuantity must be greater than zero");
        }

        BigDecimal resultingOnHand = normalizedCurrentOnHand.add(normalizedReceivedQuantity);
        if (normalizedCurrentOnHand.signum() <= 0 || resultingOnHand.signum() <= 0) {
            return normalizedReceivedUnitCost;
        }

        BigDecimal existingValue = normalizedCurrentOnHand.multiply(normalizedCurrentAverageCost);
        BigDecimal incomingValue = normalizedReceivedQuantity.multiply(normalizedReceivedUnitCost);
        BigDecimal weightedAverage = existingValue.add(incomingValue)
                .divide(resultingOnHand, 8, RoundingMode.HALF_UP);
        return normalizeCostScale(weightedAverage);
    }

    private BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
