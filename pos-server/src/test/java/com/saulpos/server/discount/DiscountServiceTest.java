package com.saulpos.server.discount;

import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.PriceResolutionSource;
import com.saulpos.api.discount.DiscountApplyRequest;
import com.saulpos.api.discount.DiscountPreviewLineRequest;
import com.saulpos.api.discount.DiscountPreviewRequest;
import com.saulpos.api.discount.DiscountPreviewResponse;
import com.saulpos.api.discount.DiscountScope;
import com.saulpos.api.discount.DiscountType;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.api.tax.TaxPreviewLineResponse;
import com.saulpos.api.tax.TaxPreviewRequest;
import com.saulpos.api.tax.TaxPreviewResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.discount.model.DiscountApplicationEntity;
import com.saulpos.server.discount.model.DiscountReasonCodeEntity;
import com.saulpos.server.discount.repository.DiscountApplicationRepository;
import com.saulpos.server.discount.repository.DiscountReasonCodeRepository;
import com.saulpos.server.discount.service.DiscountService;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.tax.service.TaxService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private StoreLocationRepository storeLocationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DiscountReasonCodeRepository discountReasonCodeRepository;

    @Mock
    private DiscountApplicationRepository discountApplicationRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private TaxService taxService;

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService(
                storeLocationRepository,
                productRepository,
                discountReasonCodeRepository,
                discountApplicationRepository,
                pricingService,
                taxService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void previewAppliesLineThenCartDiscountBeforeTaxPreview() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity store = storeLocation(10L, 1L);
        ProductEntity productA = product(101L, 1L, "SKU-A", "Product A");
        ProductEntity productB = product(102L, 1L, "SKU-B", "Product B");

        DiscountApplicationEntity lineDiscount = lineDiscount(store, productA, "10.0000");
        DiscountApplicationEntity cartDiscount = cartDiscount(store, "5.0000");

        when(storeLocationRepository.findById(10L)).thenReturn(Optional.of(store));
        when(productRepository.findById(101L)).thenReturn(Optional.of(productA));
        when(productRepository.findById(102L)).thenReturn(Optional.of(productB));
        when(pricingService.resolvePrice(10L, 101L, at))
                .thenReturn(new PriceResolutionResponse(10L, 101L, new BigDecimal("10.00"),
                        PriceResolutionSource.BASE_PRICE, 101L, null, null, at));
        when(pricingService.resolvePrice(10L, 102L, at))
                .thenReturn(new PriceResolutionResponse(10L, 102L, new BigDecimal("20.00"),
                        PriceResolutionSource.BASE_PRICE, 102L, null, null, at));
        when(discountApplicationRepository.findByStoreLocationIdAndContextKeyIgnoreCaseAndActiveTrueOrderByAppliedAtAscIdAsc(
                10L, "CTX-E1"))
                .thenReturn(List.of(lineDiscount, cartDiscount));
        when(taxService.preview(org.mockito.ArgumentMatchers.any(TaxPreviewRequest.class)))
                .thenReturn(taxPreviewStub(10L, at));

        DiscountPreviewResponse response = discountService.preview(new DiscountPreviewRequest(
                10L,
                "ctx-e1",
                at,
                null,
                List.of(
                        new DiscountPreviewLineRequest(101L, new BigDecimal("1.000"), null),
                        new DiscountPreviewLineRequest(102L, new BigDecimal("1.000"), null))));

        ArgumentCaptor<TaxPreviewRequest> captor = ArgumentCaptor.forClass(TaxPreviewRequest.class);
        org.mockito.Mockito.verify(taxService).preview(captor.capture());
        TaxPreviewRequest capturedTaxRequest = captor.getValue();

        assertThat(response.subtotalBeforeDiscount()).isEqualByComparingTo("30.00");
        assertThat(response.totalDiscount()).isEqualByComparingTo("6.00");
        assertThat(response.subtotalAfterDiscount()).isEqualByComparingTo("24.00");
        assertThat(response.appliedDiscounts()).hasSize(2);

        assertThat(capturedTaxRequest.lines()).hasSize(2);
        assertThat(capturedTaxRequest.lines().get(0).unitPrice()).isEqualByComparingTo("7.45");
        assertThat(capturedTaxRequest.lines().get(1).unitPrice()).isEqualByComparingTo("16.55");
    }

    @Test
    void applyRejectsHighThresholdDiscountWithoutOverridePermission() {
        StoreLocationEntity store = storeLocation(10L, 1L);
        ProductEntity product = product(101L, 1L, "SKU-A", "Product A");
        DiscountReasonCodeEntity reasonCode = reasonCode(1L, "PRICE_MATCH");

        when(storeLocationRepository.findById(10L)).thenReturn(Optional.of(store));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(discountReasonCodeRepository.findByMerchantIdAndCodeIgnoreCaseAndActiveTrue(1L, "PRICE_MATCH"))
                .thenReturn(Optional.of(reasonCode));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cashier", "n/a", "PERM_" + PermissionCodes.SALES_PROCESS));

        assertThatThrownBy(() -> discountService.apply(new DiscountApplyRequest(
                10L,
                "ctx-e1",
                DiscountScope.LINE,
                101L,
                DiscountType.PERCENTAGE,
                new BigDecimal("20.0000"),
                "price_match",
                "manual")))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_FORBIDDEN);
    }

    private TaxPreviewResponse taxPreviewStub(Long storeLocationId, Instant at) {
        return new TaxPreviewResponse(
                storeLocationId,
                at,
                List.of(
                        new TaxPreviewLineResponse(1, 101L, "SKU-A", "Product A",
                                new BigDecimal("1.000"), new BigDecimal("7.45"), "VAT18", TaxMode.EXCLUSIVE,
                                new BigDecimal("18.0000"), false, false,
                                new BigDecimal("7.45"), new BigDecimal("1.34"), new BigDecimal("8.79")),
                        new TaxPreviewLineResponse(2, 102L, "SKU-B", "Product B",
                                new BigDecimal("1.000"), new BigDecimal("16.55"), "VAT18", TaxMode.EXCLUSIVE,
                                new BigDecimal("18.0000"), false, false,
                                new BigDecimal("16.55"), new BigDecimal("2.98"), new BigDecimal("19.53"))),
                new BigDecimal("24.00"),
                new BigDecimal("4.32"),
                new BigDecimal("28.32"),
                new BigDecimal("0.00"),
                new BigDecimal("28.32"),
                new com.saulpos.api.tax.RoundingSummary(
                        false,
                        null,
                        null,
                        null,
                        new BigDecimal("28.32"),
                        new BigDecimal("28.32"),
                        new BigDecimal("0.00")));
    }

    private StoreLocationEntity storeLocation(Long id, Long merchantId) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);
        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setId(id);
        storeLocation.setMerchant(merchant);
        return storeLocation;
    }

    private ProductEntity product(Long id, Long merchantId, String sku, String name) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setMerchant(merchant);
        product.setSku(sku);
        product.setName(name);
        return product;
    }

    private DiscountReasonCodeEntity reasonCode(Long merchantId, String code) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);
        DiscountReasonCodeEntity reasonCode = new DiscountReasonCodeEntity();
        reasonCode.setId(700L);
        reasonCode.setMerchant(merchant);
        reasonCode.setCode(code);
        reasonCode.setDescription("Reason");
        reasonCode.setActive(true);
        return reasonCode;
    }

    private DiscountApplicationEntity lineDiscount(StoreLocationEntity store, ProductEntity product, String value) {
        DiscountApplicationEntity discount = new DiscountApplicationEntity();
        discount.setId(1L);
        discount.setStoreLocation(store);
        discount.setScope(DiscountScope.LINE);
        discount.setProduct(product);
        discount.setType(DiscountType.PERCENTAGE);
        discount.setValue(new BigDecimal(value));
        discount.setReasonCode(reasonCode(store.getMerchant().getId(), "PRICE_MATCH"));
        return discount;
    }

    private DiscountApplicationEntity cartDiscount(StoreLocationEntity store, String value) {
        DiscountApplicationEntity discount = new DiscountApplicationEntity();
        discount.setId(2L);
        discount.setStoreLocation(store);
        discount.setScope(DiscountScope.CART);
        discount.setType(DiscountType.FIXED);
        discount.setValue(new BigDecimal(value));
        discount.setReasonCode(reasonCode(store.getMerchant().getId(), "DAMAGED_ITEM"));
        return discount;
    }
}
