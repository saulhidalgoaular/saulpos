package com.saulpos.server.report.repository;

import com.saulpos.server.shift.model.CashMovementType;
import com.saulpos.server.shift.model.CashShiftEntity;
import com.saulpos.server.shift.model.CashShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface CashReportRepository extends JpaRepository<CashShiftEntity, Long> {

    @Query("""
            select
                shift.id as shiftId,
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                terminal.id as terminalDeviceId,
                terminal.code as terminalDeviceCode,
                terminal.name as terminalDeviceName,
                cashier.id as cashierUserId,
                cashier.username as cashierUsername,
                shift.status as status,
                shift.openingCash as openingCash,
                shift.totalPaidIn as totalPaidIn,
                shift.totalPaidOut as totalPaidOut,
                shift.expectedCloseCash as expectedCloseCash,
                shift.countedCloseCash as countedCloseCash,
                shift.varianceCash as varianceCash,
                closeMovement.note as varianceReason,
                shift.openedAt as openedAt,
                shift.closedAt as closedAt
            from CashShiftEntity shift
            join shift.storeLocation store
            join shift.terminalDevice terminal
            join shift.cashierUser cashier
            left join com.saulpos.server.shift.model.CashMovementEntity closeMovement
                on closeMovement.shift.id = shift.id and closeMovement.movementType = :closeType
            where (:from is null or coalesce(shift.closedAt, shift.openedAt) >= :from)
              and (:to is null or coalesce(shift.closedAt, shift.openedAt) <= :to)
              and (:storeLocationId is null or store.id = :storeLocationId)
              and (:terminalDeviceId is null or terminal.id = :terminalDeviceId)
              and (:cashierUserId is null or cashier.id = :cashierUserId)
            order by coalesce(shift.closedAt, shift.openedAt) desc, shift.id desc
            """)
    List<CashShiftReportProjection> findCashShiftRows(@Param("from") Instant from,
                                                      @Param("to") Instant to,
                                                      @Param("storeLocationId") Long storeLocationId,
                                                      @Param("terminalDeviceId") Long terminalDeviceId,
                                                      @Param("cashierUserId") Long cashierUserId,
                                                      @Param("closeType") CashMovementType closeType);

    @Query("""
            select
                cast(shift.closedAt as date) as businessDate,
                store.id as storeLocationId,
                store.code as storeLocationCode,
                store.name as storeLocationName,
                count(shift.id) as shiftCount,
                coalesce(sum(shift.expectedCloseCash), 0) as expectedCloseCash,
                coalesce(sum(shift.countedCloseCash), 0) as countedCloseCash,
                coalesce(sum(shift.varianceCash), 0) as varianceCash
            from CashShiftEntity shift
            join shift.storeLocation store
            join shift.terminalDevice terminal
            join shift.cashierUser cashier
            where shift.status = :closedStatus
              and shift.closedAt is not null
              and (:from is null or shift.closedAt >= :from)
              and (:to is null or shift.closedAt <= :to)
              and (:storeLocationId is null or store.id = :storeLocationId)
              and (:terminalDeviceId is null or terminal.id = :terminalDeviceId)
              and (:cashierUserId is null or cashier.id = :cashierUserId)
            group by cast(shift.closedAt as date), store.id, store.code, store.name
            order by cast(shift.closedAt as date) asc, store.code asc
            """)
    List<EndOfDayCashProjection> findEndOfDayRows(@Param("from") Instant from,
                                                  @Param("to") Instant to,
                                                  @Param("storeLocationId") Long storeLocationId,
                                                  @Param("terminalDeviceId") Long terminalDeviceId,
                                                  @Param("cashierUserId") Long cashierUserId,
                                                  @Param("closedStatus") CashShiftStatus closedStatus);

    @Query("""
            select
                cast(shift.closedAt as date) as businessDate,
                store.id as storeLocationId,
                coalesce(closeMovement.note, 'UNSPECIFIED') as reason,
                count(shift.id) as reasonCount
            from CashShiftEntity shift
            join shift.storeLocation store
            join shift.terminalDevice terminal
            join shift.cashierUser cashier
            left join com.saulpos.server.shift.model.CashMovementEntity closeMovement
                on closeMovement.shift.id = shift.id and closeMovement.movementType = :closeType
            where shift.status = :closedStatus
              and shift.closedAt is not null
              and shift.varianceCash is not null
              and shift.varianceCash <> 0
              and (:from is null or shift.closedAt >= :from)
              and (:to is null or shift.closedAt <= :to)
              and (:storeLocationId is null or store.id = :storeLocationId)
              and (:terminalDeviceId is null or terminal.id = :terminalDeviceId)
              and (:cashierUserId is null or cashier.id = :cashierUserId)
            group by cast(shift.closedAt as date), store.id, closeMovement.note
            order by cast(shift.closedAt as date) asc, store.id asc, reasonCount desc, reason asc
            """)
    List<EndOfDayVarianceReasonProjection> findEndOfDayVarianceReasons(@Param("from") Instant from,
                                                                       @Param("to") Instant to,
                                                                       @Param("storeLocationId") Long storeLocationId,
                                                                       @Param("terminalDeviceId") Long terminalDeviceId,
                                                                       @Param("cashierUserId") Long cashierUserId,
                                                                       @Param("closedStatus") CashShiftStatus closedStatus,
                                                                       @Param("closeType") CashMovementType closeType);

    interface CashShiftReportProjection {
        Long getShiftId();

        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        Long getTerminalDeviceId();

        String getTerminalDeviceCode();

        String getTerminalDeviceName();

        Long getCashierUserId();

        String getCashierUsername();

        CashShiftStatus getStatus();

        BigDecimal getOpeningCash();

        BigDecimal getTotalPaidIn();

        BigDecimal getTotalPaidOut();

        BigDecimal getExpectedCloseCash();

        BigDecimal getCountedCloseCash();

        BigDecimal getVarianceCash();

        String getVarianceReason();

        Instant getOpenedAt();

        Instant getClosedAt();
    }

    interface EndOfDayCashProjection {
        LocalDate getBusinessDate();

        Long getStoreLocationId();

        String getStoreLocationCode();

        String getStoreLocationName();

        long getShiftCount();

        BigDecimal getExpectedCloseCash();

        BigDecimal getCountedCloseCash();

        BigDecimal getVarianceCash();
    }

    interface EndOfDayVarianceReasonProjection {
        LocalDate getBusinessDate();

        Long getStoreLocationId();

        String getReason();

        long getReasonCount();
    }
}
