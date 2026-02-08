package com.saulpos.server.identity.repository;

import com.saulpos.server.identity.model.TerminalDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface TerminalDeviceRepository extends JpaRepository<TerminalDeviceEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT terminal FROM TerminalDeviceEntity terminal WHERE terminal.id = :id")
    Optional<TerminalDeviceEntity> findByIdForUpdate(@Param("id") Long id);

    Optional<TerminalDeviceEntity> findByCodeIgnoreCase(String code);
}
