package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.InventoryMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovementEntity, Long> {
}
