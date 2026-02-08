package com.saulpos.server.catalog.service;

import com.saulpos.api.catalog.OpenPriceEntryValidationRequest;
import com.saulpos.api.catalog.OpenPriceEntryValidationResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductRequest;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.catalog.ProductVariantRequest;
import com.saulpos.api.catalog.ProductVariantResponse;
import com.saulpos.server.catalog.model.OpenPriceEntryAuditEntity;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductBarcodeEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.model.ProductVariantEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.OpenPriceEntryAuditRepository;
import com.saulpos.server.catalog.repository.ProductBarcodeRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductBarcodeRepository productBarcodeRepository;
    private final MerchantRepository merchantRepository;
    private final ProductSaleModePolicyValidator saleModePolicyValidator;
    private final OpenPriceEntryAuditRepository openPriceEntryAuditRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        MerchantEntity merchant = requireMerchant(request.merchantId());
        String sku = normalizeCode(request.sku());
        ensureSkuAvailable(merchant.getId(), sku, null);

        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        applyProductData(product, request, merchant, sku, null);
        syncVariants(product, request.variants());

        return toProductResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(Long merchantId, Boolean active, String query) {
        String normalizedQueryPattern = toContainsPattern(normalizeSearchQuery(query));
        return productRepository.search(merchantId, active, normalizedQueryPattern)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductSearchResponse searchProducts(Long merchantId, Boolean active, String query, int page, int size) {
        requireMerchant(merchantId);
        String normalizedQueryPattern = toContainsPattern(normalizeSearchQuery(query));

        Page<Long> resultPage = productRepository.searchIds(
                merchantId,
                active,
                normalizedQueryPattern,
                PageRequest.of(page, size));

        List<ProductResponse> items = toOrderedProductResponses(resultPage.getContent());
        return new ProductSearchResponse(
                items,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.hasNext(),
                resultPage.hasPrevious());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toProductResponse(requireProductWithDetails(id));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        ProductEntity product = requireProductWithDetails(id);
        MerchantEntity merchant = requireMerchant(request.merchantId());
        String sku = normalizeCode(request.sku());
        ensureSkuAvailable(merchant.getId(), sku, id);

        product.setMerchant(merchant);
        Long existingCategoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        applyProductData(product, request, merchant, sku, existingCategoryId);

        product.getVariants().clear();
        productRepository.saveAndFlush(product);

        syncVariants(product, request.variants());
        return toProductResponse(productRepository.save(product));
    }

    @Transactional
    public OpenPriceEntryValidationResponse validateOpenPriceEntry(Long productId, OpenPriceEntryValidationRequest request) {
        ProductEntity product = requireProductWithDetails(productId);
        if (product.getSaleMode() != ProductSaleMode.OPEN_PRICE) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "product is not configured for OPEN_PRICE mode");
        }

        BigDecimal enteredPrice = saleModePolicyValidator.validateOpenPriceEntry(
                request.enteredPrice(),
                product.getOpenPriceMin(),
                product.getOpenPriceMax(),
                product.isOpenPriceRequiresReason(),
                request.reason());

        OpenPriceEntryAuditEntity auditEntity = new OpenPriceEntryAuditEntity();
        auditEntity.setProduct(product);
        auditEntity.setActorUsername(resolveActorUsername());
        auditEntity.setEnteredPrice(enteredPrice);
        auditEntity.setReason(normalizeDescription(request.reason()));
        auditEntity.setCorrelationId(MDC.get("correlationId"));
        openPriceEntryAuditRepository.save(auditEntity);

        return new OpenPriceEntryValidationResponse(
                product.getId(),
                product.getSku(),
                enteredPrice,
                product.getOpenPriceMin(),
                product.getOpenPriceMax(),
                product.isOpenPriceRequiresReason());
    }

    @Transactional
    public ProductResponse setProductActive(Long id, boolean active) {
        ProductEntity product = requireProductWithDetails(id);
        product.setActive(active);
        return toProductResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductLookupResponse lookupByBarcode(Long merchantId, String barcode) {
        requireMerchant(merchantId);
        String normalizedBarcode = normalizeBarcode(barcode);

        ProductBarcodeEntity barcodeEntity = productBarcodeRepository
                .findActiveSellableByMerchantAndBarcode(merchantId, normalizedBarcode)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "product barcode not found for merchantId=%d barcode=%s".formatted(merchantId, normalizedBarcode)));

        ProductVariantEntity variant = barcodeEntity.getVariant();
        ProductEntity product = variant.getProduct();

        return new ProductLookupResponse(
                product.getId(),
                variant.getId(),
                product.getMerchant().getId(),
                product.getSku(),
                product.getName(),
                variant.getCode(),
                variant.getName(),
                barcodeEntity.getBarcode(),
                product.getSaleMode(),
                product.getQuantityUom(),
                product.getQuantityPrecision());
    }

    private void applyProductData(ProductEntity product,
                                  ProductRequest request,
                                  MerchantEntity merchant,
                                  String sku,
                                  Long existingCategoryId) {
        ProductSaleModePolicyValidator.NormalizedPolicy normalizedPolicy = saleModePolicyValidator.normalizePolicy(
                request.saleMode(),
                request.quantityUom(),
                request.quantityPrecision(),
                request.openPriceMin(),
                request.openPriceMax(),
                request.openPriceRequiresReason());

        product.setMerchant(merchant);
        product.setCategory(resolveCategory(request.categoryId(), merchant.getId(), existingCategoryId));
        product.setSku(sku);
        product.setName(normalizeName(request.name()));
        product.setBasePrice(normalizeMoney(request.basePrice()));
        product.setSaleMode(normalizedPolicy.saleMode());
        product.setQuantityUom(normalizedPolicy.quantityUom());
        product.setQuantityPrecision(normalizedPolicy.quantityPrecision());
        product.setOpenPriceMin(normalizedPolicy.openPriceMin());
        product.setOpenPriceMax(normalizedPolicy.openPriceMax());
        product.setOpenPriceRequiresReason(normalizedPolicy.openPriceRequiresReason());
        product.setDescription(normalizeDescription(request.description()));
    }

    private void syncVariants(ProductEntity product, List<ProductVariantRequest> variants) {
        ensureVariantCodesUnique(variants);
        ensureBarcodesUnique(variants);

        for (ProductVariantRequest variantRequest : variants) {
            ProductVariantEntity variant = new ProductVariantEntity();
            variant.setCode(normalizeCode(variantRequest.code()));
            variant.setName(normalizeName(variantRequest.name()));

            List<String> sortedBarcodes = variantRequest.barcodes().stream()
                    .map(this::normalizeBarcode)
                    .distinct()
                    .sorted()
                    .toList();

            for (String barcodeValue : sortedBarcodes) {
                ProductBarcodeEntity barcode = new ProductBarcodeEntity();
                barcode.setBarcode(barcodeValue);
                variant.addBarcode(barcode);
            }

            product.addVariant(variant);
        }
    }

    private void ensureSkuAvailable(Long merchantId, String sku, Long currentProductId) {
        productRepository.findByMerchantIdAndSkuIgnoreCase(merchantId, sku)
                .ifPresent(existing -> {
                    if (currentProductId == null || !existing.getId().equals(currentProductId)) {
                        throw new BaseException(ErrorCode.CONFLICT,
                                "product sku already exists for merchantId=%d sku=%s".formatted(merchantId, sku));
                    }
                });
    }

    private void ensureVariantCodesUnique(List<ProductVariantRequest> variants) {
        Set<String> seenCodes = new LinkedHashSet<>();
        for (ProductVariantRequest variant : variants) {
            String normalizedCode = normalizeCode(variant.code());
            if (!seenCodes.add(normalizedCode)) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate variant code in request: " + normalizedCode);
            }
        }
    }

    private void ensureBarcodesUnique(List<ProductVariantRequest> variants) {
        Set<String> seenBarcodes = new LinkedHashSet<>();
        for (ProductVariantRequest variant : variants) {
            for (String barcode : variant.barcodes()) {
                String normalizedBarcode = normalizeBarcode(barcode);
                if (!seenBarcodes.add(normalizedBarcode)) {
                    throw new BaseException(ErrorCode.VALIDATION_ERROR,
                            "duplicate barcode in request: " + normalizedBarcode);
                }
            }
        }
    }

    private CategoryEntity resolveCategory(Long categoryId, Long merchantId, Long existingCategoryId) {
        if (categoryId == null) {
            return null;
        }
        CategoryEntity category = categoryRepository.findByIdAndMerchantId(categoryId, merchantId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "category not found for merchantId=%d categoryId=%d".formatted(merchantId, categoryId)));

        if (!category.isActive() && (existingCategoryId == null || !existingCategoryId.equals(categoryId))) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "inactive category cannot receive new product assignments: " + categoryId);
        }
        return category;
    }

    private ProductEntity requireProductWithDetails(Long id) {
        return productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "product not found: " + id));
    }

    private MerchantEntity requireMerchant(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "merchant not found: " + id));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return ZERO;
        }
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(ZERO) < 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "basePrice must be non-negative");
        }
        return normalized;
    }

    private String normalizeBarcode(String barcode) {
        String normalized = barcode.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "barcode is required");
        }
        return normalized;
    }

    private String normalizeSearchQuery(String query) {
        if (query == null) {
            return null;
        }
        String normalized = query.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String toContainsPattern(String query) {
        if (query == null) {
            return null;
        }
        return "%" + query.toUpperCase(Locale.ROOT) + "%";
    }

    private List<ProductResponse> toOrderedProductResponses(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }

        Map<Long, ProductEntity> productsById = productRepository.findAllByIdWithDetails(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity(), (left, right) -> left));

        return productIds.stream()
                .map(productsById::get)
                .filter(Objects::nonNull)
                .map(this::toProductResponse)
                .toList();
    }

    private ProductResponse toProductResponse(ProductEntity product) {
        Map<Long, VariantAccumulator> variantsById = new LinkedHashMap<>();
        product.getVariants().stream()
                .sorted(Comparator.comparing(ProductVariantEntity::getId, Comparator.nullsLast(Long::compareTo)))
                .forEach(variant -> {
                    Long variantId = variant.getId();
                    VariantAccumulator accumulator = variantsById.computeIfAbsent(
                            variantId,
                            ignored -> new VariantAccumulator(variant));
                    variant.getBarcodes().stream()
                            .map(ProductBarcodeEntity::getBarcode)
                            .forEach(accumulator.barcodes::add);
                });

        List<ProductVariantResponse> variants = variantsById.values().stream()
                .map(VariantAccumulator::toResponse)
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getMerchant().getId(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getSku(),
                product.getName(),
                product.getBasePrice(),
                product.getSaleMode(),
                product.getQuantityUom(),
                product.getQuantityPrecision(),
                product.getOpenPriceMin(),
                product.getOpenPriceMax(),
                product.isOpenPriceRequiresReason(),
                product.getDescription(),
                product.isActive(),
                new ArrayList<>(variants));
    }

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "unknown";
        }
        return authentication.getName();
    }

    private static final class VariantAccumulator {
        private final Long id;
        private final String code;
        private final String name;
        private final boolean active;
        private final Set<String> barcodes = new LinkedHashSet<>();

        private VariantAccumulator(ProductVariantEntity variant) {
            this.id = variant.getId();
            this.code = variant.getCode();
            this.name = variant.getName();
            this.active = variant.isActive();
        }

        private ProductVariantResponse toResponse() {
            Set<String> sortedBarcodes = barcodes.stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return new ProductVariantResponse(id, code, name, active, sortedBarcodes);
        }
    }
}
