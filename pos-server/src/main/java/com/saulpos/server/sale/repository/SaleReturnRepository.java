package com.saulpos.server.sale.repository;

import com.saulpos.api.tax.TenderType;
import com.saulpos.server.sale.model.SaleReturnEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface SaleReturnRepository extends JpaRepository<SaleReturnEntity, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.saleLine", "lines.saleLine.product"})
    List<SaleReturnEntity> findBySaleIdOrderByCreatedAtAscIdAsc(Long saleId);

    @Query("""
            select sr.id as saleReturnId,
                   sr.sale.id as saleId,
                   sr.sale.receiptNumber as receiptNumber,
                   sr.returnReference as returnReference,
                   sr.reasonCode as reasonCode,
                   sr.refundTenderType as refundTenderType,
                   sr.totalGross as totalGross,
                   sr.createdAt as returnedAt
              from SaleReturnEntity sr
             where sr.sale.customer.id = :customerId
               and (:from is null or sr.createdAt >= :from)
               and (:to is null or sr.createdAt <= :to)
             order by sr.createdAt desc, sr.id desc
            """)
    Page<CustomerReturnHistoryProjection> findCustomerReturnsHistory(
            @Param("customerId") Long customerId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    interface CustomerReturnHistoryProjection {

        Long getSaleReturnId();

        Long getSaleId();

        String getReceiptNumber();

        String getReturnReference();

        String getReasonCode();

        TenderType getRefundTenderType();

        BigDecimal getTotalGross();

        Instant getReturnedAt();
    }

    @Query("""
            select sr.id as saleReturnId,
                   sr.createdAt as returnedAt,
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
                   srl.quantity as quantity,
                   srl.netAmount as netAmount,
                   srl.taxAmount as taxAmount,
                   srl.grossAmount as grossAmount
              from SaleReturnLineEntity srl
              join srl.saleReturn sr
              join sr.sale s
              join srl.saleLine sl
              join sl.product p
              left join p.category c
              left join p.taxGroup tg
             where (:from is null or sr.createdAt >= :from)
               and (:to is null or sr.createdAt <= :to)
               and (:storeLocationId is null or s.storeLocation.id = :storeLocationId)
               and (:terminalDeviceId is null or s.terminalDevice.id = :terminalDeviceId)
               and (:cashierUserId is null or s.cashierUser.id = :cashierUserId)
               and (:categoryId is null or c.id = :categoryId)
               and (:taxGroupId is null or tg.id = :taxGroupId)
             order by sr.createdAt asc, sr.id asc, srl.id asc
            """)
    List<SaleReturnReportLineProjection> findReturnReportLines(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("storeLocationId") Long storeLocationId,
            @Param("terminalDeviceId") Long terminalDeviceId,
            @Param("cashierUserId") Long cashierUserId,
            @Param("categoryId") Long categoryId,
            @Param("taxGroupId") Long taxGroupId);

    interface SaleReturnReportLineProjection {

        Long getSaleReturnId();

        Instant getReturnedAt();

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
