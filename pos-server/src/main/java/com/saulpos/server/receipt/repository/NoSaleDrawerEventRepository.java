package com.saulpos.server.receipt.repository;

import com.saulpos.server.receipt.model.NoSaleDrawerEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoSaleDrawerEventRepository extends JpaRepository<NoSaleDrawerEventEntity, Long> {
}
