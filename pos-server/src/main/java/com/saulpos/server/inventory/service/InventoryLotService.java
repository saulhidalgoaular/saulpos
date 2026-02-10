package com.saulpos.server.inventory.service;

import com.saulpos.api.inventory.InventoryExpiryState;
import com.saulpos.api.inventory.PurchaseOrderReceiveLotRequest;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.inventory.model.InventoryLotBalanceEntity;
import com.saulpos.server.inventory.model.InventoryLotEntity;
import com.saulpos.server.inventory.model.InventoryMovementLotEntity;
import com.saulpos.server.inventory.repository.InventoryLotBalanceRepository;
import com.saulpos.server.inventory.repository.InventoryLotRepository;
import com.saulpos.server.inventory.repository.InventoryMovementLotRepository;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.SaleLineEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryLotService {

    private final InventoryLotRepository inventoryLotRepository;
    private final InventoryLotBalanceRepository inventoryLotBalanceRepository;
    private final InventoryMovementLotRepository inventoryMovementLotRepository;
    private final InventoryBalanceCalculator balanceCalculator;
    private final Clock clock;

    @Transactional
    public List<LotAllocation> allocateReceiptLots(StoreLocationEntity storeLocation,
                                                   ProductEntity product,
                                                   BigDecimal receivedQuantity,
                                                   List<PurchaseOrderReceiveLotRequest> lots) {
        BigDecimal normalizedReceivedQuantity = normalizePositiveQuantity(receivedQuantity, "receivedQuantity");

        if (!product.isLotTrackingEnabled()) {
            if (lots != null && !lots.isEmpty()) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "lot details are not allowed for product without lot tracking: " + product.getId());
            }
            return List.of();
        }

        if (lots == null || lots.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "lot details are required for lot-tracked product: " + product.getId());
        }

        Map<LotIdentity, BigDecimal> quantitiesByLot = new LinkedHashMap<>();
        for (PurchaseOrderReceiveLotRequest lotRequest : lots) {
            if (lotRequest == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "lot details cannot contain null entries");
            }

            String lotCode = normalizeLotCode(lotRequest.lotCode());
            BigDecimal lotQuantity = normalizePositiveQuantity(lotRequest.quantity(), "lot quantity");
            LotIdentity identity = new LotIdentity(lotCode, lotRequest.expiryDate());
            quantitiesByLot.merge(identity, lotQuantity, balanceCalculator::add);
        }

        BigDecimal totalLotQuantity = balanceCalculator.sum(new ArrayList<>(quantitiesByLot.values()));
        if (totalLotQuantity.compareTo(normalizedReceivedQuantity) != 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "sum of lot quantities must match receivedQuantity for productId: " + product.getId());
        }

        List<LotAllocation> allocations = new ArrayList<>();
        List<Map.Entry<LotIdentity, BigDecimal>> orderedLots = quantitiesByLot.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<LotIdentity, BigDecimal> entry) -> entry.getKey().expiryDate,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(entry -> entry.getKey().lotCode))
                .toList();

        for (Map.Entry<LotIdentity, BigDecimal> lotEntry : orderedLots) {
            LotIdentity identity = lotEntry.getKey();
            BigDecimal quantity = lotEntry.getValue();

            InventoryLotEntity lot = inventoryLotRepository.findForUpdateByIdentity(
                            storeLocation.getId(),
                            product.getId(),
                            identity.lotCode,
                            identity.expiryDate)
                    .orElseGet(() -> createLot(storeLocation, product, identity));

            InventoryLotBalanceEntity balance = inventoryLotBalanceRepository.findByInventoryLotIdForUpdate(lot.getId())
                    .orElseGet(() -> createLotBalance(lot));
            balance.setQuantityOnHand(balanceCalculator.add(balance.getQuantityOnHand(), quantity));
            inventoryLotBalanceRepository.save(balance);

            allocations.add(new LotAllocation(lot, quantity));
        }

        return allocations;
    }

    @Transactional
    public List<LotAllocation> allocateSaleLots(StoreLocationEntity storeLocation,
                                                ProductEntity product,
                                                BigDecimal requestedQuantity,
                                                boolean allowExpiredOverride) {
        BigDecimal remaining = normalizePositiveQuantity(requestedQuantity, "requested quantity");
        if (!product.isLotTrackingEnabled()) {
            return List.of();
        }

        LocalDate today = LocalDate.now(clock);
        List<InventoryLotBalanceEntity> balances = inventoryLotBalanceRepository.findPositiveBalancesForSaleForUpdate(
                storeLocation.getId(),
                product.getId());

        if (balances.isEmpty()) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "no lot stock available for lot-tracked product: " + product.getId());
        }

        List<LotAllocation> allocations = new ArrayList<>();
        boolean hasExpiredStock = false;
        for (InventoryLotBalanceEntity balance : balances) {
            if (remaining.signum() <= 0) {
                break;
            }

            InventoryLotEntity lot = balance.getInventoryLot();
            LocalDate expiryDate = lot.getExpiryDate();
            boolean expired = expiryDate != null && expiryDate.isBefore(today);
            if (expired && !allowExpiredOverride) {
                hasExpiredStock = true;
                continue;
            }

            BigDecimal available = balanceCalculator.normalizeScale(balance.getQuantityOnHand());
            if (available.signum() <= 0) {
                continue;
            }

            BigDecimal allocated = available.min(remaining);
            if (allocated.signum() <= 0) {
                continue;
            }

            balance.setQuantityOnHand(balanceCalculator.add(balance.getQuantityOnHand(), allocated.negate()));
            allocations.add(new LotAllocation(lot, allocated));
            remaining = balanceCalculator.add(remaining, allocated.negate());
        }

        if (remaining.signum() > 0) {
            if (hasExpiredStock && !allowExpiredOverride) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "insufficient non-expired lot stock for productId: " + product.getId());
            }
            throw new BaseException(ErrorCode.CONFLICT,
                    "insufficient lot stock for productId: " + product.getId());
        }

        return allocations;
    }

    @Transactional
    public List<LotAllocation> allocateReturnLots(SaleLineEntity saleLine, BigDecimal returnQuantity) {
        BigDecimal remaining = normalizePositiveQuantity(returnQuantity, "return quantity");
        ProductEntity product = saleLine.getProduct();
        if (!product.isLotTrackingEnabled()) {
            return List.of();
        }

        List<InventoryMovementLotRepository.LotQuantityProjection> soldByLot =
                inventoryMovementLotRepository.summarizeSoldBySaleLine(saleLine.getId());
        if (soldByLot.isEmpty()) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "missing lot traceability for saleLineId: " + saleLine.getId());
        }

        Map<Long, BigDecimal> returnedByLot = new LinkedHashMap<>();
        for (InventoryMovementLotRepository.LotQuantitySumProjection returnedProjection
                : inventoryMovementLotRepository.summarizeReturnedBySaleLine(saleLine.getId())) {
            returnedByLot.put(
                    returnedProjection.getInventoryLotId(),
                    balanceCalculator.normalizeScale(returnedProjection.getQuantity()));
        }

        List<LotAllocation> allocations = new ArrayList<>();
        for (InventoryMovementLotRepository.LotQuantityProjection soldProjection : soldByLot) {
            if (remaining.signum() <= 0) {
                break;
            }

            Long lotId = soldProjection.getInventoryLotId();
            BigDecimal soldQuantity = balanceCalculator.normalizeScale(soldProjection.getQuantity());
            BigDecimal returnedQuantity = returnedByLot.getOrDefault(lotId, balanceCalculator.normalizeScale(BigDecimal.ZERO));
            BigDecimal remainingForLot = balanceCalculator.add(soldQuantity, returnedQuantity.negate());

            if (remainingForLot.signum() <= 0) {
                continue;
            }

            BigDecimal allocated = remainingForLot.min(remaining);
            if (allocated.signum() <= 0) {
                continue;
            }

            InventoryLotEntity lot = inventoryLotRepository.findById(lotId)
                    .orElseThrow(() -> new BaseException(ErrorCode.CONFLICT,
                            "inventory lot not found for allocation: " + lotId));

            InventoryLotBalanceEntity balance = inventoryLotBalanceRepository.findByInventoryLotIdForUpdate(lotId)
                    .orElseGet(() -> createLotBalance(lot));
            balance.setQuantityOnHand(balanceCalculator.add(balance.getQuantityOnHand(), allocated));
            inventoryLotBalanceRepository.save(balance);

            allocations.add(new LotAllocation(lot, allocated));
            remaining = balanceCalculator.add(remaining, allocated.negate());
            returnedByLot.put(lotId, balanceCalculator.add(returnedQuantity, allocated));
        }

        if (remaining.signum() > 0) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "return quantity exceeds lot-traceable sold quantity for saleLineId: " + saleLine.getId());
        }

        return allocations;
    }

    @Transactional
    public void persistMovementLotAllocations(InventoryMovementEntity movement, List<LotAllocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return;
        }

        List<InventoryMovementLotEntity> entities = allocations.stream()
                .map(allocation -> {
                    InventoryMovementLotEntity entity = new InventoryMovementLotEntity();
                    entity.setInventoryMovement(movement);
                    entity.setInventoryLot(allocation.inventoryLot());
                    entity.setQuantity(balanceCalculator.normalizeScale(allocation.quantity()));
                    return entity;
                })
                .toList();

        inventoryMovementLotRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<InventoryMovementLotEntity>> findMovementLotsByMovementIds(Collection<Long> movementIds) {
        if (movementIds == null || movementIds.isEmpty()) {
            return Map.of();
        }

        List<InventoryMovementLotEntity> lots = inventoryMovementLotRepository.findByMovementIdsWithLot(movementIds);
        Map<Long, List<InventoryMovementLotEntity>> grouped = new LinkedHashMap<>();
        for (InventoryMovementLotEntity movementLot : lots) {
            Long movementId = movementLot.getInventoryMovement().getId();
            grouped.computeIfAbsent(movementId, ignored -> new ArrayList<>()).add(movementLot);
        }
        return grouped;
    }

    @Transactional(readOnly = true)
    public List<InventoryLotBalanceEntity> listPositiveLotBalances(Long storeLocationId, Long productId) {
        return inventoryLotBalanceRepository.findPositiveBalances(storeLocationId, productId);
    }

    public InventoryExpiryState resolveExpiryState(LocalDate expiryDate) {
        if (expiryDate == null) {
            return InventoryExpiryState.NO_EXPIRY;
        }
        return expiryDate.isBefore(LocalDate.now(clock))
                ? InventoryExpiryState.EXPIRED
                : InventoryExpiryState.ACTIVE;
    }

    private InventoryLotEntity createLot(StoreLocationEntity storeLocation, ProductEntity product, LotIdentity identity) {
        InventoryLotEntity lot = new InventoryLotEntity();
        lot.setStoreLocation(storeLocation);
        lot.setProduct(product);
        lot.setLotCode(identity.lotCode);
        lot.setExpiryDate(identity.expiryDate);
        return inventoryLotRepository.save(lot);
    }

    private InventoryLotBalanceEntity createLotBalance(InventoryLotEntity lot) {
        InventoryLotBalanceEntity balance = new InventoryLotBalanceEntity();
        balance.setInventoryLot(lot);
        balance.setQuantityOnHand(balanceCalculator.normalizeScale(BigDecimal.ZERO));
        return balance;
    }

    private BigDecimal normalizePositiveQuantity(BigDecimal quantity, String fieldName) {
        if (quantity == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, fieldName + " is required");
        }

        BigDecimal normalized = balanceCalculator.normalizeScale(quantity);
        if (normalized.signum() <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, fieldName + " must be greater than zero");
        }
        return normalized;
    }

    private String normalizeLotCode(String lotCode) {
        if (lotCode == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lotCode is required");
        }

        String normalized = lotCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lotCode is required");
        }
        if (normalized.length() > 80) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lotCode must be at most 80 characters");
        }
        return normalized;
    }

    public record LotAllocation(InventoryLotEntity inventoryLot, BigDecimal quantity) {
    }

    private record LotIdentity(String lotCode, LocalDate expiryDate) {
    }
}
