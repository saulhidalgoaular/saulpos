package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleCartEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SaleCartRepository extends JpaRepository<SaleCartEntity, Long> {

    @EntityGraph(attributePaths = {"cashierUser", "storeLocation", "terminalDevice", "lines", "lines.product"})
    @Query("select cart from SaleCartEntity cart where cart.id = :id")
    Optional<SaleCartEntity> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct cart
            from SaleCartEntity cart
            left join fetch cart.lines line
            left join fetch line.product product
            where cart.id = :id
            """)
    Optional<SaleCartEntity> findByIdForUpdate(@Param("id") Long id);
}
