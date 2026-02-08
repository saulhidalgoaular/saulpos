package com.saulpos.server.catalog;

import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.PriceResolutionSource;
import com.saulpos.server.catalog.model.PriceBookEntity;
import com.saulpos.server.catalog.model.PriceBookItemEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.model.StorePriceOverrideEntity;
import com.saulpos.server.catalog.repository.PriceBookItemRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.repository.StorePriceOverrideRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.model.CustomerGroupAssignmentEntity;
import com.saulpos.server.customer.model.CustomerGroupEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private StoreLocationRepository storeLocationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private StorePriceOverrideRepository storePriceOverrideRepository;

    @Mock
    private PriceBookItemRepository priceBookItemRepository;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(
                storeLocationRepository,
                productRepository,
                customerRepository,
                storePriceOverrideRepository,
                priceBookItemRepository);
    }

    @Test
    void resolvePriceUsesStoreOverrideBeforePriceBookAndBasePrice() {
        StoreLocationEntity storeLocation = storeLocation(10L, 1L);
        ProductEntity product = product(50L, 1L, "10.00");

        StorePriceOverrideEntity override = new StorePriceOverrideEntity();
        override.setId(700L);
        override.setPrice(new BigDecimal("7.49"));

        when(storeLocationRepository.findById(10L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(storePriceOverrideRepository.findApplicable(org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(50L),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of(override));

        PriceResolutionResponse response = pricingService.resolvePrice(10L, 50L, Instant.parse("2026-02-01T12:00:00Z"));

        assertThat(response.source()).isEqualTo(PriceResolutionSource.STORE_OVERRIDE);
        assertThat(response.resolvedPrice()).isEqualByComparingTo("7.49");
        assertThat(response.sourceId()).isEqualTo(700L);
    }

    @Test
    void resolvePriceUsesPriceBookWhenNoStoreOverride() {
        StoreLocationEntity storeLocation = storeLocation(11L, 2L);
        ProductEntity product = product(51L, 2L, "9.99");

        PriceBookEntity priceBook = new PriceBookEntity();
        priceBook.setId(801L);
        priceBook.setEffectiveFrom(Instant.parse("2026-01-01T00:00:00Z"));
        priceBook.setEffectiveTo(Instant.parse("2026-12-31T23:59:59Z"));

        PriceBookItemEntity item = new PriceBookItemEntity();
        item.setPriceBook(priceBook);
        item.setPrice(new BigDecimal("8.75"));

        when(storeLocationRepository.findById(11L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(51L)).thenReturn(Optional.of(product));
        when(storePriceOverrideRepository.findApplicable(org.mockito.ArgumentMatchers.eq(11L),
                org.mockito.ArgumentMatchers.eq(51L),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of());
        when(priceBookItemRepository.findApplicable(org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq(51L),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of(item));

        PriceResolutionResponse response = pricingService.resolvePrice(11L, 51L, Instant.parse("2026-04-15T12:00:00Z"));

        assertThat(response.source()).isEqualTo(PriceResolutionSource.PRICE_BOOK);
        assertThat(response.resolvedPrice()).isEqualByComparingTo("8.75");
        assertThat(response.sourceId()).isEqualTo(801L);
    }

    @Test
    void resolvePriceFallsBackToBasePriceWhenNoOverrideOrPriceBook() {
        StoreLocationEntity storeLocation = storeLocation(12L, 3L);
        ProductEntity product = product(52L, 3L, "6.30");

        when(storeLocationRepository.findById(12L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(52L)).thenReturn(Optional.of(product));
        when(storePriceOverrideRepository.findApplicable(org.mockito.ArgumentMatchers.eq(12L),
                org.mockito.ArgumentMatchers.eq(52L),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of());
        when(priceBookItemRepository.findApplicable(org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq(52L),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of());

        PriceResolutionResponse response = pricingService.resolvePrice(12L, 52L, Instant.parse("2026-03-01T12:00:00Z"));

        assertThat(response.source()).isEqualTo(PriceResolutionSource.BASE_PRICE);
        assertThat(response.resolvedPrice()).isEqualByComparingTo("6.30");
        assertThat(response.sourceId()).isEqualTo(52L);
    }

    @Test
    void resolvePriceRejectsStoreAndProductFromDifferentMerchants() {
        StoreLocationEntity storeLocation = storeLocation(13L, 4L);
        ProductEntity product = product(53L, 5L, "5.00");

        when(storeLocationRepository.findById(13L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(53L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> pricingService.resolvePrice(13L, 53L, Instant.parse("2026-03-02T10:00:00Z")))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void resolvePriceUsesCustomerGroupPriceBookBeforeGenericPriceBook() {
        StoreLocationEntity storeLocation = storeLocation(14L, 6L);
        ProductEntity product = product(54L, 6L, "10.00");
        CustomerEntity customer = customer(90L, 6L, 700L);

        PriceBookEntity groupPriceBook = new PriceBookEntity();
        groupPriceBook.setId(901L);
        PriceBookItemEntity groupItem = new PriceBookItemEntity();
        groupItem.setPriceBook(groupPriceBook);
        groupItem.setPrice(new BigDecimal("7.25"));

        when(storeLocationRepository.findById(14L)).thenReturn(Optional.of(storeLocation));
        when(productRepository.findById(54L)).thenReturn(Optional.of(product));
        when(customerRepository.findByIdWithDetails(90L)).thenReturn(Optional.of(customer));
        when(storePriceOverrideRepository.findApplicable(org.mockito.ArgumentMatchers.eq(14L),
                org.mockito.ArgumentMatchers.eq(54L),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of());
        when(priceBookItemRepository.findApplicableForCustomerGroups(
                org.mockito.ArgumentMatchers.eq(6L),
                org.mockito.ArgumentMatchers.eq(54L),
                org.mockito.ArgumentMatchers.eq(List.of(700L)),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of(groupItem));

        PriceResolutionResponse response = pricingService.resolvePrice(
                14L,
                54L,
                90L,
                Instant.parse("2026-04-15T12:00:00Z"));

        assertThat(response.source()).isEqualTo(PriceResolutionSource.CUSTOMER_GROUP_PRICE_BOOK);
        assertThat(response.resolvedPrice()).isEqualByComparingTo("7.25");
        assertThat(response.sourceId()).isEqualTo(901L);
    }

    private StoreLocationEntity storeLocation(Long storeLocationId, Long merchantId) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setId(storeLocationId);
        storeLocation.setMerchant(merchant);
        return storeLocation;
    }

    private ProductEntity product(Long productId, Long merchantId, String basePrice) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setMerchant(merchant);
        product.setBasePrice(new BigDecimal(basePrice));
        return product;
    }

    private CustomerEntity customer(Long customerId, Long merchantId, Long customerGroupId) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);

        CustomerGroupEntity customerGroup = new CustomerGroupEntity();
        customerGroup.setId(customerGroupId);
        customerGroup.setMerchant(merchant);
        customerGroup.setCode("WHOLESALE");
        customerGroup.setName("Wholesale");
        customerGroup.setActive(true);

        CustomerGroupAssignmentEntity assignment = new CustomerGroupAssignmentEntity();
        assignment.setCustomerGroup(customerGroup);
        assignment.setActive(true);

        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);
        customer.setMerchant(merchant);
        customer.addGroupAssignment(assignment);
        return customer;
    }
}
