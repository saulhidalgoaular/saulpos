package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.ProductBarcodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductBarcodeRepository extends JpaRepository<ProductBarcodeEntity, Long> {

    @Query("""
            select b from ProductBarcodeEntity b
            join fetch b.variant v
            join fetch v.product p
            where p.merchant.id = :merchantId
              and b.barcodeNormalized = :barcode
              and p.active = true
              and v.active = true
              and b.active = true
            """)
    Optional<ProductBarcodeEntity> findActiveSellableByMerchantAndBarcode(
            @Param("merchantId") Long merchantId,
            @Param("barcode") String barcode);
}
