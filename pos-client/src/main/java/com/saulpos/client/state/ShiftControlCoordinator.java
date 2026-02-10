package com.saulpos.client.state;

import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementType;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.api.shift.CashShiftStatus;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class ShiftControlCoordinator {

    private final PosApiClient apiClient;
    private final Consumer<Runnable> uiDispatcher;
    private final ObjectProperty<CashShiftResponse> shiftState = new SimpleObjectProperty<>();
    private final StringProperty shiftMessage = new SimpleStringProperty("No active shift loaded.");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    public ShiftControlCoordinator(PosApiClient apiClient) {
        this(apiClient, Platform::runLater);
    }

    ShiftControlCoordinator(PosApiClient apiClient, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.uiDispatcher = uiDispatcher;
    }

    public ObjectProperty<CashShiftResponse> shiftStateProperty() {
        return shiftState;
    }

    public CashShiftResponse shiftState() {
        return shiftState.get();
    }

    public StringProperty shiftMessageProperty() {
        return shiftMessage;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public CompletableFuture<Void> openShift(Long cashierUserId, Long terminalDeviceId, BigDecimal openingCash) {
        dispatch(() -> busy.set(true));
        return apiClient.openShift(new CashShiftOpenRequest(cashierUserId, terminalDeviceId, openingCash))
                .thenAccept(this::acceptShift)
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> shiftMessage.set(mapErrorMessage(throwable)));
                    }
                    dispatch(() -> busy.set(false));
                });
    }

    public CompletableFuture<Void> loadShift(Long shiftId) {
        dispatch(() -> busy.set(true));
        return apiClient.getShift(shiftId)
                .thenAccept(this::acceptShift)
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> shiftMessage.set(mapErrorMessage(throwable)));
                    }
                    dispatch(() -> busy.set(false));
                });
    }

    public CompletableFuture<Void> recordPaidIn(BigDecimal amount, String note) {
        return recordMovement(CashMovementType.PAID_IN, amount, note, "Paid-in recorded.");
    }

    public CompletableFuture<Void> recordPaidOut(BigDecimal amount, String note) {
        return recordMovement(CashMovementType.PAID_OUT, amount, note, "Paid-out recorded.");
    }

    public CompletableFuture<Void> closeShift(BigDecimal countedCash, String note) {
        CashShiftResponse activeShift = shiftState.get();
        if (activeShift == null) {
            dispatch(() -> shiftMessage.set("Open or load a shift before closing."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.closeShift(activeShift.id(), new CashShiftCloseRequest(countedCash, note))
                .thenAccept(closedShift -> dispatch(() -> {
                    shiftState.set(closedShift);
                    shiftMessage.set("Shift closed.");
                }))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> shiftMessage.set(mapErrorMessage(throwable)));
                    }
                    dispatch(() -> busy.set(false));
                });
    }

    private CompletableFuture<Void> recordMovement(CashMovementType movementType,
                                                    BigDecimal amount,
                                                    String note,
                                                    String successMessage) {
        CashShiftResponse activeShift = shiftState.get();
        if (activeShift == null || activeShift.status() != CashShiftStatus.OPEN) {
            dispatch(() -> shiftMessage.set("Open or load an active shift first."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.addCashMovement(activeShift.id(), new CashMovementRequest(movementType, amount, note))
                .thenCompose(ignored -> apiClient.getShift(activeShift.id()))
                .thenAccept(updatedShift -> dispatch(() -> {
                    shiftState.set(updatedShift);
                    shiftMessage.set(successMessage);
                }))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> shiftMessage.set(mapErrorMessage(throwable)));
                    }
                    dispatch(() -> busy.set(false));
                });
    }

    private void acceptShift(CashShiftResponse shift) {
        dispatch(() -> {
            shiftState.set(shift);
            if (shift.status() == CashShiftStatus.OPEN) {
                shiftMessage.set("Shift open and ready for cash controls.");
            } else {
                shiftMessage.set("Shift loaded with status " + shift.status() + ".");
            }
        });
    }

    private String mapErrorMessage(Throwable throwable) {
        Throwable root = throwable;
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            root = completionException.getCause();
        }
        if (root instanceof ApiProblemException apiProblemException) {
            if (apiProblemException.getMessage() != null && !apiProblemException.getMessage().isBlank()) {
                return apiProblemException.getMessage();
            }
            if (apiProblemException.code() != null) {
                return "Request failed: " + apiProblemException.code();
            }
        }
        return "Shift request failed. Verify inputs and connectivity.";
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
