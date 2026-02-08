package com.saulpos.server.customer.repository;

import com.saulpos.server.customer.model.CustomerGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroupEntity, Long> {

    boolean existsByMerchant_IdAndCode(Long merchantId, String code);

    @Query("""
            select cg
            from CustomerGroupEntity cg
            where cg.merchant.id = :merchantId
              and (:active is null or cg.active = :active)
            order by cg.code asc, cg.id asc
            """)
    List<CustomerGroupEntity> search(
            @Param("merchantId") Long merchantId,
            @Param("active") Boolean active);
}
