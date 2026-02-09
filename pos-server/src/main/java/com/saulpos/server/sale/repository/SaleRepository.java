package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    Optional<SaleEntity> findByCartId(Long cartId);

    @Override
    @EntityGraph(attributePaths = {"storeLocation", "terminalDevice", "lines", "lines.product", "cart"})
    Optional<SaleEntity> findById(Long id);

    @EntityGraph(attributePaths = {"storeLocation", "terminalDevice", "lines", "lines.product", "cart"})
    Optional<SaleEntity> findByReceiptNumberIgnoreCase(String receiptNumber);

    @Query("""
            select s.id as saleId,
                   s.receiptNumber as receiptNumber,
                   s.storeLocation.id as storeLocationId,
                   s.terminalDevice.id as terminalDeviceId,
                   s.subtotalNet as subtotalNet,
                   s.totalTax as totalTax,
                   s.totalGross as totalGross,
                   s.totalPayable as totalPayable,
                   s.createdAt as soldAt
              from SaleEntity s
             where s.customer.id = :customerId
               and (:from is null or s.createdAt >= :from)
               and (:to is null or s.createdAt <= :to)
             order by s.createdAt desc, s.id desc
            """)
    Page<CustomerSaleHistoryProjection> findCustomerSalesHistory(
            @Param("customerId") Long customerId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    interface CustomerSaleHistoryProjection {

        Long getSaleId();

        String getReceiptNumber();

        Long getStoreLocationId();

        Long getTerminalDeviceId();

        BigDecimal getSubtotalNet();

        BigDecimal getTotalTax();

        BigDecimal getTotalGross();

        BigDecimal getTotalPayable();

        Instant getSoldAt();
    }
}
