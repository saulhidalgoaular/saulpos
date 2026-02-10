package com.saulpos.server.inventory.service;

import com.saulpos.api.inventory.GoodsReceiptResponse;
import com.saulpos.api.inventory.PurchaseOrderApproveRequest;
import com.saulpos.api.inventory.PurchaseOrderCreateLineRequest;
import com.saulpos.api.inventory.PurchaseOrderCreateRequest;
import com.saulpos.api.inventory.PurchaseOrderLineResponse;
import com.saulpos.api.inventory.PurchaseOrderReceiveLineRequest;
import com.saulpos.api.inventory.PurchaseOrderReceiveLotRequest;
import com.saulpos.api.inventory.PurchaseOrderReceiveRequest;
import com.saulpos.api.inventory.PurchaseOrderResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.inventory.model.GoodsReceiptEntity;
import com.saulpos.server.inventory.model.PurchaseOrderEntity;
import com.saulpos.server.inventory.model.PurchaseOrderLineEntity;
import com.saulpos.server.inventory.model.PurchaseOrderStatus;
import com.saulpos.server.inventory.repository.PurchaseOrderRepository;
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
import java.util.ArrayList;
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
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryBalanceCalculator balanceCalculator;
    private final CostingCalculator costingCalculator;
    private final InventoryCostingService inventoryCostingService;
    private final InventoryLotService inventoryLotService;
    private final Clock clock;

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreateRequest request) {
        SupplierEntity supplier = requireSupplier(request.supplierId());
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        validateSupplierStoreMerchantConsistency(supplier, storeLocation);

        Map<Long, BigDecimal> orderedByProduct = normalizeCreateLines(request.lines());
        Map<Long, ProductEntity> productsById = requireProducts(orderedByProduct.keySet());
        validateProductsBelongToMerchant(supplier.getMerchant().getId(), productsById.values());

        PurchaseOrderEntity order = new PurchaseOrderEntity();
        order.setSupplier(supplier);
        order.setStoreLocation(storeLocation);
        order.setStatus(PurchaseOrderStatus.DRAFT);
        order.setReferenceNumber(generatePurchaseOrderReference());
        order.setNote(normalizeOptionalNote(request.note()));
        order.setCreatedBy(resolveActorUsername());
        order.setCreatedAt(Instant.now(clock));

        orderedByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    PurchaseOrderLineEntity line = new PurchaseOrderLineEntity();
                    line.setProduct(productsById.get(entry.getKey()));
                    line.setOrderedQuantity(entry.getValue());
                    line.setReceivedQuantity(balanceCalculator.normalizeScale(BigDecimal.ZERO));
                    order.addLine(line);
                });

        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrderEntity order = purchaseOrderRepository.findByIdWithDetails(purchaseOrderId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "purchase order not found: " + purchaseOrderId));
        return toResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long purchaseOrderId, PurchaseOrderApproveRequest request) {
        PurchaseOrderEntity order = requirePurchaseOrderForUpdate(purchaseOrderId);
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "purchase order can only be approved from DRAFT status: " + purchaseOrderId);
        }

        order.setStatus(PurchaseOrderStatus.APPROVED);
        order.setApprovedBy(resolveActorUsername());
        order.setApprovedAt(Instant.now(clock));
        String normalizedNote = normalizeOptionalNote(request == null ? null : request.note());
        if (normalizedNote != null) {
            order.setNote(normalizedNote);
        }

        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(Long purchaseOrderId, PurchaseOrderReceiveRequest request) {
        PurchaseOrderEntity order = requirePurchaseOrderForUpdate(purchaseOrderId);
        if (order.getStatus() != PurchaseOrderStatus.APPROVED
                && order.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "purchase order can only be received from APPROVED or PARTIALLY_RECEIVED status: " + purchaseOrderId);
        }

        Map<Long, ReceiveLineInput> receiveByProduct = normalizeReceiveLines(request.lines());
        Map<Long, PurchaseOrderLineEntity> linesByProduct = order.getLines().stream()
                .collect(Collectors.toMap(line -> line.getProduct().getId(), line -> line));

        Instant now = Instant.now(clock);
        String actor = resolveActorUsername();
        String receiptNumber = generateGoodsReceiptReference();
        List<ReceiveMovementDraft> movementDrafts = new ArrayList<>();
        for (Map.Entry<Long, ReceiveLineInput> entry : receiveByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            Long productId = entry.getKey();
            ReceiveLineInput receiveLineInput = entry.getValue();
            PurchaseOrderLineEntity line = linesByProduct.get(productId);
            if (line == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "productId is not part of purchase order: " + productId);
            }

            BigDecimal ordered = balanceCalculator.normalizeScale(line.getOrderedQuantity());
            BigDecimal received = balanceCalculator.normalizeScale(line.getReceivedQuantity());
            BigDecimal remaining = balanceCalculator.normalizeScale(ordered.subtract(received));
            BigDecimal receiveNow = receiveLineInput.quantity();
            if (receiveNow.compareTo(remaining) > 0) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "receivedQuantity exceeds remaining ordered quantity for productId: " + productId);
            }
            BigDecimal onHandBeforeReceipt = computeCurrentProductBalance(order.getStoreLocation().getId(), productId);

            BigDecimal updatedReceived = balanceCalculator.normalizeScale(received.add(receiveNow));
            line.setReceivedQuantity(updatedReceived);

            InventoryMovementEntity movement = new InventoryMovementEntity();
            movement.setStoreLocation(order.getStoreLocation());
            movement.setProduct(line.getProduct());
            movement.setSale(null);
            movement.setSaleLine(null);
            movement.setMovementType(InventoryMovementType.ADJUSTMENT);
            movement.setQuantityDelta(receiveNow);
            movement.setReferenceType(InventoryReferenceType.PURCHASE_RECEIPT);
            movement.setReferenceNumber(receiptNumber + "-P" + productId);

            List<InventoryLotService.LotAllocation> lotAllocations = inventoryLotService.allocateReceiptLots(
                    order.getStoreLocation(),
                    line.getProduct(),
                    receiveNow,
                    receiveLineInput.lots());
            movementDrafts.add(new ReceiveMovementDraft(
                    movement,
                    lotAllocations,
                    onHandBeforeReceipt,
                    receiveLineInput.unitCost()));
        }

        GoodsReceiptEntity receipt = new GoodsReceiptEntity();
        receipt.setReceiptNumber(receiptNumber);
        receipt.setReceivedBy(actor);
        receipt.setReceivedAt(now);
        receipt.setNote(normalizeOptionalNote(request.note()));
        order.addReceipt(receipt);

        inventoryMovementRepository.saveAll(movementDrafts.stream()
                .map(ReceiveMovementDraft::movement)
                .toList());
        for (ReceiveMovementDraft movementDraft : movementDrafts) {
            inventoryLotService.persistMovementLotAllocations(
                    movementDraft.movement(),
                    movementDraft.lotAllocations());
            inventoryCostingService.applyPurchaseReceiptCosting(
                    movementDraft.movement().getStoreLocation(),
                    movementDraft.movement().getProduct(),
                    movementDraft.onHandBeforeReceipt(),
                    movementDraft.movement().getQuantityDelta(),
                    movementDraft.unitCost(),
                    movementDraft.movement().getReferenceNumber(),
                    movementDraft.movement());
        }

        boolean fullyReceived = order.getLines().stream().allMatch(line -> {
            BigDecimal ordered = balanceCalculator.normalizeScale(line.getOrderedQuantity());
            BigDecimal received = balanceCalculator.normalizeScale(line.getReceivedQuantity());
            return received.compareTo(ordered) == 0;
        });

        order.setStatus(fullyReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        order.setLastReceivedBy(actor);
        order.setLastReceivedAt(now);

        return toResponse(purchaseOrderRepository.save(order));
    }

    private PurchaseOrderEntity requirePurchaseOrderForUpdate(Long purchaseOrderId) {
        return purchaseOrderRepository.findByIdForUpdate(purchaseOrderId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "purchase order not found: " + purchaseOrderId));
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
                        "product does not belong to the same merchant as purchase order");
            }
        }
    }

    private Map<Long, BigDecimal> normalizeCreateLines(List<PurchaseOrderCreateLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lines is required");
        }

        Map<Long, BigDecimal> normalized = new LinkedHashMap<>();
        for (PurchaseOrderCreateLineRequest line : lines) {
            if (line == null || line.productId() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            BigDecimal ordered = normalizePositiveQuantity(line.orderedQuantity(), "orderedQuantity is required");
            if (normalized.putIfAbsent(line.productId(), ordered) != null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate productId in purchase order lines: " + line.productId());
            }
        }
        return normalized;
    }

    private Map<Long, ReceiveLineInput> normalizeReceiveLines(List<PurchaseOrderReceiveLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lines is required");
        }

        Map<Long, ReceiveLineInput> normalized = new LinkedHashMap<>();
        for (PurchaseOrderReceiveLineRequest line : lines) {
            if (line == null || line.productId() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            BigDecimal receiveNow = normalizePositiveQuantity(line.receivedQuantity(), "receivedQuantity is required");
            BigDecimal unitCost = normalizePositiveCost(line.unitCost(), "unitCost is required");
            List<PurchaseOrderReceiveLotRequest> lots = line.lots() == null ? List.of() : List.copyOf(line.lots());
            if (normalized.putIfAbsent(line.productId(), new ReceiveLineInput(receiveNow, unitCost, lots)) != null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate productId in receive lines: " + line.productId());
            }
        }
        return normalized;
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

    private BigDecimal computeCurrentProductBalance(Long storeLocationId, Long productId) {
        return inventoryMovementRepository.sumByStoreLocationAndProduct(storeLocationId, productId)
                .stream()
                .findFirst()
                .map(InventoryMovementRepository.ProductBalanceProjection::getQuantityOnHand)
                .map(balanceCalculator::normalizeScale)
                .orElse(BigDecimal.ZERO.setScale(3));
    }

    private PurchaseOrderResponse toResponse(PurchaseOrderEntity order) {
        List<PurchaseOrderLineResponse> lines = order.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .map(line -> {
                    BigDecimal ordered = balanceCalculator.normalizeScale(line.getOrderedQuantity());
                    BigDecimal received = balanceCalculator.normalizeScale(line.getReceivedQuantity());
                    BigDecimal remaining = balanceCalculator.normalizeScale(ordered.subtract(received));
                    return new PurchaseOrderLineResponse(
                            line.getProduct().getId(),
                            line.getProduct().getSku(),
                            line.getProduct().getName(),
                            ordered,
                            received,
                            remaining);
                })
                .toList();

        List<GoodsReceiptResponse> receipts = order.getReceipts().stream()
                .sorted(Comparator.comparing(GoodsReceiptEntity::getReceivedAt)
                        .thenComparing(GoodsReceiptEntity::getId))
                .map(receipt -> new GoodsReceiptResponse(
                        receipt.getId(),
                        receipt.getReceiptNumber(),
                        receipt.getReceivedBy(),
                        receipt.getReceivedAt(),
                        receipt.getNote()))
                .toList();

        return new PurchaseOrderResponse(
                order.getId(),
                order.getSupplier().getId(),
                order.getStoreLocation().getId(),
                order.getReferenceNumber(),
                com.saulpos.api.inventory.PurchaseOrderStatus.valueOf(order.getStatus().name()),
                order.getNote(),
                order.getCreatedBy(),
                order.getCreatedAt(),
                order.getApprovedBy(),
                order.getApprovedAt(),
                order.getLastReceivedBy(),
                order.getLastReceivedAt(),
                lines,
                receipts);
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

    private String generatePurchaseOrderReference() {
        return "PO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
    }

    private String generateGoodsReceiptReference() {
        return "GR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
    }

    private record ReceiveLineInput(BigDecimal quantity,
                                    BigDecimal unitCost,
                                    List<PurchaseOrderReceiveLotRequest> lots) {
    }

    private record ReceiveMovementDraft(InventoryMovementEntity movement,
                                        List<InventoryLotService.LotAllocation> lotAllocations,
                                        BigDecimal onHandBeforeReceipt,
                                        BigDecimal unitCost) {
    }
}
