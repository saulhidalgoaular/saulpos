package com.saulpos.server.tax;

import com.saulpos.api.tax.RoundingMethod;
import com.saulpos.api.tax.RoundingSummary;
import com.saulpos.api.tax.TenderType;
import com.saulpos.server.tax.model.RoundingPolicyEntity;
import com.saulpos.server.tax.repository.RoundingPolicyRepository;
import com.saulpos.server.tax.service.RoundingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoundingServiceTest {

    @Mock
    private RoundingPolicyRepository roundingPolicyRepository;

    private RoundingService roundingService;

    @BeforeEach
    void setUp() {
        roundingService = new RoundingService(roundingPolicyRepository);
    }

    @Test
    void applyRoundsMidpointUpForNearestPolicy() {
        when(roundingPolicyRepository.findFirstByStoreLocationIdAndTenderTypeAndActiveTrue(10L, TenderType.CASH))
                .thenReturn(Optional.of(policy(TenderType.CASH, RoundingMethod.NEAREST, "0.10")));

        RoundingSummary summary = roundingService.apply(10L, TenderType.CASH, new BigDecimal("10.05"));

        assertThat(summary.applied()).isTrue();
        assertThat(summary.originalAmount()).isEqualByComparingTo("10.05");
        assertThat(summary.roundedAmount()).isEqualByComparingTo("10.10");
        assertThat(summary.adjustment()).isEqualByComparingTo("0.05");
    }

    @Test
    void applyRoundsDownForEdgeValuesWithDownPolicy() {
        when(roundingPolicyRepository.findFirstByStoreLocationIdAndTenderTypeAndActiveTrue(11L, TenderType.CASH))
                .thenReturn(Optional.of(policy(TenderType.CASH, RoundingMethod.DOWN, "0.05")));

        RoundingSummary summary = roundingService.apply(11L, TenderType.CASH, new BigDecimal("10.04"));

        assertThat(summary.applied()).isTrue();
        assertThat(summary.roundedAmount()).isEqualByComparingTo("10.00");
        assertThat(summary.adjustment()).isEqualByComparingTo("-0.04");
    }

    @Test
    void applyRoundsUpForEdgeValuesWithUpPolicy() {
        when(roundingPolicyRepository.findFirstByStoreLocationIdAndTenderTypeAndActiveTrue(12L, TenderType.CARD))
                .thenReturn(Optional.of(policy(TenderType.CARD, RoundingMethod.UP, "0.05")));

        RoundingSummary summary = roundingService.apply(12L, TenderType.CARD, new BigDecimal("10.01"));

        assertThat(summary.applied()).isTrue();
        assertThat(summary.roundedAmount()).isEqualByComparingTo("10.05");
        assertThat(summary.adjustment()).isEqualByComparingTo("0.04");
    }

    @Test
    void applyReturnsNoAdjustmentWhenTenderTypeMissing() {
        RoundingSummary summary = roundingService.apply(13L, null, new BigDecimal("17.89"));

        assertThat(summary.applied()).isFalse();
        assertThat(summary.roundedAmount()).isEqualByComparingTo("17.89");
        assertThat(summary.adjustment()).isEqualByComparingTo("0.00");
    }

    @Test
    void applyReturnsNoAdjustmentWhenNoPolicyConfiguredForTenderType() {
        when(roundingPolicyRepository.findFirstByStoreLocationIdAndTenderTypeAndActiveTrue(14L, TenderType.CARD))
                .thenReturn(Optional.empty());

        RoundingSummary summary = roundingService.apply(14L, TenderType.CARD, new BigDecimal("17.89"));

        assertThat(summary.applied()).isFalse();
        assertThat(summary.tenderType()).isEqualTo(TenderType.CARD);
        assertThat(summary.roundedAmount()).isEqualByComparingTo("17.89");
        assertThat(summary.adjustment()).isEqualByComparingTo("0.00");
    }

    private RoundingPolicyEntity policy(TenderType tenderType, RoundingMethod method, String incrementAmount) {
        RoundingPolicyEntity policy = new RoundingPolicyEntity();
        policy.setTenderType(tenderType);
        policy.setRoundingMethod(method);
        policy.setIncrementAmount(new BigDecimal(incrementAmount));
        policy.setActive(true);
        return policy;
    }
}
