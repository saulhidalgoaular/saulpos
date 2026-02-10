package com.saulpos.server.sale.web;

import com.saulpos.api.sale.PaymentDetailsResponse;
import com.saulpos.api.sale.PaymentTransitionRequest;
import com.saulpos.server.sale.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public PaymentDetailsResponse get(@PathVariable("id") Long id) {
        return paymentService.getPayment(id);
    }

    @PostMapping("/{id}/capture")
    public PaymentDetailsResponse capture(@PathVariable("id") Long id,
                                          @RequestHeader("Idempotency-Key") String idempotencyKey,
                                          @Valid @RequestBody(required = false) PaymentTransitionRequest request) {
        return paymentService.capture(id, idempotencyKey, request);
    }

    @PostMapping("/{id}/void")
    public PaymentDetailsResponse voidPayment(@PathVariable("id") Long id,
                                              @RequestHeader("Idempotency-Key") String idempotencyKey,
                                              @Valid @RequestBody(required = false) PaymentTransitionRequest request) {
        return paymentService.voidPayment(id, idempotencyKey, request);
    }

    @PostMapping("/{id}/refund")
    public PaymentDetailsResponse refund(@PathVariable("id") Long id,
                                         @RequestHeader("Idempotency-Key") String idempotencyKey,
                                         @Valid @RequestBody(required = false) PaymentTransitionRequest request) {
        return paymentService.refund(id, idempotencyKey, request);
    }
}
