package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.StocktakeSessionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StocktakeSessionRepository extends JpaRepository<StocktakeSessionEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct ss
            from StocktakeSessionEntity ss
            join fetch ss.storeLocation
            left join fetch ss.lines sl
            left join fetch sl.product p
            left join fetch p.category
            left join fetch sl.inventoryMovement
            where ss.id = :id
            """)
    Optional<StocktakeSessionEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select distinct ss
            from StocktakeSessionEntity ss
            join fetch ss.storeLocation
            left join fetch ss.lines sl
            left join fetch sl.product p
            left join fetch p.category
            left join fetch sl.inventoryMovement
            where ss.id = :id
            """)
    Optional<StocktakeSessionEntity> findByIdWithLines(@Param("id") Long id);
}
