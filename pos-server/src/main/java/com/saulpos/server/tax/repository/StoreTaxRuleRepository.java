package com.saulpos.server.tax.repository;

import com.saulpos.server.tax.model.StoreTaxRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface StoreTaxRuleRepository extends JpaRepository<StoreTaxRuleEntity, Long> {

    @Query("""
            select str
            from StoreTaxRuleEntity str
            join fetch str.taxGroup tg
            where str.storeLocation.id = :storeLocationId
              and tg.id = :taxGroupId
              and str.active = true
              and (str.effectiveFrom is null or str.effectiveFrom <= :at)
              and (str.effectiveTo is null or str.effectiveTo >= :at)
            order by coalesce(str.effectiveFrom, :minimumInstant) desc,
                     str.updatedAt desc,
                     str.id desc
            """)
    List<StoreTaxRuleEntity> findApplicable(
            @Param("storeLocationId") Long storeLocationId,
            @Param("taxGroupId") Long taxGroupId,
            @Param("at") Instant at,
            @Param("minimumInstant") Instant minimumInstant);
}
