package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.PurchaseOrderEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
