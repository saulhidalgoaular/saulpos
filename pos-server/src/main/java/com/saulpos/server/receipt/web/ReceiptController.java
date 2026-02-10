package com.saulpos.server.receipt.web;

import com.saulpos.api.receipt.ReceiptAllocationRequest;
import com.saulpos.api.receipt.ReceiptAllocationResponse;
import com.saulpos.api.receipt.CashDrawerOpenRequest;
import com.saulpos.api.receipt.CashDrawerOpenResponse;
import com.saulpos.api.receipt.ReceiptJournalResponse;
import com.saulpos.api.receipt.ReceiptPrintRequest;
import com.saulpos.api.receipt.ReceiptPrintResponse;
import com.saulpos.api.receipt.ReceiptReprintRequest;
import com.saulpos.server.receipt.service.CashDrawerService;
import com.saulpos.server.receipt.service.ReceiptPrintService;
import com.saulpos.server.receipt.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Validated
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ReceiptPrintService receiptPrintService;
    private final CashDrawerService cashDrawerService;

    @PostMapping("/allocate")
    public ReceiptAllocationResponse allocate(@Valid @RequestBody ReceiptAllocationRequest request) {
        return receiptService.allocate(request);
    }

    @PostMapping("/print")
    public ReceiptPrintResponse print(@Valid @RequestBody ReceiptPrintRequest request) {
        return receiptPrintService.print(request);
    }

    @PostMapping("/reprint")
    public ReceiptPrintResponse reprint(@Valid @RequestBody ReceiptReprintRequest request) {
        return receiptPrintService.reprint(request);
    }

    @GetMapping("/journal/by-sale/{saleId}")
    public ReceiptJournalResponse journalBySaleId(@PathVariable("saleId") Long saleId) {
        return receiptService.getJournalBySaleId(saleId);
    }

    @GetMapping("/journal/by-number/{receiptNumber}")
    public ReceiptJournalResponse journalByReceiptNumber(@PathVariable("receiptNumber") String receiptNumber) {
        return receiptService.getJournalByReceiptNumber(receiptNumber);
    }

    @PostMapping("/drawer/open")
    public CashDrawerOpenResponse openDrawer(@Valid @RequestBody CashDrawerOpenRequest request) {
        return cashDrawerService.open(request);
    }
}
