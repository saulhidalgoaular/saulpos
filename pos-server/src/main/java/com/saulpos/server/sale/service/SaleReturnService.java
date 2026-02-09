package com.saulpos.server.sale.service;

import com.saulpos.api.refund.SaleReturnLineResponse;
import com.saulpos.api.refund.SaleReturnLookupLineResponse;
import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitLineRequest;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.InventoryMovementType;
import com.saulpos.server.sale.model.InventoryReferenceType;
import com.saulpos.server.sale.model.PaymentEntity;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleLineEntity;
import com.saulpos.server.sale.model.SaleReturnEntity;
import com.saulpos.server.sale.model.SaleReturnLineEntity;
import com.saulpos.server.sale.model.SaleReturnRefundEntity;
import com.saulpos.server.sale.repository.InventoryMovementRepository;
import com.saulpos.server.sale.repository.PaymentRepository;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.sale.repository.SaleReturnLineRepository;
import com.saulpos.server.sale.repository.SaleReturnRepository;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.authorization.SecurityAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleReturnService {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_QUANTITY = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    private final SaleRepository saleRepository;
    private final PaymentRepository paymentRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final SaleReturnLineRepository saleReturnLineRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final SaleReturnAmountAllocator amountAllocator;
    private final Clock clock;

    @Value("${app.sales.return-restricted-window-days:30}")
    private long restrictedWindowDays;

    @Transactional(readOnly = true)
    public SaleReturnLookupResponse lookupByReceipt(String receiptNumber) {
        SaleEntity sale = requireSaleByReceiptNumber(receiptNumber);
        Map<Long, ReturnedTotals> returnedTotalsByLineId = summarizeReturnedTotalsByLineId(sale.getId());

        List<SaleReturnLookupLineResponse> lines = sale.getLines().stream()
                .sorted(Comparator.comparingInt(SaleLineEntity::getLineNumber)
                        .thenComparing(SaleLineEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(line -> toLookupLineResponse(line, returnedTotalsByLineId.get(line.getId())))
                .toList();

        return new SaleReturnLookupResponse(
                sale.getId(),
                sale.getReceiptNumber(),
                sale.getStoreLocation().getId(),
                sale.getTerminalDevice().getId(),
                sale.getCreatedAt(),
                lines);
    }

    @Transactional
    public SaleReturnResponse submit(SaleReturnSubmitRequest request) {
        SaleEntity sale = requireSale(request.saleId(), request.receiptNumber());
        enforceRestrictedWindowApproval(sale);
        validateDistinctSaleLineIds(request.lines());

        PaymentEntity payment = paymentRepository.findByCartIdWithAllocations(sale.getCart().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.CONFLICT,
                        "payment record not found for sale: " + sale.getId()));

        Map<Long, SaleLineEntity> saleLinesById = indexSaleLines(sale);
        Map<Long, ReturnedTotals> returnedTotalsByLineId = summarizeReturnedTotalsByLineId(sale.getId());

        SaleReturnEntity saleReturn = new SaleReturnEntity();
        saleReturn.setSale(sale);
        saleReturn.setPayment(payment);
        saleReturn.setReasonCode(normalizeRequiredReasonCode(request.reasonCode()));
        saleReturn.setRefundTenderType(request.refundTenderType());
        saleReturn.setRefundNote(normalizeOptionalText(request.note(), 255));
        saleReturn.setReturnReference(generateReturnReference(sale.getId()));

        BigDecimal subtotalNet = ZERO_MONEY;
        BigDecimal totalTax = ZERO_MONEY;
        BigDecimal totalGross = ZERO_MONEY;

        for (SaleReturnSubmitLineRequest lineRequest : request.lines()) {
            SaleLineEntity saleLine = saleLinesById.get(lineRequest.saleLineId());
            if (saleLine == null) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "saleLineId does not belong to sale: " + lineRequest.saleLineId());
            }

            ReturnedTotals returnedTotals = returnedTotalsByLineId.getOrDefault(lineRequest.saleLineId(), ReturnedTotals.zero());
            SaleReturnAmountAllocator.Allocation allocation = amountAllocator.allocate(
                    saleLine.getQuantity(),
                    saleLine.getNetAmount(),
                    saleLine.getTaxAmount(),
                    saleLine.getGrossAmount(),
                    returnedTotals.returnedQuantity(),
                    returnedTotals.returnedNet(),
                    returnedTotals.returnedTax(),
                    returnedTotals.returnedGross(),
                    lineRequest.quantity());

            SaleReturnLineEntity returnLine = new SaleReturnLineEntity();
            returnLine.setSaleLine(saleLine);
            returnLine.setQuantity(allocation.quantity());
            returnLine.setNetAmount(allocation.netAmount());
            returnLine.setTaxAmount(allocation.taxAmount());
            returnLine.setGrossAmount(allocation.grossAmount());
            saleReturn.addLine(returnLine);

            subtotalNet = normalizeMoney(subtotalNet.add(allocation.netAmount()));
            totalTax = normalizeMoney(totalTax.add(allocation.taxAmount()));
            totalGross = normalizeMoney(totalGross.add(allocation.grossAmount()));
        }

        if (saleReturn.getLines().isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "at least one return line is required");
        }

        if (totalGross.compareTo(ZERO_MONEY) <= 0) {
            throw new BaseException(ErrorCode.CONFLICT, "return total must be greater than zero");
        }

        saleReturn.setSubtotalNet(subtotalNet);
        saleReturn.setTotalTax(totalTax);
        saleReturn.setTotalGross(totalGross);

        SaleReturnRefundEntity refund = new SaleReturnRefundEntity();
        refund.setPayment(payment);
        refund.setTenderType(request.refundTenderType());
        refund.setAmount(totalGross);
        refund.setReference(normalizeOptionalText(request.refundReference(), 120));
        saleReturn.addRefund(refund);

        SaleReturnEntity savedReturn = saleReturnRepository.save(saleReturn);
        inventoryMovementRepository.saveAll(createReturnMovements(savedReturn));

        return toResponse(savedReturn);
    }

    private void validateDistinctSaleLineIds(List<SaleReturnSubmitLineRequest> lines) {
        Set<Long> seenSaleLineIds = new HashSet<>();
        for (SaleReturnSubmitLineRequest line : lines) {
            if (!seenSaleLineIds.add(line.saleLineId())) {
                throw new BaseException(ErrorCode.VALIDATION_ERROR,
                        "duplicate saleLineId in request: " + line.saleLineId());
            }
        }
    }

    private SaleReturnLookupLineResponse toLookupLineResponse(SaleLineEntity line, ReturnedTotals returnedTotals) {
        ReturnedTotals normalizedReturned = returnedTotals == null ? ReturnedTotals.zero() : returnedTotals;
        BigDecimal soldQuantity = normalizeQuantity(line.getQuantity());
        BigDecimal returnedQuantity = normalizeQuantity(normalizedReturned.returnedQuantity());
        BigDecimal availableQuantity = soldQuantity.subtract(returnedQuantity).max(ZERO_QUANTITY).setScale(3, RoundingMode.HALF_UP);

        return new SaleReturnLookupLineResponse(
                line.getId(),
                line.getProduct().getId(),
                line.getLineNumber(),
                soldQuantity,
                returnedQuantity,
                availableQuantity,
                normalizeMoney(line.getUnitPrice()),
                normalizeMoney(line.getGrossAmount()));
    }

    private List<InventoryMovementEntity> createReturnMovements(SaleReturnEntity saleReturn) {
        return saleReturn.getLines().stream()
                .map(line -> {
                    InventoryMovementEntity movement = new InventoryMovementEntity();
                    movement.setStoreLocation(saleReturn.getSale().getStoreLocation());
                    movement.setProduct(line.getSaleLine().getProduct());
                    movement.setSale(null);
                    movement.setSaleLine(null);
                    movement.setMovementType(InventoryMovementType.RETURN);
                    movement.setQuantityDelta(normalizeQuantity(line.getQuantity()));
                    movement.setReferenceType(InventoryReferenceType.SALE_RETURN);
                    movement.setReferenceNumber(saleReturn.getReturnReference());
                    return movement;
                })
                .toList();
    }

    private Map<Long, ReturnedTotals> summarizeReturnedTotalsByLineId(Long saleId) {
        Map<Long, ReturnedTotals> totals = new HashMap<>();
        for (SaleReturnLineRepository.ReturnedLineTotalsProjection projection
                : saleReturnLineRepository.summarizeReturnedBySaleId(saleId)) {
            totals.put(
                    projection.getSaleLineId(),
                    new ReturnedTotals(
                            normalizeQuantity(projection.getReturnedQuantity()),
                            normalizeMoney(projection.getReturnedNet()),
                            normalizeMoney(projection.getReturnedTax()),
                            normalizeMoney(projection.getReturnedGross())));
        }
        return totals;
    }

    private Map<Long, SaleLineEntity> indexSaleLines(SaleEntity sale) {
        Map<Long, SaleLineEntity> indexed = new HashMap<>();
        for (SaleLineEntity line : sale.getLines()) {
            indexed.put(line.getId(), line);
        }
        return indexed;
    }

    private SaleEntity requireSale(Long saleId, String receiptNumber) {
        if (saleId == null && (receiptNumber == null || receiptNumber.isBlank())) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "either saleId or receiptNumber is required");
        }

        if (saleId != null) {
            SaleEntity sale = saleRepository.findById(saleId)
                    .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                            "sale not found: " + saleId));
            if (receiptNumber != null && !receiptNumber.isBlank()
                    && !sale.getReceiptNumber().equalsIgnoreCase(receiptNumber.trim())) {
                throw new BaseException(ErrorCode.CONFLICT,
                        "saleId and receiptNumber do not reference the same sale");
            }
            return sale;
        }

        return requireSaleByReceiptNumber(receiptNumber);
    }

    private SaleEntity requireSaleByReceiptNumber(String receiptNumber) {
        String normalizedReceiptNumber = normalizeRequiredReceiptNumber(receiptNumber);
        return saleRepository.findByReceiptNumberIgnoreCase(normalizedReceiptNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "sale not found for receiptNumber: " + normalizedReceiptNumber));
    }

    private void enforceRestrictedWindowApproval(SaleEntity sale) {
        long effectiveWindowDays = Math.max(restrictedWindowDays, 1);
        Instant cutoff = sale.getCreatedAt().plus(Duration.ofDays(effectiveWindowDays));
        Instant now = Instant.now(clock);
        if (now.isAfter(cutoff) && !currentUserHasPermission(PermissionCodes.CONFIGURATION_MANAGE)) {
            throw new BaseException(ErrorCode.AUTH_FORBIDDEN,
                    "return is outside allowed window and requires manager approval");
        }
    }

    private boolean currentUserHasPermission(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        String requiredAuthority = SecurityAuthority.permission(permissionCode);
        return authentication.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .anyMatch(requiredAuthority::equalsIgnoreCase);
    }

    private SaleReturnResponse toResponse(SaleReturnEntity saleReturn) {
        List<SaleReturnLineResponse> lineResponses = saleReturn.getLines().stream()
                .sorted(Comparator.comparing((SaleReturnLineEntity line) -> line.getSaleLine().getLineNumber())
                        .thenComparing(SaleReturnLineEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(line -> new SaleReturnLineResponse(
                        line.getId(),
                        line.getSaleLine().getId(),
                        line.getSaleLine().getProduct().getId(),
                        line.getSaleLine().getLineNumber(),
                        normalizeQuantity(line.getQuantity()),
                        normalizeMoney(line.getNetAmount()),
                        normalizeMoney(line.getTaxAmount()),
                        normalizeMoney(line.getGrossAmount())))
                .toList();

        return new SaleReturnResponse(
                saleReturn.getId(),
                saleReturn.getSale().getId(),
                saleReturn.getSale().getReceiptNumber(),
                saleReturn.getReturnReference(),
                saleReturn.getReasonCode(),
                saleReturn.getRefundTenderType(),
                normalizeMoney(saleReturn.getSubtotalNet()),
                normalizeMoney(saleReturn.getTotalTax()),
                normalizeMoney(saleReturn.getTotalGross()),
                lineResponses,
                saleReturn.getCreatedAt());
    }

    private String normalizeRequiredReceiptNumber(String receiptNumber) {
        if (receiptNumber == null || receiptNumber.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "receiptNumber is required");
        }
        return receiptNumber.trim();
    }

    private String normalizeRequiredReasonCode(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "reasonCode is required");
        }
        return reasonCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "value exceeds maximum length: " + maxLength);
        }
        return normalized;
    }

    private String generateReturnReference(Long saleId) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return "RET-" + saleId + "-" + suffix;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return ZERO_MONEY;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return ZERO_QUANTITY;
        }
        return quantity.setScale(3, RoundingMode.HALF_UP);
    }

    private record ReturnedTotals(
            BigDecimal returnedQuantity,
            BigDecimal returnedNet,
            BigDecimal returnedTax,
            BigDecimal returnedGross
    ) {
        static ReturnedTotals zero() {
            return new ReturnedTotals(ZERO_QUANTITY, ZERO_MONEY, ZERO_MONEY, ZERO_MONEY);
        }
    }
}
