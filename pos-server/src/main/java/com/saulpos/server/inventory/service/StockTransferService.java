package com.saulpos.server.inventory.service;

import com.saulpos.api.inventory.StockTransferCreateLineRequest;
import com.saulpos.api.inventory.StockTransferCreateRequest;
import com.saulpos.api.inventory.StockTransferLineResponse;
import com.saulpos.api.inventory.StockTransferReceiveLineRequest;
import com.saulpos.api.inventory.StockTransferReceiveRequest;
import com.saulpos.api.inventory.StockTransferResponse;
import com.saulpos.api.inventory.StockTransferShipLineRequest;
import com.saulpos.api.inventory.StockTransferShipRequest;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.inventory.model.StockTransferEntity;
import com.saulpos.server.inventory.model.StockTransferLineEntity;
import com.saulpos.server.inventory.model.StockTransferStatus;
import com.saulpos.server.inventory.repository.StockTransferRepository;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.InventoryMovementType;
import com.saulpos.server.sale.model.InventoryReferenceType;
import com.saulpos.server.sale.repository.InventoryMovementRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryBalanceCalculator balanceCalculator;
    private final Clock clock;

    @Transactional
    public StockTransferResponse createTransfer(StockTransferCreateRequest request) {
        StoreLocationEntity sourceStore = requireStoreLocation(request.sourceStoreLocationId());
        StoreLocationEntity destinationStore = requireStoreLocation(request.destinationStoreLocationId());
        validateStorePair(sourceStore, destinationStore);

        Map<Long, BigDecimal> requestedByProduct = normalizeCreateLines(request.lines());
        Map<Long, ProductEntity> productsById = requireProducts(requestedByProduct.keySet());
        validateProductsBelongToStoreMerchant(sourceStore, destinationStore, productsById.values());

        StockTransferEntity transfer = new StockTransferEntity();
        transfer.setSourceStoreLocation(sourceStore);
        transfer.setDestinationStoreLocation(destinationStore);
        transfer.setStatus(StockTransferStatus.DRAFT);
        transfer.setReferenceNumber(generateReferenceNumber());
        transfer.setNote(normalizeOptionalNote(request.note()));
        transfer.setCreatedBy(resolveActorUsername());
        transfer.setCreatedAt(Instant.now(clock));

        requestedByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    StockTransferLineEntity line = new StockTransferLineEntity();
                    line.setProduct(productsById.get(entry.getKey()));
                    line.setRequestedQuantity(entry.getValue());
                    line.setShippedQuantity(null);
                    line.setReceivedQuantity(balanceCalculator.normalizeScale(BigDecimal.ZERO));
                    transfer.addLine(line);
                });

        return toResponse(stockTransferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public StockTransferResponse getTransfer(Long transferId) {
        StockTransferEntity transfer = stockTransferRepository.findByIdWithLines(transferId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "stock transfer not found: " + transferId));
        return toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse shipTransfer(Long transferId, StockTransferShipRequest request) {
        StockTransferEntity transfer = requireTransferForUpdate(transferId);

        if (transfer.getStatus() != StockTransferStatus.DRAFT) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock transfer can only be shipped from DRAFT status: " + transferId);
        }

        Map<Long, BigDecimal> shippedByProduct = normalizeShipLines(request.lines());
        Set<Long> transferProductIds = transfer.getLines().stream()
                .map(line -> line.getProduct().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!transferProductIds.equals(shippedByProduct.keySet())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "ship lines must include exactly the transfer products");
        }

        List<InventoryMovementEntity> movements = new ArrayList<>();
        for (StockTransferLineEntity line : transfer.getLines()) {
            BigDecimal requested = balanceCalculator.normalizeScale(line.getRequestedQuantity());
            BigDecimal shipped = shippedByProduct.get(line.getProduct().getId());
            if (shipped.compareTo(requested) > 0) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "shippedQuantity cannot exceed requestedQuantity for productId: " + line.getProduct().getId());
            }

            line.setShippedQuantity(shipped);
            line.setReceivedQuantity(balanceCalculator.normalizeScale(BigDecimal.ZERO));

            InventoryMovementEntity outbound = createMovement(
                    transfer.getSourceStoreLocation(),
                    line.getProduct(),
                    shipped.negate(),
                    InventoryReferenceType.STOCK_TRANSFER_OUT,
                    buildOutboundReference(transfer.getReferenceNumber(), line.getProduct().getId()));
            movements.add(outbound);
        }

        inventoryMovementRepository.saveAll(movements);

        transfer.setStatus(StockTransferStatus.SHIPPED);
        transfer.setShippedBy(resolveActorUsername());
        transfer.setShippedAt(Instant.now(clock));
        String normalizedNote = normalizeOptionalNote(request.note());
        if (normalizedNote != null) {
            transfer.setNote(normalizedNote);
        }

        return toResponse(stockTransferRepository.save(transfer));
    }

    @Transactional
    public StockTransferResponse receiveTransfer(Long transferId, StockTransferReceiveRequest request) {
        StockTransferEntity transfer = requireTransferForUpdate(transferId);

        if (transfer.getStatus() != StockTransferStatus.SHIPPED
                && transfer.getStatus() != StockTransferStatus.PARTIALLY_RECEIVED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stock transfer can only be received from SHIPPED or PARTIALLY_RECEIVED status: " + transferId);
        }

        Map<Long, BigDecimal> receivedByProduct = normalizeReceiveLines(request.lines());
        Map<Long, StockTransferLineEntity> linesByProduct = transfer.getLines().stream()
                .collect(Collectors.toMap(line -> line.getProduct().getId(), line -> line));

        List<InventoryMovementEntity> inboundMovements = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : receivedByProduct.entrySet()) {
            Long productId = entry.getKey();
            StockTransferLineEntity line = linesByProduct.get(productId);
            if (line == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "productId is not part of transfer: " + productId);
            }
            if (line.getShippedQuantity() == null) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "transfer line is not shipped for productId: " + productId);
            }

            BigDecimal shipped = balanceCalculator.normalizeScale(line.getShippedQuantity());
            BigDecimal received = balanceCalculator.normalizeScale(line.getReceivedQuantity());
            BigDecimal receiveNow = entry.getValue();
            BigDecimal remaining = balanceCalculator.normalizeScale(shipped.subtract(received));
            if (receiveNow.compareTo(remaining) > 0) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "receivedQuantity exceeds remaining shipped quantity for productId: " + productId);
            }

            BigDecimal updatedReceived = balanceCalculator.normalizeScale(received.add(receiveNow));
            line.setReceivedQuantity(updatedReceived);

            InventoryMovementEntity inbound = createMovement(
                    transfer.getDestinationStoreLocation(),
                    line.getProduct(),
                    receiveNow,
                    InventoryReferenceType.STOCK_TRANSFER_IN,
                    buildInboundReference(transfer.getReferenceNumber(), productId));
            inboundMovements.add(inbound);
        }

        inventoryMovementRepository.saveAll(inboundMovements);

        boolean allReconciled = transfer.getLines().stream().allMatch(line -> {
            if (line.getShippedQuantity() == null) {
                return false;
            }
            BigDecimal shipped = balanceCalculator.normalizeScale(line.getShippedQuantity());
            BigDecimal received = balanceCalculator.normalizeScale(line.getReceivedQuantity());
            return received.compareTo(shipped) == 0;
        });

        transfer.setStatus(allReconciled ? StockTransferStatus.RECEIVED : StockTransferStatus.PARTIALLY_RECEIVED);
        transfer.setReceivedBy(resolveActorUsername());
        transfer.setReceivedAt(Instant.now(clock));
        String normalizedNote = normalizeOptionalNote(request.note());
        if (normalizedNote != null) {
            transfer.setNote(normalizedNote);
        }

        return toResponse(stockTransferRepository.save(transfer));
    }

    private StockTransferEntity requireTransferForUpdate(Long transferId) {
        return stockTransferRepository.findByIdForUpdate(transferId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "stock transfer not found: " + transferId));
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

    private void validateStorePair(StoreLocationEntity sourceStore, StoreLocationEntity destinationStore) {
        if (Objects.equals(sourceStore.getId(), destinationStore.getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "source and destination stores must be different");
        }
        if (!Objects.equals(sourceStore.getMerchant().getId(), destinationStore.getMerchant().getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "source and destination stores must belong to the same merchant");
        }
    }

    private void validateProductsBelongToStoreMerchant(StoreLocationEntity sourceStore,
                                                       StoreLocationEntity destinationStore,
                                                       java.util.Collection<ProductEntity> products) {
        Long sourceMerchantId = sourceStore.getMerchant().getId();
        Long destinationMerchantId = destinationStore.getMerchant().getId();
        for (ProductEntity product : products) {
            Long productMerchantId = product.getMerchant().getId();
            if (!Objects.equals(productMerchantId, sourceMerchantId)
                    || !Objects.equals(productMerchantId, destinationMerchantId)) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "product does not belong to the same merchant as transfer stores");
            }
        }
    }

    private Map<Long, BigDecimal> normalizeCreateLines(List<StockTransferCreateLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lines is required");
        }

        Map<Long, BigDecimal> normalized = new LinkedHashMap<>();
        for (StockTransferCreateLineRequest line : lines) {
            if (line == null || line.productId() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            BigDecimal quantity = normalizePositiveQuantity(line.requestedQuantity(), "requestedQuantity is required");
            if (normalized.putIfAbsent(line.productId(), quantity) != null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate productId in transfer lines: " + line.productId());
            }
        }
        return normalized;
    }

    private Map<Long, BigDecimal> normalizeShipLines(List<StockTransferShipLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lines is required");
        }

        Map<Long, BigDecimal> normalized = new LinkedHashMap<>();
        for (StockTransferShipLineRequest line : lines) {
            if (line == null || line.productId() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            BigDecimal quantity = normalizePositiveQuantity(line.shippedQuantity(), "shippedQuantity is required");
            if (normalized.putIfAbsent(line.productId(), quantity) != null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate productId in ship lines: " + line.productId());
            }
        }
        return normalized;
    }

    private Map<Long, BigDecimal> normalizeReceiveLines(List<StockTransferReceiveLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lines is required");
        }

        Map<Long, BigDecimal> normalized = new LinkedHashMap<>();
        for (StockTransferReceiveLineRequest line : lines) {
            if (line == null || line.productId() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            BigDecimal quantity = normalizePositiveQuantity(line.receivedQuantity(), "receivedQuantity is required");
            if (normalized.putIfAbsent(line.productId(), quantity) != null) {
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

    private InventoryMovementEntity createMovement(StoreLocationEntity storeLocation,
                                                   ProductEntity product,
                                                   BigDecimal quantityDelta,
                                                   InventoryReferenceType referenceType,
                                                   String referenceNumber) {
        InventoryMovementEntity movement = new InventoryMovementEntity();
        movement.setStoreLocation(storeLocation);
        movement.setProduct(product);
        movement.setSale(null);
        movement.setSaleLine(null);
        movement.setMovementType(InventoryMovementType.ADJUSTMENT);
        movement.setQuantityDelta(balanceCalculator.normalizeScale(quantityDelta));
        movement.setReferenceType(referenceType);
        movement.setReferenceNumber(referenceNumber);
        return movement;
    }

    private StockTransferResponse toResponse(StockTransferEntity transfer) {
        List<StockTransferLineResponse> lines = transfer.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .map(line -> {
                    BigDecimal requested = balanceCalculator.normalizeScale(line.getRequestedQuantity());
                    BigDecimal shipped = line.getShippedQuantity() == null
                            ? null
                            : balanceCalculator.normalizeScale(line.getShippedQuantity());
                    BigDecimal received = balanceCalculator.normalizeScale(line.getReceivedQuantity());
                    BigDecimal remaining = shipped == null
                            ? null
                            : balanceCalculator.normalizeScale(shipped.subtract(received));
                    return new StockTransferLineResponse(
                            line.getProduct().getId(),
                            line.getProduct().getSku(),
                            line.getProduct().getName(),
                            requested,
                            shipped,
                            received,
                            remaining);
                })
                .toList();

        return new StockTransferResponse(
                transfer.getId(),
                transfer.getSourceStoreLocation().getId(),
                transfer.getDestinationStoreLocation().getId(),
                transfer.getReferenceNumber(),
                com.saulpos.api.inventory.StockTransferStatus.valueOf(transfer.getStatus().name()),
                transfer.getNote(),
                transfer.getCreatedBy(),
                transfer.getCreatedAt(),
                transfer.getShippedBy(),
                transfer.getShippedAt(),
                transfer.getReceivedBy(),
                transfer.getReceivedAt(),
                lines);
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
        return "TRF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
    }

    private String buildOutboundReference(String transferReference, Long productId) {
        return transferReference + "-P" + productId + "-OUT";
    }

    private String buildInboundReference(String transferReference, Long productId) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return transferReference + "-P" + productId + "-IN-" + suffix;
    }
}
