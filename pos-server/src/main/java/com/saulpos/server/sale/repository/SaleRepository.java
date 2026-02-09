package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    Optional<SaleEntity> findByCartId(Long cartId);

    @Override
    @EntityGraph(attributePaths = {"storeLocation", "terminalDevice", "lines", "lines.product", "cart"})
    Optional<SaleEntity> findById(Long id);

    @EntityGraph(attributePaths = {"storeLocation", "terminalDevice", "lines", "lines.product", "cart"})
    Optional<SaleEntity> findByReceiptNumberIgnoreCase(String receiptNumber);
}
