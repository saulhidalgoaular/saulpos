package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.OpenPriceEntryAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenPriceEntryAuditRepository extends JpaRepository<OpenPriceEntryAuditEntity, Long> {
}
