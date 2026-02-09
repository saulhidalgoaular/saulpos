package com.saulpos.server.inventory.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class InventoryBalanceCalculator {

    public BigDecimal add(BigDecimal currentBalance, BigDecimal delta) {
        BigDecimal current = normalize(currentBalance);
        return normalize(current.add(normalize(delta)));
    }

    public BigDecimal sum(List<BigDecimal> deltas) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal delta : deltas) {
            total = add(total, delta);
        }
        return total;
    }

    public BigDecimal normalizeScale(BigDecimal value) {
        return normalize(value);
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
