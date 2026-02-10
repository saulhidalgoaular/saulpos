package com.saulpos.server.fiscal.repository;

import com.saulpos.server.fiscal.model.FiscalDocumentEntity;
import com.saulpos.server.fiscal.model.FiscalDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FiscalDocumentRepository extends JpaRepository<FiscalDocumentEntity, Long> {

    Optional<FiscalDocumentEntity> findBySale_IdAndDocumentType(Long saleId, FiscalDocumentType documentType);

    Optional<FiscalDocumentEntity> findBySaleReturn_IdAndDocumentType(Long saleReturnId, FiscalDocumentType documentType);
}
