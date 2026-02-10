package com.saulpos.server.receipt.service;

import com.saulpos.api.receipt.ReceiptPrintRequest;
import com.saulpos.api.receipt.ReceiptPrintResponse;
import com.saulpos.api.receipt.ReceiptPrintStatus;
import com.saulpos.core.printing.PrintJob;
import com.saulpos.core.printing.PrintResult;
import com.saulpos.core.printing.PrintStatus;
import com.saulpos.core.printing.PrinterAdapter;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.receipt.printing.ReceiptPrintPayload;
import com.saulpos.server.receipt.printing.ReceiptTemplateRenderer;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleLineEntity;
import com.saulpos.server.sale.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptPrintService {

    private static final String ADAPTER_NAME = "ESC_POS";

    private final SaleRepository saleRepository;
    private final ReceiptTemplateRenderer receiptTemplateRenderer;
    private final PrinterAdapter printerAdapter;

    public ReceiptPrintResponse print(ReceiptPrintRequest request) {
        SaleEntity sale = requireSale(normalizeReceiptNumber(request.receiptNumber()));
        ReceiptPrintPayload payload = toPrintPayload(sale, request.copy());
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

        return new ReceiptPrintResponse(
                sale.getReceiptNumber(),
                mapStatus(result.status()),
                ADAPTER_NAME,
                result.retryable(),
                result.message(),
                Instant.now());
    }

    private ReceiptPrintPayload toPrintPayload(SaleEntity sale, boolean copy) {
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
                copy);
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
}
