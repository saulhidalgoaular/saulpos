package com.saulpos.server.receipt.repository;

import com.saulpos.server.receipt.model.ReceiptSeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptSeriesRepository extends JpaRepository<ReceiptSeriesEntity, Long> {

    Optional<ReceiptSeriesEntity> findByTerminalDeviceId(Long terminalDeviceId);
}
