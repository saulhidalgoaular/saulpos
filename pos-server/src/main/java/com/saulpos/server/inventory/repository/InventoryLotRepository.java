package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.InventoryLotEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface InventoryLotRepository extends JpaRepository<InventoryLotEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select lot
            from InventoryLotEntity lot
            where lot.storeLocation.id = :storeLocationId
              and lot.product.id = :productId
              and upper(lot.lotCode) = :lotCode
              and ((:expiryDate is null and lot.expiryDate is null) or lot.expiryDate = :expiryDate)
            """)
    Optional<InventoryLotEntity> findForUpdateByIdentity(@Param("storeLocationId") Long storeLocationId,
                                                          @Param("productId") Long productId,
                                                          @Param("lotCode") String lotCode,
                                                          @Param("expiryDate") LocalDate expiryDate);
}
