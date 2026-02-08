package com.saulpos.server.tax.repository;

import com.saulpos.api.tax.TenderType;
import com.saulpos.server.tax.model.RoundingPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoundingPolicyRepository extends JpaRepository<RoundingPolicyEntity, Long> {

    Optional<RoundingPolicyEntity> findFirstByStoreLocationIdAndTenderTypeAndActiveTrue(Long storeLocationId,
                                                                                          TenderType tenderType);
}
