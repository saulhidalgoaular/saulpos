package com.saulpos.server.catalog.service;

import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.PriceResolutionSource;
import com.saulpos.server.catalog.model.PriceBookItemEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.model.StorePriceOverrideEntity;
import com.saulpos.server.catalog.repository.PriceBookItemRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.repository.StorePriceOverrideRepository;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.model.CustomerGroupAssignmentEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingService {

    private static final Instant MINIMUM_EFFECTIVE_INSTANT = Instant.parse("1970-01-01T00:00:00Z");

    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StorePriceOverrideRepository storePriceOverrideRepository;
    private final PriceBookItemRepository priceBookItemRepository;

    @Transactional(readOnly = true)
    public PriceResolutionResponse resolvePrice(Long storeLocationId, Long productId, Instant at) {
        return resolvePrice(storeLocationId, productId, null, at);
    }

    @Transactional(readOnly = true)
    public PriceResolutionResponse resolvePrice(Long storeLocationId, Long productId, Long customerId, Instant at) {
        StoreLocationEntity storeLocation = requireStoreLocation(storeLocationId);
        ProductEntity product = requireProduct(productId);
        ensureSameMerchant(storeLocation, product);

        Instant resolvedAt = at != null ? at : Instant.now();

        Optional<StorePriceOverrideEntity> storeOverride = storePriceOverrideRepository
                .findApplicable(storeLocationId, productId, resolvedAt, MINIMUM_EFFECTIVE_INSTANT)
                .stream()
                .findFirst();
        if (storeOverride.isPresent()) {
            StorePriceOverrideEntity override = storeOverride.get();
            return new PriceResolutionResponse(
                    storeLocationId,
                    productId,
                    normalizeMoney(override.getPrice()),
                    PriceResolutionSource.STORE_OVERRIDE,
                    override.getId(),
                    override.getEffectiveFrom(),
                    override.getEffectiveTo(),
                    resolvedAt);
        }

        if (customerId != null) {
            CustomerEntity customer = requireCustomer(customerId);
            ensureSameMerchant(storeLocation, customer);

            List<Long> customerGroupIds = customer.getGroupAssignments().stream()
                    .filter(CustomerGroupAssignmentEntity::isActive)
                    .map(CustomerGroupAssignmentEntity::getCustomerGroup)
                    .filter(customerGroup -> customerGroup != null && customerGroup.isActive())
                    .map(customerGroup -> customerGroup.getId())
                    .distinct()
                    .toList();

            if (!customerGroupIds.isEmpty()) {
                Optional<PriceBookItemEntity> customerGroupPriceBookItem = priceBookItemRepository
                        .findApplicableForCustomerGroups(
                                storeLocation.getMerchant().getId(),
                                productId,
                                customerGroupIds,
                                resolvedAt,
                                MINIMUM_EFFECTIVE_INSTANT)
                        .stream()
                        .findFirst();
                if (customerGroupPriceBookItem.isPresent()) {
                    PriceBookItemEntity item = customerGroupPriceBookItem.get();
                    return new PriceResolutionResponse(
                            storeLocationId,
                            productId,
                            normalizeMoney(item.getPrice()),
                            PriceResolutionSource.CUSTOMER_GROUP_PRICE_BOOK,
                            item.getPriceBook().getId(),
                            item.getPriceBook().getEffectiveFrom(),
                            item.getPriceBook().getEffectiveTo(),
                            resolvedAt);
                }
            }
        }

        Optional<PriceBookItemEntity> priceBookItem = priceBookItemRepository
                .findApplicable(storeLocation.getMerchant().getId(), productId, resolvedAt, MINIMUM_EFFECTIVE_INSTANT)
                .stream()
                .findFirst();
        if (priceBookItem.isPresent()) {
            PriceBookItemEntity item = priceBookItem.get();
            return new PriceResolutionResponse(
                    storeLocationId,
                    productId,
                    normalizeMoney(item.getPrice()),
                    PriceResolutionSource.PRICE_BOOK,
                    item.getPriceBook().getId(),
                    item.getPriceBook().getEffectiveFrom(),
                    item.getPriceBook().getEffectiveTo(),
                    resolvedAt);
        }

        return new PriceResolutionResponse(
                storeLocationId,
                productId,
                normalizeMoney(product.getBasePrice()),
                PriceResolutionSource.BASE_PRICE,
                product.getId(),
                null,
                null,
                resolvedAt);
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private ProductEntity requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "product not found: " + productId));
    }

    private CustomerEntity requireCustomer(Long customerId) {
        return customerRepository.findByIdWithDetails(customerId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "customer not found: " + customerId));
    }

    private void ensureSameMerchant(StoreLocationEntity storeLocation, ProductEntity product) {
        if (!storeLocation.getMerchant().getId().equals(product.getMerchant().getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product does not belong to store merchant context");
        }
    }

    private void ensureSameMerchant(StoreLocationEntity storeLocation, CustomerEntity customer) {
        if (!storeLocation.getMerchant().getId().equals(customer.getMerchant().getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "customer does not belong to store merchant context");
        }
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
