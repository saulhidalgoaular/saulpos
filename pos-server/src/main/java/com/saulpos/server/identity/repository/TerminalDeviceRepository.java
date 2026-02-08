package com.saulpos.server.identity.repository;

import com.saulpos.server.identity.model.TerminalDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TerminalDeviceRepository extends JpaRepository<TerminalDeviceEntity, Long> {
    Optional<TerminalDeviceEntity> findByCodeIgnoreCase(String code);
}
