package com.saulpos.server.sale;

import com.saulpos.server.sale.service.SaleReturnAmountAllocator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SaleReturnAmountAllocatorTest {

    private final SaleReturnAmountAllocator allocator = new SaleReturnAmountAllocator();

    @Test
    void allocatesProRatedAmountsForPartialReturn() {
        SaleReturnAmountAllocator.Allocation allocation = allocator.allocate(
                new BigDecimal("2.000"),
                new BigDecimal("10.00"),
                new BigDecimal("1.00"),
                new BigDecimal("11.00"),
                new BigDecimal("0.000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("1.000"));

        assertThat(allocation.quantity()).isEqualByComparingTo(new BigDecimal("1.000"));
        assertThat(allocation.netAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(allocation.taxAmount()).isEqualByComparingTo(new BigDecimal("0.50"));
        assertThat(allocation.grossAmount()).isEqualByComparingTo(new BigDecimal("5.50"));
    }

    @Test
    void allocatesRemainingAmountOnFinalReturnToAvoidRoundingLoss() {
        SaleReturnAmountAllocator.Allocation allocation = allocator.allocate(
                new BigDecimal("3.000"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("2.000"),
                new BigDecimal("6.67"),
                BigDecimal.ZERO,
                new BigDecimal("6.67"),
                new BigDecimal("1.000"));

        assertThat(allocation.netAmount()).isEqualByComparingTo(new BigDecimal("3.33"));
        assertThat(allocation.grossAmount()).isEqualByComparingTo(new BigDecimal("3.33"));
    }
}
