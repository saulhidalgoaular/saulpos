package com.saulpos.server.receipt.repository;

import com.saulpos.server.receipt.model.ReceiptHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptHeaderRepository extends JpaRepository<ReceiptHeaderEntity, Long> {
}
