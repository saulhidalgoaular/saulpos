package com.saulpos.server.receipt.repository;

import com.saulpos.server.receipt.model.ReceiptPrintEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptPrintEventRepository extends JpaRepository<ReceiptPrintEventEntity, Long> {
}
