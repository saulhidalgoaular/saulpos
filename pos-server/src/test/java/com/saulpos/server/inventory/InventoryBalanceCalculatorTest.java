package com.saulpos.server.inventory;

import com.saulpos.server.inventory.service.InventoryBalanceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryBalanceCalculatorTest {

    private final InventoryBalanceCalculator calculator = new InventoryBalanceCalculator();

    @Test
    void sumProducesDeterministicThreeDecimalBalance() {
        BigDecimal result = calculator.sum(List.of(
                new BigDecimal("10"),
                new BigDecimal("-2.125"),
                new BigDecimal("0.333"),
                new BigDecimal("-0.008")));

        assertThat(result).isEqualByComparingTo(new BigDecimal("8.200"));
    }

    @Test
    void addNormalizesNullAndScale() {
        BigDecimal result = calculator.add(null, new BigDecimal("1.2"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("1.200"));

        BigDecimal next = calculator.add(result, new BigDecimal("-0.105"));
        assertThat(next).isEqualByComparingTo(new BigDecimal("1.095"));
    }
}
