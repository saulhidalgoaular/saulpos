package com.saulpos.server.storecredit.web;

import com.saulpos.api.storecredit.StoreCreditAccountResponse;
import com.saulpos.api.storecredit.StoreCreditIssueRequest;
import com.saulpos.api.storecredit.StoreCreditRedeemRequest;
import com.saulpos.server.storecredit.service.StoreCreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store-credits")
@RequiredArgsConstructor
@Validated
public class StoreCreditController {

    private final StoreCreditService storeCreditService;

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreCreditAccountResponse issue(@Valid @RequestBody StoreCreditIssueRequest request) {
        return storeCreditService.issue(request);
    }

    @PostMapping("/{accountId}/redeem")
    public StoreCreditAccountResponse redeem(@PathVariable("accountId") Long accountId,
                                             @Valid @RequestBody StoreCreditRedeemRequest request) {
        return storeCreditService.redeem(accountId, request);
    }

    @GetMapping("/{accountId}")
    public StoreCreditAccountResponse getById(@PathVariable("accountId") Long accountId,
                                              @RequestParam("merchantId") Long merchantId) {
        return storeCreditService.getById(merchantId, accountId);
    }
}
