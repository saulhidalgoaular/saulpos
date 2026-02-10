package com.saulpos.server.inventory.service;

import com.saulpos.api.inventory.SupplierReturnApproveRequest;
import com.saulpos.api.inventory.SupplierReturnCreateLineRequest;
import com.saulpos.api.inventory.SupplierReturnCreateRequest;
import com.saulpos.api.inventory.SupplierReturnLineResponse;
import com.saulpos.api.inventory.SupplierReturnPostRequest;
import com.saulpos.api.inventory.SupplierReturnResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.inventory.model.PurchaseOrderStatus;
import com.saulpos.server.inventory.model.SupplierReturnEntity;
import com.saulpos.server.inventory.model.SupplierReturnLineEntity;
import com.saulpos.server.inventory.model.SupplierReturnStatus;
import com.saulpos.server.inventory.repository.PurchaseOrderRepository;
import com.saulpos.server.inventory.repository.SupplierReturnRepository;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.InventoryMovementType;
import com.saulpos.server.sale.model.InventoryReferenceType;
import com.saulpos.server.sale.repository.InventoryMovementRepository;
import com.saulpos.server.supplier.model.SupplierEntity;
import com.saulpos.server.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierReturnService {

    private final SupplierReturnRepository supplierReturnRepository;
    private final SupplierRepository supplierRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryBalanceCalculator balanceCalculator;
    private final CostingCalculator costingCalculator;
    private final Clock clock;

    @Transactional
    public SupplierReturnResponse createSupplierReturn(SupplierReturnCreateRequest request) {
        SupplierEntity supplier = requireSupplier(request.supplierId());
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        validateSupplierStoreMerchantConsistency(supplier, storeLocation);

        Map<Long, ReturnLineInput> requestedLines = normalizeCreateLines(request.lines());
        Map<Long, ProductEntity> productsById = requireProducts(requestedLines.keySet());
        validateProductsBelongToMerchant(supplier.getMerchant().getId(), productsById.values());

        validateEligibility(requestedLines, supplier.getId(), storeLocation.getId());

        SupplierReturnEntity supplierReturn = new SupplierReturnEntity();
        supplierReturn.setSupplier(supplier);
        supplierReturn.setStoreLocation(storeLocation);
        supplierReturn.setStatus(SupplierReturnStatus.DRAFT);
        supplierReturn.setReferenceNumber(generateReferenceNumber());
        supplierReturn.setNote(normalizeOptionalNote(request.note()));
        supplierReturn.setCreatedBy(resolveActorUsername());
        supplierReturn.setCreatedAt(Instant.now(clock));

        requestedLines.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    SupplierReturnLineEntity line = new SupplierReturnLineEntity();
                    line.setProduct(productsById.get(entry.getKey()));
                    line.setReturnQuantity(entry.getValue().quantity());
                    line.setUnitCost(entry.getValue().unitCost());
                    supplierReturn.addLine(line);
                });

        return toResponse(supplierReturnRepository.save(supplierReturn));
    }

    @Transactional(readOnly = true)
    public SupplierReturnResponse getSupplierReturn(Long supplierReturnId) {
        SupplierReturnEntity supplierReturn = supplierReturnRepository.findByIdWithDetails(supplierReturnId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "supplier return not found: " + supplierReturnId));
        return toResponse(supplierReturn);
    }

    @Transactional
    public SupplierReturnResponse approveSupplierReturn(Long supplierReturnId, SupplierReturnApproveRequest request) {
        SupplierReturnEntity supplierReturn = requireSupplierReturnForUpdate(supplierReturnId);
        if (supplierReturn.getStatus() != SupplierReturnStatus.DRAFT) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "supplier return can only be approved from DRAFT status: " + supplierReturnId);
        }

        supplierReturn.setStatus(SupplierReturnStatus.APPROVED);
        supplierReturn.setApprovedBy(resolveActorUsername());
        supplierReturn.setApprovedAt(Instant.now(clock));
        String normalizedNote = normalizeOptionalNote(request == null ? null : request.note());
        if (normalizedNote != null) {
            supplierReturn.setNote(normalizedNote);
        }

        return toResponse(supplierReturnRepository.save(supplierReturn));
    }

    @Transactional
    public SupplierReturnResponse postSupplierReturn(Long supplierReturnId, SupplierReturnPostRequest request) {
        SupplierReturnEntity supplierReturn = requireSupplierReturnForUpdate(supplierReturnId);
        if (supplierReturn.getStatus() != SupplierReturnStatus.APPROVED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "supplier return can only be posted from APPROVED status: " + supplierReturnId);
        }

        Map<Long, ReturnLineInput> lineInputs = supplierReturn.getLines().stream()
                .collect(Collectors.toMap(
                        line -> line.getProduct().getId(),
                        line -> new ReturnLineInput(
                                balanceCalculator.normalizeScale(line.getReturnQuantity()),
                                costingCalculator.normalizeCostScale(line.getUnitCost()))));
        validateEligibility(lineInputs, supplierReturn.getSupplier().getId(), supplierReturn.getStoreLocation().getId());

        String referenceNumber = supplierReturn.getReferenceNumber();
        List<SupplierReturnLineEntity> sortedLines = supplierReturn.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .toList();

        List<InventoryMovementEntity> movements = sortedLines.stream()
                .map(line -> {
                    InventoryMovementEntity movement = new InventoryMovementEntity();
                    movement.setStoreLocation(supplierReturn.getStoreLocation());
                    movement.setProduct(line.getProduct());
                    movement.setSale(null);
                    movement.setSaleLine(null);
                    movement.setMovementType(InventoryMovementType.ADJUSTMENT);
                    movement.setQuantityDelta(balanceCalculator.normalizeScale(line.getReturnQuantity()).negate());
                    movement.setReferenceType(InventoryReferenceType.SUPPLIER_RETURN);
                    movement.setReferenceNumber(referenceNumber + "-P" + line.getProduct().getId());
                    return movement;
                })
                .toList();

        List<InventoryMovementEntity> savedMovements = inventoryMovementRepository.saveAll(movements);
        for (int i = 0; i < sortedLines.size(); i++) {
            sortedLines.get(i).setInventoryMovementId(savedMovements.get(i).getId());
        }

        supplierReturn.setStatus(SupplierReturnStatus.POSTED);
        supplierReturn.setPostedBy(resolveActorUsername());
        supplierReturn.setPostedAt(Instant.now(clock));
        String normalizedNote = normalizeOptionalNote(request == null ? null : request.note());
        if (normalizedNote != null) {
            supplierReturn.setNote(normalizedNote);
        }

        return toResponse(supplierReturnRepository.save(supplierReturn));
    }

    private SupplierReturnEntity requireSupplierReturnForUpdate(Long supplierReturnId) {
        return supplierReturnRepository.findByIdForUpdate(supplierReturnId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "supplier return not found: " + supplierReturnId));
    }

    private SupplierEntity requireSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "supplier not found: " + supplierId));
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private Map<Long, ProductEntity> requireProducts(Set<Long> productIds) {
        List<ProductEntity> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            Set<Long> foundIds = products.stream().map(ProductEntity::getId).collect(Collectors.toSet());
            Long missingId = productIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "product not found: " + missingId);
        }
        return products.stream().collect(Collectors.toMap(ProductEntity::getId, product -> product));
    }

    private void validateSupplierStoreMerchantConsistency(SupplierEntity supplier, StoreLocationEntity storeLocation) {
        Long supplierMerchantId = supplier.getMerchant().getId();
        Long storeMerchantId = storeLocation.getMerchant().getId();
        if (!supplierMerchantId.equals(storeMerchantId)) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "supplier and storeLocation must belong to the same merchant");
        }
    }

    private void validateProductsBelongToMerchant(Long merchantId, java.util.Collection<ProductEntity> products) {
        for (ProductEntity product : products) {
            if (!merchantId.equals(product.getMerchant().getId())) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "product does not belong to the same merchant as supplier return");
            }
        }
    }

    private Map<Long, ReturnLineInput> normalizeCreateLines(List<SupplierReturnCreateLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lines is required");
        }

        Map<Long, ReturnLineInput> normalized = new LinkedHashMap<>();
        for (SupplierReturnCreateLineRequest line : lines) {
            if (line == null || line.productId() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            BigDecimal quantity = normalizePositiveQuantity(line.returnQuantity(), "returnQuantity is required");
            BigDecimal unitCost = normalizePositiveCost(line.unitCost(), "unitCost is required");
            if (normalized.putIfAbsent(line.productId(), new ReturnLineInput(quantity, unitCost)) != null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate productId in supplier return lines: " + line.productId());
            }
        }
        return normalized;
    }

    private void validateEligibility(Map<Long, ReturnLineInput> lines,
                                     Long supplierId,
                                     Long storeLocationId) {
        for (Map.Entry<Long, ReturnLineInput> entry : lines.entrySet()) {
            Long productId = entry.getKey();
            BigDecimal requestedQuantity = entry.getValue().quantity();

            BigDecimal totalReceived = balanceCalculator.normalizeScale(
                    purchaseOrderRepository.sumReceivedQuantityBySupplierStoreAndProduct(
                            supplierId,
                            storeLocationId,
                            productId,
                            PurchaseOrderStatus.PARTIALLY_RECEIVED,
                            PurchaseOrderStatus.RECEIVED));
            BigDecimal totalReturned = balanceCalculator.normalizeScale(
                    supplierReturnRepository.sumReturnQuantityBySupplierStoreAndProduct(
                            supplierId,
                            storeLocationId,
                            productId,
                            SupplierReturnStatus.POSTED));
            BigDecimal eligibleQuantity = balanceCalculator.normalizeScale(totalReceived.subtract(totalReturned));
            if (eligibleQuantity.signum() < 0) {
                eligibleQuantity = BigDecimal.ZERO.setScale(3);
            }

            if (requestedQuantity.compareTo(eligibleQuantity) > 0) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "returnQuantity exceeds received-eligible quantity for productId: " + productId);
            }

            BigDecimal onHand = computeCurrentProductBalance(storeLocationId, productId);
            if (requestedQuantity.compareTo(onHand) > 0) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "returnQuantity exceeds current on-hand quantity for productId: " + productId);
            }
        }
    }

    private BigDecimal computeCurrentProductBalance(Long storeLocationId, Long productId) {
        return inventoryMovementRepository.sumByStoreLocationAndProduct(storeLocationId, productId)
                .stream()
                .findFirst()
                .map(InventoryMovementRepository.ProductBalanceProjection::getQuantityOnHand)
                .map(balanceCalculator::normalizeScale)
                .orElse(BigDecimal.ZERO.setScale(3));
    }

    private BigDecimal normalizePositiveQuantity(BigDecimal quantity, String requiredMessage) {
        if (quantity == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, requiredMessage);
        }
        BigDecimal normalized = balanceCalculator.normalizeScale(quantity);
        if (normalized.signum() <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    requiredMessage.replace(" is required", " must be greater than zero"));
        }
        return normalized;
    }

    private BigDecimal normalizePositiveCost(BigDecimal unitCost, String requiredMessage) {
        if (unitCost == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, requiredMessage);
        }
        BigDecimal normalized = costingCalculator.normalizeCostScale(unitCost);
        if (normalized.signum() <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    requiredMessage.replace(" is required", " must be greater than zero"));
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

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        String normalized = authentication.getName().trim();
        return normalized.isEmpty() ? "system" : normalized;
    }

    private String generateReferenceNumber() {
        return "SR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
    }

    private SupplierReturnResponse toResponse(SupplierReturnEntity supplierReturn) {
        List<SupplierReturnLineResponse> lines = supplierReturn.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .map(line -> {
                    BigDecimal quantity = balanceCalculator.normalizeScale(line.getReturnQuantity());
                    BigDecimal unitCost = costingCalculator.normalizeCostScale(line.getUnitCost());
                    BigDecimal lineTotal = costingCalculator.normalizeCostScale(quantity.multiply(unitCost));
                    return new SupplierReturnLineResponse(
                            line.getProduct().getId(),
                            line.getProduct().getSku(),
                            line.getProduct().getName(),
                            quantity,
                            unitCost,
                            lineTotal,
                            line.getInventoryMovementId());
                })
                .toList();

        BigDecimal totalCost = lines.stream()
                .map(SupplierReturnLineResponse::lineTotal)
                .reduce(BigDecimal.ZERO.setScale(4), BigDecimal::add);

        return new SupplierReturnResponse(
                supplierReturn.getId(),
                supplierReturn.getSupplier().getId(),
                supplierReturn.getStoreLocation().getId(),
                supplierReturn.getReferenceNumber(),
                com.saulpos.api.inventory.SupplierReturnStatus.valueOf(supplierReturn.getStatus().name()),
                supplierReturn.getNote(),
                costingCalculator.normalizeCostScale(totalCost),
                supplierReturn.getCreatedBy(),
                supplierReturn.getCreatedAt(),
                supplierReturn.getApprovedBy(),
                supplierReturn.getApprovedAt(),
                supplierReturn.getPostedBy(),
                supplierReturn.getPostedAt(),
                lines);
    }

    private record ReturnLineInput(BigDecimal quantity, BigDecimal unitCost) {
    }
}
