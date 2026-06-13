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
import com.saulpos.api.identity.MerchantResponse;
import com.saulpos.api.identity.StoreLocationResponse;
import com.saulpos.api.identity.StoreUserAssignmentResponse;
import com.saulpos.api.identity.TerminalDeviceResponse;
import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.security.PermissionResponse;
import com.saulpos.api.security.RoleRequest;
import com.saulpos.api.security.RoleResponse;
import com.saulpos.api.security.UserAccountCreateRequest;
import com.saulpos.api.security.UserAccountPasswordResetRequest;
import com.saulpos.api.security.UserAccountResponse;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminCoordinatorTest {

    @Test
    void refreshAllShouldLoadUserCatalog() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.users = List.of(user(1L, "manager", true));
        AdminCoordinator coordinator = new AdminCoordinator(apiClient, Runnable::run);

        coordinator.refreshAll().join();

        assertEquals(1, coordinator.usersProperty().get().size());
        assertEquals("Administration data refreshed.", coordinator.adminMessageProperty().get());
    }

    @Test
    void createUserAccountShouldValidateInputsLocally() {
        AdminCoordinator coordinator = new AdminCoordinator(new FakePosApiClient(), Runnable::run);

        coordinator.createUserAccount("", "").join();

        assertEquals("Username and password are required.", coordinator.adminMessageProperty().get());
    }

    @Test
    void createUserAccountShouldUpdateCatalogAndMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        AdminCoordinator coordinator = new AdminCoordinator(apiClient, Runnable::run);

        coordinator.createUserAccount("cashier2", "Pass!123").join();

        assertEquals(1, coordinator.usersProperty().get().size());
        assertEquals("cashier2", coordinator.usersProperty().get().get(0).username());
        assertEquals("User created: cashier2.", coordinator.adminMessageProperty().get());
    }

    @Test
    void setUserActiveShouldUpdateMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.users = new ArrayList<>(List.of(user(7L, "cashier1", true)));
        AdminCoordinator coordinator = new AdminCoordinator(apiClient, Runnable::run);

        coordinator.setUserActive(7L, false).join();

        assertEquals(false, coordinator.usersProperty().get().get(0).active());
        assertEquals("User cashier1 is now INACTIVE.", coordinator.adminMessageProperty().get());
    }

    @Test
    void resetUserPasswordShouldRequireIdAndPassword() {
        AdminCoordinator coordinator = new AdminCoordinator(new FakePosApiClient(), Runnable::run);

        coordinator.resetUserPassword(null, "").join();

        assertEquals("User ID and new password are required.", coordinator.adminMessageProperty().get());
    }

    private static UserAccountResponse user(Long id, String username, boolean active) {
        Instant now = Instant.parse("2026-02-10T12:00:00Z");
        return new UserAccountResponse(id, username, active, 0, null, now, now);
    }

    private static final class FakePosApiClient implements PosApiClient {

        private List<MerchantResponse> merchants = List.of();
        private List<StoreLocationResponse> stores = List.of();
        private List<TerminalDeviceResponse> terminals = List.of();
        private List<StoreUserAssignmentResponse> assignments = List.of();
        private List<RoleResponse> roles = List.of(new RoleResponse(5L, "MANAGER", "Manager", Set.of("CONFIGURATION_MANAGE")));
        private List<PermissionResponse> permissions = List.of(new PermissionResponse(1L, "CONFIGURATION_MANAGE", "Config"));
        private List<UserAccountResponse> users = new ArrayList<>();

        @Override
        public CompletableFuture<Boolean> ping() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<AuthTokenResponse> login(String username, String password) {
            return unsupported();
        }

        @Override
        public CompletableFuture<AuthTokenResponse> refresh(String refreshToken) {
            return unsupported();
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
            return unsupported();
        }

        @Override
        public CompletableFuture<CashMovementResponse> addCashMovement(Long shiftId, CashMovementRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<CashShiftResponse> closeShift(Long shiftId, CashShiftCloseRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<CashShiftResponse> getShift(Long shiftId) {
            return unsupported();
        }

        @Override
        public CompletableFuture<ProductLookupResponse> lookupProductByBarcode(Long merchantId, String barcode) {
            return unsupported();
        }

        @Override
        public CompletableFuture<ProductSearchResponse> searchProducts(Long merchantId, String query, Boolean active, int page, int size) {
            return unsupported();
        }

        @Override
        public CompletableFuture<List<ProductResponse>> listProducts(Long merchantId, Boolean active, String query) {
            return unsupported();
        }

        @Override
        public CompletableFuture<ProductResponse> createProduct(ProductRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<ProductResponse> updateProduct(Long productId, ProductRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<PriceResolutionResponse> resolvePrice(Long storeLocationId, Long productId, Long customerId) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleCartResponse> createCart(SaleCartCreateRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleCartResponse> getCart(Long cartId) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleCartResponse> addCartLine(Long cartId, SaleCartAddLineRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleCartResponse> updateCartLine(Long cartId, Long lineId, SaleCartUpdateLineRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleCartResponse> removeCartLine(Long cartId, Long lineId) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleCartResponse> recalculateCart(Long cartId) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleCheckoutResponse> checkout(SaleCheckoutRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleReturnLookupResponse> lookupReturnByReceipt(String receiptNumber) {
            return unsupported();
        }

        @Override
        public CompletableFuture<SaleReturnResponse> submitReturn(SaleReturnSubmitRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<List<CustomerResponse>> listCustomers(Long merchantId, Boolean active) {
            return unsupported();
        }

        @Override
        public CompletableFuture<List<CustomerResponse>> lookupCustomers(Long merchantId,
                                                                         String documentType,
                                                                         String documentValue,
                                                                         String email,
                                                                         String phone) {
            return unsupported();
        }

        @Override
        public CompletableFuture<CustomerResponse> createCustomer(CustomerRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request) {
            return unsupported();
        }

        @Override
        public CompletableFuture<List<MerchantResponse>> listMerchants() {
            return CompletableFuture.completedFuture(merchants);
        }

        @Override
        public CompletableFuture<List<StoreLocationResponse>> listStoreLocations() {
            return CompletableFuture.completedFuture(stores);
        }

        @Override
        public CompletableFuture<List<TerminalDeviceResponse>> listTerminalDevices() {
            return CompletableFuture.completedFuture(terminals);
        }

        @Override
        public CompletableFuture<List<StoreUserAssignmentResponse>> listStoreUserAssignments() {
            return CompletableFuture.completedFuture(assignments);
        }

        @Override
        public CompletableFuture<List<RoleResponse>> listRoles() {
            return CompletableFuture.completedFuture(roles);
        }

        @Override
        public CompletableFuture<List<PermissionResponse>> permissionCatalog() {
            return CompletableFuture.completedFuture(permissions);
        }

        @Override
        public CompletableFuture<List<UserAccountResponse>> listUserAccounts() {
            return CompletableFuture.completedFuture(List.copyOf(users));
        }

        @Override
        public CompletableFuture<UserAccountResponse> createUserAccount(UserAccountCreateRequest request) {
            UserAccountResponse created = user((long) users.size() + 1, request.username(), true);
            users = new ArrayList<>(users);
            users.add(created);
            return CompletableFuture.completedFuture(created);
        }

        @Override
        public CompletableFuture<UserAccountResponse> activateUserAccount(Long userId) {
            return CompletableFuture.completedFuture(updateUserActive(userId, true));
        }

        @Override
        public CompletableFuture<UserAccountResponse> deactivateUserAccount(Long userId) {
            return CompletableFuture.completedFuture(updateUserActive(userId, false));
        }

        @Override
        public CompletableFuture<UserAccountResponse> resetUserAccountPassword(Long userId,
                                                                               UserAccountPasswordResetRequest request) {
            return CompletableFuture.completedFuture(users.stream()
                    .filter(user -> user.id().equals(userId))
                    .findFirst()
                    .orElse(user(userId, "unknown", true)));
        }

        @Override
        public CompletableFuture<RoleResponse> createRole(RoleRequest request) {
            return unsupported();
        }

        @Override
        public void setAccessToken(String accessToken) {
        }

        private UserAccountResponse updateUserActive(Long userId, boolean active) {
            List<UserAccountResponse> updated = new ArrayList<>();
            UserAccountResponse changed = null;
            for (UserAccountResponse user : users) {
                if (user.id().equals(userId)) {
                    changed = new UserAccountResponse(
                            user.id(),
                            user.username(),
                            active,
                            user.failedAttempts(),
                            user.lockedUntil(),
                            user.createdAt(),
                            Instant.parse("2026-02-10T12:30:00Z"));
                    updated.add(changed);
                } else {
                    updated.add(user);
                }
            }
            users = updated;
            return changed == null ? user(userId, "unknown", active) : changed;
        }

        private static <T> CompletableFuture<T> unsupported() {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }
    }
}
