package com.saulpos.client.state;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.catalog.ProductUnitOfMeasure;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartLineResponse;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SellScreenCoordinatorTest {

    private static final Instant NOW = Instant.parse("2026-02-10T12:00:00Z");

    @Test
    void createCartSuccess_shouldStoreCartAndMessage() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.cart = cart(55L, List.of());
        SellScreenCoordinator coordinator = new SellScreenCoordinator(
                apiClient,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.createCart(10L, 20L, 30L).join();

        assertEquals(55L, coordinator.cartState().id());
        assertEquals("Cart created and ready for scanning.", coordinator.sellMessageProperty().get());
    }

    @Test
    void scanBarcodeWithoutCart_shouldReject() {
        SellScreenCoordinator coordinator = new SellScreenCoordinator(
                new FakePosApiClient(),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.scanBarcode(1L, "7751234", BigDecimal.ONE).join();

        assertNull(coordinator.cartState());
        assertEquals("Create or load an ACTIVE cart before scanning.", coordinator.sellMessageProperty().get());
    }

    @Test
    void searchAndQuickAdd_shouldUpdateSearchStateAndCart() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.searchResponse = new ProductSearchResponse(
                List.of(product(300L, "SODA-350", "Soda 350ml", true)),
                0,
                12,
                1,
                1,
                false,
                false
        );
        apiClient.cart = cart(99L, List.of());
        apiClient.cartAfterAdd = cart(99L, List.of(
                new SaleCartLineResponse(
                        801L,
                        "scan",
                        300L,
                        "SODA-350",
                        "Soda 350ml",
                        ProductSaleMode.UNIT,
                        BigDecimal.ONE,
                        new BigDecimal("1.50"),
                        new BigDecimal("1.50"),
                        BigDecimal.ZERO,
                        new BigDecimal("1.50"),
                        null
                )
        ));

        SellScreenCoordinator coordinator = new SellScreenCoordinator(
                apiClient,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.loadCart(99L).join();
        coordinator.searchProducts(1L, "soda", 0).join();
        coordinator.quickAddProduct(300L, BigDecimal.ONE).join();

        assertEquals(1, coordinator.searchStateProperty().get().items().size());
        assertEquals(1, coordinator.cartState().lines().size());
        assertEquals("Product added to cart.", coordinator.sellMessageProperty().get());
    }

    @Test
    void updateLineApiFailure_shouldExposeApiDetail() {
        FakePosApiClient apiClient = new FakePosApiClient();
        apiClient.cart = cart(99L, List.of());
        apiClient.updateFailure = new ApiProblemException(400, "POS-4010", "quantity must be greater than zero");

        SellScreenCoordinator coordinator = new SellScreenCoordinator(
                apiClient,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run
        );

        coordinator.loadCart(99L).join();
        coordinator.updateLineQuantity(5L, BigDecimal.ONE).handle((ok, ex) -> null).join();

        assertEquals("quantity must be greater than zero", coordinator.sellMessageProperty().get());
    }

    private static ProductResponse product(Long id, String sku, String name, boolean active) {
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
                active,
                List.of()
        );
    }

    private static SaleCartResponse cart(Long cartId, List<SaleCartLineResponse> lines) {
        return new SaleCartResponse(
                cartId,
                10L,
                20L,
                30L,
                SaleCartStatus.ACTIVE,
                NOW,
                lines,
                new BigDecimal("1.50"),
                BigDecimal.ZERO,
                new BigDecimal("1.50"),
                BigDecimal.ZERO,
                new BigDecimal("1.50"),
                null,
                NOW,
                NOW
        );
    }

    private static final class FakePosApiClient implements PosApiClient {

        private SaleCartResponse cart;
        private SaleCartResponse cartAfterAdd;
        private ProductSearchResponse searchResponse;
        private RuntimeException updateFailure;

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
            return CompletableFuture.completedFuture(new CurrentUserResponse(10L, "cashier", Set.of("CASHIER")));
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
            return CompletableFuture.completedFuture(new ProductLookupResponse(
                    300L,
                    null,
                    merchantId,
                    "SODA-350",
                    "Soda 350ml",
                    null,
                    null,
                    barcode,
                    ProductSaleMode.UNIT,
                    ProductUnitOfMeasure.UNIT,
                    0
            ));
        }

        @Override
        public CompletableFuture<ProductSearchResponse> searchProducts(Long merchantId, String query, Boolean active, int page, int size) {
            return CompletableFuture.completedFuture(searchResponse);
        }

        @Override
        public CompletableFuture<SaleCartResponse> createCart(SaleCartCreateRequest request) {
            return CompletableFuture.completedFuture(cart);
        }

        @Override
        public CompletableFuture<SaleCartResponse> getCart(Long cartId) {
            return CompletableFuture.completedFuture(cart);
        }

        @Override
        public CompletableFuture<SaleCartResponse> addCartLine(Long cartId, SaleCartAddLineRequest request) {
            return CompletableFuture.completedFuture(cartAfterAdd == null ? cart : cartAfterAdd);
        }

        @Override
        public CompletableFuture<SaleCartResponse> updateCartLine(Long cartId, Long lineId, SaleCartUpdateLineRequest request) {
            if (updateFailure != null) {
                return CompletableFuture.failedFuture(updateFailure);
            }
            return CompletableFuture.completedFuture(cart);
        }

        @Override
        public CompletableFuture<SaleCartResponse> removeCartLine(Long cartId, Long lineId) {
            return CompletableFuture.completedFuture(cart);
        }

        @Override
        public CompletableFuture<SaleCartResponse> recalculateCart(Long cartId) {
            return CompletableFuture.completedFuture(cart);
        }

        @Override
        public void setAccessToken(String accessToken) {
        }
    }
}
