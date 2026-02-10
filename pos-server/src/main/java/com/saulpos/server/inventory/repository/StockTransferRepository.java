package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.StockTransferEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockTransferRepository extends JpaRepository<StockTransferEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct st
            from StockTransferEntity st
            join fetch st.sourceStoreLocation
            join fetch st.destinationStoreLocation
            left join fetch st.lines line
            left join fetch line.product
            where st.id = :id
            """)
    Optional<StockTransferEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select distinct st
            from StockTransferEntity st
            join fetch st.sourceStoreLocation
            join fetch st.destinationStoreLocation
            left join fetch st.lines line
            left join fetch line.product
            where st.id = :id
            """)
    Optional<StockTransferEntity> findByIdWithLines(@Param("id") Long id);
}
