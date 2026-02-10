package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleOverrideEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface SaleOverrideEventRepository extends JpaRepository<SaleOverrideEventEntity, Long> {

    @Query("""
            select e.id as eventId,
                   e.createdAt as occurredAt,
                   s.storeLocation.id as storeLocationId,
                   s.terminalDevice.id as terminalDeviceId,
                   s.cashierUser.id as cashierUserId,
                   cl.product.category.id as categoryId,
                   cl.product.taxGroup.id as taxGroupId,
                   e.beforeUnitPrice as beforeUnitPrice,
                   e.afterUnitPrice as afterUnitPrice,
                   cl.quantity as quantity
              from SaleOverrideEventEntity e
              join e.cart c
              join SaleEntity s on s.cart.id = c.id
              left join SaleCartLineEntity cl on cl.id = e.lineId
             where e.eventType = com.saulpos.server.sale.model.SaleOverrideEventType.PRICE_OVERRIDE
               and e.beforeUnitPrice is not null
               and e.afterUnitPrice is not null
               and e.beforeUnitPrice > e.afterUnitPrice
               and (:from is null or e.createdAt >= :from)
               and (:to is null or e.createdAt <= :to)
               and (:storeLocationId is null or s.storeLocation.id = :storeLocationId)
               and (:terminalDeviceId is null or s.terminalDevice.id = :terminalDeviceId)
               and (:cashierUserId is null or s.cashierUser.id = :cashierUserId)
               and (:categoryId is null or cl.product.category.id = :categoryId)
               and (:taxGroupId is null or cl.product.taxGroup.id = :taxGroupId)
            """)
    List<SaleDiscountReportProjection> findDiscountReportLines(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("storeLocationId") Long storeLocationId,
            @Param("terminalDeviceId") Long terminalDeviceId,
            @Param("cashierUserId") Long cashierUserId,
            @Param("categoryId") Long categoryId,
            @Param("taxGroupId") Long taxGroupId);

    interface SaleDiscountReportProjection {

        Long getEventId();

        Instant getOccurredAt();

        Long getStoreLocationId();

        Long getTerminalDeviceId();

        Long getCashierUserId();

        Long getCategoryId();

        Long getTaxGroupId();

        BigDecimal getBeforeUnitPrice();

        BigDecimal getAfterUnitPrice();

        BigDecimal getQuantity();
    }
}
