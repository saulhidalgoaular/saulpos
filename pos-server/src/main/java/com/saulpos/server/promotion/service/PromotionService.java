package com.saulpos.server.promotion.service;

import com.saulpos.api.promotion.PromotionAppliedResponse;
import com.saulpos.api.promotion.PromotionEvaluateLineRequest;
import com.saulpos.api.promotion.PromotionEvaluateLineResponse;
import com.saulpos.api.promotion.PromotionEvaluateRequest;
import com.saulpos.api.promotion.PromotionEvaluateResponse;
import com.saulpos.api.promotion.PromotionRuleType;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.promotion.model.PromotionEntity;
import com.saulpos.server.promotion.model.PromotionRuleEntity;
import com.saulpos.server.promotion.model.PromotionWindowEntity;
import com.saulpos.server.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_QUANTITY = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final PricingService pricingService;
    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public PromotionEvaluateResponse evaluate(PromotionEvaluateRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        List<EvaluationLineState> baseLineStates = buildLineStates(storeLocation, request);

        BigDecimal subtotalBeforeDiscount = normalizeMoney(baseLineStates.stream()
                .map(EvaluationLineState::lineSubtotalBeforeDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));

        List<PromotionEntity> candidates = promotionRepository.findActiveForMerchant(storeLocation.getMerchant().getId())
                .stream()
                .filter(promotion -> isPromotionEligibleAt(promotion, request.at()))
                .toList();

        PromotionEvaluation winner = selectWinningPromotion(candidates, baseLineStates);

        if (winner == null) {
            return new PromotionEvaluateResponse(
                    request.storeLocationId(),
                    request.at(),
                    toLineResponses(baseLineStates),
                    subtotalBeforeDiscount,
                    ZERO_MONEY,
                    subtotalBeforeDiscount,
                    null);
        }

        BigDecimal subtotalAfterDiscount = normalizeMoney(winner.lineStates().stream()
                .map(EvaluationLineState::lineSubtotalAfterDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));
        BigDecimal totalDiscount = normalizeMoney(subtotalBeforeDiscount.subtract(subtotalAfterDiscount));

        PromotionAppliedResponse appliedPromotion = new PromotionAppliedResponse(
                winner.promotion().getId(),
                winner.promotion().getCode(),
                winner.promotion().getName(),
                winner.promotion().getPriority(),
                totalDiscount,
                winner.explanations());

        return new PromotionEvaluateResponse(
                request.storeLocationId(),
                request.at(),
                toLineResponses(winner.lineStates()),
                subtotalBeforeDiscount,
                totalDiscount,
                subtotalAfterDiscount,
                appliedPromotion);
    }

    private PromotionEvaluation selectWinningPromotion(List<PromotionEntity> candidates,
                                                       List<EvaluationLineState> baseLineStates) {
        PromotionEvaluation winner = null;
        for (PromotionEntity candidate : candidates) {
            PromotionEvaluation evaluation = evaluatePromotion(candidate, baseLineStates);
            if (evaluation == null) {
                continue;
            }

            if (winner == null || isBetterEvaluation(evaluation, winner)) {
                winner = evaluation;
            }
        }
        return winner;
    }

    private boolean isBetterEvaluation(PromotionEvaluation challenger, PromotionEvaluation incumbent) {
        int priorityCompare = Integer.compare(
                challenger.promotion().getPriority(),
                incumbent.promotion().getPriority());
        if (priorityCompare != 0) {
            return priorityCompare > 0;
        }

        int discountCompare = challenger.totalDiscount().compareTo(incumbent.totalDiscount());
        if (discountCompare != 0) {
            return discountCompare > 0;
        }

        return challenger.promotion().getId() < incumbent.promotion().getId();
    }

    private PromotionEvaluation evaluatePromotion(PromotionEntity promotion, List<EvaluationLineState> baseLineStates) {
        List<PromotionRuleEntity> activeRules = promotion.getRules().stream()
                .filter(PromotionRuleEntity::isActive)
                .sorted(Comparator.comparing(PromotionRuleEntity::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        if (activeRules.isEmpty()) {
            return null;
        }

        List<EvaluationLineState> lineStates = cloneLineStates(baseLineStates);
        List<String> explanations = new ArrayList<>();

        for (PromotionRuleEntity rule : activeRules) {
            switch (rule.getRuleType()) {
                case PRODUCT_PERCENTAGE -> applyProductPercentageRule(rule, lineStates, explanations);
                case CART_FIXED -> applyCartFixedRule(rule, lineStates, explanations);
                default -> throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "unsupported promotion rule type: " + rule.getRuleType());
            }
        }

        BigDecimal subtotalBefore = normalizeMoney(baseLineStates.stream()
                .map(EvaluationLineState::lineSubtotalBeforeDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));
        BigDecimal subtotalAfter = normalizeMoney(lineStates.stream()
                .map(EvaluationLineState::lineSubtotalAfterDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));
        BigDecimal totalDiscount = normalizeMoney(subtotalBefore.subtract(subtotalAfter));

        if (totalDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return new PromotionEvaluation(promotion, lineStates, totalDiscount, explanations);
    }

    private void applyProductPercentageRule(PromotionRuleEntity rule,
                                            List<EvaluationLineState> lineStates,
                                            List<String> explanations) {
        if (rule.getTargetProduct() == null) {
            return;
        }

        BigDecimal percent = normalizePercentage(rule.getDiscountValue());
        BigDecimal minQuantity = normalizeMinQuantity(rule.getMinQuantity());

        BigDecimal totalApplied = ZERO_MONEY;
        for (EvaluationLineState lineState : lineStates) {
            if (!lineState.product().getId().equals(rule.getTargetProduct().getId())) {
                continue;
            }
            if (lineState.quantity().compareTo(minQuantity) < 0) {
                continue;
            }

            BigDecimal requested = normalizeMoney(lineState.lineSubtotalAfterDiscount()
                    .multiply(percent)
                    .divide(HUNDRED, 6, RoundingMode.HALF_UP));
            BigDecimal applied = lineState.applyDiscount(requested);
            totalApplied = normalizeMoney(totalApplied.add(applied));
        }

        if (totalApplied.compareTo(BigDecimal.ZERO) > 0) {
            explanations.add("Applied " + percent + "% product promo on "
                    + rule.getTargetProduct().getSku() + " for " + totalApplied);
        }
    }

    private void applyCartFixedRule(PromotionRuleEntity rule,
                                    List<EvaluationLineState> lineStates,
                                    List<String> explanations) {
        BigDecimal minSubtotal = rule.getMinSubtotal() == null ? ZERO_MONEY : normalizeMoney(rule.getMinSubtotal());
        BigDecimal subtotalBefore = normalizeMoney(lineStates.stream()
                .map(EvaluationLineState::lineSubtotalAfterDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));

        if (subtotalBefore.compareTo(minSubtotal) < 0) {
            return;
        }

        BigDecimal requestedDiscount = normalizeMoney(rule.getDiscountValue());
        BigDecimal boundedDiscount = requestedDiscount.compareTo(subtotalBefore) > 0
                ? subtotalBefore
                : requestedDiscount;
        BigDecimal appliedDiscount = applyProportionalDiscount(lineStates, boundedDiscount);
        if (appliedDiscount.compareTo(BigDecimal.ZERO) > 0) {
            explanations.add("Applied fixed cart promo " + appliedDiscount + " with minimum subtotal " + minSubtotal);
        }
    }

    private BigDecimal applyProportionalDiscount(List<EvaluationLineState> lineStates, BigDecimal totalDiscount) {
        if (lineStates.isEmpty() || totalDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_MONEY;
        }

        if (lineStates.size() == 1) {
            return lineStates.getFirst().applyDiscount(totalDiscount);
        }

        BigDecimal totalBase = normalizeMoney(lineStates.stream()
                .map(EvaluationLineState::lineSubtotalAfterDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));
        if (totalBase.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_MONEY;
        }

        BigDecimal allocated = ZERO_MONEY;
        BigDecimal remaining = normalizeMoney(totalDiscount);

        for (int i = 0; i < lineStates.size(); i++) {
            EvaluationLineState line = lineStates.get(i);
            BigDecimal allocation;
            if (i == lineStates.size() - 1) {
                allocation = normalizeMoney(remaining.max(BigDecimal.ZERO));
            } else {
                BigDecimal ratio = line.lineSubtotalAfterDiscount().divide(totalBase, 8, RoundingMode.HALF_UP);
                allocation = normalizeMoney(totalDiscount.multiply(ratio));
                if (allocation.compareTo(line.lineSubtotalAfterDiscount()) > 0) {
                    allocation = line.lineSubtotalAfterDiscount();
                }
                if (allocation.compareTo(remaining) > 0) {
                    allocation = remaining;
                }
            }
            BigDecimal applied = line.applyDiscount(allocation);
            allocated = normalizeMoney(allocated.add(applied));
            remaining = normalizeMoney(totalDiscount.subtract(allocated));
        }

        return allocated;
    }

    private List<EvaluationLineState> buildLineStates(StoreLocationEntity storeLocation, PromotionEvaluateRequest request) {
        List<EvaluationLineState> lineStates = new ArrayList<>();
        int lineNumber = 1;
        for (PromotionEvaluateLineRequest lineRequest : request.lines()) {
            ProductEntity product = requireProduct(lineRequest.productId());
            ensureSameMerchant(storeLocation, product);
            BigDecimal quantity = normalizeQuantity(lineRequest.quantity());
            BigDecimal originalUnitPrice = resolveUnitPrice(
                    request.storeLocationId(),
                    product.getId(),
                    request.at(),
                    lineRequest.unitPrice());
            BigDecimal lineSubtotal = normalizeMoney(originalUnitPrice.multiply(quantity));
            lineStates.add(new EvaluationLineState(
                    lineNumber,
                    product,
                    quantity,
                    originalUnitPrice,
                    lineSubtotal,
                    ZERO_MONEY,
                    lineSubtotal));
            lineNumber++;
        }
        return lineStates;
    }

    private List<EvaluationLineState> cloneLineStates(List<EvaluationLineState> baseLineStates) {
        return baseLineStates.stream()
                .map(line -> new EvaluationLineState(
                        line.lineNumber(),
                        line.product(),
                        line.quantity(),
                        line.originalUnitPrice(),
                        line.lineSubtotalBeforeDiscount(),
                        ZERO_MONEY,
                        line.lineSubtotalBeforeDiscount()))
                .toList();
    }

    private List<PromotionEvaluateLineResponse> toLineResponses(List<EvaluationLineState> lineStates) {
        return lineStates.stream()
                .map(line -> new PromotionEvaluateLineResponse(
                        line.lineNumber(),
                        line.product().getId(),
                        line.product().getSku(),
                        line.product().getName(),
                        line.quantity(),
                        line.originalUnitPrice(),
                        line.discountedUnitPrice(),
                        line.lineSubtotalBeforeDiscount(),
                        line.lineDiscountAmount(),
                        line.lineSubtotalAfterDiscount()))
                .toList();
    }

    private boolean isPromotionEligibleAt(PromotionEntity promotion, Instant at) {
        return promotion.getWindows().stream()
                .filter(PromotionWindowEntity::isActive)
                .anyMatch(window -> isWindowActiveAt(window, at));
    }

    private boolean isWindowActiveAt(PromotionWindowEntity window, Instant at) {
        boolean startsOk = window.getStartsAt() == null || !window.getStartsAt().isAfter(at);
        boolean endsOk = window.getEndsAt() == null || !window.getEndsAt().isBefore(at);
        return startsOk && endsOk;
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private ProductEntity requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "product not found: " + productId));
    }

    private void ensureSameMerchant(StoreLocationEntity storeLocation, ProductEntity product) {
        if (!storeLocation.getMerchant().getId().equals(product.getMerchant().getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product does not belong to store merchant context");
        }
    }

    private BigDecimal resolveUnitPrice(Long storeLocationId, Long productId, Instant at, BigDecimal providedUnitPrice) {
        if (providedUnitPrice != null) {
            return normalizeMoney(providedUnitPrice);
        }
        return normalizeMoney(pricingService.resolvePrice(storeLocationId, productId, at).resolvedPrice());
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "quantity must be greater than zero");
        }
        return quantity.stripTrailingZeros();
    }

    private BigDecimal normalizeMinQuantity(BigDecimal minQuantity) {
        if (minQuantity == null) {
            return ZERO_QUANTITY;
        }
        if (minQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "minQuantity must be greater than zero");
        }
        return minQuantity.stripTrailingZeros();
    }

    private BigDecimal normalizePercentage(BigDecimal value) {
        if (value == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "discountValue is required");
        }
        BigDecimal normalized = value.setScale(4, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0 || normalized.compareTo(HUNDRED) > 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "percentage discountValue must be greater than zero and at most 100");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount is required");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private record PromotionEvaluation(
            PromotionEntity promotion,
            List<EvaluationLineState> lineStates,
            BigDecimal totalDiscount,
            List<String> explanations
    ) {
    }

    private static final class EvaluationLineState {
        private final int lineNumber;
        private final ProductEntity product;
        private final BigDecimal quantity;
        private final BigDecimal originalUnitPrice;
        private final BigDecimal lineSubtotalBeforeDiscount;
        private BigDecimal lineDiscountAmount;
        private BigDecimal lineSubtotalAfterDiscount;

        private EvaluationLineState(int lineNumber,
                                    ProductEntity product,
                                    BigDecimal quantity,
                                    BigDecimal originalUnitPrice,
                                    BigDecimal lineSubtotalBeforeDiscount,
                                    BigDecimal lineDiscountAmount,
                                    BigDecimal lineSubtotalAfterDiscount) {
            this.lineNumber = lineNumber;
            this.product = product;
            this.quantity = quantity;
            this.originalUnitPrice = originalUnitPrice;
            this.lineSubtotalBeforeDiscount = lineSubtotalBeforeDiscount;
            this.lineDiscountAmount = lineDiscountAmount;
            this.lineSubtotalAfterDiscount = lineSubtotalAfterDiscount;
        }

        private int lineNumber() {
            return lineNumber;
        }

        private ProductEntity product() {
            return product;
        }

        private BigDecimal quantity() {
            return quantity;
        }

        private BigDecimal originalUnitPrice() {
            return originalUnitPrice;
        }

        private BigDecimal lineSubtotalBeforeDiscount() {
            return lineSubtotalBeforeDiscount;
        }

        private BigDecimal lineDiscountAmount() {
            return lineDiscountAmount;
        }

        private BigDecimal lineSubtotalAfterDiscount() {
            return lineSubtotalAfterDiscount;
        }

        private BigDecimal discountedUnitPrice() {
            return lineSubtotalAfterDiscount.divide(quantity, 2, RoundingMode.HALF_UP);
        }

        private BigDecimal applyDiscount(BigDecimal discountAmount) {
            BigDecimal normalizedDiscount = discountAmount.setScale(2, RoundingMode.HALF_UP);
            if (normalizedDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                return ZERO_MONEY;
            }
            if (normalizedDiscount.compareTo(lineSubtotalAfterDiscount) > 0) {
                normalizedDiscount = lineSubtotalAfterDiscount;
            }
            this.lineDiscountAmount = this.lineDiscountAmount.add(normalizedDiscount).setScale(2, RoundingMode.HALF_UP);
            this.lineSubtotalAfterDiscount = this.lineSubtotalAfterDiscount.subtract(normalizedDiscount)
                    .setScale(2, RoundingMode.HALF_UP);
            return normalizedDiscount;
        }
    }
}
