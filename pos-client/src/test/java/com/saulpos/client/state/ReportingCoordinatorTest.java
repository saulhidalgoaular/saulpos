package com.saulpos.client.state;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductRequest;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.customer.CustomerRequest;
import com.saulpos.api.customer.CustomerResponse;
import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.report.CashShiftReportResponse;
import com.saulpos.api.report.CashShiftReportRowResponse;
import com.saulpos.api.report.CashShiftReportSummaryResponse;
import com.saulpos.api.report.ExceptionReportEventType;
import com.saulpos.api.report.ExceptionReportRowResponse;
import com.saulpos.api.report.ExceptionReportResponse;
import com.saulpos.api.report.InventoryMovementReportResponse;
import com.saulpos.api.report.SalesReturnsReportBucketResponse;
import com.saulpos.api.report.SalesReturnsReportResponse;
import com.saulpos.api.report.SalesReturnsReportSummaryResponse;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportingCoordinatorTest {

    @Test
    void loadSalesReturns_shouldApplyFiltersAndPopulatePreview() {
        FakePosApiClient apiClient = new FakePosApiClient();
        ReportingCoordinator coordinator = new ReportingCoordinator(apiClient, Runnable::run);
        Instant from = Instant.parse("2026-02-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-10T23:59:59Z");

        coordinator.loadSalesReturns(from, to, 10L, 20L, 30L, 40L, 50L).join();

        assertEquals(from, apiClient.salesFrom);
        assertEquals(50L, apiClient.salesTaxGroupId);
        assertEquals(1, coordinator.tableRowsProperty().get().size());
        assertTrue(coordinator.reportSummaryProperty().get().contains("Sales=4"));
        assertTrue(coordinator.reportingMessageProperty().get().contains("Streaming preview"));
    }

    @Test
    void exportCashShifts_shouldShowExportFeedback() {
        FakePosApiClient apiClient = new FakePosApiClient();
        ReportingCoordinator coordinator = new ReportingCoordinator(apiClient, Runnable::run);

        coordinator.exportCashShifts(null, null, 10L, 20L, 30L).join();

        assertEquals(10L, apiClient.cashStoreLocationId);
        assertTrue(coordinator.reportingMessageProperty().get().contains("CSV export ready"));
    }

    @Test
    void loadExceptions_problemDetail_shouldBubbleMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.exceptionFailure = new ApiProblemException(403, "POS-4030", "Forbidden report access");
        ReportingCoordinator coordinator = new ReportingCoordinator(apiClient, Runnable::run);

        coordinator.loadExceptions(null, null, null, null, null, null, ExceptionReportEventType.NO_SALE)
                .handle((ok, ex) -> null)
                .join();

        assertEquals("Forbidden report access", coordinator.reportingMessageProperty().get());
    }

    @Test
    void loadExceptions_shouldIncludeDrillDownContextInPreviewRows() {
        FakePosApiClient apiClient = new FakePosApiClient();
        ReportingCoordinator coordinator = new ReportingCoordinator(apiClient, Runnable::run);

        coordinator.loadExceptions(null, null, 1L, 2L, 3L, "VOID", ExceptionReportEventType.LINE_VOID).join();

        String row = coordinator.tableRowsProperty().get().get(0);
        assertTrue(row.contains("terminal=TERM-2"));
        assertTrue(row.contains("actor=manager"));
        assertTrue(row.contains("approver=supervisor"));
        assertTrue(row.contains("reason=VOID"));
        assertTrue(row.contains("correlation=corr-001"));
    }

    private static final class FakePosApiClient implements PosApiClient {

        private Instant salesFrom;
        private Long salesTaxGroupId;
        private Long cashStoreLocationId;
        private RuntimeException exceptionFailure;

        @Override
        public CompletableFuture<Boolean> ping() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<AuthTokenResponse> login(String username, String password) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<AuthTokenResponse> refresh(String refreshToken) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<CurrentUserResponse> currentUser() {
            return CompletableFuture.completedFuture(new CurrentUserResponse(1L, "manager", Set.of("MANAGER")));
        }

        @Override
        public CompletableFuture<Void> logout() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<CashShiftResponse> openShift(CashShiftOpenRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<CashMovementResponse> addCashMovement(Long shiftId, CashMovementRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<CashShiftResponse> closeShift(Long shiftId, CashShiftCloseRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<CashShiftResponse> getShift(Long shiftId) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<ProductLookupResponse> lookupProductByBarcode(Long merchantId, String barcode) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<ProductSearchResponse> searchProducts(Long merchantId, String query, Boolean active, int page, int size) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<List<ProductResponse>> listProducts(Long merchantId, Boolean active, String query) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<ProductResponse> createProduct(ProductRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<ProductResponse> updateProduct(Long productId, ProductRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<PriceResolutionResponse> resolvePrice(Long storeLocationId, Long productId, Long customerId) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleCartResponse> createCart(SaleCartCreateRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleCartResponse> getCart(Long cartId) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleCartResponse> addCartLine(Long cartId, SaleCartAddLineRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleCartResponse> updateCartLine(Long cartId, Long lineId, SaleCartUpdateLineRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleCartResponse> removeCartLine(Long cartId, Long lineId) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleCartResponse> recalculateCart(Long cartId) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleCheckoutResponse> checkout(SaleCheckoutRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleReturnLookupResponse> lookupReturnByReceipt(String receiptNumber) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleReturnResponse> submitReturn(SaleReturnSubmitRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<List<CustomerResponse>> listCustomers(Long merchantId, Boolean active) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<List<CustomerResponse>> lookupCustomers(Long merchantId,
                                                                         String documentType,
                                                                         String documentValue,
                                                                         String email,
                                                                         String phone) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<CustomerResponse> createCustomer(CustomerRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SalesReturnsReportResponse> getSalesReturnsReport(Instant from,
                                                                                    Instant to,
                                                                                    Long storeLocationId,
                                                                                    Long terminalDeviceId,
                                                                                    Long cashierUserId,
                                                                                    Long categoryId,
                                                                                    Long taxGroupId) {
            this.salesFrom = from;
            this.salesTaxGroupId = taxGroupId;
            return CompletableFuture.completedFuture(new SalesReturnsReportResponse(
                    from,
                    to,
                    storeLocationId,
                    terminalDeviceId,
                    cashierUserId,
                    categoryId,
                    taxGroupId,
                    new SalesReturnsReportSummaryResponse(
                            4,
                            1,
                            new BigDecimal("20.00"),
                            new BigDecimal("1.00"),
                            new BigDecimal("100.00"),
                            new BigDecimal("2.00"),
                            new BigDecimal("18.00"),
                            new BigDecimal("0.36"),
                            new BigDecimal("118.00"),
                            new BigDecimal("2.36"),
                            new BigDecimal("115.64"),
                            BigDecimal.ZERO
                    ),
                    List.of(new SalesReturnsReportBucketResponse(
                            "2026-02-10",
                            null,
                            null,
                            "Day",
                            new BigDecimal("20.00"),
                            BigDecimal.ONE,
                            new BigDecimal("100.00"),
                            new BigDecimal("2.00"),
                            new BigDecimal("18.00"),
                            new BigDecimal("0.36"),
                            new BigDecimal("118.00"),
                            new BigDecimal("2.36"),
                            new BigDecimal("115.64")
                    )),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            ));
        }

        @Override
        public CompletableFuture<CashShiftReportResponse> getCashShiftReport(Instant from,
                                                                              Instant to,
                                                                              Long storeLocationId,
                                                                              Long terminalDeviceId,
                                                                              Long cashierUserId) {
            this.cashStoreLocationId = storeLocationId;
            return CompletableFuture.completedFuture(new CashShiftReportResponse(
                    from,
                    to,
                    storeLocationId,
                    terminalDeviceId,
                    cashierUserId,
                    new CashShiftReportSummaryResponse(1, 1, 0,
                            new BigDecimal("100.00"),
                            new BigDecimal("10.00"),
                            BigDecimal.ZERO,
                            new BigDecimal("110.00"),
                            new BigDecimal("109.00"),
                            new BigDecimal("-1.00")),
                    List.of(new CashShiftReportRowResponse(
                            1L,
                            storeLocationId,
                            "S-1",
                            "Store 1",
                            terminalDeviceId,
                            "T-1",
                            "Terminal 1",
                            cashierUserId,
                            "cashier",
                            com.saulpos.api.shift.CashShiftStatus.CLOSED,
                            new BigDecimal("100.00"),
                            new BigDecimal("10.00"),
                            BigDecimal.ZERO,
                            new BigDecimal("110.00"),
                            new BigDecimal("109.00"),
                            new BigDecimal("-1.00"),
                            "minor",
                            Instant.parse("2026-02-10T10:00:00Z"),
                            Instant.parse("2026-02-10T20:00:00Z")
                    ))
            ));
        }

        @Override
        public CompletableFuture<ExceptionReportResponse> getExceptionReport(Instant from,
                                                                             Instant to,
                                                                             Long storeLocationId,
                                                                             Long terminalDeviceId,
                                                                             Long cashierUserId,
                                                                             String reasonCode,
                                                                             ExceptionReportEventType eventType) {
            if (exceptionFailure != null) {
                return CompletableFuture.failedFuture(exceptionFailure);
            }
            return CompletableFuture.completedFuture(new ExceptionReportResponse(
                    from,
                    to,
                    storeLocationId,
                    terminalDeviceId,
                    cashierUserId,
                    reasonCode,
                    eventType,
                    List.of(new ExceptionReportRowResponse(
                            11L,
                            Instant.parse("2026-02-10T12:00:00Z"),
                            eventType == null ? ExceptionReportEventType.LINE_VOID : eventType,
                            storeLocationId == null ? 1L : storeLocationId,
                            "S-1",
                            "Store 1",
                            terminalDeviceId == null ? 2L : terminalDeviceId,
                            "TERM-2",
                            "Terminal 2",
                            cashierUserId == null ? 3L : cashierUserId,
                            "cashier",
                            "manager",
                            "supervisor",
                            reasonCode == null ? "VOID" : reasonCode,
                            "manual override",
                            "corr-001",
                            "REF-100"
                    ))
            ));
        }

        @Override
        public CompletableFuture<InventoryMovementReportResponse> getInventoryMovementReport(Instant from,
                                                                                              Instant to,
                                                                                              Long storeLocationId,
                                                                                              Long categoryId,
                                                                                              Long supplierId) {
            return CompletableFuture.completedFuture(new InventoryMovementReportResponse(
                    from,
                    to,
                    storeLocationId,
                    categoryId,
                    supplierId,
                    List.of()
            ));
        }

        @Override
        public CompletableFuture<String> exportCashShiftReportCsv(Instant from,
                                                                   Instant to,
                                                                   Long storeLocationId,
                                                                   Long terminalDeviceId,
                                                                   Long cashierUserId) {
            this.cashStoreLocationId = storeLocationId;
            return CompletableFuture.completedFuture("shift_id,variance\n1,-1.00\n");
        }

        @Override
        public void setAccessToken(String accessToken) {
        }
    }
}
