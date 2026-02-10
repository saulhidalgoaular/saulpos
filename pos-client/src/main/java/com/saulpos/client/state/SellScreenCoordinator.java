package com.saulpos.client.state;

import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class SellScreenCoordinator {

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final PosApiClient apiClient;
    private final Clock clock;
    private final Consumer<Runnable> uiDispatcher;
    private final ObjectProperty<SaleCartResponse> cartState = new SimpleObjectProperty<>();
    private final ObjectProperty<ProductSearchResponse> searchState = new SimpleObjectProperty<>();
    private final StringProperty sellMessage = new SimpleStringProperty("Create or load a cart to begin selling.");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    public SellScreenCoordinator(PosApiClient apiClient) {
        this(apiClient, Clock.systemUTC(), Platform::runLater);
    }

    SellScreenCoordinator(PosApiClient apiClient, Clock clock, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.clock = clock;
        this.uiDispatcher = uiDispatcher;
    }

    public ObjectProperty<SaleCartResponse> cartStateProperty() {
        return cartState;
    }

    public SaleCartResponse cartState() {
        return cartState.get();
    }

    public ObjectProperty<ProductSearchResponse> searchStateProperty() {
        return searchState;
    }

    public StringProperty sellMessageProperty() {
        return sellMessage;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public CompletableFuture<Void> createCart(Long cashierUserId, Long storeLocationId, Long terminalDeviceId) {
        if (cashierUserId == null || storeLocationId == null || terminalDeviceId == null) {
            dispatch(() -> sellMessage.set("Cashier, store, and terminal are required to create cart."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.createCart(new SaleCartCreateRequest(cashierUserId, storeLocationId, terminalDeviceId, Instant.now(clock)))
                .thenAccept(cart -> acceptCart(cart, "Cart created and ready for scanning."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> loadCart(Long cartId) {
        if (cartId == null) {
            dispatch(() -> sellMessage.set("Cart ID must be numeric."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.getCart(cartId)
                .thenAccept(cart -> acceptCart(cart, "Cart loaded."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> scanBarcode(Long merchantId, String barcode, BigDecimal quantity) {
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before scanning."));
            return CompletableFuture.completedFuture(null);
        }
        if (merchantId == null || barcode == null || barcode.isBlank()) {
            dispatch(() -> sellMessage.set("Merchant ID and barcode are required."));
            return CompletableFuture.completedFuture(null);
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            dispatch(() -> sellMessage.set("Scan quantity must be greater than zero."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.lookupProductByBarcode(merchantId, barcode)
                .thenCompose(product -> apiClient.addCartLine(requireCartId(), toAddLineRequest(product, quantity)))
                .thenAccept(cart -> acceptCart(cart, "Scanned item added to cart."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> searchProducts(Long merchantId, String query, int page) {
        if (merchantId == null || query == null || query.isBlank()) {
            dispatch(() -> sellMessage.set("Merchant ID and search query are required."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.searchProducts(merchantId, query, true, Math.max(0, page), DEFAULT_PAGE_SIZE)
                .thenAccept(result -> dispatch(() -> {
                    searchState.set(result);
                    sellMessage.set("Search returned " + result.items().size() + " products.");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> quickAddProduct(Long productId, BigDecimal quantity) {
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before adding products."));
            return CompletableFuture.completedFuture(null);
        }
        if (productId == null) {
            dispatch(() -> sellMessage.set("Select a product to add."));
            return CompletableFuture.completedFuture(null);
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            dispatch(() -> sellMessage.set("Quantity must be greater than zero."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.addCartLine(requireCartId(), new SaleCartAddLineRequest(null, productId, quantity, null, null))
                .thenAccept(cart -> acceptCart(cart, "Product added to cart."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> updateLineQuantity(Long lineId, BigDecimal quantity) {
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before editing lines."));
            return CompletableFuture.completedFuture(null);
        }
        if (lineId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            dispatch(() -> sellMessage.set("Line ID and quantity > 0 are required for update."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.updateCartLine(requireCartId(), lineId, new SaleCartUpdateLineRequest(quantity, null, null))
                .thenAccept(cart -> acceptCart(cart, "Cart line updated."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> removeLine(Long lineId) {
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before removing lines."));
            return CompletableFuture.completedFuture(null);
        }
        if (lineId == null) {
            dispatch(() -> sellMessage.set("Line ID is required for removal."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.removeCartLine(requireCartId(), lineId)
                .thenAccept(cart -> acceptCart(cart, "Cart line removed."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> recalculate() {
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before recalculation."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.recalculateCart(requireCartId())
                .thenAccept(cart -> acceptCart(cart, "Cart totals recalculated."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    private SaleCartAddLineRequest toAddLineRequest(ProductLookupResponse product, BigDecimal quantity) {
        String lineKey = "scan-" + product.productId() + "-" + System.nanoTime();
        return new SaleCartAddLineRequest(lineKey, product.productId(), quantity, null, null);
    }

    private boolean hasActiveCart() {
        return cartState.get() != null && cartState.get().status() == SaleCartStatus.ACTIVE;
    }

    private Long requireCartId() {
        return cartState.get().id();
    }

    private void acceptCart(SaleCartResponse cart, String message) {
        dispatch(() -> {
            cartState.set(cart);
            sellMessage.set(message);
        });
    }

    private void finish(Throwable throwable) {
        if (throwable != null) {
            dispatch(() -> sellMessage.set(mapErrorMessage(throwable)));
        }
        dispatch(() -> busy.set(false));
    }

    private String mapErrorMessage(Throwable throwable) {
        Throwable root = throwable;
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            root = completionException.getCause();
        }
        if (root instanceof ApiProblemException problem) {
            if (problem.getMessage() != null && !problem.getMessage().isBlank()) {
                return problem.getMessage();
            }
            if (problem.code() != null) {
                return "Request failed: " + problem.code();
            }
        }
        return "Sell operation failed. Verify inputs and connectivity.";
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
