package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.PurchaseOrderEntity;
import com.saulpos.server.inventory.model.PurchaseOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct po
            from PurchaseOrderEntity po
            join fetch po.supplier
            join fetch po.storeLocation
            left join fetch po.lines line
            left join fetch line.product
            where po.id = :id
            """)
    Optional<PurchaseOrderEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select distinct po
            from PurchaseOrderEntity po
            join fetch po.supplier
            join fetch po.storeLocation
            left join fetch po.lines line
            left join fetch line.product
            where po.id = :id
            """)
    Optional<PurchaseOrderEntity> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            select coalesce(sum(line.receivedQuantity), 0)
            from PurchaseOrderLineEntity line
            join line.purchaseOrder purchaseOrder
            where purchaseOrder.supplier.id = :supplierId
              and purchaseOrder.storeLocation.id = :storeLocationId
              and line.product.id = :productId
              and purchaseOrder.status in (:statusOne, :statusTwo)
            """)
    BigDecimal sumReceivedQuantityBySupplierStoreAndProduct(@Param("supplierId") Long supplierId,
                                                            @Param("storeLocationId") Long storeLocationId,
                                                            @Param("productId") Long productId,
                                                            @Param("statusOne") PurchaseOrderStatus statusOne,
                                                            @Param("statusTwo") PurchaseOrderStatus statusTwo);
}
