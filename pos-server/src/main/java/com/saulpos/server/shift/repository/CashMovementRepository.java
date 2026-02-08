package com.saulpos.server.shift.repository;

import com.saulpos.server.shift.model.CashMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMovementRepository extends JpaRepository<CashMovementEntity, Long> {
}
