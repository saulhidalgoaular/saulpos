package com.saulpos.server.sale.web;

import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.server.sale.service.SaleReturnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Validated
public class SaleReturnController {

    private final SaleReturnService saleReturnService;

    @GetMapping("/lookup")
    public SaleReturnLookupResponse lookupByReceipt(
            @RequestParam("receiptNumber")
            @NotBlank(message = "receiptNumber is required")
            @Size(max = 80, message = "receiptNumber must be at most 80 characters") String receiptNumber) {
        return saleReturnService.lookupByReceipt(receiptNumber);
    }

    @PostMapping("/submit")
    public SaleReturnResponse submit(@Valid @RequestBody SaleReturnSubmitRequest request) {
        return saleReturnService.submit(request);
    }
}
