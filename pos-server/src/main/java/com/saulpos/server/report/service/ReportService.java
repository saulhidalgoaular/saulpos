package com.saulpos.server.report.service;

import com.saulpos.api.report.SalesReturnsReportBucketResponse;
import com.saulpos.api.report.SalesReturnsReportResponse;
import com.saulpos.api.report.SalesReturnsReportSummaryResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.sale.repository.SaleOverrideEventRepository;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.sale.repository.SaleReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final SaleOverrideEventRepository saleOverrideEventRepository;

    @Transactional(readOnly = true)
    public SalesReturnsReportResponse getSalesReturnsReport(Instant from,
                                                            Instant to,
                                                            Long storeLocationId,
                                                            Long terminalDeviceId,
                                                            Long cashierUserId,
                                                            Long categoryId,
                                                            Long taxGroupId) {
        validateDateRange(from, to);

        List<SaleRepository.SaleReportLineProjection> salesLines = saleRepository.findSalesReportLines(
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                categoryId,
                taxGroupId);

        List<SaleReturnRepository.SaleReturnReportLineProjection> returnLines = saleReturnRepository.findReturnReportLines(
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                categoryId,
                taxGroupId);

        List<SaleOverrideEventRepository.SaleDiscountReportProjection> discountLines =
                saleOverrideEventRepository.findDiscountReportLines(
                        from,
                        to,
                        storeLocationId,
                        terminalDeviceId,
                        cashierUserId,
                        categoryId,
                        taxGroupId);

        Set<Long> saleIds = new LinkedHashSet<>();
        Set<Long> returnIds = new LinkedHashSet<>();

        Aggregates summary = new Aggregates();
        Map<String, AggregationBucket> byDay = new LinkedHashMap<>();
        Map<String, AggregationBucket> byStore = new LinkedHashMap<>();
        Map<String, AggregationBucket> byTerminal = new LinkedHashMap<>();
        Map<String, AggregationBucket> byCashier = new LinkedHashMap<>();
        Map<String, AggregationBucket> byCategory = new LinkedHashMap<>();
        Map<String, AggregationBucket> byTaxGroup = new LinkedHashMap<>();

        for (SaleRepository.SaleReportLineProjection line : salesLines) {
            saleIds.add(line.getSaleId());
            BigDecimal quantity = toMoney(line.getQuantity());
            BigDecimal net = toMoney(line.getNetAmount());
            BigDecimal tax = toMoney(line.getTaxAmount());
            BigDecimal gross = toMoney(line.getGrossAmount());

            summary.salesQuantity = summary.salesQuantity.add(quantity);
            summary.salesNet = summary.salesNet.add(net);
            summary.salesTax = summary.salesTax.add(tax);
            summary.salesGross = summary.salesGross.add(gross);

            applySale(toDayBucket(line.getSoldAt()), byDay, quantity, net, tax, gross);
            applySale(toStoreBucket(line.getStoreLocationId(), line.getStoreLocationCode(), line.getStoreLocationName()), byStore,
                    quantity, net, tax, gross);
            applySale(toTerminalBucket(line.getTerminalDeviceId(), line.getTerminalDeviceCode(), line.getTerminalDeviceName()), byTerminal,
                    quantity, net, tax, gross);
            applySale(toCashierBucket(line.getCashierUserId(), line.getCashierUsername()), byCashier,
                    quantity, net, tax, gross);
            applySale(toCategoryBucket(line.getCategoryId(), line.getCategoryCode(), line.getCategoryName()), byCategory,
                    quantity, net, tax, gross);
            applySale(toTaxGroupBucket(line.getTaxGroupId(), line.getTaxGroupCode(), line.getTaxGroupName()), byTaxGroup,
                    quantity, net, tax, gross);
        }

        for (SaleReturnRepository.SaleReturnReportLineProjection line : returnLines) {
            returnIds.add(line.getSaleReturnId());
            BigDecimal quantity = toMoney(line.getQuantity());
            BigDecimal net = toMoney(line.getNetAmount());
            BigDecimal tax = toMoney(line.getTaxAmount());
            BigDecimal gross = toMoney(line.getGrossAmount());

            summary.returnQuantity = summary.returnQuantity.add(quantity);
            summary.returnNet = summary.returnNet.add(net);
            summary.returnTax = summary.returnTax.add(tax);
            summary.returnGross = summary.returnGross.add(gross);

            applyReturn(toDayBucket(line.getReturnedAt()), byDay, quantity, net, tax, gross);
            applyReturn(toStoreBucket(line.getStoreLocationId(), line.getStoreLocationCode(), line.getStoreLocationName()), byStore,
                    quantity, net, tax, gross);
            applyReturn(toTerminalBucket(line.getTerminalDeviceId(), line.getTerminalDeviceCode(), line.getTerminalDeviceName()), byTerminal,
                    quantity, net, tax, gross);
            applyReturn(toCashierBucket(line.getCashierUserId(), line.getCashierUsername()), byCashier,
                    quantity, net, tax, gross);
            applyReturn(toCategoryBucket(line.getCategoryId(), line.getCategoryCode(), line.getCategoryName()), byCategory,
                    quantity, net, tax, gross);
            applyReturn(toTaxGroupBucket(line.getTaxGroupId(), line.getTaxGroupCode(), line.getTaxGroupName()), byTaxGroup,
                    quantity, net, tax, gross);
        }

        BigDecimal discountGross = ZERO;
        for (SaleOverrideEventRepository.SaleDiscountReportProjection discount : discountLines) {
            BigDecimal quantity = discount.getQuantity() == null ? BigDecimal.ONE : discount.getQuantity();
            BigDecimal before = toMoney(discount.getBeforeUnitPrice());
            BigDecimal after = toMoney(discount.getAfterUnitPrice());
            BigDecimal amount = before.subtract(after).multiply(quantity).setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                discountGross = discountGross.add(amount);
            }
        }

        summary.discountGross = discountGross;

        SalesReturnsReportSummaryResponse summaryResponse = new SalesReturnsReportSummaryResponse(
                saleIds.size(),
                returnIds.size(),
                summary.salesQuantity,
                summary.returnQuantity,
                summary.salesNet,
                summary.returnNet,
                summary.salesTax,
                summary.returnTax,
                summary.salesGross,
                summary.returnGross,
                summary.salesGross.subtract(summary.returnGross).setScale(2, RoundingMode.HALF_UP),
                summary.discountGross);

        return new SalesReturnsReportResponse(
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                categoryId,
                taxGroupId,
                summaryResponse,
                toSortedBuckets(byDay),
                toSortedBuckets(byStore),
                toSortedBuckets(byTerminal),
                toSortedBuckets(byCashier),
                toSortedBuckets(byCategory),
                toSortedBuckets(byTaxGroup));
    }

    private void validateDateRange(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "from must be before or equal to to");
        }
    }

    private void applySale(BucketKey key,
                           Map<String, AggregationBucket> target,
                           BigDecimal quantity,
                           BigDecimal net,
                           BigDecimal tax,
                           BigDecimal gross) {
        AggregationBucket bucket = target.computeIfAbsent(key.key(), ignored -> new AggregationBucket(key));
        bucket.salesQuantity = bucket.salesQuantity.add(quantity);
        bucket.salesNet = bucket.salesNet.add(net);
        bucket.salesTax = bucket.salesTax.add(tax);
        bucket.salesGross = bucket.salesGross.add(gross);
    }

    private void applyReturn(BucketKey key,
                             Map<String, AggregationBucket> target,
                             BigDecimal quantity,
                             BigDecimal net,
                             BigDecimal tax,
                             BigDecimal gross) {
        AggregationBucket bucket = target.computeIfAbsent(key.key(), ignored -> new AggregationBucket(key));
        bucket.returnQuantity = bucket.returnQuantity.add(quantity);
        bucket.returnNet = bucket.returnNet.add(net);
        bucket.returnTax = bucket.returnTax.add(tax);
        bucket.returnGross = bucket.returnGross.add(gross);
    }

    private List<SalesReturnsReportBucketResponse> toSortedBuckets(Map<String, AggregationBucket> source) {
        List<AggregationBucket> buckets = new ArrayList<>(source.values());
        buckets.sort(Comparator.comparing(item -> item.key.key()));

        return buckets.stream()
                .map(bucket -> new SalesReturnsReportBucketResponse(
                        bucket.key.key,
                        bucket.key.id,
                        bucket.key.code,
                        bucket.key.label,
                        bucket.salesQuantity,
                        bucket.returnQuantity,
                        bucket.salesNet,
                        bucket.returnNet,
                        bucket.salesTax,
                        bucket.returnTax,
                        bucket.salesGross,
                        bucket.returnGross,
                        bucket.salesGross.subtract(bucket.returnGross).setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private BucketKey toDayBucket(Instant instant) {
        String day = instant.atZone(ZoneOffset.UTC).toLocalDate().toString();
        return new BucketKey(day, null, null, day);
    }

    private BucketKey toStoreBucket(Long id, String code, String name) {
        return new BucketKey("STORE-" + id, id, code, name);
    }

    private BucketKey toTerminalBucket(Long id, String code, String name) {
        return new BucketKey("TERMINAL-" + id, id, code, name);
    }

    private BucketKey toCashierBucket(Long id, String username) {
        return new BucketKey("CASHIER-" + id, id, username, username);
    }

    private BucketKey toCategoryBucket(Long id, String code, String name) {
        if (id == null) {
            return new BucketKey("CATEGORY-NONE", null, null, "Uncategorized");
        }
        return new BucketKey("CATEGORY-" + id, id, code, name);
    }

    private BucketKey toTaxGroupBucket(Long id, String code, String name) {
        if (id == null) {
            return new BucketKey("TAXGROUP-NONE", null, null, "No Tax Group");
        }
        return new BucketKey("TAXGROUP-" + id, id, code, name);
    }

    private BigDecimal toMoney(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static final class Aggregates {
        private BigDecimal salesQuantity = ZERO;
        private BigDecimal returnQuantity = ZERO;
        private BigDecimal salesNet = ZERO;
        private BigDecimal returnNet = ZERO;
        private BigDecimal salesTax = ZERO;
        private BigDecimal returnTax = ZERO;
        private BigDecimal salesGross = ZERO;
        private BigDecimal returnGross = ZERO;
        private BigDecimal discountGross = ZERO;
    }

    private static final class AggregationBucket {
        private final BucketKey key;
        private BigDecimal salesQuantity = ZERO;
        private BigDecimal returnQuantity = ZERO;
        private BigDecimal salesNet = ZERO;
        private BigDecimal returnNet = ZERO;
        private BigDecimal salesTax = ZERO;
        private BigDecimal returnTax = ZERO;
        private BigDecimal salesGross = ZERO;
        private BigDecimal returnGross = ZERO;

        private AggregationBucket(BucketKey key) {
            this.key = key;
        }
    }

    private record BucketKey(String key, Long id, String code, String label) {
    }
}
