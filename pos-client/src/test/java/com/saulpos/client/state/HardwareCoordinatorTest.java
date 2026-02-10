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
import com.saulpos.api.receipt.CashDrawerOpenRequest;
import com.saulpos.api.receipt.CashDrawerOpenResponse;
import com.saulpos.api.receipt.CashDrawerOpenStatus;
import com.saulpos.api.receipt.ReceiptJournalResponse;
import com.saulpos.api.receipt.ReceiptPrintRequest;
import com.saulpos.api.receipt.ReceiptPrintResponse;
import com.saulpos.api.receipt.ReceiptPrintStatus;
import com.saulpos.api.receipt.ReceiptReprintRequest;
import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.security.CurrentUserPermissionsResponse;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HardwareCoordinatorTest {

    @Test
    void refreshPermissionsAndPrint_shouldSetDrawerAuthorizationAndPrintSuccessStatus() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.permissionsResponse = new CurrentUserPermissionsResponse(
                7L,
                "cashier",
                Set.of("CASHIER"),
                Set.of("SALES_PROCESS", "CASH_DRAWER_OPEN", "RECEIPT_REPRINT")
        );
        apiClient.printResponse = new ReceiptPrintResponse(
                "R-0000501",
                ReceiptPrintStatus.SUCCESS,
                "escpos",
                false,
                "Printed",
                Instant.parse("2026-02-10T12:06:00Z")
        );
        HardwareCoordinator coordinator = new HardwareCoordinator(apiClient, Runnable::run);

        coordinator.refreshPermissions().join();
        coordinator.printReceipt("R-0000501", false).join();

        assertTrue(coordinator.drawerAuthorizedProperty().get());
        assertTrue(coordinator.reprintAuthorizedProperty().get());
        assertEquals(HardwareActionStatus.SUCCESS, coordinator.printStatusProperty().get());
        assertEquals("R-0000501", apiClient.printRequest.receiptNumber());
    }

    @Test
    void lookupReceiptJournalAndReprint_shouldPopulateJournalAndInvokeReprintContract() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.permissionsResponse = new CurrentUserPermissionsResponse(
                7L,
                "cashier",
                Set.of("MANAGER"),
                Set.of("SALES_PROCESS", "RECEIPT_REPRINT")
        );
        apiClient.printResponse = new ReceiptPrintResponse(
                "R-0000701",
                ReceiptPrintStatus.SUCCESS,
                "escpos",
                false,
                "Reprint completed",
                Instant.parse("2026-02-10T13:06:00Z")
        );
        HardwareCoordinator coordinator = new HardwareCoordinator(apiClient, Runnable::run);

        coordinator.refreshPermissions().join();
        coordinator.lookupReceiptJournalByNumber("R-0000701").join();
        coordinator.reprintReceipt("R-0000701").join();

        assertEquals("R-0000701", coordinator.receiptJournalProperty().get().receiptNumber());
        assertEquals("R-0000701", apiClient.reprintRequest.receiptNumber());
        assertEquals(HardwareActionStatus.SUCCESS, coordinator.printStatusProperty().get());
    }

    @Test
    void openDrawerWithoutPermission_shouldBlockRequest() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.permissionsResponse = new CurrentUserPermissionsResponse(
                7L,
                "cashier",
                Set.of("CASHIER"),
                Set.of("SALES_PROCESS")
        );
        HardwareCoordinator coordinator = new HardwareCoordinator(apiClient, Runnable::run);

        coordinator.refreshPermissions().join();
        coordinator.openDrawer(3L, "NO_SALE", null, null).join();

        assertFalse(coordinator.drawerAuthorizedProperty().get());
        assertEquals(HardwareActionStatus.IDLE, coordinator.drawerStatusProperty().get());
        assertTrue(coordinator.hardwareMessageProperty().get().contains("requires CASH_DRAWER_OPEN"));
    }

    @Test
    void openDrawerFailure_shouldSetFailedStatusAndSurfaceApiMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.permissionsResponse = new CurrentUserPermissionsResponse(
                7L,
                "cashier",
                Set.of("MANAGER"),
                Set.of("SALES_PROCESS", "CASH_DRAWER_OPEN")
        );
        apiClient.drawerFailure = new ApiProblemException(403, "POS-4030", "drawer open permission denied");
        HardwareCoordinator coordinator = new HardwareCoordinator(apiClient, Runnable::run);

        coordinator.refreshPermissions().join();
        coordinator.openDrawer(3L, "NO_SALE", "manual open", null)
                .handle((ok, ex) -> null)
                .join();

        assertEquals(HardwareActionStatus.FAILED, coordinator.drawerStatusProperty().get());
        assertEquals("drawer open permission denied", coordinator.hardwareMessageProperty().get());
    }

    private static final class FakePosApiClient implements PosApiClient {

        private CurrentUserPermissionsResponse permissionsResponse;
        private ReceiptPrintResponse printResponse;
        private CashDrawerOpenResponse drawerResponse = new CashDrawerOpenResponse(
                9001L,
                3L,
                "TERM-3",
                CashDrawerOpenStatus.SUCCESS,
                "escpos",
                false,
                "Drawer pulse sent",
                Instant.parse("2026-02-10T12:07:00Z")
        );
        private ReceiptPrintRequest printRequest;
        private ReceiptReprintRequest reprintRequest;
        private CashDrawerOpenRequest drawerRequest;
        private RuntimeException drawerFailure;

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
            return CompletableFuture.completedFuture(new CurrentUserResponse(7L, "cashier", Set.of("CASHIER")));
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
        public CompletableFuture<CurrentUserPermissionsResponse> currentUserPermissions() {
            return CompletableFuture.completedFuture(permissionsResponse);
        }

        @Override
        public CompletableFuture<ReceiptPrintResponse> printReceipt(ReceiptPrintRequest request) {
            this.printRequest = request;
            return CompletableFuture.completedFuture(printResponse);
        }

        @Override
        public CompletableFuture<ReceiptPrintResponse> reprintReceipt(ReceiptReprintRequest request) {
            this.reprintRequest = request;
            return CompletableFuture.completedFuture(printResponse);
        }

        @Override
        public CompletableFuture<ReceiptJournalResponse> getReceiptJournalBySaleId(Long saleId) {
            return CompletableFuture.completedFuture(new ReceiptJournalResponse(
                    saleId,
                    201L,
                    "R-0000701",
                    1L,
                    "STORE-1",
                    3L,
                    "TERM-3",
                    7L,
                    "cashier",
                    java.math.BigDecimal.valueOf(25.40),
                    Instant.parse("2026-02-10T13:00:00Z")
            ));
        }

        @Override
        public CompletableFuture<ReceiptJournalResponse> getReceiptJournalByNumber(String receiptNumber) {
            return CompletableFuture.completedFuture(new ReceiptJournalResponse(
                    701L,
                    201L,
                    receiptNumber,
                    1L,
                    "STORE-1",
                    3L,
                    "TERM-3",
                    7L,
                    "cashier",
                    java.math.BigDecimal.valueOf(25.40),
                    Instant.parse("2026-02-10T13:00:00Z")
            ));
        }

        @Override
        public CompletableFuture<CashDrawerOpenResponse> openCashDrawer(CashDrawerOpenRequest request) {
            this.drawerRequest = request;
            if (drawerFailure != null) {
                return CompletableFuture.failedFuture(drawerFailure);
            }
            return CompletableFuture.completedFuture(drawerResponse);
        }

        @Override
        public void setAccessToken(String accessToken) {
        }
    }
}
