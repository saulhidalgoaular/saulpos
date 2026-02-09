package com.saulpos.server.sale.service;

import com.saulpos.api.sale.SaleCheckoutPaymentRequest;
import com.saulpos.api.tax.TenderType;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentAllocationValidator {

    private static final int MONEY_SCALE = 2;

    public ValidationResult validate(BigDecimal payableAmount, List<SaleCheckoutPaymentRequest> paymentRequests) {
        BigDecimal payable = normalizeNonNegativeMoney(payableAmount, "total payable amount");

        if (paymentRequests == null || paymentRequests.isEmpty()) {
            throw validation("payments is required");
        }

        List<ValidatedPayment> validatedPayments = new ArrayList<>(paymentRequests.size());
        BigDecimal totalAllocated = zeroMoney();
        BigDecimal totalTendered = zeroMoney();
        BigDecimal totalChange = zeroMoney();
        boolean hasCashAllocation = false;

        int sequence = 1;
        for (SaleCheckoutPaymentRequest request : paymentRequests) {
            if (request == null) {
                throw validation("payment allocation entry is required");
            }
            if (request.tenderType() == null) {
                throw validation("tenderType is required");
            }

            BigDecimal amount = normalizePositiveMoney(request.amount(), "payment amount");
            BigDecimal tenderedAmount;
            BigDecimal changeAmount;

            if (request.tenderType() == TenderType.CASH) {
                hasCashAllocation = true;
                tenderedAmount = request.tenderedAmount() == null
                        ? amount
                        : normalizeMoney(request.tenderedAmount(), "cash tenderedAmount");
                if (tenderedAmount.compareTo(amount) < 0) {
                    throw validation("cash tenderedAmount cannot be less than allocated amount");
                }
                changeAmount = tenderedAmount.subtract(amount);
            } else {
                if (request.tenderedAmount() != null) {
                    BigDecimal suppliedTenderedAmount = normalizeMoney(request.tenderedAmount(), "card tenderedAmount");
                    if (suppliedTenderedAmount.compareTo(amount) != 0) {
                        throw validation("non-cash tenderedAmount must equal allocated amount");
                    }
                }
                tenderedAmount = amount;
                changeAmount = zeroMoney();
            }

            ValidatedPayment validatedPayment = new ValidatedPayment(
                    sequence++,
                    request.tenderType(),
                    amount,
                    tenderedAmount,
                    changeAmount,
                    normalizeReference(request.reference()));
            validatedPayments.add(validatedPayment);

            totalAllocated = totalAllocated.add(amount);
            totalTendered = totalTendered.add(tenderedAmount);
            totalChange = totalChange.add(changeAmount);
        }

        if (totalAllocated.compareTo(payable) != 0) {
            throw validation("sum of payment allocations must equal total payable");
        }

        BigDecimal expectedChange = totalTendered.subtract(payable);
        if (expectedChange.compareTo(zeroMoney()) < 0) {
            throw validation("sum of tendered amounts cannot be less than total payable");
        }
        if (expectedChange.compareTo(zeroMoney()) > 0 && !hasCashAllocation) {
            throw validation("change is only allowed when at least one CASH allocation is present");
        }
        if (expectedChange.compareTo(totalChange) != 0) {
            throw validation("computed change does not match allocation change totals");
        }

        return new ValidationResult(payable, totalAllocated, totalTendered, totalChange, List.copyOf(validatedPayments));
    }

    private BigDecimal normalizePositiveMoney(BigDecimal value, String fieldName) {
        BigDecimal normalized = normalizeMoney(value, fieldName);
        if (normalized.compareTo(zeroMoney()) <= 0) {
            throw validation(fieldName + " must be greater than zero");
        }
        return normalized;
    }

    private BigDecimal normalizeNonNegativeMoney(BigDecimal value, String fieldName) {
        BigDecimal normalized = normalizeMoney(value, fieldName);
        if (normalized.compareTo(zeroMoney()) < 0) {
            throw validation(fieldName + " cannot be negative");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value, String fieldName) {
        if (value == null) {
            throw validation(fieldName + " is required");
        }
        if (value.scale() > MONEY_SCALE) {
            throw validation(fieldName + " has too many decimal places");
        }
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String normalizeReference(String reference) {
        if (reference == null) {
            return null;
        }
        String normalized = reference.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BaseException validation(String message) {
        return new BaseException(ErrorCode.VALIDATION_ERROR, message);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public record ValidatedPayment(
            int sequenceNumber,
            TenderType tenderType,
            BigDecimal amount,
            BigDecimal tenderedAmount,
            BigDecimal changeAmount,
            String reference
    ) {
    }

    public record ValidationResult(
            BigDecimal totalPayable,
            BigDecimal totalAllocated,
            BigDecimal totalTendered,
            BigDecimal changeAmount,
            List<ValidatedPayment> payments
    ) {
    }
}
