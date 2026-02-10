package com.saulpos.server.report.web;

import com.saulpos.api.report.InventoryLowStockReportResponse;
import com.saulpos.api.report.InventoryMovementReportResponse;
import com.saulpos.api.report.InventoryStockOnHandReportResponse;
import com.saulpos.api.report.SalesReturnsReportResponse;
import com.saulpos.server.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    @GetMapping({"/sales", "/sales-returns"})
    public SalesReturnsReportResponse getSalesReturnsReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "taxGroupId", required = false) Long taxGroupId) {
        return reportService.getSalesReturnsReport(
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                categoryId,
                taxGroupId);
    }

    @GetMapping("/inventory/stock-on-hand")
    public InventoryStockOnHandReportResponse getInventoryStockOnHandReport(
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId) {
        return reportService.getInventoryStockOnHandReport(storeLocationId, categoryId, supplierId);
    }

    @GetMapping("/inventory/low-stock")
    public InventoryLowStockReportResponse getInventoryLowStockReport(
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId,
            @RequestParam("minimumQuantity") BigDecimal minimumQuantity) {
        return reportService.getInventoryLowStockReport(storeLocationId, categoryId, supplierId, minimumQuantity);
    }

    @GetMapping("/inventory/movements")
    public InventoryMovementReportResponse getInventoryMovementReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId) {
        return reportService.getInventoryMovementReport(from, to, storeLocationId, categoryId, supplierId);
    }
}
