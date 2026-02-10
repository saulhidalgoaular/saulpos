package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.InventoryMovementLotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface InventoryMovementLotRepository extends JpaRepository<InventoryMovementLotEntity, Long> {

    @Query("""
            select movementLot
            from InventoryMovementLotEntity movementLot
            join fetch movementLot.inventoryLot lot
            where movementLot.inventoryMovement.id in :movementIds
            order by
                movementLot.inventoryMovement.id asc,
                case when lot.expiryDate is null then 1 else 0 end,
                lot.expiryDate asc,
                lot.id asc,
                movementLot.id asc
            """)
    List<InventoryMovementLotEntity> findByMovementIdsWithLot(@Param("movementIds") Collection<Long> movementIds);

    @Query("""
            select lot.id as inventoryLotId,
                   lot.lotCode as lotCode,
                   lot.expiryDate as expiryDate,
                   coalesce(sum(movementLot.quantity), 0) as quantity
            from InventoryMovementLotEntity movementLot
            join movementLot.inventoryMovement movement
            join movementLot.inventoryLot lot
            where movement.saleLine.id = :saleLineId
              and movement.movementType = com.saulpos.server.sale.model.InventoryMovementType.SALE
            group by lot.id, lot.lotCode, lot.expiryDate
            order by
                case when lot.expiryDate is null then 1 else 0 end,
                lot.expiryDate asc,
                lot.id asc
            """)
    List<LotQuantityProjection> summarizeSoldBySaleLine(@Param("saleLineId") Long saleLineId);

    @Query("""
            select lot.id as inventoryLotId,
                   coalesce(sum(movementLot.quantity), 0) as quantity
            from InventoryMovementLotEntity movementLot
            join movementLot.inventoryMovement movement
            join movementLot.inventoryLot lot
            where movement.saleLine.id = :saleLineId
              and movement.movementType = com.saulpos.server.sale.model.InventoryMovementType.RETURN
            group by lot.id
            """)
    List<LotQuantitySumProjection> summarizeReturnedBySaleLine(@Param("saleLineId") Long saleLineId);

    interface LotQuantityProjection {
        Long getInventoryLotId();

        String getLotCode();

        LocalDate getExpiryDate();

        BigDecimal getQuantity();
    }

    interface LotQuantitySumProjection {
        Long getInventoryLotId();

        BigDecimal getQuantity();
    }
}
