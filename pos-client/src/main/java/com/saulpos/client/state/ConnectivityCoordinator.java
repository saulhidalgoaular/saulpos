package com.saulpos.client.state;

import com.saulpos.api.system.OfflineMode;
import com.saulpos.api.system.OfflineOperationPolicyResponse;
import com.saulpos.api.system.OfflinePolicyResponse;
import com.saulpos.client.api.PosApiClient;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ConnectivityCoordinator {

    public static final String AUTH_LOGIN = "AUTH_LOGIN";
    public static final String CART_MUTATION = "CART_MUTATION";
    public static final String CHECKOUT = "CHECKOUT";

    private final PosApiClient apiClient;
    private final Consumer<Runnable> uiDispatcher;
    private final BooleanProperty online = new SimpleBooleanProperty(true);
    private final BooleanProperty checking = new SimpleBooleanProperty(false);
    private final StringProperty connectivityMessage =
            new SimpleStringProperty("Connectivity online. Transactional actions are available.");
    private final ObjectProperty<OfflinePolicyResponse> offlinePolicy = new SimpleObjectProperty<>();

    public ConnectivityCoordinator(PosApiClient apiClient) {
        this(apiClient, Platform::runLater);
    }

    ConnectivityCoordinator(PosApiClient apiClient, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.uiDispatcher = uiDispatcher;
    }

    public BooleanProperty onlineProperty() {
        return online;
    }

    public boolean isOnline() {
        return online.get();
    }

    public BooleanProperty checkingProperty() {
        return checking;
    }

    public StringProperty connectivityMessageProperty() {
        return connectivityMessage;
    }

    public CompletableFuture<Void> refresh() {
        dispatch(() -> checking.set(true));
        return apiClient.ping()
                .thenCompose(reachable -> {
                    if (!reachable) {
                        dispatch(() -> {
                            online.set(false);
                            connectivityMessage.set(buildOfflineConnectivityMessage());
                        });
                        return CompletableFuture.completedFuture(null);
                    }
                    return apiClient.offlinePolicy()
                            .thenAccept(policy -> dispatch(() -> {
                                offlinePolicy.set(policy);
                                online.set(true);
                                connectivityMessage.set("Connectivity online. Transactional actions are available.");
                            }))
                            .exceptionally(throwable -> {
                                dispatch(() -> {
                                    online.set(true);
                                    connectivityMessage.set("Connectivity online. Policy details unavailable.");
                                });
                                return null;
                            });
                })
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> {
                            online.set(false);
                            connectivityMessage.set(buildOfflineConnectivityMessage());
                        });
                    }
                    dispatch(() -> checking.set(false));
                });
    }

    public boolean isOperationBlocked(String operation) {
        if (isOnline()) {
            return false;
        }
        OfflineOperationPolicyResponse operationPolicy = operationPolicy(operation);
        if (operationPolicy == null) {
            return true;
        }
        return operationPolicy.mode() == OfflineMode.ONLINE_ONLY;
    }

    public String blockedMessage(String operation, String fallback) {
        OfflineOperationPolicyResponse operationPolicy = operationPolicy(operation);
        if (operationPolicy != null && operationPolicy.userMessage() != null && !operationPolicy.userMessage().isBlank()) {
            return operationPolicy.userMessage();
        }
        return fallback;
    }

    private OfflineOperationPolicyResponse operationPolicy(String operation) {
        OfflinePolicyResponse policy = offlinePolicy.get();
        if (policy == null || policy.operations() == null) {
            return null;
        }
        return policy.operations().stream()
                .filter(candidate -> operation.equals(candidate.operation()))
                .findFirst()
                .orElse(null);
    }

    private String buildOfflineConnectivityMessage() {
        OfflinePolicyResponse policy = offlinePolicy.get();
        if (policy != null && policy.connectivityExpectation() != null && !policy.connectivityExpectation().isBlank()) {
            return "Connectivity offline. " + policy.connectivityExpectation();
        }
        return "Connectivity offline. Reconnect to continue transactional actions.";
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
