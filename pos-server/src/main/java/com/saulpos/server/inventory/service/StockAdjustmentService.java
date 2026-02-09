package com.saulpos.server.inventory.service;

import com.saulpos.api.inventory.StockAdjustmentApproveRequest;
import com.saulpos.api.inventory.StockAdjustmentCreateRequest;
import com.saulpos.api.inventory.StockAdjustmentPostRequest;
import com.saulpos.api.inventory.StockAdjustmentResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.inventory.model.StockAdjustmentEntity;
import com.saulpos.server.inventory.model.StockAdjustmentStatus;
import com.saulpos.server.inventory.repository.StockAdjustmentRepository;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.InventoryMovementType;
import com.saulpos.server.sale.model.InventoryReferenceType;
import com.saulpos.server.sale.repository.InventoryMovementRepository;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.authorization.SecurityAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockAdjustmentService {

    private static final BigDecimal DEFAULT_APPROVAL_THRESHOLD = new BigDecimal("25.000");

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceCalculator balanceCalculator;
    private final Clock clock;

    @Value("${app.inventory.adjustment-manager-approval-threshold:25.000}")
    private BigDecimal managerApprovalThreshold;

    @Transactional
    public StockAdjustmentResponse createAdjustment(StockAdjustmentCreateRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        ProductEntity product = requireProduct(request.productId());
        validateStoreProductMerchantConsistency(storeLocation, product);

        BigDecimal quantityDelta = normalizeQuantity(request.quantityDelta());
        String reasonCode = normalizeRequiredReasonCode(request.reasonCode());
        String requestNote = normalizeOptionalNote(request.note());
        String actor = resolveActorUsername();
        Instant now = Instant.now(clock);
        boolean approvalRequired = requiresManagerApproval(quantityDelta);

        StockAdjustmentEntity adjustment = new StockAdjustmentEntity();
        adjustment.setStoreLocation(storeLocation);
        adjustment.setProduct(product);
        adjustment.setQuantityDelta(quantityDelta);
        adjustment.setReasonCode(reasonCode);
        adjustment.setStatus(approvalRequired ? StockAdjustmentStatus.PENDING_APPROVAL : StockAdjustmentStatus.APPROVED);
        adjustment.setApprovalRequired(approvalRequired);
        adjustment.setReferenceNumber(generateReferenceNumber());
        adjustment.setRequestNote(requestNote);
        adjustment.setRequestedBy(actor);
        adjustment.setRequestedAt(now);

        if (!approvalRequired) {
            adjustment.setApprovedBy(actor);
            adjustment.setApprovedAt(now);
        }

        return toResponse(stockAdjustmentRepository.save(adjustment));
    }

    @Transactional
    public StockAdjustmentResponse approveAdjustment(Long adjustmentId, StockAdjustmentApproveRequest request) {
        requireManagerPermission();
        StockAdjustmentEntity adjustment = requireAdjustmentForUpdate(adjustmentId);

        if (!adjustment.isApprovalRequired()) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock adjustment does not require manager approval: " + adjustmentId);
        }
        if (adjustment.getStatus() == StockAdjustmentStatus.POSTED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock adjustment already posted: " + adjustmentId);
        }
        if (adjustment.getStatus() != StockAdjustmentStatus.PENDING_APPROVAL) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock adjustment is not pending approval: " + adjustmentId);
        }

        adjustment.setStatus(StockAdjustmentStatus.APPROVED);
        adjustment.setApprovedBy(resolveActorUsername());
        adjustment.setApprovedAt(Instant.now(clock));
        adjustment.setApprovalNote(normalizeOptionalNote(request == null ? null : request.note()));

        return toResponse(stockAdjustmentRepository.save(adjustment));
    }

    @Transactional
    public StockAdjustmentResponse postAdjustment(Long adjustmentId, StockAdjustmentPostRequest request) {
        StockAdjustmentEntity adjustment = requireAdjustmentForUpdate(adjustmentId);

        if (adjustment.getStatus() == StockAdjustmentStatus.POSTED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock adjustment already posted: " + adjustmentId);
        }
        if (adjustment.isApprovalRequired() && adjustment.getStatus() != StockAdjustmentStatus.APPROVED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock adjustment requires manager approval before posting: " + adjustmentId);
        }
        if (!adjustment.isApprovalRequired() && adjustment.getStatus() != StockAdjustmentStatus.APPROVED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock adjustment is not in postable status: " + adjustmentId);
        }

        InventoryMovementEntity movement = new InventoryMovementEntity();
        movement.setStoreLocation(adjustment.getStoreLocation());
        movement.setProduct(adjustment.getProduct());
        movement.setSale(null);
        movement.setSaleLine(null);
        movement.setMovementType(InventoryMovementType.ADJUSTMENT);
        movement.setQuantityDelta(adjustment.getQuantityDelta());
        movement.setReferenceType(InventoryReferenceType.STOCK_ADJUSTMENT);
        movement.setReferenceNumber(adjustment.getReferenceNumber());
        InventoryMovementEntity savedMovement = inventoryMovementRepository.save(movement);

        adjustment.setStatus(StockAdjustmentStatus.POSTED);
        adjustment.setPostedBy(resolveActorUsername());
        adjustment.setPostedAt(Instant.now(clock));
        adjustment.setPostNote(normalizeOptionalNote(request == null ? null : request.note()));
        adjustment.setInventoryMovement(savedMovement);

        return toResponse(stockAdjustmentRepository.save(adjustment));
    }

    private StockAdjustmentEntity requireAdjustmentForUpdate(Long adjustmentId) {
        return stockAdjustmentRepository.findByIdForUpdate(adjustmentId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "stock adjustment not found: " + adjustmentId));
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

    private void validateStoreProductMerchantConsistency(StoreLocationEntity storeLocation, ProductEntity product) {
        Long storeMerchantId = storeLocation.getMerchant().getId();
        Long productMerchantId = product.getMerchant().getId();
        if (!storeMerchantId.equals(productMerchantId)) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product does not belong to the same merchant as storeLocation");
        }
    }

    private BigDecimal normalizeQuantity(BigDecimal quantityDelta) {
        BigDecimal normalized = balanceCalculator.normalizeScale(quantityDelta);
        if (normalized.signum() == 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "quantityDelta must be non-zero");
        }
        return normalized;
    }

    private String normalizeRequiredReasonCode(String reasonCode) {
        if (reasonCode == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "reasonCode is required");
        }
        String normalized = reasonCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "reasonCode is required");
        }
        if (normalized.length() > 40) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "reasonCode must be at most 40 characters");
        }
        return normalized;
    }

    private String normalizeOptionalNote(String note) {
        if (note == null) {
            return null;
        }
        String normalized = note.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean requiresManagerApproval(BigDecimal quantityDelta) {
        return quantityDelta.abs().compareTo(normalizedApprovalThreshold()) >= 0;
    }

    private BigDecimal normalizedApprovalThreshold() {
        BigDecimal threshold = managerApprovalThreshold == null ? DEFAULT_APPROVAL_THRESHOLD : managerApprovalThreshold.abs();
        return balanceCalculator.normalizeScale(threshold);
    }

    private void requireManagerPermission() {
        if (!currentUserHasPermission(PermissionCodes.CONFIGURATION_MANAGE)) {
            throw new BaseException(ErrorCode.AUTH_FORBIDDEN,
                    "manager approval requires CONFIGURATION_MANAGE permission");
        }
    }

    private boolean currentUserHasPermission(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().contains(
                new SimpleGrantedAuthority(SecurityAuthority.permission(permissionCode)));
    }

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        String normalized = authentication.getName().trim();
        return normalized.isEmpty() ? "system" : normalized;
    }

    private String generateReferenceNumber() {
        return "ADJ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
    }

    private StockAdjustmentResponse toResponse(StockAdjustmentEntity adjustment) {
        return new StockAdjustmentResponse(
                adjustment.getId(),
                adjustment.getStoreLocation().getId(),
                adjustment.getProduct().getId(),
                balanceCalculator.normalizeScale(adjustment.getQuantityDelta()),
                adjustment.getReasonCode(),
                com.saulpos.api.inventory.StockAdjustmentStatus.valueOf(adjustment.getStatus().name()),
                adjustment.isApprovalRequired(),
                adjustment.getReferenceNumber(),
                adjustment.getRequestNote(),
                adjustment.getApprovalNote(),
                adjustment.getPostNote(),
                adjustment.getRequestedBy(),
                adjustment.getRequestedAt(),
                adjustment.getApprovedBy(),
                adjustment.getApprovedAt(),
                adjustment.getPostedBy(),
                adjustment.getPostedAt(),
                adjustment.getInventoryMovement() == null ? null : adjustment.getInventoryMovement().getId());
    }
}
