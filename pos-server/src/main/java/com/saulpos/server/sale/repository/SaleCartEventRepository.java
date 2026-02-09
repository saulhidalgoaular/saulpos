package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleCartEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleCartEventRepository extends JpaRepository<SaleCartEventEntity, Long> {
}
