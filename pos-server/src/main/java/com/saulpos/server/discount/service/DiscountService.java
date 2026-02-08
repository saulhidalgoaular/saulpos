package com.saulpos.server.discount.service;

import com.saulpos.api.discount.DiscountApplyRequest;
import com.saulpos.api.discount.DiscountApplyResponse;
import com.saulpos.api.discount.DiscountPreviewAppliedResponse;
import com.saulpos.api.discount.DiscountPreviewLineRequest;
import com.saulpos.api.discount.DiscountPreviewLineResponse;
import com.saulpos.api.discount.DiscountPreviewRequest;
import com.saulpos.api.discount.DiscountPreviewResponse;
import com.saulpos.api.discount.DiscountRemoveResponse;
import com.saulpos.api.discount.DiscountScope;
import com.saulpos.api.discount.DiscountType;
import com.saulpos.api.tax.TaxPreviewLineRequest;
import com.saulpos.api.tax.TaxPreviewRequest;
import com.saulpos.api.tax.TaxPreviewResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.discount.model.DiscountApplicationEntity;
import com.saulpos.server.discount.model.DiscountReasonCodeEntity;
import com.saulpos.server.discount.repository.DiscountApplicationRepository;
import com.saulpos.server.discount.repository.DiscountReasonCodeRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.authorization.SecurityAuthority;
import com.saulpos.server.tax.service.TaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal HIGH_DISCOUNT_PERCENT_THRESHOLD = new BigDecimal("15.0000");
    private static final BigDecimal HIGH_DISCOUNT_FIXED_THRESHOLD = new BigDecimal("20.0000");

    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final DiscountReasonCodeRepository discountReasonCodeRepository;
    private final DiscountApplicationRepository discountApplicationRepository;
    private final PricingService pricingService;
    private final TaxService taxService;

    @Transactional
    public DiscountApplyResponse apply(DiscountApplyRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        String contextKey = normalizeContextKey(request.contextKey());

        DiscountScope scope = requireScope(request.scope());
        ProductEntity product = resolveProductForScope(storeLocation, scope, request.productId());
        DiscountType discountType = requireType(request.type());
        BigDecimal value = normalizeDiscountValue(request.value(), discountType);
        DiscountReasonCodeEntity reasonCode = requireReasonCode(storeLocation.getMerchant().getId(), request.reasonCode());

        boolean managerApprovalRequired = requiresManagerApproval(discountType, value);
        if (managerApprovalRequired && !currentUserHasPermission(PermissionCodes.DISCOUNT_OVERRIDE)) {
            throw new BaseException(ErrorCode.AUTH_FORBIDDEN,
                    "discount exceeds configured threshold and requires manager override permission");
        }

        DiscountApplicationEntity application = new DiscountApplicationEntity();
        application.setStoreLocation(storeLocation);
        application.setContextKey(contextKey);
        application.setScope(scope);
        application.setProduct(product);
        application.setType(discountType);
        application.setValue(value);
        application.setReasonCode(reasonCode);
        application.setNote(normalizeNote(request.note()));
        application.setAppliedByUsername(resolveActorUsername());
        application.setActive(true);

        DiscountApplicationEntity saved = discountApplicationRepository.save(application);

        return new DiscountApplyResponse(
                saved.getId(),
                saved.getStoreLocation().getId(),
                saved.getContextKey(),
                saved.getScope(),
                saved.getProduct() != null ? saved.getProduct().getId() : null,
                saved.getType(),
                saved.getValue(),
                saved.getReasonCode().getCode(),
                saved.getNote(),
                managerApprovalRequired,
                saved.getAppliedByUsername(),
                saved.getAppliedAt());
    }

    @Transactional
    public DiscountRemoveResponse remove(Long discountId, Long storeLocationId, String contextKey) {
        String normalizedContextKey = normalizeContextKey(contextKey);
        DiscountApplicationEntity application = discountApplicationRepository
                .findByIdAndStoreLocationIdAndContextKeyIgnoreCaseAndActiveTrue(discountId, storeLocationId, normalizedContextKey)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "active discount not found for id=%d storeLocationId=%d contextKey=%s"
                                .formatted(discountId, storeLocationId, normalizedContextKey)));

        application.setActive(false);
        application.setRemovedAt(Instant.now());
        application.setRemovedByUsername(resolveActorUsername());
        DiscountApplicationEntity saved = discountApplicationRepository.save(application);

        return new DiscountRemoveResponse(
                saved.getId(),
                true,
                saved.getRemovedByUsername(),
                saved.getRemovedAt());
    }

    @Transactional(readOnly = true)
    public DiscountPreviewResponse preview(DiscountPreviewRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        String contextKey = normalizeContextKey(request.contextKey());

        List<PreviewLineState> lineStates = buildPreviewLineStates(storeLocation, request);
        BigDecimal subtotalBefore = normalizeMoney(lineStates.stream()
                .map(PreviewLineState::lineSubtotalBeforeDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));

        List<DiscountApplicationEntity> applications = discountApplicationRepository
                .findByStoreLocationIdAndContextKeyIgnoreCaseAndActiveTrueOrderByAppliedAtAscIdAsc(
                        storeLocation.getId(),
                        contextKey);

        List<AppliedDiscountResult> appliedDiscountResults = new ArrayList<>();
        applyLineScopedDiscounts(lineStates, applications, appliedDiscountResults);
        applyCartScopedDiscounts(lineStates, applications, appliedDiscountResults);

        BigDecimal subtotalAfter = normalizeMoney(lineStates.stream()
                .map(PreviewLineState::lineSubtotalAfterDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));
        BigDecimal totalDiscount = normalizeMoney(subtotalBefore.subtract(subtotalAfter));

        TaxPreviewResponse taxPreview = taxService.preview(new TaxPreviewRequest(
                request.storeLocationId(),
                request.at(),
                request.tenderType(),
                lineStates.stream()
                        .map(line -> new TaxPreviewLineRequest(
                                line.product().getId(),
                                line.quantity(),
                                line.discountedUnitPrice()))
                        .toList()));

        List<DiscountPreviewLineResponse> lines = lineStates.stream()
                .map(line -> new DiscountPreviewLineResponse(
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

        List<DiscountPreviewAppliedResponse> appliedDiscounts = appliedDiscountResults.stream()
                .map(result -> new DiscountPreviewAppliedResponse(
                        result.discount().getId(),
                        result.discount().getScope(),
                        result.discount().getProduct() != null ? result.discount().getProduct().getId() : null,
                        result.discount().getType(),
                        result.discount().getValue(),
                        result.discount().getReasonCode().getCode(),
                        result.appliedAmount()))
                .toList();

        return new DiscountPreviewResponse(
                request.storeLocationId(),
                contextKey,
                request.at(),
                lines,
                appliedDiscounts,
                subtotalBefore,
                totalDiscount,
                subtotalAfter,
                taxPreview);
    }

    private void applyLineScopedDiscounts(List<PreviewLineState> lines,
                                          List<DiscountApplicationEntity> discounts,
                                          List<AppliedDiscountResult> appliedResults) {
        for (DiscountApplicationEntity discount : discounts) {
            if (discount.getScope() != DiscountScope.LINE || discount.getProduct() == null) {
                continue;
            }

            List<PreviewLineState> targetLines = lines.stream()
                    .filter(line -> line.product().getId().equals(discount.getProduct().getId()))
                    .toList();
            if (targetLines.isEmpty()) {
                continue;
            }

            BigDecimal baseAmount = normalizeMoney(targetLines.stream()
                    .map(PreviewLineState::lineSubtotalAfterDiscount)
                    .reduce(ZERO_MONEY, BigDecimal::add));
            BigDecimal discountAmount = computeDiscountAmount(discount.getType(), discount.getValue(), baseAmount);
            if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            applyProportionalDiscount(targetLines, discountAmount);
            appliedResults.add(new AppliedDiscountResult(discount, discountAmount));
        }
    }

    private void applyCartScopedDiscounts(List<PreviewLineState> lines,
                                          List<DiscountApplicationEntity> discounts,
                                          List<AppliedDiscountResult> appliedResults) {
        for (DiscountApplicationEntity discount : discounts) {
            if (discount.getScope() != DiscountScope.CART) {
                continue;
            }
            BigDecimal baseAmount = normalizeMoney(lines.stream()
                    .map(PreviewLineState::lineSubtotalAfterDiscount)
                    .reduce(ZERO_MONEY, BigDecimal::add));
            BigDecimal discountAmount = computeDiscountAmount(discount.getType(), discount.getValue(), baseAmount);
            if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            applyProportionalDiscount(lines, discountAmount);
            appliedResults.add(new AppliedDiscountResult(discount, discountAmount));
        }
    }

    private void applyProportionalDiscount(List<PreviewLineState> lines, BigDecimal totalDiscount) {
        if (lines.isEmpty() || totalDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (lines.size() == 1) {
            lines.getFirst().applyDiscount(totalDiscount);
            return;
        }

        BigDecimal totalBase = normalizeMoney(lines.stream()
                .map(PreviewLineState::lineSubtotalAfterDiscount)
                .reduce(ZERO_MONEY, BigDecimal::add));
        if (totalBase.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal remainingDiscount = totalDiscount;
        BigDecimal allocated = ZERO_MONEY;
        for (int i = 0; i < lines.size(); i++) {
            PreviewLineState line = lines.get(i);
            BigDecimal allocation;
            if (i == lines.size() - 1) {
                allocation = normalizeMoney(remainingDiscount.max(BigDecimal.ZERO));
            } else {
                BigDecimal ratio = line.lineSubtotalAfterDiscount().divide(totalBase, 8, RoundingMode.HALF_UP);
                allocation = normalizeMoney(totalDiscount.multiply(ratio));
                BigDecimal maxAllowed = line.lineSubtotalAfterDiscount();
                if (allocation.compareTo(maxAllowed) > 0) {
                    allocation = maxAllowed;
                }
            }
            if (allocation.compareTo(remainingDiscount) > 0) {
                allocation = normalizeMoney(remainingDiscount);
            }
            allocation = normalizeMoney(allocation);
            line.applyDiscount(allocation);
            allocated = normalizeMoney(allocated.add(allocation));
            remainingDiscount = normalizeMoney(totalDiscount.subtract(allocated));
        }
    }

    private List<PreviewLineState> buildPreviewLineStates(StoreLocationEntity storeLocation, DiscountPreviewRequest request) {
        List<PreviewLineState> lineStates = new ArrayList<>();
        int lineNumber = 1;
        for (DiscountPreviewLineRequest lineRequest : request.lines()) {
            ProductEntity product = requireProduct(lineRequest.productId());
            ensureSameMerchant(storeLocation, product);
            BigDecimal quantity = normalizeQuantity(lineRequest.quantity());
            BigDecimal originalUnitPrice = resolveUnitPrice(
                    request.storeLocationId(),
                    product.getId(),
                    request.at(),
                    lineRequest.unitPrice());
            BigDecimal lineSubtotal = normalizeMoney(originalUnitPrice.multiply(quantity));
            lineStates.add(new PreviewLineState(
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

    private BigDecimal computeDiscountAmount(DiscountType type, BigDecimal value, BigDecimal baseAmount) {
        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_MONEY;
        }
        BigDecimal requestedAmount;
        if (type == DiscountType.PERCENTAGE) {
            requestedAmount = baseAmount.multiply(value).divide(HUNDRED, 6, RoundingMode.HALF_UP);
        } else {
            requestedAmount = value;
        }

        BigDecimal normalizedAmount = normalizeMoney(requestedAmount);
        if (normalizedAmount.compareTo(baseAmount) > 0) {
            return baseAmount;
        }
        return normalizedAmount;
    }

    private DiscountReasonCodeEntity requireReasonCode(Long merchantId, String reasonCode) {
        String normalizedCode = normalizeCode(reasonCode);
        return discountReasonCodeRepository.findByMerchantIdAndCodeIgnoreCaseAndActiveTrue(merchantId, normalizedCode)
                .or(() -> discountReasonCodeRepository.findByMerchantIsNullAndCodeIgnoreCaseAndActiveTrue(normalizedCode))
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "discount reason code not found for merchantId=%d code=%s".formatted(merchantId, normalizedCode)));
    }

    private ProductEntity resolveProductForScope(StoreLocationEntity storeLocation, DiscountScope scope, Long productId) {
        if (scope == DiscountScope.CART) {
            if (productId != null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId must be omitted for CART discounts");
            }
            return null;
        }

        if (productId == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required for LINE discounts");
        }

        ProductEntity product = requireProduct(productId);
        ensureSameMerchant(storeLocation, product);
        return product;
    }

    private boolean requiresManagerApproval(DiscountType type, BigDecimal value) {
        if (type == DiscountType.PERCENTAGE) {
            return value.compareTo(HIGH_DISCOUNT_PERCENT_THRESHOLD) > 0;
        }
        return value.compareTo(HIGH_DISCOUNT_FIXED_THRESHOLD) > 0;
    }

    private boolean currentUserHasPermission(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        String requiredAuthority = SecurityAuthority.permission(permissionCode);
        return authentication.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .anyMatch(requiredAuthority::equalsIgnoreCase);
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private ProductEntity requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "product not found: " + productId));
    }

    private void ensureSameMerchant(StoreLocationEntity storeLocation, ProductEntity product) {
        if (!storeLocation.getMerchant().getId().equals(product.getMerchant().getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product does not belong to store merchant context");
        }
    }

    private DiscountScope requireScope(DiscountScope scope) {
        if (scope == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "scope is required");
        }
        return scope;
    }

    private DiscountType requireType(DiscountType type) {
        if (type == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "type is required");
        }
        return type;
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

    private BigDecimal normalizeDiscountValue(BigDecimal value, DiscountType type) {
        if (value == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "value is required");
        }
        BigDecimal normalized = value.setScale(4, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "value must be greater than zero");
        }
        if (type == DiscountType.PERCENTAGE && normalized.compareTo(HUNDRED) > 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "percentage discount cannot exceed 100");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount is required");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeContextKey(String contextKey) {
        if (contextKey == null || contextKey.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "contextKey is required");
        }
        return contextKey.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "reasonCode is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String normalized = note.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "unknown";
        }
        return authentication.getName();
    }

    private record AppliedDiscountResult(
            DiscountApplicationEntity discount,
            BigDecimal appliedAmount
    ) {
    }

    private static final class PreviewLineState {
        private final int lineNumber;
        private final ProductEntity product;
        private final BigDecimal quantity;
        private final BigDecimal originalUnitPrice;
        private final BigDecimal lineSubtotalBeforeDiscount;
        private BigDecimal lineDiscountAmount;
        private BigDecimal lineSubtotalAfterDiscount;

        private PreviewLineState(int lineNumber,
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
            if (quantity.compareTo(BigDecimal.ZERO) == 0) {
                return ZERO_MONEY;
            }
            return lineSubtotalAfterDiscount.divide(quantity, 2, RoundingMode.HALF_UP);
        }

        private void applyDiscount(BigDecimal discountAmount) {
            BigDecimal normalizedDiscount = discountAmount.setScale(2, RoundingMode.HALF_UP);
            if (normalizedDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            if (normalizedDiscount.compareTo(lineSubtotalAfterDiscount) > 0) {
                normalizedDiscount = lineSubtotalAfterDiscount;
            }
            this.lineDiscountAmount = this.lineDiscountAmount.add(normalizedDiscount).setScale(2, RoundingMode.HALF_UP);
            this.lineSubtotalAfterDiscount = this.lineSubtotalAfterDiscount.subtract(normalizedDiscount).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
