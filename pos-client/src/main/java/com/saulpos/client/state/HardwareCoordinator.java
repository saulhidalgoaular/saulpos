package com.saulpos.client.state;

import com.saulpos.api.receipt.CashDrawerOpenRequest;
import com.saulpos.api.receipt.CashDrawerOpenResponse;
import com.saulpos.api.receipt.CashDrawerOpenStatus;
import com.saulpos.api.receipt.ReceiptPrintRequest;
import com.saulpos.api.receipt.ReceiptPrintResponse;
import com.saulpos.api.receipt.ReceiptPrintStatus;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class HardwareCoordinator {

    private static final String DRAWER_PERMISSION = "CASH_DRAWER_OPEN";

    private final PosApiClient apiClient;
    private final Consumer<Runnable> uiDispatcher;
    private final ObjectProperty<HardwareActionStatus> printStatus = new SimpleObjectProperty<>(HardwareActionStatus.IDLE);
    private final ObjectProperty<HardwareActionStatus> drawerStatus = new SimpleObjectProperty<>(HardwareActionStatus.IDLE);
    private final ObjectProperty<ReceiptPrintResponse> printResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<CashDrawerOpenResponse> drawerResponse = new SimpleObjectProperty<>();
    private final BooleanProperty drawerAuthorized = new SimpleBooleanProperty(false);
    private final StringProperty hardwareMessage =
            new SimpleStringProperty("Hardware ready: use receipt print and drawer controls.");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    public HardwareCoordinator(PosApiClient apiClient) {
        this(apiClient, Platform::runLater);
    }

    HardwareCoordinator(PosApiClient apiClient, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.uiDispatcher = uiDispatcher;
    }

    public ObjectProperty<HardwareActionStatus> printStatusProperty() {
        return printStatus;
    }

    public ObjectProperty<HardwareActionStatus> drawerStatusProperty() {
        return drawerStatus;
    }

    public ObjectProperty<ReceiptPrintResponse> printResponseProperty() {
        return printResponse;
    }

    public ObjectProperty<CashDrawerOpenResponse> drawerResponseProperty() {
        return drawerResponse;
    }

    public BooleanProperty drawerAuthorizedProperty() {
        return drawerAuthorized;
    }

    public StringProperty hardwareMessageProperty() {
        return hardwareMessage;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public CompletableFuture<Void> refreshPermissions() {
        dispatch(() -> busy.set(true));
        return apiClient.currentUserPermissions()
                .thenAccept(response -> dispatch(() -> {
                    boolean allowed = response.permissions() != null && response.permissions().contains(DRAWER_PERMISSION);
                    drawerAuthorized.set(allowed);
                    hardwareMessage.set(allowed
                            ? "Hardware permissions loaded. Drawer controls are enabled."
                            : "Hardware permissions loaded. Drawer controls are restricted for this user.");
                }))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> {
                            drawerAuthorized.set(false);
                            hardwareMessage.set(mapError("permission refresh", throwable));
                        });
                    }
                    dispatch(() -> busy.set(false));
                });
    }

    public CompletableFuture<Void> printReceipt(String receiptNumber, boolean copy) {
        if (receiptNumber == null || receiptNumber.isBlank()) {
            dispatch(() -> hardwareMessage.set("Receipt number is required."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> {
            busy.set(true);
            printStatus.set(HardwareActionStatus.QUEUED);
            hardwareMessage.set("Receipt print queued for " + receiptNumber.trim() + ".");
        });

        return apiClient.printReceipt(new ReceiptPrintRequest(receiptNumber.trim(), copy))
                .thenAccept(response -> dispatch(() -> {
                    printResponse.set(response);
                    printStatus.set(response.status() == ReceiptPrintStatus.SUCCESS
                            ? HardwareActionStatus.SUCCESS
                            : HardwareActionStatus.FAILED);
                    hardwareMessage.set(response.message() == null || response.message().isBlank()
                            ? "Receipt print request completed."
                            : response.message());
                }))
                .whenComplete((ignored, throwable) -> finishAction("print", printStatus, throwable));
    }

    public CompletableFuture<Void> openDrawer(Long terminalDeviceId,
                                              String reasonCode,
                                              String note,
                                              String referenceNumber) {
        if (!drawerAuthorized.get()) {
            dispatch(() -> hardwareMessage.set("Drawer open requires CASH_DRAWER_OPEN permission."));
            return CompletableFuture.completedFuture(null);
        }
        if (terminalDeviceId == null) {
            dispatch(() -> hardwareMessage.set("Terminal device ID is required."));
            return CompletableFuture.completedFuture(null);
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            dispatch(() -> hardwareMessage.set("Reason code is required."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> {
            busy.set(true);
            drawerStatus.set(HardwareActionStatus.QUEUED);
            hardwareMessage.set("Drawer open queued for terminal " + terminalDeviceId + ".");
        });

        return apiClient.openCashDrawer(new CashDrawerOpenRequest(
                        terminalDeviceId,
                        reasonCode.trim().toUpperCase(Locale.ROOT),
                        normalizeOptional(note),
                        normalizeOptional(referenceNumber)))
                .thenAccept(response -> dispatch(() -> {
                    drawerResponse.set(response);
                    drawerStatus.set(response.status() == CashDrawerOpenStatus.SUCCESS
                            ? HardwareActionStatus.SUCCESS
                            : HardwareActionStatus.FAILED);
                    hardwareMessage.set(response.message() == null || response.message().isBlank()
                            ? "Drawer open request completed."
                            : response.message());
                }))
                .whenComplete((ignored, throwable) -> finishAction("drawer open", drawerStatus, throwable));
    }

    private void finishAction(String operation,
                              ObjectProperty<HardwareActionStatus> status,
                              Throwable throwable) {
        if (throwable != null) {
            dispatch(() -> {
                status.set(HardwareActionStatus.FAILED);
                hardwareMessage.set(mapError(operation, throwable));
            });
        }
        dispatch(() -> busy.set(false));
    }

    private String mapError(String operation, Throwable throwable) {
        Throwable root = throwable;
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            root = completionException.getCause();
        }
        if (root instanceof ApiProblemException problem) {
            if (problem.getMessage() != null && !problem.getMessage().isBlank()) {
                return problem.getMessage();
            }
            if (problem.code() != null) {
                return "Hardware " + operation + " failed: " + problem.code();
            }
        }
        return "Hardware " + operation + " failed. Verify connectivity and permissions.";
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
