package com.saulpos.server.customer.service;

import com.saulpos.api.customer.CustomerContactRequest;
import com.saulpos.api.customer.CustomerContactResponse;
import com.saulpos.api.customer.CustomerContactType;
import com.saulpos.api.customer.CustomerGroupAssignmentRequest;
import com.saulpos.api.customer.CustomerGroupRequest;
import com.saulpos.api.customer.CustomerGroupResponse;
import com.saulpos.api.customer.CustomerRequest;
import com.saulpos.api.customer.CustomerResponse;
import com.saulpos.api.customer.CustomerTaxIdentityRequest;
import com.saulpos.api.customer.CustomerTaxIdentityResponse;
import com.saulpos.server.customer.model.CustomerContactEntity;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.model.CustomerGroupAssignmentEntity;
import com.saulpos.server.customer.model.CustomerGroupEntity;
import com.saulpos.server.customer.model.CustomerTaxIdentityEntity;
import com.saulpos.server.customer.repository.CustomerGroupRepository;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.customer.repository.CustomerTaxIdentityRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final CustomerTaxIdentityRepository customerTaxIdentityRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        MerchantEntity merchant = requireMerchant(request.merchantId());

        CustomerEntity customer = new CustomerEntity();
        customer.setMerchant(merchant);
        applyCustomerData(customer, request, merchant);

        return toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers(Long merchantId, Boolean active) {
        if (merchantId != null) {
            requireMerchant(merchantId);
            return customerRepository.search(merchantId, active)
                    .stream()
                    .map(this::toCustomerResponse)
                    .toList();
        }

        if (active == null) {
            return customerRepository.findAll(Sort.by("id")).stream()
                    .map(this::toCustomerResponse)
                    .toList();
        }

        return customerRepository.search(null, active).stream()
                .map(this::toCustomerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        return toCustomerResponse(requireCustomerWithDetails(id));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        CustomerEntity customer = requireCustomerWithDetails(id);
        MerchantEntity merchant = requireMerchant(request.merchantId());

        customer.setMerchant(merchant);
        applyCustomerData(customer, request, merchant);

        return toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse setCustomerActive(Long id, boolean active) {
        CustomerEntity customer = requireCustomerWithDetails(id);
        customer.setActive(active);
        return toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> lookupCustomers(Long merchantId,
                                                  String documentType,
                                                  String documentValue,
                                                  String email,
                                                  String phone) {
        requireMerchant(merchantId);

        LookupCriterion criterion = resolveLookupCriterion(documentType, documentValue, email, phone);
        List<CustomerEntity> customers = switch (criterion.type()) {
            case DOCUMENT -> customerRepository.findActiveByDocument(
                    merchantId,
                    normalizeDocumentType(criterion.documentType()),
                    normalizeDocumentValueForLookup(criterion.documentValue()));
            case EMAIL -> customerRepository.findActiveByContact(
                    merchantId,
                    CustomerContactType.EMAIL,
                    normalizeContactValueForLookup(CustomerContactType.EMAIL, criterion.email()));
            case PHONE -> customerRepository.findActiveByContact(
                    merchantId,
                    CustomerContactType.PHONE,
                    normalizeContactValueForLookup(CustomerContactType.PHONE, criterion.phone()));
        };

        return customers.stream()
                .map(this::toCustomerResponse)
                .toList();
    }

    @Transactional
    public CustomerGroupResponse createCustomerGroup(CustomerGroupRequest request) {
        MerchantEntity merchant = requireMerchant(request.merchantId());
        String code = normalizeGroupCode(request.code());
        String name = normalizeRequiredText(request.name(), "name is required");

        if (customerGroupRepository.existsByMerchant_IdAndCode(merchant.getId(), code)) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "customer group already exists for merchantId=%d code=%s".formatted(merchant.getId(), code));
        }

        CustomerGroupEntity customerGroup = new CustomerGroupEntity();
        customerGroup.setMerchant(merchant);
        customerGroup.setCode(code);
        customerGroup.setName(name);
        customerGroup.setActive(request.active() == null || request.active());

        return toCustomerGroupResponse(customerGroupRepository.save(customerGroup));
    }

    @Transactional(readOnly = true)
    public List<CustomerGroupResponse> listCustomerGroups(Long merchantId, Boolean active) {
        requireMerchant(merchantId);
        return customerGroupRepository.search(merchantId, active).stream()
                .map(this::toCustomerGroupResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse assignCustomerGroups(Long customerId, CustomerGroupAssignmentRequest request) {
        CustomerEntity customer = requireCustomerWithDetails(customerId);
        List<Long> requestedGroupIds = sanitizeRequestedGroupIds(request.customerGroupIds());

        if (requestedGroupIds.isEmpty()) {
            customer.getGroupAssignments().clear();
            return toCustomerResponse(customerRepository.save(customer));
        }

        Map<Long, CustomerGroupEntity> groupsById = customerGroupRepository.findAllById(requestedGroupIds).stream()
                .collect(LinkedHashMap::new, (map, group) -> map.put(group.getId(), group), Map::putAll);
        if (groupsById.size() != requestedGroupIds.size()) {
            Long missingId = requestedGroupIds.stream()
                    .filter(id -> !groupsById.containsKey(id))
                    .findFirst()
                    .orElse(null);
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                    "customer group not found: " + missingId);
        }

        Map<Long, CustomerGroupAssignmentEntity> existingByGroupId = new LinkedHashMap<>();
        for (CustomerGroupAssignmentEntity assignment : customer.getGroupAssignments()) {
            existingByGroupId.put(assignment.getCustomerGroup().getId(), assignment);
        }

        customer.getGroupAssignments().clear();
        for (Long groupId : requestedGroupIds) {
            CustomerGroupEntity customerGroup = groupsById.get(groupId);
            if (!customer.getMerchant().getId().equals(customerGroup.getMerchant().getId())) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "customer group merchant mismatch for customerId=%d groupId=%d"
                                .formatted(customer.getId(), customerGroup.getId()));
            }

            CustomerGroupAssignmentEntity assignment = existingByGroupId.get(groupId);
            if (assignment == null) {
                assignment = new CustomerGroupAssignmentEntity();
            }
            assignment.setCustomerGroup(customerGroup);
            assignment.setActive(true);
            customer.addGroupAssignment(assignment);
        }

        return toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerGroupResponse> listCustomerGroupsForCustomer(Long customerId) {
        CustomerEntity customer = requireCustomerWithDetails(customerId);
        return toCustomerGroupResponses(customer);
    }

    private void applyCustomerData(CustomerEntity customer, CustomerRequest request, MerchantEntity merchant) {
        customer.setDisplayName(normalizeOptionalText(request.displayName()));
        customer.setInvoiceRequired(Boolean.TRUE.equals(request.invoiceRequired()));
        customer.setCreditEnabled(Boolean.TRUE.equals(request.creditEnabled()));

        syncTaxIdentities(customer, merchant, request.taxIdentities());
        syncContacts(customer, merchant, request.contacts());
    }

    private void syncTaxIdentities(CustomerEntity customer,
                                   MerchantEntity merchant,
                                   List<CustomerTaxIdentityRequest> requestedTaxIdentities) {
        List<CustomerTaxIdentityRequest> requests = requestedTaxIdentities == null ? List.of() : requestedTaxIdentities;

        Map<String, CustomerTaxIdentityEntity> existingByType = new LinkedHashMap<>();
        for (CustomerTaxIdentityEntity existing : customer.getTaxIdentities()) {
            existingByType.put(normalizeDocumentType(existing.getDocumentType()), existing);
        }

        Set<String> seenDocumentTypes = new LinkedHashSet<>();
        List<CustomerTaxIdentityEntity> synchronizedTaxIdentities = new ArrayList<>();
        for (CustomerTaxIdentityRequest request : requests) {
            String documentType = normalizeDocumentType(request.documentType());
            if (!seenDocumentTypes.add(documentType)) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate tax identity documentType in request: " + documentType);
            }

            String documentValue = normalizeDocumentValue(request.documentValue());
            String normalizedDocumentValue = normalizeDocumentValueForLookup(documentValue);
            ensureTaxIdentityAvailable(merchant.getId(), documentType, normalizedDocumentValue, customer.getId());

            CustomerTaxIdentityEntity identity = existingByType.remove(documentType);
            if (identity == null) {
                identity = new CustomerTaxIdentityEntity();
            }

            identity.setMerchant(merchant);
            identity.setDocumentType(documentType);
            identity.setDocumentValue(documentValue);
            identity.setActive(true);
            synchronizedTaxIdentities.add(identity);
        }

        customer.getTaxIdentities().clear();
        for (CustomerTaxIdentityEntity identity : synchronizedTaxIdentities) {
            customer.addTaxIdentity(identity);
        }
    }

    private void syncContacts(CustomerEntity customer,
                              MerchantEntity merchant,
                              List<CustomerContactRequest> requestedContacts) {
        List<CustomerContactRequest> requests = requestedContacts == null ? List.of() : requestedContacts;

        Map<String, CustomerContactEntity> existingByKey = new LinkedHashMap<>();
        for (CustomerContactEntity existing : customer.getContacts()) {
            String key = toContactKey(existing.getContactType(), existing.getContactValueNormalized());
            existingByKey.put(key, existing);
        }

        Set<String> seenContactKeys = new LinkedHashSet<>();
        Set<CustomerContactType> primaryByType = new LinkedHashSet<>();
        List<CustomerContactEntity> synchronizedContacts = new ArrayList<>();
        for (CustomerContactRequest request : requests) {
            CustomerContactType contactType = requireContactType(request.contactType());
            String contactValue = normalizeContactValue(request.contactValue());
            String normalizedValue = normalizeContactValueForLookup(contactType, contactValue);
            String key = toContactKey(contactType, normalizedValue);

            if (!seenContactKeys.add(key)) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate contact in request: " + contactType + "=" + normalizedValue);
            }

            boolean primary = Boolean.TRUE.equals(request.primary());
            if (primary && !primaryByType.add(contactType)) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "only one primary contact allowed per type: " + contactType);
            }

            CustomerContactEntity contact = existingByKey.remove(key);
            if (contact == null) {
                contact = new CustomerContactEntity();
            }

            contact.setMerchant(merchant);
            contact.setContactType(contactType);
            contact.setContactValue(contactValue);
            contact.setPrimary(primary);
            contact.setActive(true);
            synchronizedContacts.add(contact);
        }

        customer.getContacts().clear();
        for (CustomerContactEntity contact : synchronizedContacts) {
            customer.addContact(contact);
        }
    }

    private void ensureTaxIdentityAvailable(Long merchantId,
                                            String documentType,
                                            String documentValueNormalized,
                                            Long currentCustomerId) {
        customerTaxIdentityRepository
                .findByMerchantIdAndDocumentTypeAndDocumentValueNormalized(
                        merchantId,
                        documentType,
                        documentValueNormalized)
                .ifPresent(existing -> {
                    Long existingCustomerId = existing.getCustomer().getId();
                    if (currentCustomerId == null || !existingCustomerId.equals(currentCustomerId)) {
                        throw new BaseException(ErrorCode.CONFLICT,
                                "tax identity already exists for merchantId=%d documentType=%s documentValue=%s"
                                        .formatted(merchantId, documentType, documentValueNormalized));
                    }
                });
    }

    private LookupCriterion resolveLookupCriterion(String documentType,
                                                   String documentValue,
                                                   String email,
                                                   String phone) {
        boolean hasDocumentType = hasText(documentType);
        boolean hasDocumentValue = hasText(documentValue);
        boolean hasEmail = hasText(email);
        boolean hasPhone = hasText(phone);

        int criteriaCount = 0;
        if (hasDocumentType || hasDocumentValue) {
            if (!hasDocumentType || !hasDocumentValue) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "documentType and documentValue are both required for document lookup");
            }
            criteriaCount++;
        }
        if (hasEmail) {
            criteriaCount++;
        }
        if (hasPhone) {
            criteriaCount++;
        }

        if (criteriaCount != 1) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "exactly one lookup criterion is required: document, email, or phone");
        }

        if (hasDocumentType) {
            return LookupCriterion.document(documentType, documentValue);
        }
        if (hasEmail) {
            return LookupCriterion.email(email);
        }
        return LookupCriterion.phone(phone);
    }

    private CustomerEntity requireCustomerWithDetails(Long id) {
        return customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "customer not found: " + id));
    }

    private MerchantEntity requireMerchant(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "merchant not found: " + merchantId));
    }

    private CustomerContactType requireContactType(CustomerContactType contactType) {
        if (contactType == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "contactType is required");
        }
        return contactType;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeDocumentType(String documentType) {
        String normalized = normalizeRequiredText(documentType, "documentType is required");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeGroupCode(String code) {
        return normalizeRequiredText(code, "code is required").toUpperCase(Locale.ROOT);
    }

    private String normalizeDocumentValue(String documentValue) {
        return normalizeRequiredText(documentValue, "documentValue is required");
    }

    private String normalizeDocumentValueForLookup(String documentValue) {
        return normalizeRequiredText(documentValue, "documentValue is required")
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeContactValue(String contactValue) {
        return normalizeRequiredText(contactValue, "contactValue is required");
    }

    private String normalizeContactValueForLookup(CustomerContactType contactType, String contactValue) {
        String normalized = normalizeRequiredText(contactValue, "contactValue is required");
        if (contactType == CustomerContactType.EMAIL) {
            return normalized.toLowerCase(Locale.ROOT);
        }

        String normalizedPhone = normalized
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .toUpperCase(Locale.ROOT);
        if (normalizedPhone.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "contactValue is required");
        }
        return normalizedPhone;
    }

    private String normalizeRequiredText(String value, String message) {
        if (value == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, message);
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, message);
        }
        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String toContactKey(CustomerContactType contactType, String normalizedValue) {
        return contactType.name() + "|" + normalizedValue;
    }

    private List<Long> sanitizeRequestedGroupIds(List<Long> customerGroupIds) {
        if (customerGroupIds == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "customerGroupIds is required");
        }
        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (Long customerGroupId : customerGroupIds) {
            if (customerGroupId == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "customerGroupIds cannot contain null values");
            }
            uniqueIds.add(customerGroupId);
        }
        return new ArrayList<>(uniqueIds);
    }

    private List<CustomerGroupResponse> toCustomerGroupResponses(CustomerEntity customer) {
        return customer.getGroupAssignments().stream()
                .filter(CustomerGroupAssignmentEntity::isActive)
                .map(CustomerGroupAssignmentEntity::getCustomerGroup)
                .filter(Objects::nonNull)
                .filter(CustomerGroupEntity::isActive)
                .sorted(Comparator.comparing(CustomerGroupEntity::getCode)
                        .thenComparing(CustomerGroupEntity::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toCustomerGroupResponse)
                .toList();
    }

    private CustomerGroupResponse toCustomerGroupResponse(CustomerGroupEntity customerGroup) {
        return new CustomerGroupResponse(
                customerGroup.getId(),
                customerGroup.getMerchant().getId(),
                customerGroup.getCode(),
                customerGroup.getName(),
                customerGroup.isActive());
    }

    private CustomerResponse toCustomerResponse(CustomerEntity customer) {
        List<CustomerGroupResponse> groups = toCustomerGroupResponses(customer);

        List<CustomerTaxIdentityResponse> taxIdentities = customer.getTaxIdentities().stream()
                .sorted(Comparator.comparing(CustomerTaxIdentityEntity::getDocumentType)
                        .thenComparing(CustomerTaxIdentityEntity::getId, Comparator.nullsLast(Long::compareTo)))
                .map(taxIdentity -> new CustomerTaxIdentityResponse(
                        taxIdentity.getId(),
                        taxIdentity.getDocumentType(),
                        taxIdentity.getDocumentValue(),
                        taxIdentity.isActive()))
                .toList();

        List<CustomerContactResponse> contacts = customer.getContacts().stream()
                .sorted(Comparator.comparing(CustomerContactEntity::getContactType)
                        .thenComparing(CustomerContactEntity::isPrimary, Comparator.reverseOrder())
                        .thenComparing(CustomerContactEntity::getId, Comparator.nullsLast(Long::compareTo)))
                .map(contact -> new CustomerContactResponse(
                        contact.getId(),
                        contact.getContactType(),
                        contact.getContactValue(),
                        contact.isPrimary(),
                        contact.isActive()))
                .toList();

        return new CustomerResponse(
                customer.getId(),
                customer.getMerchant().getId(),
                customer.getDisplayName(),
                customer.isInvoiceRequired(),
                customer.isCreditEnabled(),
                customer.isActive(),
                groups,
                taxIdentities,
                contacts);
    }

    private enum LookupType {
        DOCUMENT,
        EMAIL,
        PHONE
    }

    private record LookupCriterion(LookupType type,
                                   String documentType,
                                   String documentValue,
                                   String email,
                                   String phone) {

        static LookupCriterion document(String documentType, String documentValue) {
            return new LookupCriterion(LookupType.DOCUMENT, documentType, documentValue, null, null);
        }

        static LookupCriterion email(String email) {
            return new LookupCriterion(LookupType.EMAIL, null, null, email, null);
        }

        static LookupCriterion phone(String phone) {
            return new LookupCriterion(LookupType.PHONE, null, null, null, phone);
        }
    }
}
