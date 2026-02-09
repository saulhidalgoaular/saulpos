package com.saulpos.server.sale.service;

import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SaleReturnAmountAllocator {

    private static final int MONEY_SCALE = 2;
    private static final int QUANTITY_SCALE = 3;

    public Allocation allocate(BigDecimal soldQuantity,
                               BigDecimal soldNet,
                               BigDecimal soldTax,
                               BigDecimal soldGross,
                               BigDecimal returnedQuantity,
                               BigDecimal alreadyReturnedNet,
                               BigDecimal alreadyReturnedTax,
                               BigDecimal alreadyReturnedGross,
                               BigDecimal requestedQuantity) {
        BigDecimal normalizedSoldQuantity = normalizeQuantity(soldQuantity);
        BigDecimal normalizedRequestedQuantity = normalizeQuantity(requestedQuantity);
        BigDecimal normalizedReturnedQuantity = normalizeQuantity(returnedQuantity);

        if (normalizedSoldQuantity.signum() <= 0) {
            throw new BaseException(ErrorCode.CONFLICT, "sale line quantity must be positive");
        }

        BigDecimal availableQuantity = normalizedSoldQuantity.subtract(normalizedReturnedQuantity).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        if (normalizedRequestedQuantity.compareTo(availableQuantity) > 0) {
            throw new BaseException(ErrorCode.CONFLICT, "return quantity exceeds available quantity for sale line");
        }

        BigDecimal normalizedSoldNet = normalizeMoney(soldNet);
        BigDecimal normalizedSoldTax = normalizeMoney(soldTax);
        BigDecimal normalizedSoldGross = normalizeMoney(soldGross);

        BigDecimal normalizedReturnedNet = normalizeMoney(alreadyReturnedNet);
        BigDecimal normalizedReturnedTax = normalizeMoney(alreadyReturnedTax);
        BigDecimal normalizedReturnedGross = normalizeMoney(alreadyReturnedGross);

        BigDecimal availableNet = normalizedSoldNet.subtract(normalizedReturnedNet).max(zeroMoney());
        BigDecimal availableTax = normalizedSoldTax.subtract(normalizedReturnedTax).max(zeroMoney());
        BigDecimal availableGross = normalizedSoldGross.subtract(normalizedReturnedGross).max(zeroMoney());

        boolean fullRemainingReturn = normalizedRequestedQuantity.compareTo(availableQuantity) == 0;

        BigDecimal allocatedNet = fullRemainingReturn
                ? availableNet
                : prorate(normalizedSoldNet, normalizedRequestedQuantity, normalizedSoldQuantity).min(availableNet);
        BigDecimal allocatedTax = fullRemainingReturn
                ? availableTax
                : prorate(normalizedSoldTax, normalizedRequestedQuantity, normalizedSoldQuantity).min(availableTax);
        BigDecimal allocatedGross = fullRemainingReturn
                ? availableGross
                : prorate(normalizedSoldGross, normalizedRequestedQuantity, normalizedSoldQuantity).min(availableGross);

        return new Allocation(
                normalizeQuantity(normalizedRequestedQuantity),
                normalizeMoney(allocatedNet),
                normalizeMoney(allocatedTax),
                normalizeMoney(allocatedGross));
    }

    private BigDecimal prorate(BigDecimal total, BigDecimal requestedQuantity, BigDecimal soldQuantity) {
        if (total.signum() == 0) {
            return zeroMoney();
        }
        return total.multiply(requestedQuantity)
                .divide(soldQuantity, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return BigDecimal.ZERO.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        }
        return quantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return zeroMoney();
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public record Allocation(
            BigDecimal quantity,
            BigDecimal netAmount,
            BigDecimal taxAmount,
            BigDecimal grossAmount
    ) {
    }
}
