package com.saulpos.server.tax.web;

import com.saulpos.api.tax.TaxPreviewRequest;
import com.saulpos.api.tax.TaxPreviewResponse;
import com.saulpos.server.tax.service.TaxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
@Validated
public class TaxController {

    private final TaxService taxService;

    @PostMapping("/preview")
    public TaxPreviewResponse preview(@Valid @RequestBody TaxPreviewRequest request) {
        return taxService.preview(request);
    }
}
