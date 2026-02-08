package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.PriceBookItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PriceBookItemRepository extends JpaRepository<PriceBookItemEntity, Long> {

    @Query("""
            select item
            from PriceBookItemEntity item
            join fetch item.priceBook book
            where item.product.id = :productId
              and book.merchant.id = :merchantId
              and book.customerGroup is null
              and book.active = true
              and (book.effectiveFrom is null or book.effectiveFrom <= :at)
              and (book.effectiveTo is null or book.effectiveTo >= :at)
            order by coalesce(book.effectiveFrom, :minimumInstant) desc,
                     book.updatedAt desc,
                     book.id desc,
                     item.id desc
            """)
    List<PriceBookItemEntity> findApplicable(
            @Param("merchantId") Long merchantId,
            @Param("productId") Long productId,
            @Param("at") Instant at,
            @Param("minimumInstant") Instant minimumInstant);

    @Query("""
            select item
            from PriceBookItemEntity item
            join fetch item.priceBook book
            where item.product.id = :productId
              and book.merchant.id = :merchantId
              and book.customerGroup.id in :customerGroupIds
              and book.active = true
              and (book.effectiveFrom is null or book.effectiveFrom <= :at)
              and (book.effectiveTo is null or book.effectiveTo >= :at)
            order by coalesce(book.effectiveFrom, :minimumInstant) desc,
                     book.updatedAt desc,
                     book.id desc,
                     item.id desc
            """)
    List<PriceBookItemEntity> findApplicableForCustomerGroups(
            @Param("merchantId") Long merchantId,
            @Param("productId") Long productId,
            @Param("customerGroupIds") List<Long> customerGroupIds,
            @Param("at") Instant at,
            @Param("minimumInstant") Instant minimumInstant);
}
