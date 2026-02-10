package com.saulpos.server.fiscal.repository;

import com.saulpos.server.fiscal.model.FiscalEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalEventRepository extends JpaRepository<FiscalEventEntity, Long> {
}
