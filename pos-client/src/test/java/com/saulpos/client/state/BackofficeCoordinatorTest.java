package com.saulpos.client.state;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.PriceResolutionSource;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductRequest;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.catalog.ProductUnitOfMeasure;
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
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BackofficeCoordinatorTest {

    @Test
    void loadProductsSuccess_shouldPopulateStateAndMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.products = List.of(product(300L, "SODA-350", "Soda 350ml"));
        BackofficeCoordinator coordinator = new BackofficeCoordinator(apiClient, Runnable::run);

        coordinator.loadProducts(1L, "soda").join();

        assertEquals(1, coordinator.productsProperty().get().size());
        assertEquals("Catalog loaded with 1 product(s).", coordinator.backofficeMessageProperty().get());
    }

    @Test
    void saveCustomerWithPartialTaxIdentity_shouldRejectLocally() {
        BackofficeCoordinator coordinator = new BackofficeCoordinator(new FakePosApiClient(), Runnable::run);

        coordinator.saveCustomer(null, 1L, "Walk-in", false, false, "NIT", null, null, null).join();

        assertEquals("Document type and value must both be provided when setting tax identity.",
                coordinator.backofficeMessageProperty().get());
    }

    @Test
    void resolvePriceSuccess_shouldExposeResolvedSource() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.priceResolution = new PriceResolutionResponse(
                10L,
                300L,
                new BigDecimal("1.25"),
                PriceResolutionSource.STORE_OVERRIDE,
                77L,
                Instant.parse("2026-02-10T00:00:00Z"),
                null,
                Instant.parse("2026-02-10T12:00:00Z")
        );
        BackofficeCoordinator coordinator = new BackofficeCoordinator(apiClient, Runnable::run);

        coordinator.resolveStorePrice(10L, 300L, null).join();

        assertEquals(PriceResolutionSource.STORE_OVERRIDE, coordinator.priceResolutionProperty().get().source());
        assertEquals("Resolved price 1.25 from STORE_OVERRIDE.", coordinator.backofficeMessageProperty().get());
    }

    private static ProductResponse product(Long id, String sku, String name) {
        return new ProductResponse(
                id,
                1L,
                null,
                null,
                sku,
                name,
                new BigDecimal("1.50"),
                ProductSaleMode.UNIT,
                ProductUnitOfMeasure.UNIT,
                0,
                null,
                null,
                false,
                false,
                null,
                true,
                List.of()
        );
    }

    private static final class FakePosApiClient implements PosApiClient {

        private List<ProductResponse> products = List.of();
        private List<CustomerResponse> customers = List.of();
        private PriceResolutionResponse priceResolution;

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
            return CompletableFuture.completedFuture(products);
        }

        @Override
        public CompletableFuture<ProductResponse> createProduct(ProductRequest request) {
            return CompletableFuture.completedFuture(product(401L, request.sku(), request.name()));
        }

        @Override
        public CompletableFuture<ProductResponse> updateProduct(Long productId, ProductRequest request) {
            return CompletableFuture.completedFuture(product(productId, request.sku(), request.name()));
        }

        @Override
        public CompletableFuture<PriceResolutionResponse> resolvePrice(Long storeLocationId, Long productId, Long customerId) {
            return CompletableFuture.completedFuture(priceResolution);
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
            return CompletableFuture.completedFuture(customers);
        }

        @Override
        public CompletableFuture<List<CustomerResponse>> lookupCustomers(Long merchantId,
                                                                         String documentType,
                                                                         String documentValue,
                                                                         String email,
                                                                         String phone) {
            return CompletableFuture.completedFuture(customers);
        }

        @Override
        public CompletableFuture<CustomerResponse> createCustomer(CustomerRequest request) {
            return CompletableFuture.completedFuture(new CustomerResponse(
                    601L, request.merchantId(), request.displayName(), request.invoiceRequired(), request.creditEnabled(),
                    true, List.of(), List.of(), List.of()));
        }

        @Override
        public CompletableFuture<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request) {
            return CompletableFuture.completedFuture(new CustomerResponse(
                    customerId, request.merchantId(), request.displayName(), request.invoiceRequired(), request.creditEnabled(),
                    true, List.of(), List.of(), List.of()));
        }

        @Override
        public void setAccessToken(String accessToken) {
        }
    }
}
