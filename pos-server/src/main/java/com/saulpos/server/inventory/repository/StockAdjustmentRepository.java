package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.StockAdjustmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustmentEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select sa
            from StockAdjustmentEntity sa
            left join fetch sa.inventoryMovement
            join fetch sa.storeLocation
            join fetch sa.product
            where sa.id = :id
            """)
    Optional<StockAdjustmentEntity> findByIdForUpdate(@Param("id") Long id);
}
