package com.saulpos.server.receipt.service;

import com.saulpos.api.receipt.ReceiptPrintRequest;
import com.saulpos.api.receipt.ReceiptReprintRequest;
import com.saulpos.api.receipt.ReceiptPrintResponse;
import com.saulpos.api.receipt.ReceiptPrintStatus;
import com.saulpos.core.printing.PrintJob;
import com.saulpos.core.printing.PrintResult;
import com.saulpos.core.printing.PrintStatus;
import com.saulpos.core.printing.PrinterAdapter;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.receipt.model.ReceiptPrintEventEntity;
import com.saulpos.server.receipt.printing.ReceiptPrintPayload;
import com.saulpos.server.receipt.printing.ReceiptTemplateRenderer;
import com.saulpos.server.receipt.repository.ReceiptPrintEventRepository;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleLineEntity;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.authorization.SecurityAuthority;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptPrintService {

    private static final String ADAPTER_NAME = "ESC_POS";

    private final SaleRepository saleRepository;
    private final UserAccountRepository userAccountRepository;
    private final ReceiptPrintEventRepository receiptPrintEventRepository;
    private final ReceiptTemplateRenderer receiptTemplateRenderer;
    private final PrinterAdapter printerAdapter;

    @Transactional
    public ReceiptPrintResponse print(ReceiptPrintRequest request) {
        String actorUsername = resolveActorUsername();
        if (request.copy() && !canReprint()) {
            throw new BaseException(ErrorCode.AUTH_FORBIDDEN, "receipt reprint permission is required");
        }
        return printInternal(normalizeReceiptNumber(request.receiptNumber()), request.copy(), actorUsername);
    }

    @Transactional
    public ReceiptPrintResponse reprint(ReceiptReprintRequest request) {
        if (!canReprint()) {
            throw new BaseException(ErrorCode.AUTH_FORBIDDEN, "receipt reprint permission is required");
        }
        return printInternal(normalizeReceiptNumber(request.receiptNumber()), true, resolveActorUsername());
    }

    private ReceiptPrintResponse printInternal(String receiptNumber, boolean copy, String actorUsername) {
        SaleEntity sale = requireSale(receiptNumber);
        Instant printedAt = Instant.now();
        ReceiptPrintPayload payload = toPrintPayload(sale, copy, actorUsername, printedAt);
        String renderedReceipt = receiptTemplateRenderer.render(payload);

        PrintResult result;
        try {
            result = printerAdapter.print(new PrintJob(
                    "receipt-print-" + sale.getId(),
                    sale.getTerminalDevice().getCode(),
                    renderedReceipt.getBytes(StandardCharsets.UTF_8)));
        } catch (RuntimeException exception) {
            result = PrintResult.failed(true, "print dispatch failed: " + exception.getMessage());
        }

        if (result == null) {
            result = PrintResult.failed(true, "print adapter returned no result");
        }

        ReceiptPrintResponse response = new ReceiptPrintResponse(
                sale.getReceiptNumber(),
                mapStatus(result.status()),
                ADAPTER_NAME,
                result.retryable(),
                result.message(),
                printedAt);

        if (copy) {
            persistCopyPrintEvent(sale, actorUsername, response);
        }
        return response;
    }

    private ReceiptPrintPayload toPrintPayload(SaleEntity sale, boolean copy, String actorUsername, Instant printedAt) {
        List<ReceiptPrintPayload.Line> lines = sale.getLines().stream()
                .sorted(Comparator.comparingInt(SaleLineEntity::getLineNumber))
                .map(line -> new ReceiptPrintPayload.Line(
                        line.getLineNumber(),
                        line.getProduct().getName(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getGrossAmount()))
                .toList();

        return new ReceiptPrintPayload(
                sale.getReceiptNumber(),
                sale.getStoreLocation().getCode(),
                sale.getStoreLocation().getName(),
                sale.getTerminalDevice().getCode(),
                sale.getCashierUser().getUsername(),
                sale.getCreatedAt(),
                lines,
                sale.getSubtotalNet(),
                sale.getTotalTax(),
                sale.getTotalPayable(),
                copy,
                copy ? actorUsername : null,
                copy ? printedAt : null);
    }

    private void persistCopyPrintEvent(SaleEntity sale, String actorUsername, ReceiptPrintResponse response) {
        ReceiptPrintEventEntity event = new ReceiptPrintEventEntity();
        event.setSale(sale);
        event.setReceiptHeaderId(sale.getReceiptHeaderId());
        event.setStoreLocation(sale.getStoreLocation());
        event.setTerminalDevice(sale.getTerminalDevice());
        event.setActorUser(userAccountRepository.findByUsernameIgnoreCase(actorUsername).orElse(null));
        event.setActorUsername(actorUsername);
        event.setCopy(true);
        event.setAdapter(ADAPTER_NAME);
        event.setStatus(response.status());
        event.setRetryable(response.retryable());
        event.setMessage(response.message());
        event.setCorrelationId(MDC.get("correlationId"));
        event.setPrintedAt(response.printedAt());
        receiptPrintEventRepository.save(event);
    }

    private SaleEntity requireSale(String receiptNumber) {
        return saleRepository.findByReceiptNumberIgnoreCase(receiptNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "sale not found for receiptNumber: " + receiptNumber));
    }

    private String normalizeReceiptNumber(String receiptNumber) {
        if (receiptNumber == null || receiptNumber.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "receiptNumber is required");
        }
        return receiptNumber.trim();
    }

    private ReceiptPrintStatus mapStatus(PrintStatus printStatus) {
        return printStatus == PrintStatus.SUCCESS ? ReceiptPrintStatus.SUCCESS : ReceiptPrintStatus.FAILED;
    }

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "unknown";
        }
        return authentication.getName().trim();
    }

    private boolean canReprint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        String reprintAuthority = SecurityAuthority.permission(PermissionCodes.RECEIPT_REPRINT);
        String configAuthority = SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE);
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> reprintAuthority.equals(granted.getAuthority())
                        || configAuthority.equals(granted.getAuthority()));
    }
}
