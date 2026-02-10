package com.saulpos.server.report.repository;

import com.saulpos.server.sale.model.SaleOverrideEventEntity;
import com.saulpos.server.sale.model.SaleOverrideEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ExceptionReportRepository extends JpaRepository<SaleOverrideEventEntity, Long> {

    @Query("""
            select
                e.id as eventId,
                e.createdAt as occurredAt,
                e.eventType as eventType,
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                terminal.id as terminalDeviceId,
                terminal.code as terminalDeviceCode,
                terminal.name as terminalDeviceName,
                cashier.id as cashierUserId,
                cashier.username as cashierUsername,
                e.actorUsername as actorUsername,
                e.approvedByUsername as approverUsername,
                e.reasonCodeValue as reasonCode,
                e.note as note,
                e.correlationId as correlationId,
                sale.receiptNumber as referenceNumber
            from SaleOverrideEventEntity e
            join e.cart cart
            join SaleEntity sale on sale.cart.id = cart.id
            join sale.storeLocation store
            join sale.terminalDevice terminal
            join sale.cashierUser cashier
            where (:from is null or e.createdAt >= :from)
              and (:to is null or e.createdAt <= :to)
              and (:storeLocationId is null or store.id = :storeLocationId)
              and (:terminalDeviceId is null or terminal.id = :terminalDeviceId)
              and (:cashierUserId is null or cashier.id = :cashierUserId)
              and (:reasonCode is null or upper(e.reasonCodeValue) = :reasonCode)
            order by e.createdAt desc, e.id desc
            """)
    List<OverrideExceptionProjection> findOverrideRows(@Param("from") Instant from,
                                                       @Param("to") Instant to,
                                                       @Param("storeLocationId") Long storeLocationId,
                                                       @Param("terminalDeviceId") Long terminalDeviceId,
                                                       @Param("cashierUserId") Long cashierUserId,
                                                       @Param("reasonCode") String reasonCode);

    @Query("""
            select
                transition.id as eventId,
                transition.createdAt as occurredAt,
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                terminal.id as terminalDeviceId,
                terminal.code as terminalDeviceCode,
                terminal.name as terminalDeviceName,
                cashier.id as cashierUserId,
                cashier.username as cashierUsername,
                transition.actorUsername as actorUsername,
                transition.note as note,
                transition.correlationId as correlationId,
                sale.receiptNumber as referenceNumber
            from PaymentTransitionEntity transition
            join transition.payment payment
            join payment.cart cart
            join SaleEntity sale on sale.cart.id = cart.id
            join sale.storeLocation store
            join sale.terminalDevice terminal
            join sale.cashierUser cashier
            where transition.action = com.saulpos.api.sale.PaymentTransitionAction.REFUND
              and (:from is null or transition.createdAt >= :from)
              and (:to is null or transition.createdAt <= :to)
              and (:storeLocationId is null or store.id = :storeLocationId)
              and (:terminalDeviceId is null or terminal.id = :terminalDeviceId)
              and (:cashierUserId is null or cashier.id = :cashierUserId)
            order by transition.createdAt desc, transition.id desc
            """)
    List<RefundExceptionProjection> findRefundRows(@Param("from") Instant from,
                                                   @Param("to") Instant to,
                                                   @Param("storeLocationId") Long storeLocationId,
                                                   @Param("terminalDeviceId") Long terminalDeviceId,
                                                   @Param("cashierUserId") Long cashierUserId);

    @Query(value = """
            select
                event.id as eventId,
                event.created_at as occurredAt,
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                terminal.id as terminalDeviceId,
                terminal.code as terminalDeviceCode,
                terminal.name as terminalDeviceName,
                cashier.id as cashierUserId,
                cashier.username as cashierUsername,
                coalesce(event.actor_username, actor.username) as actorUsername,
                coalesce(event.approved_by_username, approver.username) as approverUsername,
                event.reason_code as reasonCode,
                event.note as note,
                event.correlation_id as correlationId,
                event.reference_number as referenceNumber
            from no_sale_drawer_event event
            join store_location store on store.id = event.store_location_id
            left join terminal_device terminal on terminal.id = event.terminal_device_id
            left join user_account cashier on cashier.id = event.cashier_user_id
            left join user_account actor on actor.id = event.actor_user_id
            left join user_account approver on approver.id = event.approved_by_user_id
            where (:from is null or event.created_at >= :from)
              and (:to is null or event.created_at <= :to)
              and (:storeLocationId is null or event.store_location_id = :storeLocationId)
              and (:terminalDeviceId is null or event.terminal_device_id = :terminalDeviceId)
              and (:cashierUserId is null or event.cashier_user_id = :cashierUserId)
              and (:reasonCode is null or upper(event.reason_code) = :reasonCode)
            order by event.created_at desc, event.id desc
            """, nativeQuery = true)
    List<NoSaleExceptionProjection> findNoSaleRows(@Param("from") Instant from,
                                                   @Param("to") Instant to,
                                                   @Param("storeLocationId") Long storeLocationId,
                                                   @Param("terminalDeviceId") Long terminalDeviceId,
                                                   @Param("cashierUserId") Long cashierUserId,
                                                   @Param("reasonCode") String reasonCode);

    interface OverrideExceptionProjection {
        Long getEventId();

        Instant getOccurredAt();

        SaleOverrideEventType getEventType();

        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getTerminalDeviceId();

        String getTerminalDeviceCode();

        String getTerminalDeviceName();

        Long getCashierUserId();

        String getCashierUsername();

        String getActorUsername();

        String getApproverUsername();

        String getReasonCode();

        String getNote();

        String getCorrelationId();

        String getReferenceNumber();
    }

    interface RefundExceptionProjection {
        Long getEventId();

        Instant getOccurredAt();

        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getTerminalDeviceId();

        String getTerminalDeviceCode();

        String getTerminalDeviceName();

        Long getCashierUserId();

        String getCashierUsername();

        String getActorUsername();

        String getNote();

        String getCorrelationId();

        String getReferenceNumber();
    }

    interface NoSaleExceptionProjection {
        Long getEventId();

        Instant getOccurredAt();

        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getTerminalDeviceId();

        String getTerminalDeviceCode();

        String getTerminalDeviceName();

        Long getCashierUserId();

        String getCashierUsername();

        String getActorUsername();

        String getApproverUsername();

        String getReasonCode();

        String getNote();

        String getCorrelationId();

        String getReferenceNumber();
    }
}
