package com.saulpos.server.identity.repository;

import com.saulpos.server.identity.model.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<MerchantEntity, Long> {
    Optional<MerchantEntity> findByCodeIgnoreCase(String code);
}
