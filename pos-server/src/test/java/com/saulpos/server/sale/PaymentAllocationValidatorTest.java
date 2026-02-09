package com.saulpos.server.sale;

import com.saulpos.api.sale.SaleCheckoutPaymentRequest;
import com.saulpos.api.tax.TenderType;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.sale.service.PaymentAllocationValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentAllocationValidatorTest {

    private final PaymentAllocationValidator validator = new PaymentAllocationValidator();

    @Test
    void validatesSplitAllocationAndComputesCashChange() {
        PaymentAllocationValidator.ValidationResult result = validator.validate(
                new BigDecimal("11.00"),
                List.of(
                        new SaleCheckoutPaymentRequest(TenderType.CARD, new BigDecimal("6.00"), null, "auth-1"),
                        new SaleCheckoutPaymentRequest(TenderType.CASH, new BigDecimal("5.00"), new BigDecimal("10.00"), null)));

        assertThat(result.totalPayable()).isEqualByComparingTo("11.00");
        assertThat(result.totalAllocated()).isEqualByComparingTo("11.00");
        assertThat(result.totalTendered()).isEqualByComparingTo("16.00");
        assertThat(result.changeAmount()).isEqualByComparingTo("5.00");
        assertThat(result.payments()).hasSize(2);
        assertThat(result.payments().get(1).changeAmount()).isEqualByComparingTo("5.00");
    }

    @Test
    void rejectsWhenTotalAllocatedDoesNotMatchPayable() {
        assertThatThrownBy(() -> validator.validate(
                new BigDecimal("11.00"),
                List.of(
                        new SaleCheckoutPaymentRequest(TenderType.CARD, new BigDecimal("4.00"), null, null),
                        new SaleCheckoutPaymentRequest(TenderType.CASH, new BigDecimal("5.00"), new BigDecimal("5.00"), null))))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void rejectsNonCashTenderedAmountWhenItDiffersFromAllocatedAmount() {
        assertThatThrownBy(() -> validator.validate(
                new BigDecimal("11.00"),
                List.of(
                        new SaleCheckoutPaymentRequest(TenderType.CARD, new BigDecimal("11.00"), new BigDecimal("12.00"), null))))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}
