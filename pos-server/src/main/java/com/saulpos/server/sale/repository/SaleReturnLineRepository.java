package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleReturnLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SaleReturnLineRepository extends JpaRepository<SaleReturnLineEntity, Long> {

    @Query("""
            select line.saleLine.id as saleLineId,
                   coalesce(sum(line.quantity), 0) as returnedQuantity,
                   coalesce(sum(line.netAmount), 0) as returnedNet,
                   coalesce(sum(line.taxAmount), 0) as returnedTax,
                   coalesce(sum(line.grossAmount), 0) as returnedGross
            from SaleReturnLineEntity line
            where line.saleReturn.sale.id = :saleId
            group by line.saleLine.id
            """)
    List<ReturnedLineTotalsProjection> summarizeReturnedBySaleId(@Param("saleId") Long saleId);

    interface ReturnedLineTotalsProjection {
        Long getSaleLineId();

        BigDecimal getReturnedQuantity();

        BigDecimal getReturnedNet();

        BigDecimal getReturnedTax();

        BigDecimal getReturnedGross();
    }
}
