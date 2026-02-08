package com.saulpos.server.discount.repository;

import com.saulpos.server.discount.model.DiscountReasonCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountReasonCodeRepository extends JpaRepository<DiscountReasonCodeEntity, Long> {

    Optional<DiscountReasonCodeEntity> findByMerchantIdAndCodeIgnoreCaseAndActiveTrue(Long merchantId, String code);

    Optional<DiscountReasonCodeEntity> findByMerchantIsNullAndCodeIgnoreCaseAndActiveTrue(String code);
}
