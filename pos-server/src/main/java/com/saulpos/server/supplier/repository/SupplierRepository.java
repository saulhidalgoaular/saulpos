package com.saulpos.server.supplier.repository;

import com.saulpos.server.supplier.model.SupplierEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {

    Optional<SupplierEntity> findByMerchant_IdAndCode(Long merchantId, String code);

    Optional<SupplierEntity> findByMerchant_IdAndTaxIdentifierNormalized(Long merchantId, String taxIdentifierNormalized);

    @EntityGraph(attributePaths = {"merchant", "contacts", "terms"})
    @Query("select s from SupplierEntity s where s.id = :id")
    Optional<SupplierEntity> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"merchant", "contacts", "terms"})
    @Query("""
            select distinct s
            from SupplierEntity s
            where (:merchantId is null or s.merchant.id = :merchantId)
              and (:active is null or s.active = :active)
              and (:query is null
                    or upper(s.code) like :query
                    or upper(s.name) like :query
                    or upper(coalesce(s.taxIdentifierNormalized, '')) like :query)
            order by s.code asc, s.id asc
            """)
    List<SupplierEntity> search(
            @Param("merchantId") Long merchantId,
            @Param("active") Boolean active,
            @Param("query") String query);
}
