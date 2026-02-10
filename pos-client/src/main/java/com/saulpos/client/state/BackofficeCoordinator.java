package com.saulpos.client.state;

import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.ProductRequest;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.catalog.ProductUnitOfMeasure;
import com.saulpos.api.catalog.ProductVariantRequest;
import com.saulpos.api.customer.CustomerContactRequest;
import com.saulpos.api.customer.CustomerContactType;
import com.saulpos.api.customer.CustomerRequest;
import com.saulpos.api.customer.CustomerResponse;
import com.saulpos.api.customer.CustomerTaxIdentityRequest;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class BackofficeCoordinator {

    private final PosApiClient apiClient;
    private final Consumer<Runnable> uiDispatcher;
    private final ObjectProperty<List<ProductResponse>> products = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<List<CustomerResponse>> customers = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<PriceResolutionResponse> priceResolution = new SimpleObjectProperty<>();
    private final StringProperty backofficeMessage =
            new SimpleStringProperty("Backoffice ready: manage catalog, pricing, and customers.");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    public BackofficeCoordinator(PosApiClient apiClient) {
        this(apiClient, Platform::runLater);
    }

    BackofficeCoordinator(PosApiClient apiClient, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.uiDispatcher = uiDispatcher;
    }

    public ObjectProperty<List<ProductResponse>> productsProperty() {
        return products;
    }

    public ObjectProperty<List<CustomerResponse>> customersProperty() {
        return customers;
    }

    public ObjectProperty<PriceResolutionResponse> priceResolutionProperty() {
        return priceResolution;
    }

    public StringProperty backofficeMessageProperty() {
        return backofficeMessage;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public CompletableFuture<Void> loadProducts(Long merchantId, String query) {
        if (merchantId == null) {
            dispatch(() -> backofficeMessage.set("Merchant ID is required to load catalog products."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        return apiClient.listProducts(merchantId, true, normalizeOptional(query))
                .thenAccept(result -> dispatch(() -> {
                    products.set(result);
                    backofficeMessage.set("Catalog loaded with " + result.size() + " product(s).");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> saveProduct(Long productId,
                                               Long merchantId,
                                               String sku,
                                               String name,
                                               BigDecimal basePrice,
                                               String barcode) {
        if (merchantId == null || isBlank(sku) || isBlank(name) || basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            dispatch(() -> backofficeMessage.set("Merchant, SKU, name, non-negative base price, and barcode are required."));
            return CompletableFuture.completedFuture(null);
        }
        if (isBlank(barcode)) {
            dispatch(() -> backofficeMessage.set("Merchant, SKU, name, non-negative base price, and barcode are required."));
            return CompletableFuture.completedFuture(null);
        }
        ProductRequest request = new ProductRequest(
                merchantId,
                null,
                null,
                sku.trim(),
                name.trim(),
                basePrice,
                ProductSaleMode.UNIT,
                ProductUnitOfMeasure.UNIT,
                0,
                null,
                null,
                false,
                false,
                null,
                List.of(new ProductVariantRequest(
                        sku.trim(),
                        name.trim(),
                        toBarcodeSet(barcode)
                ))
        );
        dispatch(() -> busy.set(true));
        CompletableFuture<ProductResponse> call = productId == null
                ? apiClient.createProduct(request)
                : apiClient.updateProduct(productId, request);
        return call.thenAccept(saved -> dispatch(() -> {
                    backofficeMessage.set((productId == null ? "Product created: " : "Product updated: ") + saved.sku());
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> loadCustomers(Long merchantId) {
        if (merchantId == null) {
            dispatch(() -> backofficeMessage.set("Merchant ID is required to load customers."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        return apiClient.listCustomers(merchantId, true)
                .thenAccept(result -> dispatch(() -> {
                    customers.set(result);
                    backofficeMessage.set("Customer list loaded with " + result.size() + " record(s).");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> lookupCustomers(Long merchantId,
                                                   String documentType,
                                                   String documentValue,
                                                   String email,
                                                   String phone) {
        if (merchantId == null) {
            dispatch(() -> backofficeMessage.set("Merchant ID is required for customer lookup."));
            return CompletableFuture.completedFuture(null);
        }
        if (isBlank(documentType) && isBlank(documentValue) && isBlank(email) && isBlank(phone)) {
            dispatch(() -> backofficeMessage.set("Provide document, email, or phone criteria for lookup."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        return apiClient.lookupCustomers(
                        merchantId,
                        normalizeOptional(documentType),
                        normalizeOptional(documentValue),
                        normalizeOptional(email),
                        normalizeOptional(phone)
                )
                .thenAccept(result -> dispatch(() -> {
                    customers.set(result);
                    backofficeMessage.set("Customer lookup returned " + result.size() + " record(s).");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> saveCustomer(Long customerId,
                                                Long merchantId,
                                                String displayName,
                                                Boolean invoiceRequired,
                                                Boolean creditEnabled,
                                                String documentType,
                                                String documentValue,
                                                String email,
                                                String phone) {
        if (merchantId == null) {
            dispatch(() -> backofficeMessage.set("Merchant ID is required for customer save."));
            return CompletableFuture.completedFuture(null);
        }
        if (isBlank(documentType) ^ isBlank(documentValue)) {
            dispatch(() -> backofficeMessage.set("Document type and value must both be provided when setting tax identity."));
            return CompletableFuture.completedFuture(null);
        }
        List<CustomerTaxIdentityRequest> taxIdentities = toTaxIdentities(documentType, documentValue);
        List<CustomerContactRequest> contacts = toContacts(email, phone);
        CustomerRequest request = new CustomerRequest(
                merchantId,
                normalizeOptional(displayName),
                Boolean.TRUE.equals(invoiceRequired),
                Boolean.TRUE.equals(creditEnabled),
                taxIdentities,
                contacts
        );
        dispatch(() -> busy.set(true));
        CompletableFuture<CustomerResponse> call = customerId == null
                ? apiClient.createCustomer(request)
                : apiClient.updateCustomer(customerId, request);
        return call.thenAccept(saved -> dispatch(() -> {
                    String name = saved.displayName() == null ? "ID " + saved.id() : saved.displayName();
                    backofficeMessage.set((customerId == null ? "Customer created: " : "Customer updated: ") + name);
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> resolveStorePrice(Long storeLocationId, Long productId, Long customerId) {
        if (storeLocationId == null || productId == null) {
            dispatch(() -> backofficeMessage.set("Store location and product ID are required for price resolution."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        return apiClient.resolvePrice(storeLocationId, productId, customerId)
                .thenAccept(response -> dispatch(() -> {
                    priceResolution.set(response);
                    backofficeMessage.set("Resolved price " + response.resolvedPrice() + " from " + response.source() + ".");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    private void finish(Throwable throwable) {
        if (throwable != null) {
            dispatch(() -> backofficeMessage.set(mapErrorMessage(throwable)));
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
        return "Backoffice operation failed. Verify inputs and connectivity.";
    }

    private static List<CustomerTaxIdentityRequest> toTaxIdentities(String documentType, String documentValue) {
        if (isBlank(documentType) && isBlank(documentValue)) {
            return List.of();
        }
        return List.of(new CustomerTaxIdentityRequest(documentType.trim().toUpperCase(Locale.ROOT), documentValue.trim()));
    }

    private static List<CustomerContactRequest> toContacts(String email, String phone) {
        List<CustomerContactRequest> contacts = new java.util.ArrayList<>();
        String normalizedEmail = normalizeOptional(email);
        String normalizedPhone = normalizeOptional(phone);
        if (normalizedEmail != null) {
            contacts.add(new CustomerContactRequest(CustomerContactType.EMAIL, normalizedEmail, normalizedPhone == null));
        }
        if (normalizedPhone != null) {
            contacts.add(new CustomerContactRequest(CustomerContactType.PHONE, normalizedPhone, normalizedEmail == null));
        }
        return List.copyOf(contacts);
    }

    private static Set<String> toBarcodeSet(String barcodeCsv) {
        LinkedHashSet<String> barcodes = new LinkedHashSet<>();
        for (String candidate : barcodeCsv.split(",")) {
            String value = candidate.trim();
            if (!value.isEmpty()) {
                barcodes.add(value);
            }
        }
        return barcodes;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
