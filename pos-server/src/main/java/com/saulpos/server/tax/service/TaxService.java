package com.saulpos.server.tax.service;

import com.saulpos.api.tax.TaxMode;
import com.saulpos.api.tax.TaxPreviewLineRequest;
import com.saulpos.api.tax.TaxPreviewLineResponse;
import com.saulpos.api.tax.TaxPreviewRequest;
import com.saulpos.api.tax.TaxPreviewResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.tax.model.StoreTaxRuleEntity;
import com.saulpos.server.tax.model.TaxGroupEntity;
import com.saulpos.server.tax.repository.StoreTaxRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxService {

    private static final Instant MINIMUM_EFFECTIVE_INSTANT = Instant.parse("1970-01-01T00:00:00Z");
    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 4;

    private final StoreLocationRepository storeLocationRepository;
    private final ProductRepository productRepository;
    private final StoreTaxRuleRepository storeTaxRuleRepository;
    private final PricingService pricingService;

    @Transactional(readOnly = true)
    public TaxPreviewResponse preview(TaxPreviewRequest request) {
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());

        List<TaxPreviewLineResponse> lines = new ArrayList<>();
        BigDecimal subtotalNet = zeroMoney();
        BigDecimal totalTax = zeroMoney();
        BigDecimal totalGross = zeroMoney();

        int lineNumber = 1;
        for (TaxPreviewLineRequest lineRequest : request.lines()) {
            ProductEntity product = requireProduct(lineRequest.productId());
            ensureSameMerchant(storeLocation, product);

            TaxGroupEntity taxGroup = requireTaxGroup(product);
            StoreTaxRuleEntity taxRule = requireStoreTaxRule(storeLocation.getId(), taxGroup.getId(), request.at());

            BigDecimal quantity = normalizeQuantity(lineRequest.quantity());
            BigDecimal unitPrice = resolveUnitPrice(request.storeLocationId(), product.getId(), request.at(), lineRequest.unitPrice());

            LineAmounts lineAmounts = calculateLineAmounts(quantity, unitPrice, taxGroup, taxRule);

            lines.add(new TaxPreviewLineResponse(
                    lineNumber,
                    product.getId(),
                    product.getSku(),
                    product.getName(),
                    quantity,
                    unitPrice,
                    taxGroup.getCode(),
                    taxRule.getTaxMode(),
                    lineAmounts.taxRatePercent(),
                    lineAmounts.exempt(),
                    lineAmounts.zeroRated(),
                    lineAmounts.netAmount(),
                    lineAmounts.taxAmount(),
                    lineAmounts.grossAmount()));

            subtotalNet = normalizeMoney(subtotalNet.add(lineAmounts.netAmount()));
            totalTax = normalizeMoney(totalTax.add(lineAmounts.taxAmount()));
            totalGross = normalizeMoney(totalGross.add(lineAmounts.grossAmount()));
            lineNumber++;
        }

        return new TaxPreviewResponse(
                request.storeLocationId(),
                request.at(),
                lines,
                subtotalNet,
                totalTax,
                totalGross);
    }

    private LineAmounts calculateLineAmounts(BigDecimal quantity,
                                             BigDecimal unitPrice,
                                             TaxGroupEntity taxGroup,
                                             StoreTaxRuleEntity taxRule) {
        BigDecimal taxRatePercent = normalizeTaxRatePercent(taxGroup.getTaxRatePercent());
        BigDecimal taxRateDecimal = taxRatePercent.movePointLeft(2);
        BigDecimal lineAmount = normalizeMoney(unitPrice.multiply(quantity));

        boolean zeroRated = taxGroup.isZeroRated() || taxRatePercent.compareTo(BigDecimal.ZERO) == 0;
        boolean exempt = taxRule.isExempt() || zeroRated;

        if (exempt) {
            return new LineAmounts(
                    lineAmount,
                    zeroMoney(),
                    lineAmount,
                    true,
                    zeroRated,
                    taxRatePercent);
        }

        if (taxRule.getTaxMode() == TaxMode.EXCLUSIVE) {
            BigDecimal netAmount = lineAmount;
            BigDecimal taxAmount = normalizeMoney(netAmount.multiply(taxRateDecimal));
            BigDecimal grossAmount = normalizeMoney(netAmount.add(taxAmount));
            return new LineAmounts(netAmount, taxAmount, grossAmount, false, false, taxRatePercent);
        }

        BigDecimal grossAmount = lineAmount;
        BigDecimal divisor = BigDecimal.ONE.add(taxRateDecimal);
        BigDecimal netAmount = normalizeMoney(grossAmount.divide(divisor, 6, RoundingMode.HALF_UP));
        BigDecimal taxAmount = normalizeMoney(grossAmount.subtract(netAmount));

        return new LineAmounts(netAmount, taxAmount, grossAmount, false, false, taxRatePercent);
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private ProductEntity requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "product not found: " + productId));
    }

    private TaxGroupEntity requireTaxGroup(ProductEntity product) {
        if (product.getTaxGroup() == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product tax group is not configured: " + product.getId());
        }
        return product.getTaxGroup();
    }

    private StoreTaxRuleEntity requireStoreTaxRule(Long storeLocationId, Long taxGroupId, Instant at) {
        return storeTaxRuleRepository.findApplicable(storeLocationId, taxGroupId, at, MINIMUM_EFFECTIVE_INSTANT)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.VALIDATION_ERROR,
                        "store tax rule not found for storeLocationId=%d taxGroupId=%d at=%s"
                                .formatted(storeLocationId, taxGroupId, at)));
    }

    private void ensureSameMerchant(StoreLocationEntity storeLocation, ProductEntity product) {
        if (!storeLocation.getMerchant().getId().equals(product.getMerchant().getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product does not belong to store merchant context");
        }
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "quantity must be greater than zero");
        }
        return quantity.stripTrailingZeros();
    }

    private BigDecimal resolveUnitPrice(Long storeLocationId, Long productId, Instant at, BigDecimal providedUnitPrice) {
        if (providedUnitPrice != null) {
            return normalizeMoney(providedUnitPrice);
        }

        return normalizeMoney(pricingService.resolvePrice(storeLocationId, productId, at).resolvedPrice());
    }

    private BigDecimal normalizeTaxRatePercent(BigDecimal ratePercent) {
        BigDecimal normalized = ratePercent == null
                ? BigDecimal.ZERO
                : ratePercent.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0 || normalized.compareTo(new BigDecimal("100.0000")) > 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "taxRatePercent must be between 0 and 100");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "amount is required");
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private record LineAmounts(
            BigDecimal netAmount,
            BigDecimal taxAmount,
            BigDecimal grossAmount,
            boolean exempt,
            boolean zeroRated,
            BigDecimal taxRatePercent
    ) {
    }
}
