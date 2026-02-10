package com.saulpos.server.fiscal.service;

import com.saulpos.core.fiscal.FiscalCancelInvoiceCommand;
import com.saulpos.core.fiscal.FiscalIssueCreditNoteCommand;
import com.saulpos.core.fiscal.FiscalIssueInvoiceCommand;
import com.saulpos.core.fiscal.FiscalProvider;
import com.saulpos.core.fiscal.FiscalProviderResult;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.fiscal.config.FiscalProperties;
import com.saulpos.server.fiscal.model.FiscalDocumentEntity;
import com.saulpos.server.fiscal.model.FiscalDocumentStatus;
import com.saulpos.server.fiscal.model.FiscalDocumentType;
import com.saulpos.server.fiscal.model.FiscalEventEntity;
import com.saulpos.server.fiscal.model.FiscalEventType;
import com.saulpos.server.fiscal.repository.FiscalDocumentRepository;
import com.saulpos.server.fiscal.repository.FiscalEventRepository;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleReturnEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FiscalService {

    private final FiscalDocumentRepository fiscalDocumentRepository;
    private final FiscalEventRepository fiscalEventRepository;
    private final FiscalProvider fiscalProvider;
    private final FiscalProperties fiscalProperties;

    @Transactional
    public void processSaleInvoice(SaleEntity sale) {
        if (!sale.isInvoiceRequired()) {
            return;
        }

        if (!fiscalProperties.isEnabled()) {
            if (!fiscalProperties.isAllowInvoiceWithDisabledProvider()) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "invoiceRequired sale cannot be completed while fiscal provider is disabled");
            }
            FiscalDocumentEntity skipped = upsertSaleDocument(sale, FiscalDocumentType.INVOICE);
            skipped.setStatus(FiscalDocumentStatus.SKIPPED);
            skipped.setProviderCode("DISABLED");
            skipped.setExternalDocumentId(null);
            skipped.setIssuedAt(null);
            skipped.setCancelledAt(null);
            skipped.setMessage("fiscal provider disabled by policy");
            recordEvent(fiscalDocumentRepository.save(skipped), FiscalEventType.ISSUE_SKIPPED, skipped.getMessage());
            return;
        }

        FiscalDocumentEntity document = upsertSaleDocument(sale, FiscalDocumentType.INVOICE);
        document.setProviderCode(fiscalProvider.providerCode());
        document.setRequestReference(sale.getReceiptNumber());

        try {
            FiscalProviderResult result = fiscalProvider.issueInvoice(new FiscalIssueInvoiceCommand(
                    sale.getId(),
                    sale.getReceiptNumber(),
                    sale.getStoreLocation().getMerchant().getId(),
                    sale.getStoreLocation().getId(),
                    sale.getCustomer() == null ? null : sale.getCustomer().getId(),
                    sale.getTotalPayable()));

            document.setStatus(result.success() ? FiscalDocumentStatus.ISSUED : FiscalDocumentStatus.FAILED);
            document.setExternalDocumentId(result.externalDocumentId());
            document.setIssuedAt(result.success() ? Instant.now() : null);
            document.setCancelledAt(null);
            document.setMessage(normalizeMessage(result.message()));

            FiscalDocumentEntity savedDocument = fiscalDocumentRepository.save(document);
            recordEvent(savedDocument,
                    result.success() ? FiscalEventType.ISSUE_SUCCEEDED : FiscalEventType.ISSUE_FAILED,
                    document.getMessage());
        } catch (RuntimeException exception) {
            document.setStatus(FiscalDocumentStatus.FAILED);
            document.setExternalDocumentId(null);
            document.setIssuedAt(null);
            document.setCancelledAt(null);
            document.setMessage("provider exception: " + exception.getClass().getSimpleName());
            FiscalDocumentEntity savedDocument = fiscalDocumentRepository.save(document);
            recordEvent(savedDocument, FiscalEventType.ISSUE_FAILED, document.getMessage());
        }
    }

    @Transactional
    public void processSaleReturnCreditNote(SaleReturnEntity saleReturn) {
        if (!saleReturn.getSale().isInvoiceRequired()) {
            return;
        }
        if (!fiscalProperties.isEnabled()) {
            return;
        }

        FiscalDocumentEntity document = upsertSaleReturnDocument(saleReturn, FiscalDocumentType.CREDIT_NOTE);
        document.setProviderCode(fiscalProvider.providerCode());
        document.setRequestReference(saleReturn.getReturnReference());

        try {
            FiscalProviderResult result = fiscalProvider.issueCreditNote(new FiscalIssueCreditNoteCommand(
                    saleReturn.getId(),
                    saleReturn.getSale().getId(),
                    saleReturn.getReturnReference(),
                    saleReturn.getSale().getReceiptNumber(),
                    saleReturn.getSale().getStoreLocation().getMerchant().getId(),
                    saleReturn.getSale().getStoreLocation().getId(),
                    saleReturn.getSale().getCustomer() == null ? null : saleReturn.getSale().getCustomer().getId(),
                    saleReturn.getTotalGross()));

            document.setStatus(result.success() ? FiscalDocumentStatus.ISSUED : FiscalDocumentStatus.FAILED);
            document.setExternalDocumentId(result.externalDocumentId());
            document.setIssuedAt(result.success() ? Instant.now() : null);
            document.setCancelledAt(null);
            document.setMessage(normalizeMessage(result.message()));

            FiscalDocumentEntity savedDocument = fiscalDocumentRepository.save(document);
            recordEvent(savedDocument,
                    result.success() ? FiscalEventType.CREDIT_NOTE_SUCCEEDED : FiscalEventType.CREDIT_NOTE_FAILED,
                    document.getMessage());
        } catch (RuntimeException exception) {
            document.setStatus(FiscalDocumentStatus.FAILED);
            document.setExternalDocumentId(null);
            document.setIssuedAt(null);
            document.setCancelledAt(null);
            document.setMessage("provider exception: " + exception.getClass().getSimpleName());
            FiscalDocumentEntity savedDocument = fiscalDocumentRepository.save(document);
            recordEvent(savedDocument, FiscalEventType.CREDIT_NOTE_FAILED, document.getMessage());
        }
    }

    @Transactional
    public void cancelInvoice(FiscalDocumentEntity document, String reason) {
        if (document.getExternalDocumentId() == null || document.getExternalDocumentId().isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "cannot cancel fiscal document without externalDocumentId");
        }

        try {
            FiscalProviderResult result = fiscalProvider.cancelInvoice(new FiscalCancelInvoiceCommand(
                    document.getId(),
                    document.getExternalDocumentId(),
                    reason));

            document.setStatus(result.success() ? FiscalDocumentStatus.CANCELLED : FiscalDocumentStatus.FAILED);
            document.setCancelledAt(result.success() ? Instant.now() : null);
            document.setMessage(normalizeMessage(result.message()));

            FiscalDocumentEntity savedDocument = fiscalDocumentRepository.save(document);
            recordEvent(savedDocument,
                    result.success() ? FiscalEventType.CANCEL_SUCCEEDED : FiscalEventType.CANCEL_FAILED,
                    document.getMessage());
        } catch (RuntimeException exception) {
            document.setStatus(FiscalDocumentStatus.FAILED);
            document.setCancelledAt(null);
            document.setMessage("provider exception: " + exception.getClass().getSimpleName());
            FiscalDocumentEntity savedDocument = fiscalDocumentRepository.save(document);
            recordEvent(savedDocument, FiscalEventType.CANCEL_FAILED, document.getMessage());
        }
    }

    private FiscalDocumentEntity upsertSaleDocument(SaleEntity sale, FiscalDocumentType documentType) {
        FiscalDocumentEntity document = fiscalDocumentRepository.findBySale_IdAndDocumentType(sale.getId(), documentType)
                .orElseGet(FiscalDocumentEntity::new);
        document.setSale(sale);
        document.setSaleReturn(null);
        document.setDocumentType(documentType);
        document.setRequestReference(sale.getReceiptNumber());
        return document;
    }

    private FiscalDocumentEntity upsertSaleReturnDocument(SaleReturnEntity saleReturn, FiscalDocumentType documentType) {
        FiscalDocumentEntity document = fiscalDocumentRepository.findBySaleReturn_IdAndDocumentType(
                        saleReturn.getId(),
                        documentType)
                .orElseGet(FiscalDocumentEntity::new);
        document.setSale(saleReturn.getSale());
        document.setSaleReturn(saleReturn);
        document.setDocumentType(documentType);
        document.setRequestReference(saleReturn.getReturnReference());
        return document;
    }

    private void recordEvent(FiscalDocumentEntity document, FiscalEventType eventType, String message) {
        FiscalEventEntity event = new FiscalEventEntity();
        event.setFiscalDocument(document);
        event.setEventType(eventType);
        event.setMessage(normalizeMessage(message));
        fiscalEventRepository.save(event);
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return null;
        }
        String normalized = message.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
