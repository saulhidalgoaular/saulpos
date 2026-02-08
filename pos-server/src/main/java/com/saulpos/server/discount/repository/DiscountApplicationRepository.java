package com.saulpos.server.discount.repository;

import com.saulpos.server.discount.model.DiscountApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscountApplicationRepository extends JpaRepository<DiscountApplicationEntity, Long> {

    Optional<DiscountApplicationEntity> findByIdAndStoreLocationIdAndContextKeyIgnoreCaseAndActiveTrue(
            Long id,
            Long storeLocationId,
            String contextKey
    );

    List<DiscountApplicationEntity> findByStoreLocationIdAndContextKeyIgnoreCaseAndActiveTrueOrderByAppliedAtAscIdAsc(
            Long storeLocationId,
            String contextKey
    );
}
