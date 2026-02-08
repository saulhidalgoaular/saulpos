package com.saulpos.server.receipt.web;

import com.saulpos.api.receipt.ReceiptAllocationRequest;
import com.saulpos.api.receipt.ReceiptAllocationResponse;
import com.saulpos.server.receipt.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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

    @PostMapping("/allocate")
    public ReceiptAllocationResponse allocate(@Valid @RequestBody ReceiptAllocationRequest request) {
        return receiptService.allocate(request);
    }
}
