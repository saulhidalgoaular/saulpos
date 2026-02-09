package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.InventoryMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovementEntity, Long> {

    @Query("""
            select im
            from InventoryMovementEntity im
            where im.storeLocation.id = :storeLocationId
              and (:productId is null or im.product.id = :productId)
            order by im.createdAt asc, im.id asc
            """)
    List<InventoryMovementEntity> findLedgerEntries(@Param("storeLocationId") Long storeLocationId,
                                                    @Param("productId") Long productId);

    @Query("""
            select im.product.id as productId, coalesce(sum(im.quantityDelta), 0) as quantityOnHand
            from InventoryMovementEntity im
            where im.storeLocation.id = :storeLocationId
              and (:productId is null or im.product.id = :productId)
            group by im.product.id
            order by im.product.id asc
            """)
    List<ProductBalanceProjection> sumByStoreLocationAndProduct(@Param("storeLocationId") Long storeLocationId,
                                                                @Param("productId") Long productId);

    interface ProductBalanceProjection {
        Long getProductId();

        BigDecimal getQuantityOnHand();
    }
}
