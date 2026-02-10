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
import com.saulpos.api.system.OfflineMode;
import com.saulpos.api.system.OfflineOperationPolicyResponse;
import com.saulpos.api.system.OfflinePolicyResponse;
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectivityCoordinatorTest {

    @Test
    void refreshWhenOnline_shouldLoadPolicyAndEnableOperations() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.reachable = true;
        apiClient.policy = new OfflinePolicyResponse(
                "K1-v1",
                "Server connectivity is required for transactional flows in SaulPOS v2.",
                List.of(new OfflineOperationPolicyResponse(
                        ConnectivityCoordinator.CHECKOUT,
                        OfflineMode.ONLINE_ONLY,
                        "Checkout is blocked while disconnected.",
                        "Sale cannot be completed offline. Reconnect to finalize payment."
                ))
        );
        ConnectivityCoordinator coordinator = new ConnectivityCoordinator(apiClient, Runnable::run);

        coordinator.refresh().join();

        assertTrue(coordinator.isOnline());
        assertEquals("Connectivity online. Transactional actions are available.",
                coordinator.connectivityMessageProperty().get());
        assertFalse(coordinator.isOperationBlocked(ConnectivityCoordinator.CHECKOUT));
    }

    @Test
    void refreshWhenOffline_shouldBlockOnlineOnlyOperationsAndExposePolicyMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.policy = new OfflinePolicyResponse(
                "K1-v1",
                "Server connectivity is required for transactional flows in SaulPOS v2.",
                List.of(new OfflineOperationPolicyResponse(
                        ConnectivityCoordinator.AUTH_LOGIN,
                        OfflineMode.ONLINE_ONLY,
                        "Client blocks login submission while server is unreachable.",
                        "Cannot sign in while offline. Reconnect to continue."
                ))
        );
        ConnectivityCoordinator coordinator = new ConnectivityCoordinator(apiClient, Runnable::run);
        coordinator.refresh().join();
        apiClient.reachable = false;
        coordinator.refresh().join();

        assertFalse(coordinator.isOnline());
        assertTrue(coordinator.isOperationBlocked(ConnectivityCoordinator.AUTH_LOGIN));
        assertEquals("Cannot sign in while offline. Reconnect to continue.",
                coordinator.blockedMessage(ConnectivityCoordinator.AUTH_LOGIN, "fallback"));
    }

    private static final class FakePosApiClient implements PosApiClient {

        private boolean reachable = true;
        private OfflinePolicyResponse policy = new OfflinePolicyResponse("K1-v1", "", List.of());

        @Override
        public CompletableFuture<Boolean> ping() {
            return CompletableFuture.completedFuture(reachable);
        }

        @Override
        public CompletableFuture<OfflinePolicyResponse> offlinePolicy() {
            return CompletableFuture.completedFuture(policy);
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
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Void> logout() {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
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
        public void setAccessToken(String accessToken) {
        }
    }
}
