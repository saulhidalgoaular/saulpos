package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.InventoryLotBalanceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryLotBalanceRepository extends JpaRepository<InventoryLotBalanceEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select balance
            from InventoryLotBalanceEntity balance
            join fetch balance.inventoryLot lot
            where lot.storeLocation.id = :storeLocationId
              and lot.product.id = :productId
              and balance.quantityOnHand > 0
            order by
                case when lot.expiryDate is null then 1 else 0 end,
                lot.expiryDate asc,
                lot.id asc
            """)
    List<InventoryLotBalanceEntity> findPositiveBalancesForSaleForUpdate(@Param("storeLocationId") Long storeLocationId,
                                                                          @Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select balance
            from InventoryLotBalanceEntity balance
            join fetch balance.inventoryLot lot
            where balance.inventoryLotId = :inventoryLotId
            """)
    Optional<InventoryLotBalanceEntity> findByInventoryLotIdForUpdate(@Param("inventoryLotId") Long inventoryLotId);

    @Query("""
            select balance
            from InventoryLotBalanceEntity balance
            join fetch balance.inventoryLot lot
            where lot.storeLocation.id = :storeLocationId
              and (:productId is null or lot.product.id = :productId)
              and balance.quantityOnHand > 0
            order by
                lot.product.id asc,
                case when lot.expiryDate is null then 1 else 0 end,
                lot.expiryDate asc,
                lot.id asc
            """)
    List<InventoryLotBalanceEntity> findPositiveBalances(@Param("storeLocationId") Long storeLocationId,
                                                         @Param("productId") Long productId);
}
