package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.VoidReasonCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoidReasonCodeRepository extends JpaRepository<VoidReasonCodeEntity, Long> {

    Optional<VoidReasonCodeEntity> findByMerchantIdAndCodeIgnoreCaseAndActiveTrue(Long merchantId, String code);

    Optional<VoidReasonCodeEntity> findByMerchantIsNullAndCodeIgnoreCaseAndActiveTrue(String code);
}
