package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.StorePriceOverrideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface StorePriceOverrideRepository extends JpaRepository<StorePriceOverrideEntity, Long> {

    @Query("""
            select spo
            from StorePriceOverrideEntity spo
            where spo.storeLocation.id = :storeLocationId
              and spo.product.id = :productId
              and spo.active = true
              and (spo.effectiveFrom is null or spo.effectiveFrom <= :at)
              and (spo.effectiveTo is null or spo.effectiveTo >= :at)
            order by coalesce(spo.effectiveFrom, :minimumInstant) desc,
                     spo.updatedAt desc,
                     spo.id desc
            """)
    List<StorePriceOverrideEntity> findApplicable(
            @Param("storeLocationId") Long storeLocationId,
            @Param("productId") Long productId,
            @Param("at") Instant at,
            @Param("minimumInstant") Instant minimumInstant);
}
