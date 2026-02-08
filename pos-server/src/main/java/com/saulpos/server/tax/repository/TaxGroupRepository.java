package com.saulpos.server.tax.repository;

import com.saulpos.server.tax.model.TaxGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxGroupRepository extends JpaRepository<TaxGroupEntity, Long> {

    Optional<TaxGroupEntity> findByIdAndMerchantIdAndActiveTrue(Long id, Long merchantId);
}
