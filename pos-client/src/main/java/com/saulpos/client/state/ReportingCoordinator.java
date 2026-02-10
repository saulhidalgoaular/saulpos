package com.saulpos.client.state;

import com.saulpos.api.report.CashShiftReportResponse;
import com.saulpos.api.report.ExceptionReportEventType;
import com.saulpos.api.report.ExceptionReportResponse;
import com.saulpos.api.report.InventoryMovementReportResponse;
import com.saulpos.api.report.SalesReturnsReportBucketResponse;
import com.saulpos.api.report.SalesReturnsReportResponse;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class ReportingCoordinator {

    private static final int STREAM_PREVIEW_LIMIT = 250;

    private final PosApiClient apiClient;
    private final Consumer<Runnable> uiDispatcher;
    private final ObjectProperty<List<String>> tableRows = new SimpleObjectProperty<>(List.of());
    private final StringProperty reportSummary = new SimpleStringProperty("No report loaded.");
    private final StringProperty reportingMessage = new SimpleStringProperty("Reporting ready: load filters and run a report.");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    public ReportingCoordinator(PosApiClient apiClient) {
        this(apiClient, Platform::runLater);
    }

    ReportingCoordinator(PosApiClient apiClient, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.uiDispatcher = uiDispatcher;
    }

    public ObjectProperty<List<String>> tableRowsProperty() {
        return tableRows;
    }

    public StringProperty reportSummaryProperty() {
        return reportSummary;
    }

    public StringProperty reportingMessageProperty() {
        return reportingMessage;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public CompletableFuture<Void> loadSalesReturns(Instant from,
                                                    Instant to,
                                                    Long storeLocationId,
                                                    Long terminalDeviceId,
                                                    Long cashierUserId,
                                                    Long categoryId,
                                                    Long taxGroupId) {
        dispatch(() -> busy.set(true));
        return apiClient.getSalesReturnsReport(from, to, storeLocationId, terminalDeviceId, cashierUserId, categoryId, taxGroupId)
                .thenAccept(this::acceptSalesReturns)
                .whenComplete((ignored, throwable) -> finish("sales/returns", throwable));
    }

    public CompletableFuture<Void> loadInventoryMovements(Instant from,
                                                           Instant to,
                                                           Long storeLocationId,
                                                           Long categoryId,
                                                           Long supplierId) {
        dispatch(() -> busy.set(true));
        return apiClient.getInventoryMovementReport(from, to, storeLocationId, categoryId, supplierId)
                .thenAccept(this::acceptInventoryMovements)
                .whenComplete((ignored, throwable) -> finish("inventory movement", throwable));
    }

    public CompletableFuture<Void> loadCashShifts(Instant from,
                                                  Instant to,
                                                  Long storeLocationId,
                                                  Long terminalDeviceId,
                                                  Long cashierUserId) {
        dispatch(() -> busy.set(true));
        return apiClient.getCashShiftReport(from, to, storeLocationId, terminalDeviceId, cashierUserId)
                .thenAccept(this::acceptCashShifts)
                .whenComplete((ignored, throwable) -> finish("cash shifts", throwable));
    }

    public CompletableFuture<Void> loadExceptions(Instant from,
                                                  Instant to,
                                                  Long storeLocationId,
                                                  Long terminalDeviceId,
                                                  Long cashierUserId,
                                                  String reasonCode,
                                                  ExceptionReportEventType eventType) {
        dispatch(() -> busy.set(true));
        return apiClient.getExceptionReport(from, to, storeLocationId, terminalDeviceId, cashierUserId, normalize(reasonCode), eventType)
                .thenAccept(this::acceptExceptions)
                .whenComplete((ignored, throwable) -> finish("exceptions", throwable));
    }

    public CompletableFuture<Void> exportSalesReturns(Instant from,
                                                      Instant to,
                                                      Long storeLocationId,
                                                      Long terminalDeviceId,
                                                      Long cashierUserId,
                                                      Long categoryId,
                                                      Long taxGroupId) {
        dispatch(() -> busy.set(true));
        return apiClient.exportSalesReturnsReportCsv(from, to, storeLocationId, terminalDeviceId, cashierUserId, categoryId, taxGroupId)
                .thenAccept(csv -> dispatch(() -> reportingMessage.set("Sales/returns CSV export ready: " + csv.length() + " chars.")))
                .whenComplete((ignored, throwable) -> finish("sales export", throwable));
    }

    public CompletableFuture<Void> exportInventoryMovements(Instant from,
                                                             Instant to,
                                                             Long storeLocationId,
                                                             Long categoryId,
                                                             Long supplierId) {
        dispatch(() -> busy.set(true));
        return apiClient.exportInventoryMovementReportCsv(from, to, storeLocationId, categoryId, supplierId)
                .thenAccept(csv -> dispatch(() -> reportingMessage.set("Inventory movement CSV export ready: " + csv.length() + " chars.")))
                .whenComplete((ignored, throwable) -> finish("inventory export", throwable));
    }

    public CompletableFuture<Void> exportCashShifts(Instant from,
                                                    Instant to,
                                                    Long storeLocationId,
                                                    Long terminalDeviceId,
                                                    Long cashierUserId) {
        dispatch(() -> busy.set(true));
        return apiClient.exportCashShiftReportCsv(from, to, storeLocationId, terminalDeviceId, cashierUserId)
                .thenAccept(csv -> dispatch(() -> reportingMessage.set("Cash shift CSV export ready: " + csv.length() + " chars.")))
                .whenComplete((ignored, throwable) -> finish("cash export", throwable));
    }

    public CompletableFuture<Void> exportExceptions(Instant from,
                                                    Instant to,
                                                    Long storeLocationId,
                                                    Long terminalDeviceId,
                                                    Long cashierUserId,
                                                    String reasonCode,
                                                    ExceptionReportEventType eventType) {
        dispatch(() -> busy.set(true));
        return apiClient.exportExceptionReportCsv(from, to, storeLocationId, terminalDeviceId, cashierUserId, normalize(reasonCode), eventType)
                .thenAccept(csv -> dispatch(() -> reportingMessage.set("Exception CSV export ready: " + csv.length() + " chars.")))
                .whenComplete((ignored, throwable) -> finish("exception export", throwable));
    }

    private void acceptSalesReturns(SalesReturnsReportResponse report) {
        List<SalesReturnsReportBucketResponse> source = report.byDay() == null ? List.of() : report.byDay();
        List<String> rows = new ArrayList<>();
        for (SalesReturnsReportBucketResponse bucket : source) {
            rows.add(bucket.key()
                    + " | salesGross=" + bucket.salesGross()
                    + " | returnGross=" + bucket.returnGross()
                    + " | netGross=" + bucket.netGross());
        }
        dispatch(() -> {
            tableRows.set(trimToPreview(rows));
            if (report.summary() != null) {
                reportSummary.set("Sales=" + report.summary().saleCount()
                        + " | Returns=" + report.summary().returnCount()
                        + " | NetGross=" + report.summary().netGross());
            } else {
                reportSummary.set("Sales/returns report loaded.");
            }
            reportingMessage.set("Sales/returns report loaded with " + rows.size() + " day bucket(s). Streaming preview shows up to "
                    + STREAM_PREVIEW_LIMIT + " rows.");
        });
    }

    private void acceptInventoryMovements(InventoryMovementReportResponse report) {
        List<String> rows = new ArrayList<>();
        if (report.rows() != null) {
            report.rows().forEach(row -> rows.add(row.occurredAt()
                    + " | " + row.storeLocationCode()
                    + " | " + row.productSku()
                    + " | " + row.movementType()
                    + " | qty=" + row.quantityDelta()));
        }
        dispatch(() -> {
            tableRows.set(trimToPreview(rows));
            reportSummary.set("Inventory movement rows=" + rows.size());
            reportingMessage.set("Inventory movement report loaded. Streaming preview shows up to "
                    + STREAM_PREVIEW_LIMIT + " rows.");
        });
    }

    private void acceptCashShifts(CashShiftReportResponse report) {
        List<String> rows = new ArrayList<>();
        if (report.rows() != null) {
            report.rows().forEach(row -> rows.add("Shift #" + row.shiftId()
                    + " | store=" + row.storeLocationCode()
                    + " | terminal=" + row.terminalDeviceCode()
                    + " | cashier=" + row.cashierUsername()
                    + " | variance=" + row.varianceCash()));
        }
        dispatch(() -> {
            tableRows.set(trimToPreview(rows));
            reportSummary.set(report.summary() == null
                    ? "Cash shift rows=" + rows.size()
                    : "Shifts=" + report.summary().shiftCount()
                    + " | Open=" + report.summary().openShiftCount()
                    + " | Closed=" + report.summary().closedShiftCount()
                    + " | Variance=" + report.summary().varianceCash());
            reportingMessage.set("Cash shift report loaded. Streaming preview shows up to "
                    + STREAM_PREVIEW_LIMIT + " rows.");
        });
    }

    private void acceptExceptions(ExceptionReportResponse report) {
        List<String> rows = new ArrayList<>();
        if (report.rows() != null) {
            report.rows().forEach(row -> rows.add(row.occurredAt()
                    + " | " + row.eventType()
                    + " | " + row.storeLocationCode()
                    + " | actor=" + row.actorUsername()
                    + " | reason=" + row.reasonCode()));
        }
        dispatch(() -> {
            tableRows.set(trimToPreview(rows));
            reportSummary.set("Exception rows=" + rows.size());
            reportingMessage.set("Exception report loaded. Streaming preview shows up to "
                    + STREAM_PREVIEW_LIMIT + " rows.");
        });
    }

    private List<String> trimToPreview(List<String> rows) {
        if (rows.size() <= STREAM_PREVIEW_LIMIT) {
            return List.copyOf(rows);
        }
        return List.copyOf(rows.subList(0, STREAM_PREVIEW_LIMIT));
    }

    private void finish(String operation, Throwable throwable) {
        if (throwable != null) {
            dispatch(() -> reportingMessage.set(mapError(operation, throwable)));
        }
        dispatch(() -> busy.set(false));
    }

    private String mapError(String operation, Throwable throwable) {
        Throwable root = throwable;
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            root = completionException.getCause();
        }
        if (root instanceof ApiProblemException problem) {
            if (problem.getMessage() != null && !problem.getMessage().isBlank()) {
                return problem.getMessage();
            }
            if (problem.code() != null) {
                return "Reporting " + operation + " failed: " + problem.code();
            }
        }
        return "Reporting " + operation + " failed. Verify filters and connectivity.";
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
