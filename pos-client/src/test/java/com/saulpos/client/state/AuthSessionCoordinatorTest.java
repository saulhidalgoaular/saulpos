package com.saulpos.client.state;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
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
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import com.saulpos.client.app.NavigationState;
import com.saulpos.client.app.NavigationTarget;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSessionCoordinatorTest {

    private static final Instant NOW = Instant.parse("2026-02-10T12:00:00Z");

    @Test
    void loginSuccess_shouldPopulateSessionAndNavigateToShift() {
        AppStateStore store = new AppStateStore();
        NavigationState navigationState = new NavigationState();
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.loginResponse = new AuthTokenResponse(
                "access-1",
                "refresh-1",
                NOW.plusSeconds(600),
                NOW.plusSeconds(3600),
                Set.of("CASHIER")
        );
        apiClient.currentUserResponse = new CurrentUserResponse(1L, "cashier", Set.of("CASHIER"));

        AuthSessionCoordinator coordinator = new AuthSessionCoordinator(
                apiClient,
                store,
                navigationState,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.login("cashier", "secret").join();

        assertTrue(store.isAuthenticated());
        assertEquals("cashier", store.sessionState().username());
        assertEquals(NavigationTarget.SHIFT_CONTROL, navigationState.activeTarget());
        assertEquals("Session active for cashier.", coordinator.sessionMessageProperty().get());
    }

    @Test
    void loginInvalidCredentials_shouldKeepGuestAndExposeMessage() {
        AppStateStore store = new AppStateStore();
        NavigationState navigationState = new NavigationState();
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.loginFailure = new ApiProblemException(401, "POS-4011", "Invalid username or password");

        AuthSessionCoordinator coordinator = new AuthSessionCoordinator(
                apiClient,
                store,
                navigationState,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.login("cashier", "bad-password").handle((ok, ex) -> null).join();

        assertFalse(store.isAuthenticated());
        assertEquals(NavigationTarget.LOGIN, navigationState.activeTarget());
        assertEquals("Invalid username or password.", coordinator.sessionMessageProperty().get());
    }

    @Test
    void protectedNavigationWithoutSession_shouldRedirectToLogin() {
        AppStateStore store = new AppStateStore();
        NavigationState navigationState = new NavigationState();
        AuthSessionCoordinator coordinator = new AuthSessionCoordinator(
                new FakePosApiClient(),
                store,
                navigationState,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        navigationState.navigate(NavigationTarget.SELL);
        coordinator.onNavigationChanged(NavigationTarget.SELL);

        assertEquals(NavigationTarget.LOGIN, navigationState.activeTarget());
        assertEquals("Authentication is required for this screen.", coordinator.sessionMessageProperty().get());
    }

    @Test
    void expiredSessionNavigation_shouldRefreshWhenRefreshTokenIsValid() {
        AppStateStore store = new AppStateStore();
        NavigationState navigationState = new NavigationState();
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.refreshResponse = new AuthTokenResponse(
                "access-2",
                "refresh-2",
                NOW.plusSeconds(900),
                NOW.plusSeconds(7200),
                Set.of("CASHIER")
        );

        store.updateSession(new AuthSessionState(
                "cashier",
                "access-1",
                "refresh-1",
                Set.of("CASHIER"),
                NOW.minusSeconds(1),
                NOW.plusSeconds(7200)
        ));

        AuthSessionCoordinator coordinator = new AuthSessionCoordinator(
                apiClient,
                store,
                navigationState,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.onNavigationChanged(NavigationTarget.SELL);

        assertEquals("access-2", store.sessionState().accessToken());
        assertEquals("Session refreshed.", coordinator.sessionMessageProperty().get());
    }

    @Test
    void expiredSessionRefreshFailure_shouldClearSessionAndRedirectLogin() {
        AppStateStore store = new AppStateStore();
        NavigationState navigationState = new NavigationState();
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.refreshFailure = new ApiProblemException(401, "POS-4015", "Token expired");

        store.updateSession(new AuthSessionState(
                "cashier",
                "access-1",
                "refresh-1",
                Set.of("CASHIER"),
                NOW.minusSeconds(1),
                NOW.plusSeconds(7200)
        ));
        navigationState.navigate(NavigationTarget.SELL);

        AuthSessionCoordinator coordinator = new AuthSessionCoordinator(
                apiClient,
                store,
                navigationState,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.onNavigationChanged(NavigationTarget.SELL);

        assertFalse(store.isAuthenticated());
        assertEquals(NavigationTarget.LOGIN, navigationState.activeTarget());
        assertEquals("Session expired. Please sign in again.", coordinator.sessionMessageProperty().get());
    }

    private static final class FakePosApiClient implements PosApiClient {

        private AuthTokenResponse loginResponse;
        private AuthTokenResponse refreshResponse;
        private CurrentUserResponse currentUserResponse;
        private RuntimeException loginFailure;
        private RuntimeException refreshFailure;

        @Override
        public CompletableFuture<Boolean> ping() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<AuthTokenResponse> login(String username, String password) {
            if (loginFailure != null) {
                return CompletableFuture.failedFuture(loginFailure);
            }
            return CompletableFuture.completedFuture(loginResponse);
        }

        @Override
        public CompletableFuture<AuthTokenResponse> refresh(String refreshToken) {
            if (refreshFailure != null) {
                return CompletableFuture.failedFuture(refreshFailure);
            }
            return CompletableFuture.completedFuture(refreshResponse);
        }

        @Override
        public CompletableFuture<CurrentUserResponse> currentUser() {
            return CompletableFuture.completedFuture(currentUserResponse);
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
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<SaleReturnResponse> submitReturn(SaleReturnSubmitRequest request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public void setAccessToken(String accessToken) {
        }
    }
}
