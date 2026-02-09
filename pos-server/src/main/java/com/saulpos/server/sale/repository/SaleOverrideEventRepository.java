package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleOverrideEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleOverrideEventRepository extends JpaRepository<SaleOverrideEventEntity, Long> {
}
