package com.saulpos.client.state;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashMovementType;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.api.shift.CashShiftStatus;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShiftControlCoordinatorTest {

    @Test
    void openShiftSuccess_shouldStoreShiftAndExposeReadyMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.openShiftResponse = openShift(20L, BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO);
        ShiftControlCoordinator coordinator = new ShiftControlCoordinator(apiClient, Runnable::run);

        coordinator.openShift(1L, 11L, BigDecimal.valueOf(100)).join();

        assertEquals(20L, coordinator.shiftState().id());
        assertEquals("Shift open and ready for cash controls.", coordinator.shiftMessageProperty().get());
    }

    @Test
    void paidInSuccess_shouldRefreshTotals() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.openShiftResponse = openShift(20L, BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO);
        apiClient.cashMovementResponse = new CashMovementResponse(
                77L,
                20L,
                CashMovementType.PAID_IN,
                BigDecimal.valueOf(25),
                "Petty cash",
                Instant.parse("2026-02-10T12:10:00Z")
        );
        apiClient.shiftByIdResponse = openShift(20L, BigDecimal.valueOf(100), BigDecimal.valueOf(25), BigDecimal.ZERO);

        ShiftControlCoordinator coordinator = new ShiftControlCoordinator(apiClient, Runnable::run);
        coordinator.openShift(1L, 11L, BigDecimal.valueOf(100)).join();

        coordinator.recordPaidIn(BigDecimal.valueOf(25), "Petty cash").join();

        assertEquals(BigDecimal.valueOf(25), coordinator.shiftState().totalPaidIn());
        assertEquals("Paid-in recorded.", coordinator.shiftMessageProperty().get());
    }

    @Test
    void closeWithoutShift_shouldReject() {
        ShiftControlCoordinator coordinator = new ShiftControlCoordinator(new FakePosApiClient(), Runnable::run);

        coordinator.closeShift(BigDecimal.TEN, "close").join();

        assertNull(coordinator.shiftState());
        assertEquals("Open or load a shift before closing.", coordinator.shiftMessageProperty().get());
    }

    @Test
    void openShiftProblem_shouldShowApiDetail() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.openShiftFailure = new ApiProblemException(409, "POS-4092", "cashier already has an open shift");
        ShiftControlCoordinator coordinator = new ShiftControlCoordinator(apiClient, Runnable::run);

        coordinator.openShift(1L, 11L, BigDecimal.valueOf(100)).handle((ok, ex) -> null).join();

        assertEquals("cashier already has an open shift", coordinator.shiftMessageProperty().get());
    }

    private static CashShiftResponse openShift(Long shiftId,
                                               BigDecimal opening,
                                               BigDecimal paidIn,
                                               BigDecimal paidOut) {
        return new CashShiftResponse(
                shiftId,
                1L,
                11L,
                100L,
                CashShiftStatus.OPEN,
                opening,
                paidIn,
                paidOut,
                opening.add(paidIn).subtract(paidOut),
                null,
                null,
                Instant.parse("2026-02-10T12:00:00Z"),
                null
        );
    }

    private static final class FakePosApiClient implements PosApiClient {

        private CashShiftResponse openShiftResponse;
        private CashShiftResponse shiftByIdResponse;
        private CashMovementResponse cashMovementResponse;
        private RuntimeException openShiftFailure;

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
            if (openShiftFailure != null) {
                return CompletableFuture.failedFuture(openShiftFailure);
            }
            return CompletableFuture.completedFuture(openShiftResponse);
        }

        @Override
        public CompletableFuture<CashMovementResponse> addCashMovement(Long shiftId, CashMovementRequest request) {
            return CompletableFuture.completedFuture(cashMovementResponse);
        }

        @Override
        public CompletableFuture<CashShiftResponse> closeShift(Long shiftId, CashShiftCloseRequest request) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<CashShiftResponse> getShift(Long shiftId) {
            return CompletableFuture.completedFuture(shiftByIdResponse);
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
        public void setAccessToken(String accessToken) {
        }
    }
}
