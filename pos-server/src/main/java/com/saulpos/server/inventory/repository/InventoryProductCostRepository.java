package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.InventoryProductCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InventoryProductCostRepository extends JpaRepository<InventoryProductCostEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cost
            from InventoryProductCostEntity cost
            where cost.storeLocation.id = :storeLocationId
              and cost.product.id = :productId
            """)
    Optional<InventoryProductCostEntity> findByStoreLocationIdAndProductIdForUpdate(
            @Param("storeLocationId") Long storeLocationId,
            @Param("productId") Long productId);

    @Query("""
            select cost
            from InventoryProductCostEntity cost
            where cost.storeLocation.id = :storeLocationId
              and (:productId is null or cost.product.id = :productId)
            """)
    List<InventoryProductCostEntity> findByStoreLocationIdAndOptionalProductId(
            @Param("storeLocationId") Long storeLocationId,
            @Param("productId") Long productId);
}
