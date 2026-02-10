package com.saulpos.server.report.web;

import com.saulpos.api.report.InventoryLowStockReportResponse;
import com.saulpos.api.report.InventoryMovementReportResponse;
import com.saulpos.api.report.InventoryStockOnHandReportResponse;
import com.saulpos.api.report.ExceptionReportEventType;
import com.saulpos.api.report.ExceptionReportResponse;
import com.saulpos.api.report.CashShiftReportResponse;
import com.saulpos.api.report.EndOfDayCashReportResponse;
import com.saulpos.api.report.SalesReturnsReportResponse;
import com.saulpos.server.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

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

    @GetMapping(value = "/sales/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportSalesReturnsReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "taxGroupId", required = false) Long taxGroupId) {
        String csv = reportService.exportSalesReturnsReportCsv(
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                categoryId,
                taxGroupId);
        return csvResponse("sales-returns-report.csv", csv);
    }

    @GetMapping("/inventory/stock-on-hand")
    public InventoryStockOnHandReportResponse getInventoryStockOnHandReport(
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId) {
        return reportService.getInventoryStockOnHandReport(storeLocationId, categoryId, supplierId);
    }

    @GetMapping(value = "/inventory/stock-on-hand/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportInventoryStockOnHandReport(
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId) {
        String csv = reportService.exportInventoryStockOnHandReportCsv(storeLocationId, categoryId, supplierId);
        return csvResponse("inventory-stock-on-hand-report.csv", csv);
    }

    @GetMapping("/inventory/low-stock")
    public InventoryLowStockReportResponse getInventoryLowStockReport(
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId,
            @RequestParam("minimumQuantity") BigDecimal minimumQuantity) {
        return reportService.getInventoryLowStockReport(storeLocationId, categoryId, supplierId, minimumQuantity);
    }

    @GetMapping(value = "/inventory/low-stock/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportInventoryLowStockReport(
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId,
            @RequestParam("minimumQuantity") BigDecimal minimumQuantity) {
        String csv = reportService.exportInventoryLowStockReportCsv(storeLocationId, categoryId, supplierId, minimumQuantity);
        return csvResponse("inventory-low-stock-report.csv", csv);
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

    @GetMapping(value = "/inventory/movements/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportInventoryMovementReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId) {
        String csv = reportService.exportInventoryMovementReportCsv(from, to, storeLocationId, categoryId, supplierId);
        return csvResponse("inventory-movements-report.csv", csv);
    }

    @GetMapping("/cash/shifts")
    public CashShiftReportResponse getCashShiftReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId) {
        return reportService.getCashShiftReport(from, to, storeLocationId, terminalDeviceId, cashierUserId);
    }

    @GetMapping(value = "/cash/shifts/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportCashShiftReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId) {
        String csv = reportService.exportCashShiftReportCsv(from, to, storeLocationId, terminalDeviceId, cashierUserId);
        return csvResponse("cash-shifts-report.csv", csv);
    }

    @GetMapping("/cash/end-of-day")
    public EndOfDayCashReportResponse getEndOfDayCashReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId) {
        return reportService.getEndOfDayCashReport(from, to, storeLocationId, terminalDeviceId, cashierUserId);
    }

    @GetMapping(value = "/cash/end-of-day/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportEndOfDayCashReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId) {
        String csv = reportService.exportEndOfDayCashReportCsv(from, to, storeLocationId, terminalDeviceId, cashierUserId);
        return csvResponse("cash-end-of-day-report.csv", csv);
    }

    @GetMapping("/exceptions")
    public ExceptionReportResponse getExceptionReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId,
            @RequestParam(value = "reasonCode", required = false) String reasonCode,
            @RequestParam(value = "eventType", required = false) ExceptionReportEventType eventType) {
        return reportService.getExceptionReport(
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                reasonCode,
                eventType);
    }

    @GetMapping(value = "/exceptions/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportExceptionReport(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "storeLocationId", required = false) Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId,
            @RequestParam(value = "cashierUserId", required = false) Long cashierUserId,
            @RequestParam(value = "reasonCode", required = false) String reasonCode,
            @RequestParam(value = "eventType", required = false) ExceptionReportEventType eventType) {
        String csv = reportService.exportExceptionReportCsv(
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                reasonCode,
                eventType);
        return csvResponse("exceptions-report.csv", csv);
    }

    private ResponseEntity<byte[]> csvResponse(String fileName, String csv) {
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(fileName).build();
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
