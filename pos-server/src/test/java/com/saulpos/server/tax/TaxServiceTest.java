package com.saulpos.server.tax;

import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.PriceResolutionSource;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.api.tax.TaxPreviewLineRequest;
import com.saulpos.api.tax.TaxPreviewResponse;
import com.saulpos.api.tax.TaxPreviewRequest;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.tax.model.StoreTaxRuleEntity;
import com.saulpos.server.tax.model.TaxGroupEntity;
import com.saulpos.server.tax.repository.StoreTaxRuleRepository;
import com.saulpos.server.tax.service.TaxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxServiceTest {

    @Mock
    private StoreLocationRepository storeLocationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreTaxRuleRepository storeTaxRuleRepository;

    @Mock
    private PricingService pricingService;

    private TaxService taxService;

    @BeforeEach
    void setUp() {
        taxService = new TaxService(
                storeLocationRepository,
                productRepository,
                storeTaxRuleRepository,
                pricingService);
    }

    @Test
    void previewCalculatesExclusiveTaxFromNetAmount() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity storeLocation = storeLocation(10L, 1L);
        TaxGroupEntity taxGroup = taxGroup(700L, "VAT18", "18.0000", false);
        ProductEntity product = product(100L, 1L, taxGroup);
        StoreTaxRuleEntity taxRule = storeTaxRule(10L, taxGroup, TaxMode.EXCLUSIVE, false);

        when(storeLocationRepository.findById(10L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(storeTaxRuleRepository.findApplicable(eq(10L), eq(700L), eq(at), any()))
                .thenReturn(List.of(taxRule));

        TaxPreviewResponse response = taxService.preview(new TaxPreviewRequest(
                10L,
                at,
                List.of(new TaxPreviewLineRequest(100L, new BigDecimal("2.000"), new BigDecimal("10.00")))));

        assertThat(response.subtotalNet()).isEqualByComparingTo("20.00");
        assertThat(response.totalTax()).isEqualByComparingTo("3.60");
        assertThat(response.totalGross()).isEqualByComparingTo("23.60");

        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().getFirst().taxAmount()).isEqualByComparingTo("3.60");
        assertThat(response.lines().getFirst().taxMode()).isEqualTo(TaxMode.EXCLUSIVE);
    }

    @Test
    void previewCalculatesInclusiveTaxFromGrossAmount() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity storeLocation = storeLocation(11L, 2L);
        TaxGroupEntity taxGroup = taxGroup(701L, "VAT18", "18.0000", false);
        ProductEntity product = product(101L, 2L, taxGroup);
        StoreTaxRuleEntity taxRule = storeTaxRule(11L, taxGroup, TaxMode.INCLUSIVE, false);

        when(storeLocationRepository.findById(11L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(storeTaxRuleRepository.findApplicable(eq(11L), eq(701L), eq(at), any()))
                .thenReturn(List.of(taxRule));

        TaxPreviewResponse response = taxService.preview(new TaxPreviewRequest(
                11L,
                at,
                List.of(new TaxPreviewLineRequest(101L, new BigDecimal("1.000"), new BigDecimal("11.80")))));

        assertThat(response.subtotalNet()).isEqualByComparingTo("10.00");
        assertThat(response.totalTax()).isEqualByComparingTo("1.80");
        assertThat(response.totalGross()).isEqualByComparingTo("11.80");

        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().getFirst().taxMode()).isEqualTo(TaxMode.INCLUSIVE);
    }

    @Test
    void previewTreatsExemptRuleAsZeroTax() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity storeLocation = storeLocation(12L, 3L);
        TaxGroupEntity taxGroup = taxGroup(702L, "VAT18", "18.0000", false);
        ProductEntity product = product(102L, 3L, taxGroup);
        StoreTaxRuleEntity taxRule = storeTaxRule(12L, taxGroup, TaxMode.EXCLUSIVE, true);

        when(storeLocationRepository.findById(12L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(102L)).thenReturn(Optional.of(product));
        when(storeTaxRuleRepository.findApplicable(eq(12L), eq(702L), eq(at), any()))
                .thenReturn(List.of(taxRule));

        TaxPreviewResponse response = taxService.preview(new TaxPreviewRequest(
                12L,
                at,
                List.of(new TaxPreviewLineRequest(102L, new BigDecimal("1.000"), new BigDecimal("15.50")))));

        assertThat(response.subtotalNet()).isEqualByComparingTo("15.50");
        assertThat(response.totalTax()).isEqualByComparingTo("0.00");
        assertThat(response.totalGross()).isEqualByComparingTo("15.50");

        assertThat(response.lines().getFirst().exempt()).isTrue();
        assertThat(response.lines().getFirst().zeroRated()).isFalse();
    }

    @Test
    void previewTreatsZeroRatedGroupAsZeroTax() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity storeLocation = storeLocation(13L, 4L);
        TaxGroupEntity taxGroup = taxGroup(703L, "ZERO", "0.0000", true);
        ProductEntity product = product(103L, 4L, taxGroup);
        StoreTaxRuleEntity taxRule = storeTaxRule(13L, taxGroup, TaxMode.EXCLUSIVE, false);

        when(storeLocationRepository.findById(13L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(103L)).thenReturn(Optional.of(product));
        when(storeTaxRuleRepository.findApplicable(eq(13L), eq(703L), eq(at), any()))
                .thenReturn(List.of(taxRule));

        TaxPreviewResponse response = taxService.preview(new TaxPreviewRequest(
                13L,
                at,
                List.of(new TaxPreviewLineRequest(103L, new BigDecimal("1.000"), new BigDecimal("7.25")))));

        assertThat(response.subtotalNet()).isEqualByComparingTo("7.25");
        assertThat(response.totalTax()).isEqualByComparingTo("0.00");
        assertThat(response.totalGross()).isEqualByComparingTo("7.25");

        assertThat(response.lines().getFirst().exempt()).isTrue();
        assertThat(response.lines().getFirst().zeroRated()).isTrue();
    }

    @Test
    void previewUsesPriceResolverWhenLineUnitPriceIsNotProvided() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity storeLocation = storeLocation(14L, 5L);
        TaxGroupEntity taxGroup = taxGroup(704L, "VAT18", "18.0000", false);
        ProductEntity product = product(104L, 5L, taxGroup);
        StoreTaxRuleEntity taxRule = storeTaxRule(14L, taxGroup, TaxMode.EXCLUSIVE, false);

        when(storeLocationRepository.findById(14L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(104L)).thenReturn(Optional.of(product));
        when(storeTaxRuleRepository.findApplicable(eq(14L), eq(704L), eq(at), any()))
                .thenReturn(List.of(taxRule));
        when(pricingService.resolvePrice(14L, 104L, at)).thenReturn(
                new PriceResolutionResponse(
                        14L,
                        104L,
                        new BigDecimal("8.25"),
                        PriceResolutionSource.BASE_PRICE,
                        104L,
                        null,
                        null,
                        at));

        TaxPreviewResponse response = taxService.preview(new TaxPreviewRequest(
                14L,
                at,
                List.of(new TaxPreviewLineRequest(104L, new BigDecimal("1.000"), null))));

        assertThat(response.subtotalNet()).isEqualByComparingTo("8.25");
        assertThat(response.totalTax()).isEqualByComparingTo("1.49");
        assertThat(response.totalGross()).isEqualByComparingTo("9.74");
    }

    private StoreLocationEntity storeLocation(Long storeLocationId, Long merchantId) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setId(storeLocationId);
        storeLocation.setMerchant(merchant);
        return storeLocation;
    }

    private TaxGroupEntity taxGroup(Long taxGroupId, String code, String ratePercent, boolean zeroRated) {
        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setId(taxGroupId);
        taxGroup.setCode(code);
        taxGroup.setName(code);
        taxGroup.setTaxRatePercent(new BigDecimal(ratePercent));
        taxGroup.setZeroRated(zeroRated);
        taxGroup.setActive(true);
        return taxGroup;
    }

    private ProductEntity product(Long productId, Long merchantId, TaxGroupEntity taxGroup) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setMerchant(merchant);
        product.setTaxGroup(taxGroup);
        return product;
    }

    private StoreTaxRuleEntity storeTaxRule(Long storeLocationId,
                                            TaxGroupEntity taxGroup,
                                            TaxMode taxMode,
                                            boolean exempt) {
        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setId(storeLocationId);

        StoreTaxRuleEntity storeTaxRule = new StoreTaxRuleEntity();
        storeTaxRule.setStoreLocation(storeLocation);
        storeTaxRule.setTaxGroup(taxGroup);
        storeTaxRule.setTaxMode(taxMode);
        storeTaxRule.setExempt(exempt);
        storeTaxRule.setActive(true);
        return storeTaxRule;
    }
}
