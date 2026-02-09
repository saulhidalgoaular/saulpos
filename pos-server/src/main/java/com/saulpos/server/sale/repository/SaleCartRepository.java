package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleCartEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SaleCartRepository extends JpaRepository<SaleCartEntity, Long> {

    @EntityGraph(attributePaths = {"cashierUser", "storeLocation", "terminalDevice", "lines", "lines.product", "parkedReference"})
    @Query("select cart from SaleCartEntity cart where cart.id = :id")
    Optional<SaleCartEntity> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cart
            from SaleCartEntity cart
            where cart.id = :id
            """)
    Optional<SaleCartEntity> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"cashierUser", "storeLocation", "terminalDevice", "parkedReference"})
    @Query("""
            select cart
            from SaleCartEntity cart
            left join cart.parkedReference parkedReference
            where cart.status = com.saulpos.api.sale.SaleCartStatus.PARKED
            and cart.storeLocation.id = :storeLocationId
            and (:terminalDeviceId is null or cart.terminalDevice.id = :terminalDeviceId)
            order by parkedReference.parkedAt desc, cart.id desc
            """)
    List<SaleCartEntity> findParkedByStoreAndTerminal(@Param("storeLocationId") Long storeLocationId,
                                                       @Param("terminalDeviceId") Long terminalDeviceId);
}
