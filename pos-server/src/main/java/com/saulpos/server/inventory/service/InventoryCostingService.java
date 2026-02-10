package com.saulpos.server.inventory.service;

import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.inventory.model.InventoryProductCostEntity;
import com.saulpos.server.inventory.repository.InventoryProductCostRepository;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryCostingService {

    private final InventoryProductCostRepository inventoryProductCostRepository;
    private final CostingCalculator costingCalculator;

    @Transactional
    public void applyPurchaseReceiptCosting(StoreLocationEntity storeLocation,
                                            ProductEntity product,
                                            BigDecimal onHandBeforeReceipt,
                                            BigDecimal receivedQuantity,
                                            BigDecimal receivedUnitCost,
                                            String receiptReference,
                                            InventoryMovementEntity movement) {
        InventoryProductCostEntity cost = inventoryProductCostRepository
                .findByStoreLocationIdAndProductIdForUpdate(storeLocation.getId(), product.getId())
                .orElseGet(() -> {
                    InventoryProductCostEntity created = new InventoryProductCostEntity();
                    created.setStoreLocation(storeLocation);
                    created.setProduct(product);
                    created.setWeightedAverageCost(costingCalculator.normalizeCostScale(BigDecimal.ZERO));
                    created.setLastCost(costingCalculator.normalizeCostScale(BigDecimal.ZERO));
                    return created;
                });

        BigDecimal weightedAverage = costingCalculator.weightedAverageCost(
                onHandBeforeReceipt,
                cost.getWeightedAverageCost(),
                receivedQuantity,
                receivedUnitCost);

        cost.setWeightedAverageCost(weightedAverage);
        cost.setLastCost(costingCalculator.normalizeCostScale(receivedUnitCost));
        cost.setLastReceiptReference(receiptReference);
        cost.setLastMovement(movement);

        inventoryProductCostRepository.save(cost);
    }

    @Transactional(readOnly = true)
    public Map<Long, CostSnapshot> findCostSnapshots(Long storeLocationId, Long productId) {
        return inventoryProductCostRepository.findByStoreLocationIdAndOptionalProductId(storeLocationId, productId)
                .stream()
                .collect(Collectors.toMap(
                        cost -> cost.getProduct().getId(),
                        cost -> new CostSnapshot(
                                costingCalculator.normalizeCostScale(cost.getWeightedAverageCost()),
                                costingCalculator.normalizeCostScale(cost.getLastCost()))));
    }

    public record CostSnapshot(BigDecimal weightedAverageCost, BigDecimal lastCost) {
    }
}
