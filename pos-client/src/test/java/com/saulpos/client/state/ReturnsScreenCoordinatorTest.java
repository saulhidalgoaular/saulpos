package com.saulpos.client.state;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.refund.SaleReturnLineResponse;
import com.saulpos.api.refund.SaleReturnLookupLineResponse;
import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
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
import com.saulpos.api.tax.TenderType;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReturnsScreenCoordinatorTest {

    private static final Instant NOW = Instant.parse("2026-02-10T12:00:00Z");

    @Test
    void lookupSuccess_shouldStoreReceiptContext() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.lookupResponse = lookupResponse();
        ReturnsScreenCoordinator coordinator = new ReturnsScreenCoordinator(apiClient, Runnable::run);

        coordinator.lookupByReceipt("R-0000501").join();

        assertEquals("R-0000501", coordinator.lookupState().receiptNumber());
        assertEquals("Receipt loaded. 1 lines eligible for return review.", coordinator.returnsMessageProperty().get());
    }

    @Test
    void submitWithoutLookup_shouldReject() {
        ReturnsScreenCoordinator coordinator = new ReturnsScreenCoordinator(new FakePosApiClient(), Runnable::run);

        coordinator.submitReturn(1001L, BigDecimal.ONE, "DAMAGED", TenderType.CASH, null, null).join();

        assertNull(coordinator.submitState());
        assertEquals("Lookup a receipt before submitting a return.", coordinator.returnsMessageProperty().get());
    }

    @Test
    void submitSuccess_shouldStoreReturnResponse() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.lookupResponse = lookupResponse();
        apiClient.submitResponse = returnResponse();
        ReturnsScreenCoordinator coordinator = new ReturnsScreenCoordinator(apiClient, Runnable::run);

        coordinator.lookupByReceipt("R-0000501").join();
        coordinator.submitReturn(1001L, new BigDecimal("1.000"), "damaged", TenderType.CASH, null, "can dent").join();

        assertEquals("RET-SALE-501", coordinator.submitState().returnReference());
        assertEquals("Return submitted. Reference RET-SALE-501 created.", coordinator.returnsMessageProperty().get());
        assertEquals("DAMAGED", apiClient.submitRequest.reasonCode());
    }

    @Test
    void submitManagerApprovalRequired_shouldExposeGuidance() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.lookupResponse = lookupResponse();
        apiClient.submitFailure = new ApiProblemException(403, "POS-4030",
                "return is outside allowed window and requires manager approval");
        ReturnsScreenCoordinator coordinator = new ReturnsScreenCoordinator(apiClient, Runnable::run);

        coordinator.lookupByReceipt("R-0000501").join();
        coordinator.submitReturn(1001L, new BigDecimal("1.000"), "damaged", TenderType.CASH, null, null)
                .handle((ok, ex) -> null)
                .join();

        assertTrue(coordinator.managerApprovalRequiredProperty().get());
        assertTrue(coordinator.returnsMessageProperty().get().contains("Have a manager sign in and retry."));
    }

    private static SaleReturnLookupResponse lookupResponse() {
        return new SaleReturnLookupResponse(
                501L,
                "R-0000501",
                10L,
                20L,
                NOW,
                List.of(new SaleReturnLookupLineResponse(
                        1001L,
                        301L,
                        1,
                        new BigDecimal("2.000"),
                        new BigDecimal("1.000"),
                        new BigDecimal("1.000"),
                        new BigDecimal("2.50"),
                        new BigDecimal("5.00")
                ))
        );
    }

    private static SaleReturnResponse returnResponse() {
        return new SaleReturnResponse(
                9001L,
                501L,
                "R-0000501",
                "RET-SALE-501",
                "DAMAGED",
                TenderType.CASH,
                new BigDecimal("2.12"),
                new BigDecimal("0.38"),
                new BigDecimal("2.50"),
                List.of(new SaleReturnLineResponse(
                        1L,
                        1001L,
                        301L,
                        1,
                        new BigDecimal("1.000"),
                        new BigDecimal("2.12"),
                        new BigDecimal("0.38"),
                        new BigDecimal("2.50")
                )),
                NOW
        );
    }

    private static final class FakePosApiClient implements PosApiClient {

        private SaleReturnLookupResponse lookupResponse;
        private SaleReturnResponse submitResponse;
        private SaleReturnSubmitRequest submitRequest;
        private RuntimeException submitFailure;

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
            return CompletableFuture.completedFuture(new CurrentUserResponse(1L, "cashier", Set.of("CASHIER")));
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
            return CompletableFuture.completedFuture(lookupResponse);
        }

        @Override
        public CompletableFuture<SaleReturnResponse> submitReturn(SaleReturnSubmitRequest request) {
            this.submitRequest = request;
            if (submitFailure != null) {
                return CompletableFuture.failedFuture(submitFailure);
            }
            return CompletableFuture.completedFuture(submitResponse);
        }

        @Override
        public void setAccessToken(String accessToken) {
        }
    }
}
