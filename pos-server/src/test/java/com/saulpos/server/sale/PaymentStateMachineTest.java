package com.saulpos.server.sale;

import com.saulpos.api.sale.PaymentStatus;
import com.saulpos.api.sale.PaymentTransitionAction;
import com.saulpos.server.sale.service.PaymentStateMachine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStateMachineTest {

    @Test
    void authorizedAllowsCaptureAndVoidOnly() {
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.AUTHORIZED, PaymentTransitionAction.CAPTURE))
                .isEqualTo(PaymentStatus.CAPTURED);
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.AUTHORIZED, PaymentTransitionAction.VOID))
                .isEqualTo(PaymentStatus.VOIDED);
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.AUTHORIZED, PaymentTransitionAction.REFUND))
                .isNull();
    }

    @Test
    void capturedAllowsRefundOnly() {
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.CAPTURED, PaymentTransitionAction.REFUND))
                .isEqualTo(PaymentStatus.REFUNDED);
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.CAPTURED, PaymentTransitionAction.CAPTURE))
                .isNull();
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.CAPTURED, PaymentTransitionAction.VOID))
                .isNull();
    }

    @Test
    void terminalStatesRejectAllTransitions() {
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.VOIDED, PaymentTransitionAction.CAPTURE)).isNull();
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.VOIDED, PaymentTransitionAction.VOID)).isNull();
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.REFUNDED, PaymentTransitionAction.REFUND)).isNull();
        assertThat(PaymentStateMachine.nextStatus(PaymentStatus.REFUNDED, PaymentTransitionAction.CAPTURE)).isNull();
    }
}
