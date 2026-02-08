package com.saulpos.server.loyalty.service;

import com.saulpos.api.loyalty.LoyaltyEarnRequest;
import com.saulpos.api.loyalty.LoyaltyOperationResponse;
import com.saulpos.api.loyalty.LoyaltyOperationStatus;
import com.saulpos.api.loyalty.LoyaltyOperationType;
import com.saulpos.api.loyalty.LoyaltyRedeemRequest;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.loyalty.config.LoyaltyProperties;
import com.saulpos.server.loyalty.model.LoyaltyEventEntity;
import com.saulpos.server.loyalty.repository.LoyaltyEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final StoreLocationRepository storeLocationRepository;
    private final CustomerRepository customerRepository;
    private final LoyaltyEventRepository loyaltyEventRepository;
    private final LoyaltyProvider loyaltyProvider;
    private final LoyaltyProperties loyaltyProperties;

    @Transactional
    public LoyaltyOperationResponse earn(LoyaltyEarnRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        CustomerEntity customer = requireCustomer(request.customerId());
        validateContext(storeLocation, customer);
        String reference = normalizeReference(request.reference());

        LoyaltyProviderResult providerResult = evaluateEarn(storeLocation, customer, reference, request);

        LoyaltyEventEntity event = new LoyaltyEventEntity();
        event.setMerchant(storeLocation.getMerchant());
        event.setStoreLocation(storeLocation);
        event.setCustomer(customer);
        event.setOperationType(LoyaltyOperationType.EARN);
        event.setReference(reference);
        event.setRequestedPoints(null);
        event.setPointsDelta(normalizePointsDelta(providerResult.pointsDelta()));
        event.setStatus(providerResult.status());
        event.setProviderCode(loyaltyProvider.providerCode());
        event.setProviderReference(providerResult.providerReference());
        event.setMessage(normalizeMessage(providerResult.message()));
        return toResponse(loyaltyEventRepository.save(event));
    }

    @Transactional
    public LoyaltyOperationResponse redeem(LoyaltyRedeemRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        CustomerEntity customer = requireCustomer(request.customerId());
        validateContext(storeLocation, customer);
        String reference = normalizeReference(request.reference());

        LoyaltyProviderResult providerResult = evaluateRedeem(storeLocation, customer, reference, request);

        LoyaltyEventEntity event = new LoyaltyEventEntity();
        event.setMerchant(storeLocation.getMerchant());
        event.setStoreLocation(storeLocation);
        event.setCustomer(customer);
        event.setOperationType(LoyaltyOperationType.REDEEM);
        event.setReference(reference);
        event.setRequestedPoints(request.requestedPoints());
        event.setPointsDelta(normalizePointsDelta(providerResult.pointsDelta()));
        event.setStatus(providerResult.status());
        event.setProviderCode(loyaltyProvider.providerCode());
        event.setProviderReference(providerResult.providerReference());
        event.setMessage(normalizeMessage(providerResult.message()));
        return toResponse(loyaltyEventRepository.save(event));
    }

    private LoyaltyProviderResult evaluateEarn(StoreLocationEntity storeLocation,
                                               CustomerEntity customer,
                                               String reference,
                                               LoyaltyEarnRequest request) {
        if (!loyaltyProperties.isEnabled()) {
            return new LoyaltyProviderResult(
                    LoyaltyOperationStatus.DISABLED,
                    0,
                    null,
                    "loyalty integration is disabled");
        }
        try {
            return loyaltyProvider.earn(new LoyaltyProviderEarnCommand(
                    storeLocation.getMerchant().getId(),
                    storeLocation.getId(),
                    customer.getId(),
                    reference,
                    request.saleGrossAmount()));
        } catch (RuntimeException exception) {
            return new LoyaltyProviderResult(
                    LoyaltyOperationStatus.UNAVAILABLE,
                    0,
                    null,
                    "loyalty provider is unavailable");
        }
    }

    private LoyaltyProviderResult evaluateRedeem(StoreLocationEntity storeLocation,
                                                 CustomerEntity customer,
                                                 String reference,
                                                 LoyaltyRedeemRequest request) {
        if (!loyaltyProperties.isEnabled()) {
            return new LoyaltyProviderResult(
                    LoyaltyOperationStatus.DISABLED,
                    0,
                    null,
                    "loyalty integration is disabled");
        }
        try {
            return loyaltyProvider.redeem(new LoyaltyProviderRedeemCommand(
                    storeLocation.getMerchant().getId(),
                    storeLocation.getId(),
                    customer.getId(),
                    reference,
                    request.requestedPoints()));
        } catch (RuntimeException exception) {
            return new LoyaltyProviderResult(
                    LoyaltyOperationStatus.UNAVAILABLE,
                    0,
                    null,
                    "loyalty provider is unavailable");
        }
    }

    private void validateContext(StoreLocationEntity storeLocation, CustomerEntity customer) {
        if (!storeLocation.isActive()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "storeLocation is inactive: " + storeLocation.getId());
        }
        if (!customer.isActive()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "customer is inactive: " + customer.getId());
        }
        Long storeMerchantId = storeLocation.getMerchant().getId();
        Long customerMerchantId = customer.getMerchant().getId();
        if (!storeMerchantId.equals(customerMerchantId)) {
            throw new BaseException(
                    ErrorCode.CONFLICT,
                    "customer merchant does not match store merchant for loyalty operation");
        }
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private CustomerEntity requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "customer not found: " + customerId));
    }

    private String normalizeReference(String reference) {
        if (reference == null) {
            return null;
        }
        String normalized = reference.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return null;
        }
        String normalized = message.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private int normalizePointsDelta(Integer pointsDelta) {
        return pointsDelta == null ? 0 : pointsDelta;
    }

    private LoyaltyOperationResponse toResponse(LoyaltyEventEntity event) {
        return new LoyaltyOperationResponse(
                event.getId(),
                event.getOperationType(),
                event.getStatus(),
                event.getStoreLocation().getId(),
                event.getCustomer().getId(),
                event.getReference(),
                event.getPointsDelta(),
                event.getProviderCode(),
                event.getProviderReference(),
                event.getMessage(),
                event.getProcessedAt());
    }
}
