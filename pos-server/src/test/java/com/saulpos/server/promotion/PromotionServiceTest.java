package com.saulpos.server.promotion;

import com.saulpos.api.promotion.PromotionEvaluateLineRequest;
import com.saulpos.api.promotion.PromotionEvaluateRequest;
import com.saulpos.api.promotion.PromotionEvaluateResponse;
import com.saulpos.api.promotion.PromotionRuleType;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.promotion.model.PromotionEntity;
import com.saulpos.server.promotion.model.PromotionRuleEntity;
import com.saulpos.server.promotion.model.PromotionWindowEntity;
import com.saulpos.server.promotion.repository.PromotionRepository;
import com.saulpos.server.promotion.service.PromotionService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private StoreLocationRepository storeLocationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private PromotionRepository promotionRepository;

    private PromotionService promotionService;

    @BeforeEach
    void setUp() {
        promotionService = new PromotionService(
                storeLocationRepository,
                productRepository,
                pricingService,
                promotionRepository);
    }

    @Test
    void evaluateAppliesProductPercentageRule() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity store = storeLocation(10L, 1L);
        ProductEntity product = product(101L, 1L, "SKU-A", "Product A");

        PromotionEntity promotion = promotion(1L, 1L, "PROMO10", "Product 10", 5);
        promotion.addRule(productPercentageRule(product, "10.0000", "1.000"));
        promotion.addWindow(activeWindow("2026-02-01T00:00:00Z", "2026-02-28T23:59:59Z"));

        when(storeLocationRepository.findById(10L)).thenReturn(Optional.of(store));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(promotionRepository.findActiveForMerchant(1L)).thenReturn(List.of(promotion));

        PromotionEvaluateResponse response = promotionService.evaluate(new PromotionEvaluateRequest(
                10L,
                at,
                List.of(new PromotionEvaluateLineRequest(101L, new BigDecimal("2.000"), new BigDecimal("10.00")))));

        assertThat(response.totalDiscount()).isEqualByComparingTo("2.00");
        assertThat(response.subtotalBeforeDiscount()).isEqualByComparingTo("20.00");
        assertThat(response.subtotalAfterDiscount()).isEqualByComparingTo("18.00");
        assertThat(response.appliedPromotion()).isNotNull();
        assertThat(response.appliedPromotion().code()).isEqualTo("PROMO10");
        assertThat(response.appliedPromotion().explanations()).hasSize(1);
        assertThat(response.lines().getFirst().discountedUnitPrice()).isEqualByComparingTo("9.00");
    }

    @Test
    void evaluateAppliesCartFixedRule() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity store = storeLocation(10L, 1L);
        ProductEntity product = product(101L, 1L, "SKU-A", "Product A");

        PromotionEntity promotion = promotion(2L, 1L, "CART5", "Cart 5", 5);
        promotion.addRule(cartFixedRule("5.0000", "10.00"));
        promotion.addWindow(activeWindow("2026-02-01T00:00:00Z", "2026-02-28T23:59:59Z"));

        when(storeLocationRepository.findById(10L)).thenReturn(Optional.of(store));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(promotionRepository.findActiveForMerchant(1L)).thenReturn(List.of(promotion));

        PromotionEvaluateResponse response = promotionService.evaluate(new PromotionEvaluateRequest(
                10L,
                at,
                List.of(new PromotionEvaluateLineRequest(101L, new BigDecimal("1.000"), new BigDecimal("12.00")))));

        assertThat(response.totalDiscount()).isEqualByComparingTo("5.00");
        assertThat(response.subtotalAfterDiscount()).isEqualByComparingTo("7.00");
        assertThat(response.appliedPromotion()).isNotNull();
        assertThat(response.appliedPromotion().code()).isEqualTo("CART5");
    }

    @Test
    void evaluateSelectsDeterministicWinnerForOverlappingPromotions() {
        Instant at = Instant.parse("2026-02-10T12:00:00Z");
        StoreLocationEntity store = storeLocation(10L, 1L);
        ProductEntity product = product(101L, 1L, "SKU-A", "Product A");

        PromotionEntity lowerDiscount = promotion(10L, 1L, "LOW", "Low", 7);
        lowerDiscount.addRule(cartFixedRule("1.0000", "0.00"));
        lowerDiscount.addWindow(activeWindow("2026-02-01T00:00:00Z", "2026-02-28T23:59:59Z"));

        PromotionEntity higherDiscount = promotion(20L, 1L, "HIGH", "High", 7);
        higherDiscount.addRule(cartFixedRule("2.0000", "0.00"));
        higherDiscount.addWindow(activeWindow("2026-02-01T00:00:00Z", "2026-02-28T23:59:59Z"));

        when(storeLocationRepository.findById(10L)).thenReturn(Optional.of(store));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(promotionRepository.findActiveForMerchant(1L)).thenReturn(List.of(lowerDiscount, higherDiscount));

        PromotionEvaluateResponse response = promotionService.evaluate(new PromotionEvaluateRequest(
                10L,
                at,
                List.of(new PromotionEvaluateLineRequest(101L, new BigDecimal("1.000"), new BigDecimal("10.00")))));

        assertThat(response.appliedPromotion()).isNotNull();
        assertThat(response.appliedPromotion().code()).isEqualTo("HIGH");
        assertThat(response.totalDiscount()).isEqualByComparingTo("2.00");
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

    private PromotionEntity promotion(Long id, Long merchantId, String code, String name, int priority) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);

        PromotionEntity promotion = new PromotionEntity();
        promotion.setId(id);
        promotion.setMerchant(merchant);
        promotion.setCode(code);
        promotion.setName(name);
        promotion.setPriority(priority);
        promotion.setActive(true);
        return promotion;
    }

    private PromotionRuleEntity productPercentageRule(ProductEntity targetProduct, String discountValue, String minQuantity) {
        PromotionRuleEntity rule = new PromotionRuleEntity();
        rule.setId(1L);
        rule.setRuleType(PromotionRuleType.PRODUCT_PERCENTAGE);
        rule.setTargetProduct(targetProduct);
        rule.setDiscountValue(new BigDecimal(discountValue));
        rule.setMinQuantity(new BigDecimal(minQuantity));
        rule.setActive(true);
        return rule;
    }

    private PromotionRuleEntity cartFixedRule(String discountValue, String minSubtotal) {
        PromotionRuleEntity rule = new PromotionRuleEntity();
        rule.setId(2L);
        rule.setRuleType(PromotionRuleType.CART_FIXED);
        rule.setDiscountValue(new BigDecimal(discountValue));
        rule.setMinSubtotal(new BigDecimal(minSubtotal));
        rule.setActive(true);
        return rule;
    }

    private PromotionWindowEntity activeWindow(String startsAt, String endsAt) {
        PromotionWindowEntity window = new PromotionWindowEntity();
        window.setStartsAt(Instant.parse(startsAt));
        window.setEndsAt(Instant.parse(endsAt));
        window.setActive(true);
        return window;
    }
}
