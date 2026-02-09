package com.saulpos.server.sale.service;

import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartLineResponse;
import com.saulpos.api.sale.SaleCartRecalculateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.tax.RoundingSummary;
import com.saulpos.api.tax.TaxPreviewLineRequest;
import com.saulpos.api.tax.TaxPreviewLineResponse;
import com.saulpos.api.tax.TaxPreviewRequest;
import com.saulpos.api.tax.TaxPreviewResponse;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.service.PricingService;
import com.saulpos.server.catalog.service.ProductSaleModePolicyValidator;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.sale.model.SaleCartEntity;
import com.saulpos.server.sale.model.SaleCartLineEntity;
import com.saulpos.server.sale.repository.SaleCartRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.tax.service.RoundingService;
import com.saulpos.server.tax.service.TaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SaleCartService {

    private final SaleCartRepository saleCartRepository;
    private final UserAccountRepository userAccountRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final TerminalDeviceRepository terminalDeviceRepository;
    private final ProductRepository productRepository;
    private final PricingService pricingService;
    private final TaxService taxService;
    private final RoundingService roundingService;
    private final ProductSaleModePolicyValidator productSaleModePolicyValidator;
    private final CartLinePolicyValidator cartLinePolicyValidator;

    @Transactional
    public SaleCartResponse createCart(SaleCartCreateRequest request) {
        UserAccountEntity cashierUser = requireCashierUser(request.cashierUserId());
        StoreLocationEntity storeLocation = requireStoreLocation(request.storeLocationId());
        TerminalDeviceEntity terminalDevice = requireTerminalDeviceForUpdate(request.terminalDeviceId());

        validateStoreAndTerminalConsistency(storeLocation, terminalDevice);
        validateActiveHierarchy(cashierUser, terminalDevice);

        SaleCartEntity cart = new SaleCartEntity();
        cart.setCashierUser(cashierUser);
        cart.setStoreLocation(storeLocation);
        cart.setTerminalDevice(terminalDevice);
        cart.setStatus(SaleCartStatus.ACTIVE);
        cart.setPricingAt(request.pricingAt());

        RoundingSummary rounding = recalculate(cart, null);
        SaleCartEntity savedCart = saleCartRepository.save(cart);
        return toResponse(savedCart, rounding);
    }

    @Transactional(readOnly = true)
    public SaleCartResponse getCart(Long cartId) {
        SaleCartEntity cart = requireCartWithDetails(cartId);
        RoundingSummary rounding = roundingService.apply(
                cart.getStoreLocation().getId(),
                null,
                cart.getTotalGross());
        return toResponse(cart, rounding);
    }

    @Transactional
    public SaleCartResponse addLine(Long cartId, SaleCartAddLineRequest request) {
        SaleCartEntity cart = requireActiveCartForUpdate(cartId);
        ProductEntity product = requireProduct(request.productId());
        ensureCartProductCompatibility(cart, product);

        String lineKey = cartLinePolicyValidator.normalizeLineKey(request.lineKey());
        Optional<SaleCartLineEntity> existingLine = findLineByKey(cart, lineKey);
        if (existingLine.isPresent() && !existingLine.get().getProduct().getId().equals(product.getId())) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "lineKey already exists with a different product: " + lineKey);
        }

        boolean creatingNewLine = existingLine.isEmpty();
        SaleCartLineEntity line = existingLine.orElseGet(() -> createLine(lineKey, product, nextLineNumber(cart)));

        applyLineInputs(line, product, cart, request.quantity(), request.unitPrice(), request.openPriceReason());
        if (creatingNewLine) {
            cart.addLine(line);
        }

        RoundingSummary rounding = recalculate(cart, null);
        SaleCartEntity savedCart = saleCartRepository.save(cart);
        return toResponse(savedCart, rounding);
    }

    @Transactional
    public SaleCartResponse updateLine(Long cartId, Long lineId, SaleCartUpdateLineRequest request) {
        SaleCartEntity cart = requireActiveCartForUpdate(cartId);
        SaleCartLineEntity line = requireLine(cart, lineId);
        ProductEntity product = line.getProduct();

        applyLineInputs(line, product, cart, request.quantity(), request.unitPrice(), request.openPriceReason());

        RoundingSummary rounding = recalculate(cart, null);
        SaleCartEntity savedCart = saleCartRepository.save(cart);
        return toResponse(savedCart, rounding);
    }

    @Transactional
    public SaleCartResponse removeLine(Long cartId, Long lineId) {
        SaleCartEntity cart = requireActiveCartForUpdate(cartId);
        SaleCartLineEntity line = requireLine(cart, lineId);

        cart.removeLine(line);

        RoundingSummary rounding = recalculate(cart, null);
        SaleCartEntity savedCart = saleCartRepository.save(cart);
        return toResponse(savedCart, rounding);
    }

    @Transactional
    public SaleCartResponse recalculate(Long cartId, SaleCartRecalculateRequest request) {
        SaleCartEntity cart = requireActiveCartForUpdate(cartId);
        RoundingSummary rounding = recalculate(cart, request == null ? null : request.tenderType());
        SaleCartEntity savedCart = saleCartRepository.save(cart);
        return toResponse(savedCart, rounding);
    }

    private SaleCartLineEntity createLine(String lineKey, ProductEntity product, int lineNumber) {
        SaleCartLineEntity line = new SaleCartLineEntity();
        line.setLineKey(lineKey);
        line.setLineNumber(lineNumber);
        line.setProduct(product);
        return line;
    }

    private int nextLineNumber(SaleCartEntity cart) {
        return cart.getLines().stream()
                .map(SaleCartLineEntity::getLineNumber)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private Optional<SaleCartLineEntity> findLineByKey(SaleCartEntity cart, String lineKey) {
        if (lineKey == null) {
            return Optional.empty();
        }

        return cart.getLines().stream()
                .filter(line -> lineKey.equals(line.getLineKey()))
                .findFirst();
    }

    private void applyLineInputs(SaleCartLineEntity line,
                                 ProductEntity product,
                                 SaleCartEntity cart,
                                 BigDecimal requestedQuantity,
                                 BigDecimal requestedUnitPrice,
                                 String requestedOpenPriceReason) {
        BigDecimal normalizedQuantity = cartLinePolicyValidator.normalizeQuantity(product, requestedQuantity);
        String normalizedReason = cartLinePolicyValidator.normalizeOpenPriceReason(requestedOpenPriceReason);

        line.setQuantity(normalizedQuantity);

        if (product.getSaleMode() == ProductSaleMode.OPEN_PRICE) {
            BigDecimal enteredPrice = cartLinePolicyValidator.normalizeUnitPrice(requestedUnitPrice);
            BigDecimal validatedPrice = productSaleModePolicyValidator.validateOpenPriceEntry(
                    enteredPrice,
                    product.getOpenPriceMin(),
                    product.getOpenPriceMax(),
                    product.isOpenPriceRequiresReason(),
                    normalizedReason);
            line.setUnitPrice(validatedPrice);
            line.setOpenPriceReason(normalizedReason);
            return;
        }

        if (requestedUnitPrice != null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "unitPrice is only allowed for OPEN_PRICE products");
        }
        if (normalizedReason != null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "openPriceReason is only allowed for OPEN_PRICE products");
        }

        BigDecimal resolvedPrice = pricingService.resolvePrice(
                cart.getStoreLocation().getId(),
                product.getId(),
                cart.getPricingAt())
                .resolvedPrice();
        line.setUnitPrice(cartLinePolicyValidator.normalizeMoney(resolvedPrice));
        line.setOpenPriceReason(null);
    }

    private RoundingSummary recalculate(SaleCartEntity cart, com.saulpos.api.tax.TenderType tenderType) {
        List<SaleCartLineEntity> orderedLines = orderedLines(cart);
        if (orderedLines.isEmpty()) {
            BigDecimal zero = cartLinePolicyValidator.zeroMoney();
            RoundingSummary rounding = roundingService.apply(cart.getStoreLocation().getId(), tenderType, zero);
            cart.setSubtotalNet(zero);
            cart.setTotalTax(zero);
            cart.setTotalGross(zero);
            cart.setRoundingAdjustment(rounding.adjustment());
            cart.setTotalPayable(rounding.roundedAmount());
            return rounding;
        }

        List<TaxPreviewLineRequest> taxLines = orderedLines.stream()
                .map(line -> new TaxPreviewLineRequest(
                        line.getProduct().getId(),
                        line.getQuantity(),
                        line.getUnitPrice()))
                .toList();

        TaxPreviewResponse preview = taxService.preview(new TaxPreviewRequest(
                cart.getStoreLocation().getId(),
                cart.getPricingAt(),
                tenderType,
                taxLines));

        List<TaxPreviewLineResponse> previewLines = preview.lines();
        for (int index = 0; index < orderedLines.size(); index++) {
            SaleCartLineEntity line = orderedLines.get(index);
            TaxPreviewLineResponse previewLine = previewLines.get(index);
            line.setNetAmount(cartLinePolicyValidator.normalizeMoney(previewLine.netAmount()));
            line.setTaxAmount(cartLinePolicyValidator.normalizeMoney(previewLine.taxAmount()));
            line.setGrossAmount(cartLinePolicyValidator.normalizeMoney(previewLine.grossAmount()));
        }

        cart.setSubtotalNet(cartLinePolicyValidator.normalizeMoney(preview.subtotalNet()));
        cart.setTotalTax(cartLinePolicyValidator.normalizeMoney(preview.totalTax()));
        cart.setTotalGross(cartLinePolicyValidator.normalizeMoney(preview.totalGross()));
        cart.setRoundingAdjustment(cartLinePolicyValidator.normalizeMoney(preview.roundingAdjustment()));
        cart.setTotalPayable(cartLinePolicyValidator.normalizeMoney(preview.totalPayable()));

        return preview.rounding();
    }

    private List<SaleCartLineEntity> orderedLines(SaleCartEntity cart) {
        return cart.getLines().stream()
                .sorted(Comparator.comparingInt(SaleCartLineEntity::getLineNumber)
                        .thenComparing(SaleCartLineEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private SaleCartResponse toResponse(SaleCartEntity cart, RoundingSummary rounding) {
        List<SaleCartLineResponse> lines = orderedLines(cart).stream()
                .map(line -> new SaleCartLineResponse(
                        line.getId(),
                        line.getLineKey(),
                        line.getProduct().getId(),
                        line.getProduct().getSku(),
                        line.getProduct().getName(),
                        line.getProduct().getSaleMode(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getNetAmount(),
                        line.getTaxAmount(),
                        line.getGrossAmount(),
                        line.getOpenPriceReason()))
                .toList();

        return new SaleCartResponse(
                cart.getId(),
                cart.getCashierUser().getId(),
                cart.getStoreLocation().getId(),
                cart.getTerminalDevice().getId(),
                cart.getStatus(),
                cart.getPricingAt(),
                lines,
                cart.getSubtotalNet(),
                cart.getTotalTax(),
                cart.getTotalGross(),
                cart.getRoundingAdjustment(),
                cart.getTotalPayable(),
                rounding,
                cart.getCreatedAt(),
                cart.getUpdatedAt());
    }

    private SaleCartEntity requireCartWithDetails(Long cartId) {
        return saleCartRepository.findByIdWithDetails(cartId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "sale cart not found: " + cartId));
    }

    private SaleCartEntity requireActiveCartForUpdate(Long cartId) {
        SaleCartEntity cart = saleCartRepository.findByIdForUpdate(cartId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "sale cart not found: " + cartId));
        if (cart.getStatus() != SaleCartStatus.ACTIVE) {
            throw new BaseException(ErrorCode.CONFLICT, "sale cart is not active: " + cartId);
        }
        return cart;
    }

    private SaleCartLineEntity requireLine(SaleCartEntity cart, Long lineId) {
        return cart.getLines().stream()
                .filter(line -> lineId.equals(line.getId()))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "sale cart line not found: " + lineId));
    }

    private UserAccountEntity requireCashierUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "cashier user not found: " + userId));
    }

    private StoreLocationEntity requireStoreLocation(Long storeLocationId) {
        return storeLocationRepository.findById(storeLocationId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store location not found: " + storeLocationId));
    }

    private TerminalDeviceEntity requireTerminalDeviceForUpdate(Long terminalDeviceId) {
        return terminalDeviceRepository.findByIdForUpdate(terminalDeviceId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "terminal device not found: " + terminalDeviceId));
    }

    private ProductEntity requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "product not found: " + productId));
    }

    private void validateStoreAndTerminalConsistency(StoreLocationEntity storeLocation, TerminalDeviceEntity terminalDevice) {
        if (!terminalDevice.getStoreLocation().getId().equals(storeLocation.getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "terminal does not belong to the provided store location");
        }
    }

    private void validateActiveHierarchy(UserAccountEntity cashierUser, TerminalDeviceEntity terminalDevice) {
        StoreLocationEntity storeLocation = terminalDevice.getStoreLocation();
        if (!cashierUser.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "cashier user is inactive: " + cashierUser.getId());
        }
        if (!terminalDevice.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "terminal device is inactive: " + terminalDevice.getId());
        }
        if (!storeLocation.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "store location is inactive: " + storeLocation.getId());
        }
        if (!storeLocation.getMerchant().isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "merchant is inactive: " + storeLocation.getMerchant().getId());
        }
    }

    private void ensureCartProductCompatibility(SaleCartEntity cart, ProductEntity product) {
        if (!product.isActive()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "product is inactive: " + product.getId());
        }
        Instant pricingAt = cart.getPricingAt();
        if (pricingAt == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "cart pricingAt is required");
        }
        if (!cart.getStoreLocation().getMerchant().getId().equals(product.getMerchant().getId())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "product does not belong to cart merchant context");
        }
    }
}
