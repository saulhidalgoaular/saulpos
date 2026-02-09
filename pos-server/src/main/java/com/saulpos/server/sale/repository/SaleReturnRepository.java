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
}
