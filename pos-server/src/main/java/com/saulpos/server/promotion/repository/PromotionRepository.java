package com.saulpos.server.promotion.repository;

import com.saulpos.server.promotion.model.PromotionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    @EntityGraph(attributePaths = {"rules", "rules.targetProduct"})
    @Query("""
            select distinct p from PromotionEntity p
            where p.merchant.id = :merchantId
              and p.active = true
            order by p.priority desc, p.id asc
            """)
    List<PromotionEntity> findActiveForMerchant(@Param("merchantId") Long merchantId);
}
