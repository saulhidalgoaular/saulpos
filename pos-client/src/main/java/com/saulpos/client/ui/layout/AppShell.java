package com.saulpos.client.ui.layout;

import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.api.shift.CashShiftStatus;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.sale.SaleCartLineResponse;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.client.app.NavigationState;
import com.saulpos.client.app.NavigationTarget;
import com.saulpos.client.app.ScreenDefinition;
import com.saulpos.client.app.ScreenRegistry;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.state.AuthSessionCoordinator;
import com.saulpos.client.state.AuthSessionState;
import com.saulpos.client.state.SellScreenCoordinator;
import com.saulpos.client.state.ShiftControlCoordinator;
import com.saulpos.client.ui.components.PosButton;
import com.saulpos.client.ui.components.PosTextField;
import com.saulpos.client.ui.components.ToastHost;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class AppShell {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private AppShell() {
    }

    public static Parent createRoot(AppStateStore stateStore,
                                    NavigationState navigationState,
                                    AuthSessionCoordinator authSessionCoordinator,
                                    ShiftControlCoordinator shiftControlCoordinator,
                                    SellScreenCoordinator sellScreenCoordinator) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("pos-shell");

        VBox nav = new VBox();
        nav.getStyleClass().add("pos-nav");
        nav.setPrefWidth(220);

        Label brand = new Label("SaulPOS v2");
        brand.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");
        nav.getChildren().add(brand);

        for (ScreenDefinition screen : ScreenRegistry.orderedScreens()) {
            PosButton button = PosButton.primary(screen.title());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setDisable(screen.requiresAuthenticatedSession() && !stateStore.isAuthenticated());
            stateStore.authenticatedProperty().addListener((obs, oldValue, newValue) ->
                    button.setDisable(screen.requiresAuthenticatedSession() && !newValue));
            button.setOnAction(event -> navigationState.navigate(screen.target()));
            nav.getChildren().add(button);
        }

        VBox content = new VBox(12);
        content.getStyleClass().add("pos-content-card");
        content.setPadding(new Insets(8));

        Label title = new Label();
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 700;");
        Label description = new Label();
        description.setWrapText(true);
        VBox screenBody = new VBox(10);

        updateContent(
                navigationState.activeTarget(),
                title,
                description,
                screenBody,
                authSessionCoordinator,
                shiftControlCoordinator,
                sellScreenCoordinator,
                stateStore,
                navigationState
        );
        navigationState.activeTargetProperty().addListener((obs, oldValue, newValue) -> {
            authSessionCoordinator.onNavigationChanged(newValue);
            updateContent(
                    newValue,
                    title,
                    description,
                    screenBody,
                    authSessionCoordinator,
                    shiftControlCoordinator,
                    sellScreenCoordinator,
                    stateStore,
                    navigationState
            );
        });

        content.getChildren().addAll(title, description, screenBody);

        ToastHost toastHost = new ToastHost();
        navigationState.activeTargetProperty().addListener((obs, oldValue, newValue) ->
                toastHost.showMessage("Navigated to " + newValue.name()));

        HBox top = new HBox();
        top.setPadding(new Insets(0, 0, 12, 0));
        Label sessionBadge = new Label();
        sessionBadge.textProperty().bind(Bindings.createStringBinding(
                () -> stateStore.isAuthenticated() ? "Session: AUTHENTICATED" : "Session: GUEST",
                stateStore.authenticatedProperty()
        ));
        Label sessionExpiry = new Label();
        sessionExpiry.textProperty().bind(Bindings.createStringBinding(
                () -> "Token expiry: " + formatExpiryValue(stateStore.sessionState()),
                stateStore.sessionStateProperty()
        ));
        Label authFeedback = new Label();
        authFeedback.textProperty().bind(authSessionCoordinator.sessionMessageProperty());
        PosButton logoutButton = PosButton.accent("Sign Out");
        logoutButton.disableProperty().bind(Bindings.not(stateStore.authenticatedProperty()));
        logoutButton.setOnAction(event -> authSessionCoordinator.logout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(sessionBadge, sessionExpiry, authFeedback, spacer, logoutButton, toastHost);

        root.setLeft(nav);
        root.setTop(top);
        root.setCenter(content);
        return root;
    }

    private static void updateContent(NavigationTarget target,
                                      Label title,
                                      Label description,
                                      VBox screenBody,
                                      AuthSessionCoordinator authSessionCoordinator,
                                      ShiftControlCoordinator shiftControlCoordinator,
                                      SellScreenCoordinator sellScreenCoordinator,
                                      AppStateStore appStateStore,
                                      NavigationState navigationState) {
        ScreenDefinition screen = ScreenRegistry.byTarget(target)
                .orElseThrow(() -> new IllegalStateException("Screen not found: " + target));
        title.setText(screen.title());
        description.setText(screen.description());

        screenBody.getChildren().clear();
        if (target == NavigationTarget.LOGIN) {
            renderLogin(screenBody, authSessionCoordinator);
            return;
        }

        if (target == NavigationTarget.SHIFT_CONTROL) {
            renderShiftControl(screenBody, shiftControlCoordinator);
            return;
        }

        if (target == NavigationTarget.SELL) {
            renderSell(screenBody, sellScreenCoordinator, shiftControlCoordinator, navigationState);
            return;
        }

        PosTextField actionPalette = new PosTextField("Global action palette (planned)");
        actionPalette.setDisable(true);
        screenBody.getChildren().add(actionPalette);

        if (appStateStore.sessionState() != null && appStateStore.sessionState().accessTokenExpiresAt() != null) {
            Label expiryHint = new Label("Access token expiry: " + formatExpiryValue(appStateStore.sessionState()));
            screenBody.getChildren().add(expiryHint);
        }
    }

    private static void renderLogin(VBox screenBody, AuthSessionCoordinator authSessionCoordinator) {
        PosTextField username = new PosTextField("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("pos-input");

        PosButton loginButton = PosButton.primary("Sign In");
        loginButton.disableProperty().bind(authSessionCoordinator.authenticatingProperty());
        loginButton.setOnAction(event -> authSessionCoordinator.login(username.getText(), password.getText()));
        password.setOnAction(event -> authSessionCoordinator.login(username.getText(), password.getText()));

        screenBody.getChildren().addAll(
                new Label("Enter credentials to start or resume a cashier session."),
                username,
                password,
                loginButton
        );
    }

    private static void renderShiftControl(VBox screenBody, ShiftControlCoordinator shiftControlCoordinator) {
        Label status = new Label();
        status.textProperty().bind(Bindings.createStringBinding(
                () -> toShiftSummary(shiftControlCoordinator.shiftState()),
                shiftControlCoordinator.shiftStateProperty()
        ));

        Label feedback = new Label();
        feedback.textProperty().bind(shiftControlCoordinator.shiftMessageProperty());

        PosTextField loadShiftId = new PosTextField("Shift ID");
        PosButton loadButton = PosButton.accent("Load Shift");
        loadButton.disableProperty().bind(shiftControlCoordinator.busyProperty());
        loadButton.setOnAction(event -> {
            Long shiftId = parseLong(loadShiftId.getText());
            if (shiftId == null) {
                shiftControlCoordinator.shiftMessageProperty().set("Shift ID must be numeric.");
                return;
            }
            shiftControlCoordinator.loadShift(shiftId);
        });

        PosTextField cashierUserId = new PosTextField("Cashier user ID");
        PosTextField terminalDeviceId = new PosTextField("Terminal device ID");
        PosTextField openingCash = new PosTextField("Opening float (e.g. 120.00)");
        PosButton openButton = PosButton.primary("Open Shift");
        openButton.disableProperty().bind(shiftControlCoordinator.busyProperty());
        openButton.setOnAction(event -> {
            Long cashierId = parseLong(cashierUserId.getText());
            Long terminalId = parseLong(terminalDeviceId.getText());
            BigDecimal amount = parseMoney(openingCash.getText());
            if (cashierId == null || terminalId == null || amount == null) {
                shiftControlCoordinator.shiftMessageProperty().set("Cashier/terminal/opening cash are required.");
                return;
            }
            shiftControlCoordinator.openShift(cashierId, terminalId, amount);
        });

        PosTextField movementAmount = new PosTextField("Cash movement amount");
        PosTextField movementNote = new PosTextField("Paid-in / paid-out reason");
        PosButton paidInButton = PosButton.primary("Paid-In");
        PosButton paidOutButton = PosButton.accent("Paid-Out");

        paidInButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> shiftControlCoordinator.busyProperty().get() || !isOpenShift(shiftControlCoordinator.shiftState()),
                shiftControlCoordinator.busyProperty(),
                shiftControlCoordinator.shiftStateProperty()
        ));
        paidOutButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> shiftControlCoordinator.busyProperty().get() || !isOpenShift(shiftControlCoordinator.shiftState()),
                shiftControlCoordinator.busyProperty(),
                shiftControlCoordinator.shiftStateProperty()
        ));

        paidInButton.setOnAction(event -> {
            BigDecimal amount = parseMoney(movementAmount.getText());
            if (amount == null) {
                shiftControlCoordinator.shiftMessageProperty().set("Movement amount is required.");
                return;
            }
            shiftControlCoordinator.recordPaidIn(amount, movementNote.getText());
        });

        paidOutButton.setOnAction(event -> {
            BigDecimal amount = parseMoney(movementAmount.getText());
            if (amount == null) {
                shiftControlCoordinator.shiftMessageProperty().set("Movement amount is required.");
                return;
            }
            shiftControlCoordinator.recordPaidOut(amount, movementNote.getText());
        });

        PosTextField countedCash = new PosTextField("Counted close cash");
        PosTextField closeNote = new PosTextField("Close note / variance reason");
        PosButton closeButton = PosButton.primary("Close Shift");
        closeButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> shiftControlCoordinator.busyProperty().get() || !isOpenShift(shiftControlCoordinator.shiftState()),
                shiftControlCoordinator.busyProperty(),
                shiftControlCoordinator.shiftStateProperty()
        ));
        closeButton.setOnAction(event -> {
            BigDecimal closeAmount = parseMoney(countedCash.getText());
            if (closeAmount == null) {
                shiftControlCoordinator.shiftMessageProperty().set("Counted close cash is required.");
                return;
            }
            shiftControlCoordinator.closeShift(closeAmount, closeNote.getText());
        });

        screenBody.getChildren().addAll(
                new Label("Shift lifecycle controls"),
                status,
                feedback,
                new Label("Load existing shift"),
                loadShiftId,
                loadButton,
                new Label("Open shift"),
                cashierUserId,
                terminalDeviceId,
                openingCash,
                openButton,
                new Label("Cash movements"),
                movementAmount,
                movementNote,
                new HBox(8, paidInButton, paidOutButton),
                new Label("Close and reconcile"),
                countedCash,
                closeNote,
                closeButton
        );
    }

    private static void renderSell(VBox screenBody,
                                   SellScreenCoordinator sellScreenCoordinator,
                                   ShiftControlCoordinator shiftControlCoordinator,
                                   NavigationState navigationState) {
        Label cartSummary = new Label();
        cartSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toCartSummary(sellScreenCoordinator.cartState()),
                sellScreenCoordinator.cartStateProperty()
        ));

        Label feedback = new Label();
        feedback.textProperty().bind(sellScreenCoordinator.sellMessageProperty());

        PosTextField cashierUserId = new PosTextField("Cashier user ID");
        PosTextField storeLocationId = new PosTextField("Store location ID");
        PosTextField terminalDeviceId = new PosTextField("Terminal device ID");
        PosButton createCart = PosButton.primary("Create Cart");
        createCart.disableProperty().bind(sellScreenCoordinator.busyProperty());
        createCart.setOnAction(event -> sellScreenCoordinator.createCart(
                parseLong(cashierUserId.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText())
        ));

        PosTextField loadCartId = new PosTextField("Load cart by ID");
        PosButton loadCart = PosButton.accent("Load Cart");
        loadCart.disableProperty().bind(sellScreenCoordinator.busyProperty());
        loadCart.setOnAction(event -> sellScreenCoordinator.loadCart(parseLong(loadCartId.getText())));

        PosTextField merchantId = new PosTextField("Merchant ID");
        PosTextField barcode = new PosTextField("Scan barcode and press Enter");
        PosTextField scanQuantity = new PosTextField("Scan quantity (default 1)");
        scanQuantity.setText("1");
        barcode.disableProperty().bind(sellScreenCoordinator.busyProperty());
        barcode.setOnAction(event -> sellScreenCoordinator.scanBarcode(
                parseLong(merchantId.getText()),
                barcode.getText(),
                parseDecimal(scanQuantity.getText())
        ));

        PosTextField searchQuery = new PosTextField("Search products");
        PosTextField searchPage = new PosTextField("Page");
        searchPage.setText("0");
        PosButton searchButton = PosButton.primary("Search");
        searchButton.disableProperty().bind(sellScreenCoordinator.busyProperty());
        searchButton.setOnAction(event -> sellScreenCoordinator.searchProducts(
                parseLong(merchantId.getText()),
                searchQuery.getText(),
                parseInt(searchPage.getText(), 0)
        ));

        PosButton previousPage = PosButton.accent("Prev");
        PosButton nextPage = PosButton.accent("Next");
        previousPage.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get()
                        || sellScreenCoordinator.searchStateProperty().get() == null
                        || !sellScreenCoordinator.searchStateProperty().get().hasPrevious(),
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.searchStateProperty()
        ));
        nextPage.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get()
                        || sellScreenCoordinator.searchStateProperty().get() == null
                        || !sellScreenCoordinator.searchStateProperty().get().hasNext(),
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.searchStateProperty()
        ));
        previousPage.setOnAction(event -> {
            ProductSearchResponse search = sellScreenCoordinator.searchStateProperty().get();
            if (search == null) {
                return;
            }
            int targetPage = Math.max(0, search.page() - 1);
            searchPage.setText(Integer.toString(targetPage));
            sellScreenCoordinator.searchProducts(parseLong(merchantId.getText()), searchQuery.getText(), targetPage);
        });
        nextPage.setOnAction(event -> {
            ProductSearchResponse search = sellScreenCoordinator.searchStateProperty().get();
            if (search == null) {
                return;
            }
            int targetPage = search.page() + 1;
            searchPage.setText(Integer.toString(targetPage));
            sellScreenCoordinator.searchProducts(parseLong(merchantId.getText()), searchQuery.getText(), targetPage);
        });

        ListView<ProductResponse> searchResults = new ListView<>();
        searchResults.setPrefHeight(180);
        sellScreenCoordinator.searchStateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.items() == null) {
                searchResults.setItems(FXCollections.emptyObservableList());
                return;
            }
            searchResults.setItems(FXCollections.observableArrayList(newValue.items()));
        });
        searchResults.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ProductResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.id() + " | " + item.sku() + " | " + item.name() + " | " + defaultMoney(item.basePrice()));
            }
        });

        PosTextField addQuantity = new PosTextField("Quick add quantity");
        addQuantity.setText("1");
        PosButton addSelected = PosButton.primary("Add Selected");
        addSelected.disableProperty().bind(sellScreenCoordinator.busyProperty());
        addSelected.setOnAction(event -> {
            ProductResponse selected = searchResults.getSelectionModel().getSelectedItem();
            sellScreenCoordinator.quickAddProduct(selected == null ? null : selected.id(), parseDecimal(addQuantity.getText()));
        });

        ListView<SaleCartLineResponse> cartLines = new ListView<>();
        cartLines.setPrefHeight(180);
        sellScreenCoordinator.cartStateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.lines() == null) {
                cartLines.setItems(FXCollections.emptyObservableList());
                return;
            }
            cartLines.setItems(FXCollections.observableArrayList(newValue.lines()));
        });
        cartLines.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(SaleCartLineResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText("#" + item.lineId()
                        + " | " + item.productName()
                        + " | qty=" + item.quantity()
                        + " | unit=" + defaultMoney(item.unitPrice())
                        + " | gross=" + defaultMoney(item.grossAmount()));
            }
        });

        PosTextField editLineId = new PosTextField("Line ID");
        PosTextField editQuantity = new PosTextField("New quantity");
        PosButton updateLine = PosButton.accent("Update Line");
        updateLine.disableProperty().bind(sellScreenCoordinator.busyProperty());
        updateLine.setOnAction(event -> sellScreenCoordinator.updateLineQuantity(
                parseLong(editLineId.getText()),
                parseDecimal(editQuantity.getText())
        ));

        PosTextField removeLineId = new PosTextField("Line ID to remove");
        PosButton removeLine = PosButton.accent("Remove Line");
        removeLine.disableProperty().bind(sellScreenCoordinator.busyProperty());
        removeLine.setOnAction(event -> sellScreenCoordinator.removeLine(parseLong(removeLineId.getText())));

        PosButton recalculate = PosButton.primary("Recalculate Totals");
        recalculate.disableProperty().bind(sellScreenCoordinator.busyProperty());
        recalculate.setOnAction(event -> sellScreenCoordinator.recalculate());

        PosButton continueToCheckout = PosButton.accent("Go To Checkout");
        continueToCheckout.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.cartState() == null
                        || sellScreenCoordinator.cartState().lines() == null
                        || sellScreenCoordinator.cartState().lines().isEmpty(),
                sellScreenCoordinator.cartStateProperty()
        ));
        continueToCheckout.setOnAction(event -> navigationState.navigate(NavigationTarget.CHECKOUT));

        if (shiftControlCoordinator.shiftState() != null) {
            cashierUserId.setText(Long.toString(shiftControlCoordinator.shiftState().cashierUserId()));
            storeLocationId.setText(Long.toString(shiftControlCoordinator.shiftState().storeLocationId()));
            terminalDeviceId.setText(Long.toString(shiftControlCoordinator.shiftState().terminalDeviceId()));
        }

        screenBody.getChildren().addAll(
                new Label("Sell workstation: create/load cart, scan, search, and edit lines"),
                cartSummary,
                feedback,
                new Label("Cart context"),
                cashierUserId,
                storeLocationId,
                terminalDeviceId,
                new HBox(8, createCart, loadCartId, loadCart),
                new Label("Barcode scanner flow"),
                merchantId,
                barcode,
                scanQuantity,
                new Label("Product search"),
                searchQuery,
                searchPage,
                new HBox(8, searchButton, previousPage, nextPage),
                searchResults,
                addQuantity,
                addSelected,
                new Label("Cart lines"),
                cartLines,
                new Label("Edit line quantity"),
                editLineId,
                editQuantity,
                updateLine,
                new Label("Remove line"),
                removeLineId,
                new HBox(8, removeLine, recalculate, continueToCheckout)
        );
    }

    private static boolean isOpenShift(CashShiftResponse shift) {
        return shift != null && shift.status() == CashShiftStatus.OPEN;
    }

    private static String toCartSummary(SaleCartResponse cart) {
        if (cart == null) {
            return "No cart loaded.";
        }
        int lineCount = cart.lines() == null ? 0 : cart.lines().size();
        return "Cart #" + cart.id()
                + " | status=" + cart.status()
                + " | lines=" + lineCount
                + " | subtotal=" + defaultMoney(cart.subtotalNet())
                + " | tax=" + defaultMoney(cart.totalTax())
                + " | rounding=" + defaultMoney(cart.roundingAdjustment())
                + " | payable=" + defaultMoney(cart.totalPayable());
    }

    private static String toShiftSummary(CashShiftResponse shift) {
        if (shift == null) {
            return "No shift loaded.";
        }
        return "Shift #" + shift.id()
                + " | status=" + shift.status()
                + " | opening=" + defaultMoney(shift.openingCash())
                + " | paidIn=" + defaultMoney(shift.totalPaidIn())
                + " | paidOut=" + defaultMoney(shift.totalPaidOut())
                + " | expectedClose=" + defaultMoney(shift.expectedCloseCash())
                + " | counted=" + defaultMoney(shift.countedCloseCash())
                + " | variance=" + defaultMoney(shift.varianceCash());
    }

    private static String defaultMoney(BigDecimal value) {
        return value == null ? "0.00" : value.toPlainString();
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal parseDecimal(String value) {
        return parseMoney(value);
    }

    private static String formatExpiryValue(AuthSessionState session) {
        if (session == null || session.accessTokenExpiresAt() == null) {
            return "n/a";
        }
        return EXPIRY_FORMATTER.format(session.accessTokenExpiresAt());
    }
}
