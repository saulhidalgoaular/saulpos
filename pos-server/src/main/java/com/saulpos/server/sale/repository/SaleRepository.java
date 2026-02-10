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
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    Optional<SaleEntity> findByCartId(Long cartId);

    @Override
    @EntityGraph(attributePaths = {"storeLocation", "terminalDevice", "cashierUser", "lines", "lines.product", "cart"})
    Optional<SaleEntity> findById(Long id);

    @EntityGraph(attributePaths = {"storeLocation", "terminalDevice", "cashierUser", "lines", "lines.product", "cart"})
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

    @Query("""
            select s.id as saleId,
                   s.createdAt as soldAt,
                   s.storeLocation.id as storeLocationId,
                   s.storeLocation.code as storeLocationCode,
                   s.storeLocation.name as storeLocationName,
                   s.terminalDevice.id as terminalDeviceId,
                   s.terminalDevice.code as terminalDeviceCode,
                   s.terminalDevice.name as terminalDeviceName,
                   s.cashierUser.id as cashierUserId,
                   s.cashierUser.username as cashierUsername,
                   c.id as categoryId,
                   c.code as categoryCode,
                   c.name as categoryName,
                   tg.id as taxGroupId,
                   tg.code as taxGroupCode,
                   tg.name as taxGroupName,
                   sl.quantity as quantity,
                   sl.netAmount as netAmount,
                   sl.taxAmount as taxAmount,
                   sl.grossAmount as grossAmount
              from SaleLineEntity sl
              join sl.sale s
              join sl.product p
              left join p.category c
              left join p.taxGroup tg
             where (:from is null or s.createdAt >= :from)
               and (:to is null or s.createdAt <= :to)
               and (:storeLocationId is null or s.storeLocation.id = :storeLocationId)
               and (:terminalDeviceId is null or s.terminalDevice.id = :terminalDeviceId)
               and (:cashierUserId is null or s.cashierUser.id = :cashierUserId)
               and (:categoryId is null or c.id = :categoryId)
               and (:taxGroupId is null or tg.id = :taxGroupId)
             order by s.createdAt asc, s.id asc, sl.id asc
            """)
    List<SaleReportLineProjection> findSalesReportLines(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("storeLocationId") Long storeLocationId,
            @Param("terminalDeviceId") Long terminalDeviceId,
            @Param("cashierUserId") Long cashierUserId,
            @Param("categoryId") Long categoryId,
            @Param("taxGroupId") Long taxGroupId);

    interface SaleReportLineProjection {

        Long getSaleId();

        Instant getSoldAt();

        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getTerminalDeviceId();

        String getTerminalDeviceCode();

        String getTerminalDeviceName();

        Long getCashierUserId();

        String getCashierUsername();

        Long getCategoryId();

        String getCategoryCode();

        String getCategoryName();

        Long getTaxGroupId();

        String getTaxGroupCode();

        String getTaxGroupName();

        BigDecimal getQuantity();

        BigDecimal getNetAmount();

        BigDecimal getTaxAmount();

        BigDecimal getGrossAmount();
    }
}
