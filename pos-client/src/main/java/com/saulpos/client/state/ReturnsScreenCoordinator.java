package com.saulpos.client.state;

import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitLineRequest;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class ReturnsScreenCoordinator {

    private final PosApiClient apiClient;
    private final Consumer<Runnable> uiDispatcher;
    private final ObjectProperty<SaleReturnLookupResponse> lookupState = new SimpleObjectProperty<>();
    private final ObjectProperty<SaleReturnResponse> submitState = new SimpleObjectProperty<>();
    private final StringProperty returnsMessage = new SimpleStringProperty("Lookup a receipt to begin a return.");
    private final BooleanProperty managerApprovalRequired = new SimpleBooleanProperty(false);
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    public ReturnsScreenCoordinator(PosApiClient apiClient) {
        this(apiClient, Platform::runLater);
    }

    ReturnsScreenCoordinator(PosApiClient apiClient, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.uiDispatcher = uiDispatcher;
    }

    public ObjectProperty<SaleReturnLookupResponse> lookupStateProperty() {
        return lookupState;
    }

    public SaleReturnLookupResponse lookupState() {
        return lookupState.get();
    }

    public ObjectProperty<SaleReturnResponse> submitStateProperty() {
        return submitState;
    }

    public SaleReturnResponse submitState() {
        return submitState.get();
    }

    public StringProperty returnsMessageProperty() {
        return returnsMessage;
    }

    public BooleanProperty managerApprovalRequiredProperty() {
        return managerApprovalRequired;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public CompletableFuture<Void> lookupByReceipt(String receiptNumber) {
        if (receiptNumber == null || receiptNumber.isBlank()) {
            dispatch(() -> returnsMessage.set("Receipt number is required."));
            return CompletableFuture.completedFuture(null);
        }

        dispatch(() -> busy.set(true));
        return apiClient.lookupReturnByReceipt(receiptNumber.trim())
                .thenAccept(this::acceptLookup)
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> submitReturn(Long saleLineId,
                                                BigDecimal quantity,
                                                String reasonCode,
                                                TenderType refundTenderType,
                                                String refundReference,
                                                String note) {
        SaleReturnLookupResponse lookup = lookupState.get();
        if (lookup == null) {
            dispatch(() -> returnsMessage.set("Lookup a receipt before submitting a return."));
            return CompletableFuture.completedFuture(null);
        }
        if (saleLineId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            dispatch(() -> returnsMessage.set("Sale line and quantity greater than zero are required."));
            return CompletableFuture.completedFuture(null);
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            dispatch(() -> returnsMessage.set("Reason code is required."));
            return CompletableFuture.completedFuture(null);
        }
        if (refundTenderType == null) {
            dispatch(() -> returnsMessage.set("Refund tender type is required."));
            return CompletableFuture.completedFuture(null);
        }

        SaleReturnSubmitRequest request = new SaleReturnSubmitRequest(
                lookup.saleId(),
                lookup.receiptNumber(),
                reasonCode.trim().toUpperCase(Locale.ROOT),
                refundTenderType,
                normalizeOptional(refundReference),
                normalizeOptional(note),
                List.of(new SaleReturnSubmitLineRequest(saleLineId, quantity))
        );

        dispatch(() -> busy.set(true));
        return apiClient.submitReturn(request)
                .thenAccept(this::acceptSubmit)
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    private void acceptLookup(SaleReturnLookupResponse lookup) {
        dispatch(() -> {
            lookupState.set(lookup);
            submitState.set(null);
            managerApprovalRequired.set(false);
            int lineCount = lookup.lines() == null ? 0 : lookup.lines().size();
            returnsMessage.set("Receipt loaded. " + lineCount + " lines eligible for return review.");
        });
    }

    private void acceptSubmit(SaleReturnResponse response) {
        dispatch(() -> {
            submitState.set(response);
            managerApprovalRequired.set(false);
            returnsMessage.set("Return submitted. Reference " + response.returnReference() + " created.");
        });
    }

    private void finish(Throwable throwable) {
        if (throwable != null) {
            ErrorView errorView = mapError(throwable);
            dispatch(() -> {
                managerApprovalRequired.set(errorView.managerApprovalRequired());
                returnsMessage.set(errorView.message());
            });
        }
        dispatch(() -> busy.set(false));
    }

    private ErrorView mapError(Throwable throwable) {
        Throwable root = throwable;
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            root = completionException.getCause();
        }
        if (root instanceof ApiProblemException problem) {
            String detail = problem.getMessage();
            if (detail != null && !detail.isBlank()) {
                boolean requiresApproval = detail.toLowerCase(Locale.ROOT).contains("requires manager approval");
                if (requiresApproval) {
                    return new ErrorView(detail + " Have a manager sign in and retry.", true);
                }
                return new ErrorView(detail, false);
            }
            if (problem.code() != null) {
                return new ErrorView("Request failed: " + problem.code(), false);
            }
        }
        return new ErrorView("Return request failed. Verify inputs and connectivity.", false);
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

    private record ErrorView(String message, boolean managerApprovalRequired) {
    }
}
