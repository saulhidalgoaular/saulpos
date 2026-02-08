package com.saulpos.server.customer.repository;

import com.saulpos.server.customer.model.CustomerTaxIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerTaxIdentityRepository extends JpaRepository<CustomerTaxIdentityEntity, Long> {

    Optional<CustomerTaxIdentityEntity> findByMerchantIdAndDocumentTypeAndDocumentValueNormalized(
            Long merchantId,
            String documentType,
            String documentValueNormalized);
}
