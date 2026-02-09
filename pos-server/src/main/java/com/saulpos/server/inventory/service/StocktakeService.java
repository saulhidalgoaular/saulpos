package com.saulpos.server.inventory.service;

import com.saulpos.api.inventory.StocktakeCategoryVarianceResponse;
import com.saulpos.api.inventory.StocktakeCreateRequest;
import com.saulpos.api.inventory.StocktakeFinalizeLineRequest;
import com.saulpos.api.inventory.StocktakeFinalizeRequest;
import com.saulpos.api.inventory.StocktakeLineResponse;
import com.saulpos.api.inventory.StocktakeSessionResponse;
import com.saulpos.api.inventory.StocktakeVarianceReportResponse;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.inventory.model.StocktakeLineEntity;
import com.saulpos.server.inventory.model.StocktakeSessionEntity;
import com.saulpos.server.inventory.model.StocktakeStatus;
import com.saulpos.server.inventory.repository.StocktakeSessionRepository;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.InventoryMovementType;
import com.saulpos.server.sale.model.InventoryReferenceType;
import com.saulpos.server.sale.repository.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StocktakeService {

    private final StocktakeSessionRepository stocktakeSessionRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryBalanceCalculator balanceCalculator;
    private final Clock clock;

    @Transactional
    public StocktakeSessionResponse createStocktake(StocktakeCreateRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        List<ProductEntity> products = requireProducts(request.productIds());
        products.forEach(product -> validateStoreProductMerchantConsistency(storeLocation, product));

        Instant now = Instant.now(clock);
        StocktakeSessionEntity session = new StocktakeSessionEntity();
        session.setStoreLocation(storeLocation);
        session.setReferenceNumber(generateReferenceNumber());
        session.setStatus(StocktakeStatus.DRAFT);
        session.setCreatedBy(resolveActorUsername());
        session.setCreatedAt(now);
        session.setNote(normalizeOptionalNote(request.note()));

        products.stream()
                .sorted(Comparator.comparing(ProductEntity::getId))
                .forEach(product -> {
                    StocktakeLineEntity line = new StocktakeLineEntity();
                    line.setProduct(product);
                    line.setExpectedQuantity(balanceCalculator.normalizeScale(BigDecimal.ZERO));
                    session.addLine(line);
                });

        return toSessionResponse(stocktakeSessionRepository.save(session));
    }

    @Transactional
    public StocktakeSessionResponse startStocktake(Long stocktakeId) {
        StocktakeSessionEntity session = requireStocktakeForUpdate(stocktakeId);

        if (session.getStatus() != StocktakeStatus.DRAFT) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stocktake can only be started from DRAFT status: " + stocktakeId);
        }

        Map<Long, BigDecimal> currentBalances = inventoryMovementRepository
                .sumByStoreLocationAndProduct(session.getStoreLocation().getId(), null)
                .stream()
                .collect(Collectors.toMap(
                        InventoryMovementRepository.ProductBalanceProjection::getProductId,
                        projection -> balanceCalculator.normalizeScale(projection.getQuantityOnHand())));

        Instant now = Instant.now(clock);
        session.setStatus(StocktakeStatus.STARTED);
        session.setSnapshotAt(now);
        session.setStartedBy(resolveActorUsername());
        session.setStartedAt(now);

        for (StocktakeLineEntity line : session.getLines()) {
            BigDecimal expected = currentBalances.getOrDefault(line.getProduct().getId(), BigDecimal.ZERO);
            line.setExpectedQuantity(balanceCalculator.normalizeScale(expected));
            line.setCountedQuantity(null);
            line.setVarianceQuantity(null);
            line.setInventoryMovement(null);
        }

        return toSessionResponse(stocktakeSessionRepository.save(session));
    }

    @Transactional
    public StocktakeSessionResponse finalizeStocktake(Long stocktakeId, StocktakeFinalizeRequest request) {
        StocktakeSessionEntity session = requireStocktakeForUpdate(stocktakeId);

        if (session.getStatus() != StocktakeStatus.STARTED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stocktake can only be finalized from STARTED status: " + stocktakeId);
        }

        Map<Long, BigDecimal> countedByProduct = normalizeFinalizeLines(request);
        Set<Long> expectedProducts = session.getLines().stream()
                .map(line -> line.getProduct().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!expectedProducts.equals(countedByProduct.keySet())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "finalize lines must include exactly the stocktake products");
        }

        for (StocktakeLineEntity line : session.getLines()) {
            Long productId = line.getProduct().getId();
            BigDecimal countedQuantity = countedByProduct.get(productId);
            BigDecimal expectedQuantity = balanceCalculator.normalizeScale(line.getExpectedQuantity());
            BigDecimal varianceQuantity = balanceCalculator.normalizeScale(countedQuantity.subtract(expectedQuantity));

            line.setCountedQuantity(countedQuantity);
            line.setVarianceQuantity(varianceQuantity);

            if (varianceQuantity.signum() != 0) {
                InventoryMovementEntity movement = new InventoryMovementEntity();
                movement.setStoreLocation(session.getStoreLocation());
                movement.setProduct(line.getProduct());
                movement.setSale(null);
                movement.setSaleLine(null);
                movement.setMovementType(InventoryMovementType.ADJUSTMENT);
                movement.setQuantityDelta(varianceQuantity);
                movement.setReferenceType(InventoryReferenceType.STOCKTAKE);
                movement.setReferenceNumber(generateLineReferenceNumber(session.getReferenceNumber(), productId));
                line.setInventoryMovement(inventoryMovementRepository.save(movement));
            } else {
                line.setInventoryMovement(null);
            }
        }

        session.setStatus(StocktakeStatus.FINALIZED);
        session.setFinalizedBy(resolveActorUsername());
        session.setFinalizedAt(Instant.now(clock));

        return toSessionResponse(stocktakeSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public StocktakeVarianceReportResponse getVarianceReport(Long stocktakeId) {
        StocktakeSessionEntity session = stocktakeSessionRepository.findByIdWithLines(stocktakeId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "stocktake not found: " + stocktakeId));

        if (session.getStatus() != StocktakeStatus.FINALIZED) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "stocktake variance report is available only after FINALIZED status: " + stocktakeId);
        }

        List<StocktakeLineResponse> byProduct = session.getLines().stream()
                .sorted(Comparator.comparing((StocktakeLineEntity line) -> categoryOrderKey(line.getProduct().getCategory()))
                        .thenComparing(line -> line.getProduct().getId()))
                .map(this::toLineResponse)
                .toList();

        Map<CategoryKey, CategoryTotals> totalsByCategory = new LinkedHashMap<>();
        for (StocktakeLineEntity line : session.getLines()) {
            CategoryEntity category = line.getProduct().getCategory();
            CategoryKey key = new CategoryKey(
                    category == null ? null : category.getId(),
                    category == null ? null : category.getCode(),
                    category == null ? "UNCATEGORIZED" : category.getName());
            CategoryTotals totals = totalsByCategory.computeIfAbsent(key, ignored -> new CategoryTotals());
            totals.expected = balanceCalculator.add(totals.expected, line.getExpectedQuantity());
            totals.counted = balanceCalculator.add(totals.counted, line.getCountedQuantity());
            totals.variance = balanceCalculator.add(totals.variance, line.getVarianceQuantity());
        }

        List<StocktakeCategoryVarianceResponse> byCategory = totalsByCategory.entrySet().stream()
                .map(entry -> new StocktakeCategoryVarianceResponse(
                        entry.getKey().id,
                        entry.getKey().code,
                        entry.getKey().name,
                        balanceCalculator.normalizeScale(entry.getValue().expected),
                        balanceCalculator.normalizeScale(entry.getValue().counted),
                        balanceCalculator.normalizeScale(entry.getValue().variance)))
                .toList();

        return new StocktakeVarianceReportResponse(
                session.getId(),
                session.getStoreLocation().getId(),
                session.getReferenceNumber(),
                com.saulpos.api.inventory.StocktakeStatus.valueOf(session.getStatus().name()),
                session.getSnapshotAt(),
                byProduct,
                byCategory);
    }

    private StocktakeSessionEntity requireStocktakeForUpdate(Long stocktakeId) {
        return stocktakeSessionRepository.findByIdForUpdate(stocktakeId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "stocktake not found: " + stocktakeId));
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private List<ProductEntity> requireProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "productIds is required");
        }

        Set<Long> distinctIds = new LinkedHashSet<>();
        for (Long productId : productIds) {
            if (productId == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            if (!distinctIds.add(productId)) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate productId in stocktake request: " + productId);
            }
        }

        List<ProductEntity> products = productRepository.findAllById(distinctIds);
        if (products.size() != distinctIds.size()) {
            Set<Long> found = products.stream().map(ProductEntity::getId).collect(Collectors.toSet());
            Long missing = distinctIds.stream().filter(id -> !found.contains(id)).findFirst().orElse(null);
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "product not found: " + missing);
        }

        return products;
    }

    private void validateStoreProductMerchantConsistency(StoreLocationEntity storeLocation, ProductEntity product) {
        Long storeMerchantId = storeLocation.getMerchant().getId();
        Long productMerchantId = product.getMerchant().getId();
        if (!Objects.equals(storeMerchantId, productMerchantId)) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product does not belong to the same merchant as storeLocation");
        }
    }

    private Map<Long, BigDecimal> normalizeFinalizeLines(StocktakeFinalizeRequest request) {
        if (request == null || request.lines() == null || request.lines().isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "lines is required");
        }

        Map<Long, BigDecimal> counts = new LinkedHashMap<>();
        for (StocktakeFinalizeLineRequest line : request.lines()) {
            if (line == null || line.productId() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR, "productId is required");
            }
            if (line.countedQuantity() == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "countedQuantity is required for productId: " + line.productId());
            }

            BigDecimal counted = balanceCalculator.normalizeScale(line.countedQuantity());
            if (counted.signum() < 0) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "countedQuantity must be greater than or equal to zero");
            }
            if (counts.putIfAbsent(line.productId(), counted) != null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate productId in finalize lines: " + line.productId());
            }
        }

        return counts;
    }

    private StocktakeSessionResponse toSessionResponse(StocktakeSessionEntity session) {
        List<StocktakeLineResponse> lines = session.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .map(this::toLineResponse)
                .toList();

        return new StocktakeSessionResponse(
                session.getId(),
                session.getStoreLocation().getId(),
                session.getReferenceNumber(),
                com.saulpos.api.inventory.StocktakeStatus.valueOf(session.getStatus().name()),
                session.getSnapshotAt(),
                session.getNote(),
                session.getCreatedBy(),
                session.getCreatedAt(),
                session.getStartedBy(),
                session.getStartedAt(),
                session.getFinalizedBy(),
                session.getFinalizedAt(),
                lines);
    }

    private StocktakeLineResponse toLineResponse(StocktakeLineEntity line) {
        CategoryEntity category = line.getProduct().getCategory();
        return new StocktakeLineResponse(
                line.getProduct().getId(),
                line.getProduct().getSku(),
                line.getProduct().getName(),
                category == null ? null : category.getId(),
                category == null ? null : category.getCode(),
                category == null ? null : category.getName(),
                balanceCalculator.normalizeScale(line.getExpectedQuantity()),
                line.getCountedQuantity() == null ? null : balanceCalculator.normalizeScale(line.getCountedQuantity()),
                line.getVarianceQuantity() == null ? null : balanceCalculator.normalizeScale(line.getVarianceQuantity()),
                line.getInventoryMovement() == null ? null : line.getInventoryMovement().getId());
    }

    private String normalizeOptionalNote(String note) {
        if (note == null) {
            return null;
        }
        String normalized = note.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String generateReferenceNumber() {
        return "STK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
    }

    private String generateLineReferenceNumber(String stocktakeReferenceNumber, Long productId) {
        return stocktakeReferenceNumber + "-P" + productId;
    }

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        String normalized = authentication.getName().trim();
        return normalized.isEmpty() ? "system" : normalized;
    }

    private String categoryOrderKey(CategoryEntity category) {
        if (category == null || category.getName() == null) {
            return "ZZZZZZZZZZ";
        }
        return category.getName();
    }

    private static final class CategoryKey {
        private final Long id;
        private final String code;
        private final String name;

        private CategoryKey(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CategoryKey that)) {
                return false;
            }
            return Objects.equals(id, that.id)
                    && Objects.equals(code, that.code)
                    && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, code, name);
        }
    }

    private static final class CategoryTotals {
        private BigDecimal expected = BigDecimal.ZERO;
        private BigDecimal counted = BigDecimal.ZERO;
        private BigDecimal variance = BigDecimal.ZERO;
    }
}
