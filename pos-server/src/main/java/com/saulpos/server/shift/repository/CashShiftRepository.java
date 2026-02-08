package com.saulpos.server.shift.repository;

import com.saulpos.server.shift.model.CashShiftEntity;
import com.saulpos.server.shift.model.CashShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashShiftRepository extends JpaRepository<CashShiftEntity, Long> {

    Optional<CashShiftEntity> findByCashierUserIdAndTerminalDeviceIdAndStatus(Long cashierUserId,
                                                                               Long terminalDeviceId,
                                                                               CashShiftStatus status);
}
