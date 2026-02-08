package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.ProductEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByMerchantIdAndSkuIgnoreCase(Long merchantId, String sku);

    @EntityGraph(attributePaths = {"merchant", "category", "variants", "variants.barcodes"})
    @Query("select p from ProductEntity p where p.id = :id")
    Optional<ProductEntity> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"merchant", "category", "variants", "variants.barcodes"})
    @Query("""
            select p from ProductEntity p
            where (:merchantId is null or p.merchant.id = :merchantId)
              and (:active is null or p.active = :active)
              and (:query is null
                    or lower(p.sku) like lower(concat('%', :query, '%'))
                    or lower(p.name) like lower(concat('%', :query, '%')))
            order by p.id
            """)
    List<ProductEntity> search(
            @Param("merchantId") Long merchantId,
            @Param("active") Boolean active,
            @Param("query") String query);
}
