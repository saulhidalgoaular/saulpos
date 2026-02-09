package com.saulpos.server.inventory.service;

import com.saulpos.api.inventory.InventoryMovementCreateRequest;
import com.saulpos.api.inventory.InventoryMovementResponse;
import com.saulpos.api.inventory.InventoryReferenceType;
import com.saulpos.api.inventory.InventoryStockBalanceResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.repository.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryLedgerService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceCalculator balanceCalculator;

    @Transactional
    public InventoryMovementResponse createMovement(InventoryMovementCreateRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        ProductEntity product = requireProduct(request.productId());

        com.saulpos.server.sale.model.InventoryMovementType movementType = requireMovementType(request);
        com.saulpos.server.sale.model.InventoryReferenceType referenceType = requireReferenceType(request, movementType);
        BigDecimal quantityDelta = requireValidQuantity(request, movementType);
        String referenceNumber = normalizeRequiredText(request.referenceNumber(), "referenceNumber is required");

        InventoryMovementEntity movement = new InventoryMovementEntity();
        movement.setStoreLocation(storeLocation);
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantityDelta(quantityDelta);
        movement.setReferenceType(referenceType);
        movement.setReferenceNumber(referenceNumber);
        movement.setSale(null);
        movement.setSaleLine(null);

        InventoryMovementEntity saved = inventoryMovementRepository.save(movement);
        BigDecimal runningBalance = computeCurrentProductBalance(storeLocation.getId(), product.getId());
        return toMovementResponse(saved, runningBalance);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> listMovements(Long storeLocationId, Long productId) {
        requireStoreLocation(storeLocationId);
        if (productId != null) {
            requireProduct(productId);
        }

        List<InventoryMovementEntity> entries = inventoryMovementRepository.findLedgerEntries(storeLocationId, productId);
        Map<Long, BigDecimal> runningByProduct = new LinkedHashMap<>();
        List<InventoryMovementResponse> responses = new ArrayList<>(entries.size());

        for (InventoryMovementEntity entry : entries) {
            Long entryProductId = entry.getProduct().getId();
            BigDecimal runningBalance = balanceCalculator.add(
                    runningByProduct.getOrDefault(entryProductId, BigDecimal.ZERO),
                    entry.getQuantityDelta());
            runningByProduct.put(entryProductId, runningBalance);
            responses.add(toMovementResponse(entry, runningBalance));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<InventoryStockBalanceResponse> listStockBalances(Long storeLocationId, Long productId) {
        requireStoreLocation(storeLocationId);
        if (productId != null) {
            requireProduct(productId);
        }

        return inventoryMovementRepository.sumByStoreLocationAndProduct(storeLocationId, productId)
                .stream()
                .map(balance -> new InventoryStockBalanceResponse(
                        storeLocationId,
                        balance.getProductId(),
                        balanceCalculator.normalizeScale(balance.getQuantityOnHand())))
                .toList();
    }

    private BigDecimal computeCurrentProductBalance(Long storeLocationId, Long productId) {
        return inventoryMovementRepository.sumByStoreLocationAndProduct(storeLocationId, productId)
                .stream()
                .findFirst()
                .map(InventoryMovementRepository.ProductBalanceProjection::getQuantityOnHand)
                .map(balanceCalculator::normalizeScale)
                .orElse(BigDecimal.ZERO.setScale(3));
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovementEntity entity,
                                                         BigDecimal runningBalance) {
        return new InventoryMovementResponse(
                entity.getId(),
                entity.getStoreLocation().getId(),
                entity.getProduct().getId(),
                com.saulpos.api.inventory.InventoryMovementType.valueOf(entity.getMovementType().name()),
                balanceCalculator.normalizeScale(entity.getQuantityDelta()),
                InventoryReferenceType.valueOf(entity.getReferenceType().name()),
                entity.getReferenceNumber(),
                entity.getSale() == null ? null : entity.getSale().getId(),
                entity.getSaleLine() == null ? null : entity.getSaleLine().getId(),
                balanceCalculator.normalizeScale(runningBalance),
                entity.getCreatedAt());
    }

    private com.saulpos.server.sale.model.InventoryMovementType requireMovementType(InventoryMovementCreateRequest request) {
        com.saulpos.server.sale.model.InventoryMovementType movementType =
                com.saulpos.server.sale.model.InventoryMovementType.valueOf(request.movementType().name());
        if (movementType == com.saulpos.server.sale.model.InventoryMovementType.SALE) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "SALE movement is system-generated and cannot be created manually");
        }
        return movementType;
    }

    private com.saulpos.server.sale.model.InventoryReferenceType requireReferenceType(
            InventoryMovementCreateRequest request,
            com.saulpos.server.sale.model.InventoryMovementType movementType) {
        com.saulpos.server.sale.model.InventoryReferenceType referenceType =
                com.saulpos.server.sale.model.InventoryReferenceType.valueOf(request.referenceType().name());

        if (movementType == com.saulpos.server.sale.model.InventoryMovementType.RETURN
                && referenceType != com.saulpos.server.sale.model.InventoryReferenceType.SALE_RETURN) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "RETURN movement must use referenceType SALE_RETURN");
        }

        if (movementType == com.saulpos.server.sale.model.InventoryMovementType.ADJUSTMENT
                && referenceType != com.saulpos.server.sale.model.InventoryReferenceType.STOCK_ADJUSTMENT) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "ADJUSTMENT movement must use referenceType STOCK_ADJUSTMENT");
        }

        return referenceType;
    }

    private BigDecimal requireValidQuantity(InventoryMovementCreateRequest request,
                                            com.saulpos.server.sale.model.InventoryMovementType movementType) {
        BigDecimal quantityDelta = balanceCalculator.normalizeScale(request.quantityDelta());
        if (quantityDelta.signum() == 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "quantityDelta must be non-zero");
        }

        if (movementType == com.saulpos.server.sale.model.InventoryMovementType.RETURN
                && quantityDelta.signum() < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "RETURN movement quantityDelta must be greater than zero");
        }

        return quantityDelta;
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

    private String normalizeRequiredText(String value, String message) {
        if (value == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, message);
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, message);
        }
        return normalized;
    }
}
