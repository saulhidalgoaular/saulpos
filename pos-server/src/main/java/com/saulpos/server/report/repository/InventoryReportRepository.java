package com.saulpos.server.report.repository;

import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.InventoryMovementType;
import com.saulpos.server.sale.model.InventoryReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface InventoryReportRepository extends JpaRepository<InventoryMovementEntity, Long> {

    @Query("""
            select
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                product.id as productId,
                product.sku as productSku,
                product.name as productName,
                category.id as categoryId,
                category.code as categoryCode,
                category.name as categoryName,
                coalesce(sum(movement.quantityDelta), 0) as quantityOnHand,
                cost.weightedAverageCost as weightedAverageCost,
                cost.lastCost as lastCost
            from InventoryMovementEntity movement
            join movement.storeLocation store
            join movement.product product
            left join product.category category
            left join com.saulpos.server.inventory.model.InventoryProductCostEntity cost
                on cost.storeLocation.id = store.id and cost.product.id = product.id
            where (:storeLocationId is null or store.id = :storeLocationId)
              and (:categoryId is null or category.id = :categoryId)
              and (:supplierId is null or exists (
                    select 1
                    from com.saulpos.server.inventory.model.PurchaseOrderLineEntity purchaseOrderLine
                    join purchaseOrderLine.purchaseOrder purchaseOrder
                    where purchaseOrderLine.product.id = product.id
                      and purchaseOrder.storeLocation.id = store.id
                      and purchaseOrder.supplier.id = :supplierId
              ))
            group by
                store.id,
                store.code,
                store.name,
                product.id,
                product.sku,
                product.name,
                category.id,
                category.code,
                category.name,
                cost.weightedAverageCost,
                cost.lastCost
            having coalesce(sum(movement.quantityDelta), 0) > 0
            order by store.code asc, product.sku asc
            """)
    List<StockOnHandProjection> findStockOnHandRows(@Param("storeLocationId") Long storeLocationId,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("supplierId") Long supplierId);

    @Query("""
            select
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                product.id as productId,
                product.sku as productSku,
                product.name as productName,
                category.id as categoryId,
                category.code as categoryCode,
                category.name as categoryName,
                coalesce(sum(movement.quantityDelta), 0) as quantityOnHand
            from InventoryMovementEntity movement
            join movement.storeLocation store
            join movement.product product
            left join product.category category
            where (:storeLocationId is null or store.id = :storeLocationId)
              and (:categoryId is null or category.id = :categoryId)
              and (:supplierId is null or exists (
                    select 1
                    from com.saulpos.server.inventory.model.PurchaseOrderLineEntity purchaseOrderLine
                    join purchaseOrderLine.purchaseOrder purchaseOrder
                    where purchaseOrderLine.product.id = product.id
                      and purchaseOrder.storeLocation.id = store.id
                      and purchaseOrder.supplier.id = :supplierId
              ))
            group by
                store.id,
                store.code,
                store.name,
                product.id,
                product.sku,
                product.name,
                category.id,
                category.code,
                category.name
            having coalesce(sum(movement.quantityDelta), 0) <= :minimumQuantity
            order by store.code asc, product.sku asc
            """)
    List<LowStockProjection> findLowStockRows(@Param("storeLocationId") Long storeLocationId,
                                              @Param("categoryId") Long categoryId,
                                              @Param("supplierId") Long supplierId,
                                              @Param("minimumQuantity") BigDecimal minimumQuantity);

    @Query("""
            select
                movement.id as movementId,
                movement.createdAt as occurredAt,
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                product.id as productId,
                product.sku as productSku,
                product.name as productName,
                category.id as categoryId,
                category.code as categoryCode,
                category.name as categoryName,
                movement.movementType as movementType,
                movement.referenceType as referenceType,
                movement.referenceNumber as referenceNumber,
                movement.quantityDelta as quantityDelta
            from InventoryMovementEntity movement
            join movement.storeLocation store
            join movement.product product
            left join product.category category
            where (:from is null or movement.createdAt >= :from)
              and (:to is null or movement.createdAt <= :to)
              and (:storeLocationId is null or store.id = :storeLocationId)
              and (:categoryId is null or category.id = :categoryId)
              and (:supplierId is null or exists (
                    select 1
                    from com.saulpos.server.inventory.model.PurchaseOrderLineEntity purchaseOrderLine
                    join purchaseOrderLine.purchaseOrder purchaseOrder
                    where purchaseOrderLine.product.id = product.id
                      and purchaseOrder.storeLocation.id = store.id
                      and purchaseOrder.supplier.id = :supplierId
              ))
            order by movement.createdAt desc, movement.id desc
            """)
    List<MovementProjection> findMovementRows(@Param("from") Instant from,
                                              @Param("to") Instant to,
                                              @Param("storeLocationId") Long storeLocationId,
                                              @Param("categoryId") Long categoryId,
                                              @Param("supplierId") Long supplierId);

    interface StockOnHandProjection {
        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getProductId();

        String getProductSku();

        String getProductName();

        Long getCategoryId();

        String getCategoryCode();

        String getCategoryName();

        BigDecimal getQuantityOnHand();

        BigDecimal getWeightedAverageCost();

        BigDecimal getLastCost();
    }

    interface LowStockProjection {
        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getProductId();

        String getProductSku();

        String getProductName();

        Long getCategoryId();

        String getCategoryCode();

        String getCategoryName();

        BigDecimal getQuantityOnHand();
    }

    interface MovementProjection {
        Long getMovementId();

        Instant getOccurredAt();

        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getProductId();

        String getProductSku();

        String getProductName();

        Long getCategoryId();

        String getCategoryCode();

        String getCategoryName();

        InventoryMovementType getMovementType();

        InventoryReferenceType getReferenceType();

        String getReferenceNumber();

        BigDecimal getQuantityDelta();
    }
}
