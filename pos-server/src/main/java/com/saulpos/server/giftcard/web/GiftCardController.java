package com.saulpos.server.giftcard.web;

import com.saulpos.api.giftcard.GiftCardIssueRequest;
import com.saulpos.api.giftcard.GiftCardResponse;
import com.saulpos.api.giftcard.GiftCardRedeemRequest;
import com.saulpos.server.giftcard.service.GiftCardService;
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
@RequestMapping("/api/gift-cards")
@RequiredArgsConstructor
@Validated
public class GiftCardController {

    private final GiftCardService giftCardService;

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public GiftCardResponse issue(@Valid @RequestBody GiftCardIssueRequest request) {
        return giftCardService.issue(request);
    }

    @PostMapping("/{cardNumber}/redeem")
    public GiftCardResponse redeem(@PathVariable("cardNumber") String cardNumber,
                                   @Valid @RequestBody GiftCardRedeemRequest request) {
        return giftCardService.redeem(cardNumber, request);
    }

    @GetMapping("/{cardNumber}")
    public GiftCardResponse getByCardNumber(@PathVariable("cardNumber") String cardNumber,
                                            @RequestParam("merchantId") Long merchantId) {
        return giftCardService.getByCardNumber(merchantId, cardNumber);
    }
}
