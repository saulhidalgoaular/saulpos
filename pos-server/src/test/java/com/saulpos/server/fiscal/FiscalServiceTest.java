package com.saulpos.server.fiscal;

import com.saulpos.core.fiscal.FiscalIssueInvoiceCommand;
import com.saulpos.core.fiscal.FiscalProvider;
import com.saulpos.core.fiscal.FiscalProviderResult;
import com.saulpos.server.fiscal.config.FiscalProperties;
import com.saulpos.server.fiscal.model.FiscalDocumentEntity;
import com.saulpos.server.fiscal.model.FiscalDocumentStatus;
import com.saulpos.server.fiscal.model.FiscalDocumentType;
import com.saulpos.server.fiscal.model.FiscalEventEntity;
import com.saulpos.server.fiscal.model.FiscalEventType;
import com.saulpos.server.fiscal.repository.FiscalDocumentRepository;
import com.saulpos.server.fiscal.repository.FiscalEventRepository;
import com.saulpos.server.fiscal.service.FiscalService;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.sale.model.SaleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalServiceTest {

    @Mock
    private FiscalDocumentRepository fiscalDocumentRepository;

    @Mock
    private FiscalEventRepository fiscalEventRepository;

    @Mock
    private FiscalProvider fiscalProvider;

    private FiscalProperties fiscalProperties;
    private FiscalService fiscalService;

    @BeforeEach
    void setUp() {
        fiscalProperties = new FiscalProperties();
        fiscalService = new FiscalService(
                fiscalDocumentRepository,
                fiscalEventRepository,
                fiscalProvider,
                fiscalProperties);

        when(fiscalDocumentRepository.save(any(FiscalDocumentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(fiscalEventRepository.save(any(FiscalEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void processSaleInvoiceSkipsWhenProviderDisabledAndPolicyAllows() {
        fiscalProperties.setEnabled(false);
        fiscalProperties.setAllowInvoiceWithDisabledProvider(true);

        SaleEntity sale = sale(10L, true);

        when(fiscalDocumentRepository.findBySale_IdAndDocumentType(10L, FiscalDocumentType.INVOICE))
                .thenReturn(Optional.empty());

        fiscalService.processSaleInvoice(sale);

        ArgumentCaptor<FiscalDocumentEntity> documentCaptor = ArgumentCaptor.forClass(FiscalDocumentEntity.class);
        verify(fiscalDocumentRepository).save(documentCaptor.capture());

        FiscalDocumentEntity savedDocument = documentCaptor.getValue();
        assertThat(savedDocument.getStatus()).isEqualTo(FiscalDocumentStatus.SKIPPED);
        assertThat(savedDocument.getProviderCode()).isEqualTo("DISABLED");

        ArgumentCaptor<FiscalEventEntity> eventCaptor = ArgumentCaptor.forClass(FiscalEventEntity.class);
        verify(fiscalEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(FiscalEventType.ISSUE_SKIPPED);

        verify(fiscalProvider, never()).issueInvoice(any(FiscalIssueInvoiceCommand.class));
    }

    @Test
    void processSaleInvoiceIssuesDocumentWhenProviderEnabled() {
        fiscalProperties.setEnabled(true);

        SaleEntity sale = sale(11L, true);

        when(fiscalProvider.providerCode()).thenReturn("STUB");
        when(fiscalProvider.issueInvoice(any(FiscalIssueInvoiceCommand.class)))
                .thenReturn(new FiscalProviderResult(true, "INV-11", "issued"));
        when(fiscalDocumentRepository.findBySale_IdAndDocumentType(11L, FiscalDocumentType.INVOICE))
                .thenReturn(Optional.empty());

        fiscalService.processSaleInvoice(sale);

        ArgumentCaptor<FiscalDocumentEntity> documentCaptor = ArgumentCaptor.forClass(FiscalDocumentEntity.class);
        verify(fiscalDocumentRepository).save(documentCaptor.capture());

        FiscalDocumentEntity savedDocument = documentCaptor.getValue();
        assertThat(savedDocument.getStatus()).isEqualTo(FiscalDocumentStatus.ISSUED);
        assertThat(savedDocument.getExternalDocumentId()).isEqualTo("INV-11");

        ArgumentCaptor<FiscalEventEntity> eventCaptor = ArgumentCaptor.forClass(FiscalEventEntity.class);
        verify(fiscalEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(FiscalEventType.ISSUE_SUCCEEDED);

        verify(fiscalProvider).issueInvoice(any(FiscalIssueInvoiceCommand.class));
    }

    private SaleEntity sale(Long saleId, boolean invoiceRequired) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(1L);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setId(2L);
        storeLocation.setMerchant(merchant);

        SaleEntity sale = new SaleEntity();
        sale.setId(saleId);
        sale.setStoreLocation(storeLocation);
        sale.setInvoiceRequired(invoiceRequired);
        sale.setReceiptNumber("R-" + saleId);
        sale.setTotalPayable(new BigDecimal("12.00"));
        return sale;
    }
}
