package com.saulpos.server.tax.service;

import com.saulpos.api.tax.RoundingMethod;
import com.saulpos.api.tax.RoundingSummary;
import com.saulpos.api.tax.TenderType;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.tax.model.RoundingPolicyEntity;
import com.saulpos.server.tax.repository.RoundingPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class RoundingService {

    private static final int MONEY_SCALE = 2;

    private final RoundingPolicyRepository roundingPolicyRepository;

    @Transactional(readOnly = true)
    public RoundingSummary apply(Long storeLocationId, TenderType tenderType, BigDecimal amount) {
        BigDecimal originalAmount = normalizeMoney(amount);

        if (tenderType == null) {
            return notApplied(null, originalAmount);
        }

        return roundingPolicyRepository.findFirstByStoreLocationIdAndTenderTypeAndActiveTrue(storeLocationId, tenderType)
                .map(policy -> applyPolicy(policy, originalAmount))
                .orElseGet(() -> notApplied(tenderType, originalAmount));
    }

    private RoundingSummary applyPolicy(RoundingPolicyEntity policy, BigDecimal originalAmount) {
        BigDecimal increment = normalizeIncrement(policy.getIncrementAmount());
        RoundingMode roundingMode = roundingMode(policy.getRoundingMethod());

        BigDecimal steps = originalAmount.divide(increment, 0, roundingMode);
        BigDecimal roundedAmount = normalizeMoney(steps.multiply(increment));
        BigDecimal adjustment = normalizeSignedMoney(roundedAmount.subtract(originalAmount));

        return new RoundingSummary(
                true,
                policy.getTenderType(),
                policy.getRoundingMethod(),
                increment,
                originalAmount,
                roundedAmount,
                adjustment);
    }

    private RoundingSummary notApplied(TenderType tenderType, BigDecimal amount) {
        return new RoundingSummary(
                false,
                tenderType,
                null,
                null,
                amount,
                amount,
                zeroMoney());
    }

    private RoundingMode roundingMode(RoundingMethod method) {
        if (method == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "roundingMethod is required");
        }

        return switch (method) {
            case NEAREST -> RoundingMode.HALF_UP;
            case UP -> RoundingMode.CEILING;
            case DOWN -> RoundingMode.FLOOR;
        };
    }

    private BigDecimal normalizeIncrement(BigDecimal increment) {
        if (increment == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "incrementAmount is required");
        }
        BigDecimal normalized = increment.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "incrementAmount must be greater than zero");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount must be non-negative");
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeSignedMoney(BigDecimal amount) {
        if (amount == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount is required");
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
