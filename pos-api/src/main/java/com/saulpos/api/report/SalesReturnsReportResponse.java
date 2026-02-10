package com.saulpos.api.report;

import java.time.Instant;
import java.util.List;

public record SalesReturnsReportResponse(
        Instant from,
        Instant to,
        Long storeLocationId,
        Long terminalDeviceId,
        Long cashierUserId,
        Long categoryId,
        Long taxGroupId,
        SalesReturnsReportSummaryResponse summary,
        List<SalesReturnsReportBucketResponse> byDay,
        List<SalesReturnsReportBucketResponse> byStore,
        List<SalesReturnsReportBucketResponse> byTerminal,
        List<SalesReturnsReportBucketResponse> byCashier,
        List<SalesReturnsReportBucketResponse> byCategory,
        List<SalesReturnsReportBucketResponse> byTaxGroup
) {
}
