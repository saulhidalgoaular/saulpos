package com.saulpos.server.supplier.service;

import com.saulpos.api.supplier.SupplierContactRequest;
import com.saulpos.api.supplier.SupplierContactResponse;
import com.saulpos.api.supplier.SupplierContactType;
import com.saulpos.api.supplier.SupplierRequest;
import com.saulpos.api.supplier.SupplierResponse;
import com.saulpos.api.supplier.SupplierTermsRequest;
import com.saulpos.api.supplier.SupplierTermsResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.supplier.model.SupplierContactEntity;
import com.saulpos.server.supplier.model.SupplierEntity;
import com.saulpos.server.supplier.model.SupplierTermsEntity;
import com.saulpos.server.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        MerchantEntity merchant = requireMerchant(request.merchantId());

        SupplierEntity supplier = new SupplierEntity();
        supplier.setMerchant(merchant);
        applySupplierData(supplier, request, merchant, true);

        return toSupplierResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> listSuppliers(Long merchantId, Boolean active, String query) {
        if (merchantId != null) {
            requireMerchant(merchantId);
        }
        String normalizedQuery = normalizeSearchQuery(query);
        return supplierRepository.search(merchantId, active, normalizedQuery)
                .stream()
                .map(this::toSupplierResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(Long id) {
        return toSupplierResponse(requireSupplierWithDetails(id));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        SupplierEntity supplier = requireSupplierWithDetails(id);
        MerchantEntity merchant = requireMerchant(request.merchantId());

        supplier.setMerchant(merchant);
        applySupplierData(supplier, request, merchant, false);

        return toSupplierResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse setSupplierActive(Long id, boolean active) {
        SupplierEntity supplier = requireSupplierWithDetails(id);
        supplier.setActive(active);
        return toSupplierResponse(supplierRepository.save(supplier));
    }

    private void applySupplierData(SupplierEntity supplier,
                                   SupplierRequest request,
                                   MerchantEntity merchant,
                                   boolean create) {
        String code = normalizeCode(request.code());
        String name = normalizeRequiredText(request.name(), "name is required");
        String taxIdentifier = normalizeOptionalText(request.taxIdentifier());
        String taxIdentifierNormalized = normalizeTaxIdentifier(taxIdentifier);

        ensureSupplierCodeAvailable(merchant.getId(), code, supplier.getId());
        ensureSupplierTaxIdentifierAvailable(merchant.getId(), taxIdentifierNormalized, supplier.getId());

        supplier.setCode(code);
        supplier.setName(name);
        supplier.setTaxIdentifier(taxIdentifier);
        supplier.setTaxIdentifierNormalized(taxIdentifierNormalized);
        if (request.active() != null) {
            supplier.setActive(request.active());
        } else if (create) {
            supplier.setActive(true);
        }

        syncContacts(supplier, request.contacts());
        syncTerms(supplier, request.terms());
    }

    private void syncContacts(SupplierEntity supplier, List<SupplierContactRequest> requestedContacts) {
        List<SupplierContactRequest> requests = requestedContacts == null ? List.of() : requestedContacts;

        Map<String, SupplierContactEntity> existingByKey = new LinkedHashMap<>();
        for (SupplierContactEntity existing : supplier.getContacts()) {
            String key = toContactKey(existing.getContactType(), existing.getContactValueNormalized());
            existingByKey.put(key, existing);
        }

        Set<String> seenContactKeys = new LinkedHashSet<>();
        Set<SupplierContactType> primaryByType = new LinkedHashSet<>();
        List<SupplierContactEntity> synchronizedContacts = new ArrayList<>();
        for (SupplierContactRequest request : requests) {
            SupplierContactType contactType = requireContactType(request.contactType());
            String contactValue = normalizeRequiredText(request.contactValue(), "contactValue is required");
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

            SupplierContactEntity contact = existingByKey.get(key);
            if (contact == null) {
                contact = new SupplierContactEntity();
            }
            contact.setContactType(contactType);
            contact.setContactValue(contactValue);
            contact.setPrimary(primary);
            contact.setActive(true);
            synchronizedContacts.add(contact);
        }

        supplier.getContacts().clear();
        for (SupplierContactEntity contact : synchronizedContacts) {
            supplier.addContact(contact);
        }
    }

    private void syncTerms(SupplierEntity supplier, SupplierTermsRequest request) {
        if (request == null) {
            supplier.setTerms(null);
            return;
        }

        Integer paymentTermDays = request.paymentTermDays();
        if (paymentTermDays != null && paymentTermDays < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "paymentTermDays must be zero or greater");
        }

        BigDecimal creditLimit = request.creditLimit();
        if (creditLimit != null && creditLimit.signum() < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "creditLimit must be zero or greater");
        }

        SupplierTermsEntity terms = supplier.getTerms();
        if (terms == null) {
            terms = new SupplierTermsEntity();
        }
        terms.setPaymentTermDays(paymentTermDays);
        terms.setCreditLimit(creditLimit);
        terms.setNotes(normalizeOptionalText(request.notes()));
        supplier.setTerms(terms);
    }

    private void ensureSupplierCodeAvailable(Long merchantId, String code, Long currentSupplierId) {
        supplierRepository.findByMerchant_IdAndCode(merchantId, code)
                .ifPresent(existing -> {
                    if (currentSupplierId == null || !existing.getId().equals(currentSupplierId)) {
                        throw new BaseException(ErrorCode.CONFLICT,
                                "supplier code already exists for merchantId=%d code=%s"
                                        .formatted(merchantId, code));
                    }
                });
    }

    private void ensureSupplierTaxIdentifierAvailable(Long merchantId,
                                                      String taxIdentifierNormalized,
                                                      Long currentSupplierId) {
        if (taxIdentifierNormalized == null) {
            return;
        }

        supplierRepository.findByMerchant_IdAndTaxIdentifierNormalized(merchantId, taxIdentifierNormalized)
                .ifPresent(existing -> {
                    if (currentSupplierId == null || !existing.getId().equals(currentSupplierId)) {
                        throw new BaseException(ErrorCode.CONFLICT,
                                "supplier taxIdentifier already exists for merchantId=%d taxIdentifier=%s"
                                        .formatted(merchantId, taxIdentifierNormalized));
                    }
                });
    }

    private SupplierEntity requireSupplierWithDetails(Long id) {
        return supplierRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "supplier not found: " + id));
    }

    private MerchantEntity requireMerchant(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "merchant not found: " + id));
    }

    private SupplierContactType requireContactType(SupplierContactType contactType) {
        if (contactType == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "contactType is required");
        }
        return contactType;
    }

    private String normalizeCode(String code) {
        return normalizeRequiredText(code, "code is required").toUpperCase(Locale.ROOT);
    }

    private String normalizeTaxIdentifier(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeSearchQuery(String query) {
        if (query == null) {
            return null;
        }
        String normalized = query.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        return "%" + normalized + "%";
    }

    private String normalizeContactValueForLookup(SupplierContactType contactType, String contactValue) {
        if (contactType == SupplierContactType.EMAIL) {
            return contactValue.toLowerCase(Locale.ROOT);
        }
        return contactValue.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String toContactKey(SupplierContactType contactType, String normalizedValue) {
        return contactType.name() + "|" + normalizedValue;
    }

    private String normalizeRequiredText(String value, String message) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private SupplierResponse toSupplierResponse(SupplierEntity entity) {
        List<SupplierContactResponse> contacts = entity.getContacts().stream()
                .sorted(Comparator
                        .comparing((SupplierContactEntity contact) -> contact.getContactType().name())
                        .thenComparing(contact -> contact.getId() == null ? Long.MAX_VALUE : contact.getId()))
                .map(contact -> new SupplierContactResponse(
                        contact.getId(),
                        contact.getContactType(),
                        contact.getContactValue(),
                        contact.isPrimary(),
                        contact.isActive()))
                .toList();

        SupplierTermsResponse terms = null;
        if (entity.getTerms() != null) {
            terms = new SupplierTermsResponse(
                    entity.getTerms().getId(),
                    entity.getTerms().getPaymentTermDays(),
                    entity.getTerms().getCreditLimit(),
                    entity.getTerms().getNotes());
        }

        return new SupplierResponse(
                entity.getId(),
                entity.getMerchant().getId(),
                entity.getCode(),
                entity.getName(),
                entity.getTaxIdentifier(),
                entity.isActive(),
                contacts,
                terms);
    }
}
