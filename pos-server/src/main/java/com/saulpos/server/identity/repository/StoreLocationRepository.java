package com.saulpos.server.identity.repository;

import com.saulpos.server.identity.model.StoreLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreLocationRepository extends JpaRepository<StoreLocationEntity, Long> {
    Optional<StoreLocationEntity> findByCodeIgnoreCase(String code);
}
