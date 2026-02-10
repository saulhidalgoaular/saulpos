package com.saulpos.server.inventory;

import com.saulpos.server.inventory.service.CostingCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CostingCalculatorTest {

    private final CostingCalculator calculator = new CostingCalculator();

    @Test
    void weightedAverageCostUsesCurrentStockAndIncomingCostDeterministically() {
        BigDecimal weighted = calculator.weightedAverageCost(
                new BigDecimal("4.000"),
                new BigDecimal("2.5000"),
                new BigDecimal("6.000"),
                new BigDecimal("3.5000"));

        assertThat(weighted).isEqualByComparingTo(new BigDecimal("3.1000"));
    }

    @Test
    void weightedAverageCostFallsBackToIncomingCostWhenCurrentOnHandIsZero() {
        BigDecimal weighted = calculator.weightedAverageCost(
                BigDecimal.ZERO,
                new BigDecimal("9.9999"),
                new BigDecimal("2.000"),
                new BigDecimal("1.2345"));

        assertThat(weighted).isEqualByComparingTo(new BigDecimal("1.2345"));
    }

    @Test
    void weightedAverageCostRejectsNonPositiveIncomingQuantity() {
        assertThatThrownBy(() -> calculator.weightedAverageCost(
                new BigDecimal("1.000"),
                new BigDecimal("1.1000"),
                BigDecimal.ZERO,
                new BigDecimal("2.5000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("receivedQuantity");
    }
}
