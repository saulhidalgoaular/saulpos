package com.saulpos.client.state;

import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.sale.ParkedSaleCartSummaryResponse;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartParkRequest;
import com.saulpos.api.sale.SaleCartPriceOverrideRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartResumeRequest;
import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.SaleCartVoidLineRequest;
import com.saulpos.api.sale.SaleCheckoutPaymentRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.tax.TenderType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class SellScreenCoordinator {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final String SALES_PROCESS_PERMISSION = "SALES_PROCESS";
    private static final String DISCOUNT_OVERRIDE_PERMISSION = "DISCOUNT_OVERRIDE";

    private final PosApiClient apiClient;
    private final ConnectivityCoordinator connectivityCoordinator;
    private final Clock clock;
    private final Consumer<Runnable> uiDispatcher;
    private final ObjectProperty<SaleCartResponse> cartState = new SimpleObjectProperty<>();
    private final ObjectProperty<ProductSearchResponse> searchState = new SimpleObjectProperty<>();
    private final ObjectProperty<List<ParkedSaleCartSummaryResponse>> parkedCartsState = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<SaleCheckoutResponse> checkoutState = new SimpleObjectProperty<>();
    private final StringProperty sellMessage = new SimpleStringProperty("Create or load a cart to begin selling.");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private final BooleanProperty parkAuthorized = new SimpleBooleanProperty(false);
    private final BooleanProperty overrideAuthorized = new SimpleBooleanProperty(false);

    public SellScreenCoordinator(PosApiClient apiClient) {
        this(apiClient, null, Clock.systemUTC(), Platform::runLater);
    }

    public SellScreenCoordinator(PosApiClient apiClient, ConnectivityCoordinator connectivityCoordinator) {
        this(apiClient, connectivityCoordinator, Clock.systemUTC(), Platform::runLater);
    }

    SellScreenCoordinator(PosApiClient apiClient, Clock clock, Consumer<Runnable> uiDispatcher) {
        this(apiClient, null, clock, uiDispatcher);
    }

    SellScreenCoordinator(PosApiClient apiClient,
                          ConnectivityCoordinator connectivityCoordinator,
                          Clock clock,
                          Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.connectivityCoordinator = connectivityCoordinator;
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

    public ObjectProperty<SaleCheckoutResponse> checkoutStateProperty() {
        return checkoutState;
    }

    public ObjectProperty<List<ParkedSaleCartSummaryResponse>> parkedCartsStateProperty() {
        return parkedCartsState;
    }

    public SaleCheckoutResponse checkoutState() {
        return checkoutState.get();
    }

    public StringProperty sellMessageProperty() {
        return sellMessage;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public BooleanProperty parkAuthorizedProperty() {
        return parkAuthorized;
    }

    public BooleanProperty overrideAuthorizedProperty() {
        return overrideAuthorized;
    }

    public CompletableFuture<Void> refreshPermissions() {
        dispatch(() -> busy.set(true));
        return apiClient.currentUserPermissions()
                .thenAccept(response -> dispatch(() -> {
                    boolean salesProcessAllowed = response.permissions() != null
                            && response.permissions().contains(SALES_PROCESS_PERMISSION);
                    boolean discountOverrideAllowed = response.permissions() != null
                            && response.permissions().contains(DISCOUNT_OVERRIDE_PERMISSION);
                    parkAuthorized.set(salesProcessAllowed);
                    overrideAuthorized.set(salesProcessAllowed || discountOverrideAllowed);
                    sellMessage.set(salesProcessAllowed
                            ? "Sell permissions loaded. Park and override controls are available."
                            : "Sell permissions loaded. Park and override controls are restricted for this user.");
                }))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> {
                            parkAuthorized.set(false);
                            overrideAuthorized.set(false);
                            sellMessage.set(mapErrorMessage(throwable));
                        });
                    }
                    dispatch(() -> busy.set(false));
                });
    }

    public CompletableFuture<Void> createCart(Long cashierUserId, Long storeLocationId, Long terminalDeviceId) {
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
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
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
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
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
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
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
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
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
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
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before recalculation."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.recalculateCart(requireCartId())
                .thenAccept(cart -> acceptCart(cart, "Cart totals recalculated."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> parkCurrentCart(Long cashierUserId, Long terminalDeviceId, String note) {
        if (!parkAuthorized.get()) {
            dispatch(() -> sellMessage.set("Parking carts requires SALES_PROCESS permission."));
            return CompletableFuture.completedFuture(null);
        }
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before parking."));
            return CompletableFuture.completedFuture(null);
        }
        if (cashierUserId == null || terminalDeviceId == null) {
            dispatch(() -> sellMessage.set("Cashier and terminal are required to park cart."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.parkCart(requireCartId(), new SaleCartParkRequest(cashierUserId, terminalDeviceId, normalizeReference(note)))
                .thenAccept(cart -> acceptCart(cart, "Cart parked successfully."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> listParkedCarts(Long storeLocationId, Long terminalDeviceId) {
        if (!parkAuthorized.get()) {
            dispatch(() -> sellMessage.set("Listing parked carts requires SALES_PROCESS permission."));
            return CompletableFuture.completedFuture(null);
        }
        if (storeLocationId == null) {
            dispatch(() -> sellMessage.set("Store location ID is required to list parked carts."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.listParkedCarts(storeLocationId, terminalDeviceId)
                .thenAccept(items -> dispatch(() -> {
                    parkedCartsState.set(items == null ? List.of() : items);
                    sellMessage.set("Loaded " + parkedCartsState.get().size() + " parked carts.");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> resumeParkedCart(Long cartId, Long cashierUserId, Long terminalDeviceId) {
        if (!parkAuthorized.get()) {
            dispatch(() -> sellMessage.set("Resuming parked carts requires SALES_PROCESS permission."));
            return CompletableFuture.completedFuture(null);
        }
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
        if (cartId == null || cashierUserId == null || terminalDeviceId == null) {
            dispatch(() -> sellMessage.set("Cart ID, cashier, and terminal are required to resume parked cart."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.resumeCart(cartId, new SaleCartResumeRequest(cashierUserId, terminalDeviceId))
                .thenAccept(cart -> acceptCart(cart, "Parked cart resumed and active."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> voidLine(Long lineId,
                                            Long cashierUserId,
                                            Long terminalDeviceId,
                                            String reasonCode,
                                            String note) {
        if (!overrideAuthorized.get()) {
            dispatch(() -> sellMessage.set("Line void requires override authorization."));
            return CompletableFuture.completedFuture(null);
        }
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before line void."));
            return CompletableFuture.completedFuture(null);
        }
        if (lineId == null || cashierUserId == null || terminalDeviceId == null) {
            dispatch(() -> sellMessage.set("Line ID, cashier, and terminal are required for line void."));
            return CompletableFuture.completedFuture(null);
        }
        String normalizedReason = normalizeReason(reasonCode);
        if (normalizedReason == null) {
            dispatch(() -> sellMessage.set("Void reason code is required."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.voidCartLine(requireCartId(), lineId,
                        new SaleCartVoidLineRequest(cashierUserId, terminalDeviceId, normalizedReason, normalizeReference(note)))
                .thenAccept(cart -> acceptCart(cart, "Cart line voided."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> overrideLinePrice(Long lineId,
                                                     Long cashierUserId,
                                                     Long terminalDeviceId,
                                                     BigDecimal unitPrice,
                                                     String reasonCode,
                                                     String note) {
        if (!overrideAuthorized.get()) {
            dispatch(() -> sellMessage.set("Price override requires override authorization."));
            return CompletableFuture.completedFuture(null);
        }
        if (isBlockedByConnectivity(ConnectivityCoordinator.CART_MUTATION,
                "Cart changes are unavailable offline. Reconnect and try again.")) {
            return CompletableFuture.completedFuture(null);
        }
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before price override."));
            return CompletableFuture.completedFuture(null);
        }
        if (lineId == null || cashierUserId == null || terminalDeviceId == null) {
            dispatch(() -> sellMessage.set("Line ID, cashier, and terminal are required for price override."));
            return CompletableFuture.completedFuture(null);
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            dispatch(() -> sellMessage.set("Override unit price must be non-negative."));
            return CompletableFuture.completedFuture(null);
        }
        String normalizedReason = normalizeReason(reasonCode);
        if (normalizedReason == null) {
            dispatch(() -> sellMessage.set("Override reason code is required."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.overrideCartLinePrice(requireCartId(), lineId,
                        new SaleCartPriceOverrideRequest(
                                cashierUserId,
                                terminalDeviceId,
                                unitPrice,
                                normalizedReason,
                                normalizeReference(note)))
                .thenAccept(cart -> acceptCart(cart, "Cart line price overridden."))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> checkout(Long cashierUserId,
                                            Long terminalDeviceId,
                                            BigDecimal cashAmount,
                                            BigDecimal cashTenderedAmount,
                                            BigDecimal cardAmount,
                                            String cardReference) {
        if (isBlockedByConnectivity(ConnectivityCoordinator.CHECKOUT,
                "Sale cannot be completed offline. Reconnect to finalize payment.")) {
            return CompletableFuture.completedFuture(null);
        }
        if (!hasActiveCart()) {
            dispatch(() -> sellMessage.set("Create or load an ACTIVE cart before checkout."));
            return CompletableFuture.completedFuture(null);
        }
        if (cashierUserId == null || terminalDeviceId == null) {
            dispatch(() -> sellMessage.set("Cashier and terminal are required for checkout."));
            return CompletableFuture.completedFuture(null);
        }

        BigDecimal normalizedCashAmount = normalizeAmount(cashAmount);
        BigDecimal normalizedCardAmount = normalizeAmount(cardAmount);
        if (normalizedCashAmount.compareTo(BigDecimal.ZERO) <= 0 && normalizedCardAmount.compareTo(BigDecimal.ZERO) <= 0) {
            dispatch(() -> sellMessage.set("Enter cash and/or card amount greater than zero."));
            return CompletableFuture.completedFuture(null);
        }

        BigDecimal expectedPayable = normalizeAmount(cartState.get().totalPayable());
        BigDecimal allocatedAmount = normalizedCashAmount.add(normalizedCardAmount);
        if (allocatedAmount.compareTo(expectedPayable) != 0) {
            dispatch(() -> sellMessage.set("Tender allocation must match cart payable total."));
            return CompletableFuture.completedFuture(null);
        }

        BigDecimal normalizedTendered = cashTenderedAmount == null ? normalizedCashAmount : normalizeAmount(cashTenderedAmount);
        if (normalizedCashAmount.compareTo(BigDecimal.ZERO) > 0 && normalizedTendered.compareTo(normalizedCashAmount) < 0) {
            dispatch(() -> sellMessage.set("Tendered cash must be greater than or equal to allocated cash."));
            return CompletableFuture.completedFuture(null);
        }

        List<SaleCheckoutPaymentRequest> payments = new ArrayList<>();
        if (normalizedCashAmount.compareTo(BigDecimal.ZERO) > 0) {
            payments.add(new SaleCheckoutPaymentRequest(TenderType.CASH, normalizedCashAmount, normalizedTendered, null));
        }
        if (normalizedCardAmount.compareTo(BigDecimal.ZERO) > 0) {
            payments.add(new SaleCheckoutPaymentRequest(TenderType.CARD, normalizedCardAmount, null, normalizeReference(cardReference)));
        }

        dispatch(() -> busy.set(true));
        return apiClient.checkout(new SaleCheckoutRequest(requireCartId(), cashierUserId, terminalDeviceId, payments))
                .thenAccept(this::acceptCheckout)
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
            checkoutState.set(null);
            sellMessage.set(message);
        });
    }

    private void acceptCheckout(SaleCheckoutResponse checkout) {
        dispatch(() -> {
            checkoutState.set(checkout);
            sellMessage.set("Checkout completed. Receipt " + checkout.receiptNumber() + " generated.");
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

    private boolean isBlockedByConnectivity(String operation, String fallbackMessage) {
        if (connectivityCoordinator == null || !connectivityCoordinator.isOperationBlocked(operation)) {
            return false;
        }
        dispatch(() -> sellMessage.set(connectivityCoordinator.blockedMessage(operation, fallbackMessage)));
        return true;
    }

    private static BigDecimal normalizeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String normalizeReference(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeReason(String value) {
        String trimmed = normalizeReference(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
