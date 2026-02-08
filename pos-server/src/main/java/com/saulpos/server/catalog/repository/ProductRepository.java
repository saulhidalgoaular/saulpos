package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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
              and (:queryPattern is null
                    or p.skuNormalized like :queryPattern
                    or p.nameNormalized like :queryPattern)
            order by p.skuNormalized asc, p.id asc
            """)
    List<ProductEntity> search(
            @Param("merchantId") Long merchantId,
            @Param("active") Boolean active,
            @Param("queryPattern") String queryPattern);

    @Query(value = """
            select p.id from ProductEntity p
            where p.merchant.id = :merchantId
              and (:active is null or p.active = :active)
              and (:queryPattern is null
                   or p.skuNormalized like :queryPattern
                   or p.nameNormalized like :queryPattern
                   or exists (
                       select 1 from ProductVariantEntity v
                       join v.barcodes b
                       where v.product = p
                         and b.barcodeNormalized like :queryPattern
                   ))
            order by p.skuNormalized asc, p.id asc
            """,
            countQuery = """
            select count(p.id) from ProductEntity p
            where p.merchant.id = :merchantId
              and (:active is null or p.active = :active)
              and (:queryPattern is null
                   or p.skuNormalized like :queryPattern
                   or p.nameNormalized like :queryPattern
                   or exists (
                       select 1 from ProductVariantEntity v
                       join v.barcodes b
                       where v.product = p
                         and b.barcodeNormalized like :queryPattern
                   ))
            """)
    Page<Long> searchIds(
            @Param("merchantId") Long merchantId,
            @Param("active") Boolean active,
            @Param("queryPattern") String queryPattern,
            Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "category", "variants", "variants.barcodes"})
    @Query("select distinct p from ProductEntity p where p.id in :ids")
    List<ProductEntity> findAllByIdWithDetails(@Param("ids") Collection<Long> ids);
}
