package com.saulpos.server.receipt;

import com.saulpos.api.receipt.ReceiptPrintStatus;
import com.saulpos.api.receipt.ReceiptReprintRequest;
import com.saulpos.core.printing.PrintResult;
import com.saulpos.core.printing.PrinterAdapter;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.receipt.model.ReceiptPrintEventEntity;
import com.saulpos.server.receipt.printing.ReceiptTemplateRenderer;
import com.saulpos.server.receipt.repository.ReceiptPrintEventRepository;
import com.saulpos.server.receipt.service.ReceiptPrintService;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptPrintServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private ReceiptPrintEventRepository receiptPrintEventRepository;

    @Mock
    private ReceiptTemplateRenderer receiptTemplateRenderer;

    @Mock
    private PrinterAdapter printerAdapter;

    @InjectMocks
    private ReceiptPrintService receiptPrintService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reprintPersistsCopyAuditEvent() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("manager-m4", "n/a", "PERM_RECEIPT_REPRINT");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SaleEntity sale = buildSale();

        when(saleRepository.findByReceiptNumberIgnoreCase("RCPT-TERM-M4-00000001")).thenReturn(Optional.of(sale));
        when(userAccountRepository.findByUsernameIgnoreCase("manager-m4"))
                .thenReturn(Optional.of(sale.getCashierUser()));
        when(receiptTemplateRenderer.render(any())).thenReturn("RECEIPT COPY");
        when(printerAdapter.print(any())).thenReturn(PrintResult.success("ok"));

        var response = receiptPrintService.reprint(new ReceiptReprintRequest("RCPT-TERM-M4-00000001"));

        ArgumentCaptor<ReceiptPrintEventEntity> captor = ArgumentCaptor.forClass(ReceiptPrintEventEntity.class);
        verify(receiptPrintEventRepository).save(captor.capture());

        ReceiptPrintEventEntity event = captor.getValue();
        assertThat(response.status()).isEqualTo(ReceiptPrintStatus.SUCCESS);
        assertThat(response.receiptNumber()).isEqualTo("RCPT-TERM-M4-00000001");
        assertThat(event.isCopy()).isTrue();
        assertThat(event.getActorUsername()).isEqualTo("manager-m4");
        assertThat(event.getStatus()).isEqualTo(ReceiptPrintStatus.SUCCESS);
        assertThat(event.getSale().getId()).isEqualTo(sale.getId());
    }

    @Test
    void reprintRejectsWhenPermissionMissing() {
        assertThatThrownBy(() -> receiptPrintService.reprint(new ReceiptReprintRequest("RCPT-TERM-M4-00000001")))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_FORBIDDEN);

        verify(saleRepository, never()).findByReceiptNumberIgnoreCase(any());
    }

    private SaleEntity buildSale() {
        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setId(11L);
        cashier.setUsername("cashier-m4");

        StoreLocationEntity store = new StoreLocationEntity();
        store.setId(21L);
        store.setCode("STORE-M4");
        store.setName("Store M4");

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setId(31L);
        terminal.setCode("TERM-M4");

        SaleEntity sale = new SaleEntity();
        sale.setId(41L);
        sale.setCashierUser(cashier);
        sale.setStoreLocation(store);
        sale.setTerminalDevice(terminal);
        sale.setReceiptHeaderId(1001L);
        sale.setReceiptNumber("RCPT-TERM-M4-00000001");
        sale.setSubtotalNet(new BigDecimal("9.09"));
        sale.setTotalTax(new BigDecimal("0.91"));
        sale.setTotalPayable(new BigDecimal("10.00"));
        sale.setCreatedAt(Instant.parse("2026-02-10T12:00:00Z"));
        return sale;
    }
}
