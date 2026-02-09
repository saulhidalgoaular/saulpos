package com.saulpos.server.sale.service;

import com.saulpos.api.sale.PaymentStatus;
import com.saulpos.api.sale.PaymentTransitionAction;

import java.util.Map;

public final class PaymentStateMachine {

    private static final Map<PaymentTransitionAction, PaymentStatus> FROM_AUTHORIZED = Map.of(
            PaymentTransitionAction.CAPTURE, PaymentStatus.CAPTURED,
            PaymentTransitionAction.VOID, PaymentStatus.VOIDED);

    private static final Map<PaymentTransitionAction, PaymentStatus> FROM_CAPTURED = Map.of(
            PaymentTransitionAction.REFUND, PaymentStatus.REFUNDED);

    private PaymentStateMachine() {
    }

    public static PaymentStatus nextStatus(PaymentStatus currentStatus, PaymentTransitionAction action) {
        if (currentStatus == null || action == null) {
            return null;
        }
        return switch (currentStatus) {
            case AUTHORIZED -> FROM_AUTHORIZED.get(action);
            case CAPTURED -> FROM_CAPTURED.get(action);
            case VOIDED, REFUNDED -> null;
        };
    }
}
