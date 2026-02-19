package com.saulpos.client.ui.layout;

import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.api.shift.CashShiftStatus;
import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.customer.CustomerResponse;
import com.saulpos.api.inventory.InventoryStockBalanceResponse;
import com.saulpos.api.inventory.SupplierReturnResponse;
import com.saulpos.api.report.ExceptionReportEventType;
import com.saulpos.api.receipt.CashDrawerOpenResponse;
import com.saulpos.api.receipt.ReceiptJournalResponse;
import com.saulpos.api.receipt.ReceiptPrintResponse;
import com.saulpos.api.sale.ParkedSaleCartSummaryResponse;
import com.saulpos.api.sale.SaleCartLineResponse;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.refund.SaleReturnLookupLineResponse;
import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.tax.TenderType;
import com.saulpos.client.app.NavigationState;
import com.saulpos.client.app.NavigationTarget;
import com.saulpos.client.app.ScreenDefinition;
import com.saulpos.client.app.ScreenRegistry;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.state.AuthSessionCoordinator;
import com.saulpos.client.state.AuthSessionState;
import com.saulpos.client.state.BackofficeCoordinator;
import com.saulpos.client.state.ConnectivityCoordinator;
import com.saulpos.client.state.HardwareActionStatus;
import com.saulpos.client.state.HardwareCoordinator;
import com.saulpos.client.state.ReportingCoordinator;
import com.saulpos.client.state.ReturnsScreenCoordinator;
import com.saulpos.client.state.SellScreenCoordinator;
import com.saulpos.client.state.ShiftControlCoordinator;
import com.saulpos.client.ui.components.PosButton;
import com.saulpos.client.ui.components.PosTextField;
import com.saulpos.client.ui.components.ToastHost;
import com.saulpos.client.ui.i18n.UiI18n;
import com.saulpos.client.ui.i18n.UiLanguage;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class AppShell {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private AppShell() {
    }

    public static Parent createRoot(UiI18n i18n,
                                    AppStateStore stateStore,
                                    NavigationState navigationState,
                                    AuthSessionCoordinator authSessionCoordinator,
                                    ShiftControlCoordinator shiftControlCoordinator,
                                    SellScreenCoordinator sellScreenCoordinator,
                                    ReturnsScreenCoordinator returnsScreenCoordinator,
                                    BackofficeCoordinator backofficeCoordinator,
                                    ReportingCoordinator reportingCoordinator,
                                    HardwareCoordinator hardwareCoordinator,
                                    ConnectivityCoordinator connectivityCoordinator) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("pos-shell");

        VBox nav = new VBox();
        nav.getStyleClass().add("pos-nav");
        nav.setPrefWidth(220);

        Label brand = label(i18n, "SaulPOS v2");
        brand.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");
        nav.getChildren().add(brand);

        for (ScreenDefinition screen : ScreenRegistry.orderedScreens()) {
            PosButton button = PosButton.primary(i18n.translate(screen.title()));
            button.textProperty().bind(Bindings.createStringBinding(
                    () -> i18n.translate(screen.title()),
                    i18n.languageProperty()
            ));
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
                i18n,
                navigationState.activeTarget(),
                title,
                description,
                screenBody,
                authSessionCoordinator,
                shiftControlCoordinator,
                sellScreenCoordinator,
                returnsScreenCoordinator,
                backofficeCoordinator,
                reportingCoordinator,
                hardwareCoordinator,
                stateStore,
                navigationState
        );
        navigationState.activeTargetProperty().addListener((obs, oldValue, newValue) -> {
            authSessionCoordinator.onNavigationChanged(newValue);
            updateContent(
                    i18n,
                    newValue,
                    title,
                    description,
                    screenBody,
                    authSessionCoordinator,
                    shiftControlCoordinator,
                    sellScreenCoordinator,
                    returnsScreenCoordinator,
                    backofficeCoordinator,
                    reportingCoordinator,
                    hardwareCoordinator,
                    stateStore,
                    navigationState
            );
        });
        i18n.languageProperty().addListener((obs, oldValue, newValue) -> updateContent(
                i18n,
                navigationState.activeTarget(),
                title,
                description,
                screenBody,
                authSessionCoordinator,
                shiftControlCoordinator,
                sellScreenCoordinator,
                returnsScreenCoordinator,
                backofficeCoordinator,
                reportingCoordinator,
                hardwareCoordinator,
                stateStore,
                navigationState
        ));

        content.getChildren().addAll(title, description, screenBody);

        ToastHost toastHost = new ToastHost();
        navigationState.activeTargetProperty().addListener((obs, oldValue, newValue) ->
                toastHost.showMessage(i18n.translate("Navigated to " + newValue.name())));

        HBox top = new HBox();
        top.setPadding(new Insets(0, 0, 12, 0));
        Label sessionBadge = new Label();
        sessionBadge.textProperty().bind(Bindings.createStringBinding(
                () -> stateStore.isAuthenticated()
                        ? i18n.translate("Session: AUTHENTICATED")
                        : i18n.translate("Session: GUEST"),
                stateStore.authenticatedProperty(),
                i18n.languageProperty()
        ));
        Label sessionExpiry = new Label();
        sessionExpiry.textProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("Token expiry: ") + formatExpiryValue(stateStore.sessionState(), i18n),
                stateStore.sessionStateProperty(),
                i18n.languageProperty()
        ));
        Label authFeedback = new Label();
        authFeedback.textProperty().bind(i18n.bindTranslated(authSessionCoordinator.sessionMessageProperty()));
        Label connectivityBadge = new Label();
        connectivityBadge.textProperty().bind(Bindings.createStringBinding(
                () -> connectivityCoordinator.isOnline()
                        ? i18n.translate("Connectivity: ONLINE")
                        : i18n.translate("Connectivity: OFFLINE"),
                connectivityCoordinator.onlineProperty(),
                i18n.languageProperty()
        ));
        Label connectivityFeedback = new Label();
        connectivityFeedback.textProperty().bind(i18n.bindTranslated(connectivityCoordinator.connectivityMessageProperty()));
        PosButton refreshConnectivityButton = PosButton.accent(i18n.translate("Retry Connectivity"));
        refreshConnectivityButton.textProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("Retry Connectivity"),
                i18n.languageProperty()
        ));
        refreshConnectivityButton.disableProperty().bind(connectivityCoordinator.checkingProperty());
        refreshConnectivityButton.setOnAction(event -> connectivityCoordinator.refresh());
        PosButton logoutButton = PosButton.accent(i18n.translate("Sign Out"));
        logoutButton.textProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("Sign Out"),
                i18n.languageProperty()
        ));
        logoutButton.disableProperty().bind(Bindings.not(stateStore.authenticatedProperty()));
        logoutButton.setOnAction(event -> authSessionCoordinator.logout());

        Label languageLabel = new Label();
        languageLabel.textProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("Language"),
                i18n.languageProperty()
        ));
        ComboBox<UiLanguage> languagePicker = new ComboBox<>(FXCollections.observableArrayList(UiLanguage.values()));
        languagePicker.valueProperty().bindBidirectional(i18n.languageProperty());
        languagePicker.setPrefWidth(120);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(
                sessionBadge,
                sessionExpiry,
                authFeedback,
                connectivityBadge,
                connectivityFeedback,
                refreshConnectivityButton,
                languageLabel,
                languagePicker,
                spacer,
                logoutButton,
                toastHost
        );

        root.setLeft(nav);
        root.setTop(top);
        root.setCenter(content);
        return root;
    }

    static void updateContent(UiI18n i18n,
                              NavigationTarget target,
                              Label title,
                              Label description,
                              VBox screenBody,
                              AuthSessionCoordinator authSessionCoordinator,
                              ShiftControlCoordinator shiftControlCoordinator,
                              SellScreenCoordinator sellScreenCoordinator,
                              ReturnsScreenCoordinator returnsScreenCoordinator,
                              BackofficeCoordinator backofficeCoordinator,
                              ReportingCoordinator reportingCoordinator,
                              HardwareCoordinator hardwareCoordinator,
                              AppStateStore appStateStore,
                              NavigationState navigationState) {
        ScreenDefinition screen = ScreenRegistry.byTarget(target)
                .orElseThrow(() -> new IllegalStateException("Screen not found: " + target));
        title.setText(i18n.translate(screen.title()));
        description.setText(i18n.translate(screen.description()));

        screenBody.getChildren().clear();
        if (target == NavigationTarget.LOGIN) {
            renderLogin(i18n, screenBody, authSessionCoordinator);
            return;
        }

        if (target == NavigationTarget.SHIFT_CONTROL) {
            renderShiftControl(i18n, screenBody, shiftControlCoordinator);
            return;
        }

        if (target == NavigationTarget.SELL) {
            renderSell(i18n, screenBody, sellScreenCoordinator, shiftControlCoordinator, navigationState);
            return;
        }

        if (target == NavigationTarget.CHECKOUT) {
            renderCheckout(i18n, screenBody, sellScreenCoordinator, shiftControlCoordinator, navigationState);
            return;
        }

        if (target == NavigationTarget.RETURNS) {
            renderReturns(i18n, screenBody, returnsScreenCoordinator);
            return;
        }

        if (target == NavigationTarget.BACKOFFICE) {
            renderBackoffice(i18n, screenBody, backofficeCoordinator);
            return;
        }

        if (target == NavigationTarget.REPORTING) {
            renderReporting(i18n, screenBody, reportingCoordinator);
            return;
        }
        if (target == NavigationTarget.HARDWARE) {
            renderHardware(i18n, screenBody, hardwareCoordinator, appStateStore);
            return;
        }
    }

    private static void renderLogin(UiI18n i18n, VBox screenBody, AuthSessionCoordinator authSessionCoordinator) {
        PosTextField username = input(i18n, "Username");
        PasswordField password = new PasswordField();
        password.setPromptText(i18n.translate("Password"));
        password.getStyleClass().add("pos-input");

        PosButton loginButton = primary(i18n, "Sign In");
        loginButton.disableProperty().bind(authSessionCoordinator.authenticatingProperty());
        loginButton.setOnAction(event -> authSessionCoordinator.login(username.getText(), password.getText()));
        password.setOnAction(event -> authSessionCoordinator.login(username.getText(), password.getText()));

        screenBody.getChildren().addAll(
                label(i18n, "Enter credentials to start or resume a cashier session."),
                username,
                password,
                loginButton
        );
    }

    private static void renderShiftControl(UiI18n i18n, VBox screenBody, ShiftControlCoordinator shiftControlCoordinator) {
        Label status = new Label();
        status.textProperty().bind(Bindings.createStringBinding(
                () -> toShiftSummary(i18n, shiftControlCoordinator.shiftState()),
                shiftControlCoordinator.shiftStateProperty(),
                i18n.languageProperty()
        ));

        Label feedback = new Label();
        feedback.textProperty().bind(i18n.bindTranslated(shiftControlCoordinator.shiftMessageProperty()));

        PosTextField loadShiftId = input(i18n, "Shift ID");
        PosButton loadButton = accent(i18n, "Load Shift");
        loadButton.disableProperty().bind(shiftControlCoordinator.busyProperty());
        loadButton.setOnAction(event -> {
            Long shiftId = parseLong(loadShiftId.getText());
            if (shiftId == null) {
                shiftControlCoordinator.shiftMessageProperty().set(i18n.translate("Shift ID must be numeric."));
                return;
            }
            shiftControlCoordinator.loadShift(shiftId);
        });

        PosTextField cashierUserId = input(i18n, "Cashier user ID");
        PosTextField terminalDeviceId = input(i18n, "Terminal device ID");
        PosTextField openingCash = input(i18n, "Opening float (e.g. 120.00)");
        PosButton openButton = primary(i18n, "Open Shift");
        openButton.disableProperty().bind(shiftControlCoordinator.busyProperty());
        openButton.setOnAction(event -> {
            Long cashierId = parseLong(cashierUserId.getText());
            Long terminalId = parseLong(terminalDeviceId.getText());
            BigDecimal amount = parseMoney(openingCash.getText());
            if (cashierId == null || terminalId == null || amount == null) {
                shiftControlCoordinator.shiftMessageProperty().set(i18n.translate("Cashier/terminal/opening cash are required."));
                return;
            }
            shiftControlCoordinator.openShift(cashierId, terminalId, amount);
        });

        PosTextField movementAmount = input(i18n, "Cash movement amount");
        PosTextField movementNote = input(i18n, "Paid-in / paid-out reason");
        PosButton paidInButton = primary(i18n, "Paid-In");
        PosButton paidOutButton = accent(i18n, "Paid-Out");

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
                shiftControlCoordinator.shiftMessageProperty().set(i18n.translate("Movement amount is required."));
                return;
            }
            shiftControlCoordinator.recordPaidIn(amount, movementNote.getText());
        });

        paidOutButton.setOnAction(event -> {
            BigDecimal amount = parseMoney(movementAmount.getText());
            if (amount == null) {
                shiftControlCoordinator.shiftMessageProperty().set(i18n.translate("Movement amount is required."));
                return;
            }
            shiftControlCoordinator.recordPaidOut(amount, movementNote.getText());
        });

        PosTextField countedCash = input(i18n, "Counted close cash");
        PosTextField closeNote = input(i18n, "Close note / variance reason");
        PosButton closeButton = primary(i18n, "Close Shift");
        closeButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> shiftControlCoordinator.busyProperty().get() || !isOpenShift(shiftControlCoordinator.shiftState()),
                shiftControlCoordinator.busyProperty(),
                shiftControlCoordinator.shiftStateProperty()
        ));
        closeButton.setOnAction(event -> {
            BigDecimal closeAmount = parseMoney(countedCash.getText());
            if (closeAmount == null) {
                shiftControlCoordinator.shiftMessageProperty().set(i18n.translate("Counted close cash is required."));
                return;
            }
            shiftControlCoordinator.closeShift(closeAmount, closeNote.getText());
        });

        screenBody.getChildren().addAll(
                label(i18n, "Shift lifecycle controls"),
                status,
                feedback,
                label(i18n, "Load existing shift"),
                loadShiftId,
                loadButton,
                label(i18n, "Open shift"),
                cashierUserId,
                terminalDeviceId,
                openingCash,
                openButton,
                label(i18n, "Cash movements"),
                movementAmount,
                movementNote,
                new HBox(8, paidInButton, paidOutButton),
                label(i18n, "Close and reconcile"),
                countedCash,
                closeNote,
                closeButton
        );
    }

    private static void renderSell(UiI18n i18n, VBox screenBody,
                                   SellScreenCoordinator sellScreenCoordinator,
                                   ShiftControlCoordinator shiftControlCoordinator,
                                   NavigationState navigationState) {
        Label cartSummary = new Label();
        cartSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toCartSummary(i18n, sellScreenCoordinator.cartState()),
                sellScreenCoordinator.cartStateProperty(),
                i18n.languageProperty()
        ));

        Label feedback = new Label();
        feedback.textProperty().bind(i18n.bindTranslated(sellScreenCoordinator.sellMessageProperty()));

        PosTextField cashierUserId = input(i18n, "Cashier user ID");
        PosTextField storeLocationId = input(i18n, "Store location ID");
        PosTextField terminalDeviceId = input(i18n, "Terminal device ID");
        PosButton createCart = primary(i18n, "Create Cart");
        createCart.disableProperty().bind(sellScreenCoordinator.busyProperty());
        createCart.setOnAction(event -> sellScreenCoordinator.createCart(
                parseLong(cashierUserId.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText())
        ));

        PosTextField loadCartId = input(i18n, "Load cart by ID");
        PosButton loadCart = accent(i18n, "Load Cart");
        loadCart.disableProperty().bind(sellScreenCoordinator.busyProperty());
        loadCart.setOnAction(event -> sellScreenCoordinator.loadCart(parseLong(loadCartId.getText())));

        PosTextField merchantId = input(i18n, "Merchant ID");
        PosTextField barcode = input(i18n, "Scan barcode and press Enter");
        PosTextField scanQuantity = input(i18n, "Scan quantity (default 1)");
        scanQuantity.setText("1");
        barcode.disableProperty().bind(sellScreenCoordinator.busyProperty());
        barcode.setOnAction(event -> sellScreenCoordinator.scanBarcode(
                parseLong(merchantId.getText()),
                barcode.getText(),
                parseDecimal(scanQuantity.getText())
        ));

        PosTextField searchQuery = input(i18n, "Search products");
        PosTextField searchPage = input(i18n, "Page");
        searchPage.setText("0");
        PosButton searchButton = primary(i18n, "Search");
        searchButton.disableProperty().bind(sellScreenCoordinator.busyProperty());
        searchButton.setOnAction(event -> sellScreenCoordinator.searchProducts(
                parseLong(merchantId.getText()),
                searchQuery.getText(),
                parseInt(searchPage.getText(), 0)
        ));

        PosButton previousPage = accent(i18n, "Prev");
        PosButton nextPage = accent(i18n, "Next");
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
                setText(i18n.translate(item.id() + " | " + item.sku() + " | " + item.name() + " | " + defaultMoney(item.basePrice())));
            }
        });

        PosTextField addQuantity = input(i18n, "Quick add quantity");
        addQuantity.setText("1");
        PosButton addSelected = primary(i18n, "Add Selected");
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
                setText(i18n.translate("#" + item.lineId()
                        + " | " + item.productName()
                        + " | qty=" + item.quantity()
                        + " | unit=" + defaultMoney(item.unitPrice())
                        + " | gross=" + defaultMoney(item.grossAmount())));
            }
        });

        PosTextField editLineId = input(i18n, "Line ID");
        PosTextField editQuantity = input(i18n, "New quantity");
        PosButton updateLine = accent(i18n, "Update Line");
        updateLine.disableProperty().bind(sellScreenCoordinator.busyProperty());
        updateLine.setOnAction(event -> sellScreenCoordinator.updateLineQuantity(
                parseLong(editLineId.getText()),
                parseDecimal(editQuantity.getText())
        ));

        PosTextField removeLineId = input(i18n, "Line ID to remove");
        PosButton removeLine = accent(i18n, "Remove Line");
        removeLine.disableProperty().bind(sellScreenCoordinator.busyProperty());
        removeLine.setOnAction(event -> sellScreenCoordinator.removeLine(parseLong(removeLineId.getText())));

        PosButton recalculate = primary(i18n, "Recalculate Totals");
        recalculate.disableProperty().bind(sellScreenCoordinator.busyProperty());
        recalculate.setOnAction(event -> sellScreenCoordinator.recalculate());

        PosButton loadSellPermissions = accent(i18n, "Load Sell Permissions");
        loadSellPermissions.disableProperty().bind(sellScreenCoordinator.busyProperty());
        loadSellPermissions.setOnAction(event -> sellScreenCoordinator.refreshPermissions());

        PosTextField parkNote = input(i18n, "Park note (optional)");
        PosButton parkCart = accent(i18n, "Park Cart");
        parkCart.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get() || !sellScreenCoordinator.parkAuthorizedProperty().get(),
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.parkAuthorizedProperty()
        ));
        parkCart.setOnAction(event -> sellScreenCoordinator.parkCurrentCart(
                parseLong(cashierUserId.getText()),
                parseLong(terminalDeviceId.getText()),
                parkNote.getText()
        ));

        PosButton listParked = accent(i18n, "List Parked");
        listParked.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get() || !sellScreenCoordinator.parkAuthorizedProperty().get(),
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.parkAuthorizedProperty()
        ));
        listParked.setOnAction(event -> sellScreenCoordinator.listParkedCarts(
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText())
        ));

        ListView<ParkedSaleCartSummaryResponse> parkedCarts = new ListView<>();
        parkedCarts.setPrefHeight(140);
        sellScreenCoordinator.parkedCartsStateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                parkedCarts.setItems(FXCollections.emptyObservableList());
                return;
            }
            parkedCarts.setItems(FXCollections.observableArrayList(newValue));
        });
        parkedCarts.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ParkedSaleCartSummaryResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(i18n.translate(item.cartId()
                        + " | ref=" + item.referenceCode()
                        + " | total=" + defaultMoney(item.totalPayable())
                        + " | parkedAt=" + item.parkedAt()));
            }
        });

        PosTextField resumeCartId = input(i18n, "Parked cart ID");
        PosButton resumeParked = primary(i18n, "Resume Parked");
        resumeParked.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get() || !sellScreenCoordinator.parkAuthorizedProperty().get(),
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.parkAuthorizedProperty()
        ));
        resumeParked.setOnAction(event -> sellScreenCoordinator.resumeParkedCart(
                parseLong(resumeCartId.getText()),
                parseLong(cashierUserId.getText()),
                parseLong(terminalDeviceId.getText())
        ));

        PosTextField overrideLineId = input(i18n, "Line ID for void/override");
        PosTextField overrideReasonCode = input(i18n, "Override reason code");
        PosTextField overrideNote = input(i18n, "Override note (optional)");
        PosTextField overrideUnitPrice = input(i18n, "Override unit price");

        PosButton voidLine = accent(i18n, "Void Line");
        voidLine.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get() || !sellScreenCoordinator.overrideAuthorizedProperty().get(),
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.overrideAuthorizedProperty()
        ));
        voidLine.setOnAction(event -> sellScreenCoordinator.voidLine(
                parseLong(overrideLineId.getText()),
                parseLong(cashierUserId.getText()),
                parseLong(terminalDeviceId.getText()),
                overrideReasonCode.getText(),
                overrideNote.getText()
        ));

        PosButton priceOverride = accent(i18n, "Override Price");
        priceOverride.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get() || !sellScreenCoordinator.overrideAuthorizedProperty().get(),
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.overrideAuthorizedProperty()
        ));
        priceOverride.setOnAction(event -> sellScreenCoordinator.overrideLinePrice(
                parseLong(overrideLineId.getText()),
                parseLong(cashierUserId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseDecimal(overrideUnitPrice.getText()),
                overrideReasonCode.getText(),
                overrideNote.getText()
        ));

        PosButton continueToCheckout = accent(i18n, "Go To Checkout");
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
                label(i18n, "Sell workstation: create/load cart, scan, search, and edit lines"),
                cartSummary,
                feedback,
                label(i18n, "Cart context"),
                cashierUserId,
                storeLocationId,
                terminalDeviceId,
                new HBox(8, createCart, loadCartId, loadCart),
                label(i18n, "Barcode scanner flow"),
                merchantId,
                barcode,
                scanQuantity,
                label(i18n, "Product search"),
                searchQuery,
                searchPage,
                new HBox(8, searchButton, previousPage, nextPage),
                searchResults,
                addQuantity,
                addSelected,
                label(i18n, "Cart lines"),
                cartLines,
                label(i18n, "Edit line quantity"),
                editLineId,
                editQuantity,
                updateLine,
                label(i18n, "Remove line"),
                removeLineId,
                label(i18n, "Suspended sales"),
                new HBox(8, loadSellPermissions, parkCart, listParked),
                parkNote,
                parkedCarts,
                resumeCartId,
                resumeParked,
                label(i18n, "Sensitive line controls"),
                overrideLineId,
                overrideReasonCode,
                overrideNote,
                overrideUnitPrice,
                new HBox(8, voidLine, priceOverride),
                new HBox(8, removeLine, recalculate, continueToCheckout)
        );
    }

    private static void renderCheckout(UiI18n i18n, VBox screenBody,
                                       SellScreenCoordinator sellScreenCoordinator,
                                       ShiftControlCoordinator shiftControlCoordinator,
                                       NavigationState navigationState) {
        Label cartSummary = new Label();
        cartSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toCartSummary(i18n, sellScreenCoordinator.cartState()),
                sellScreenCoordinator.cartStateProperty(),
                i18n.languageProperty()
        ));

        Label checkoutSummary = new Label();
        checkoutSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toCheckoutSummary(i18n, sellScreenCoordinator.checkoutState()),
                sellScreenCoordinator.checkoutStateProperty(),
                i18n.languageProperty()
        ));

        Label feedback = new Label();
        feedback.textProperty().bind(i18n.bindTranslated(sellScreenCoordinator.sellMessageProperty()));

        PosTextField cashierUserId = input(i18n, "Cashier user ID");
        PosTextField terminalDeviceId = input(i18n, "Terminal device ID");
        PosTextField cashAmount = input(i18n, "Cash amount");
        PosTextField cashTendered = input(i18n, "Cash tendered");
        PosTextField cardAmount = input(i18n, "Card amount");
        PosTextField cardReference = input(i18n, "Card reference");

        Label tenderPreview = new Label();
        tenderPreview.textProperty().bind(Bindings.createStringBinding(() -> {
                    SaleCartResponse cart = sellScreenCoordinator.cartState();
                    if (cart == null) {
                        return i18n.translate("No cart loaded.");
                    }
                    BigDecimal payable = cart.totalPayable() == null ? BigDecimal.ZERO : cart.totalPayable();
                    BigDecimal cash = parseMoney(cashAmount.getText());
                    BigDecimal card = parseMoney(cardAmount.getText());
                    BigDecimal tendered = parseMoney(cashTendered.getText());
                    BigDecimal allocated = (cash == null ? BigDecimal.ZERO : cash).add(card == null ? BigDecimal.ZERO : card);
                    BigDecimal due = payable.subtract(allocated);
                    BigDecimal changeEstimate = (tendered == null ? BigDecimal.ZERO : tendered).subtract(cash == null ? BigDecimal.ZERO : cash);
                    return i18n.translate("Payable=" + defaultMoney(payable)
                            + " | Allocated=" + defaultMoney(allocated)
                            + " | Due=" + defaultMoney(due)
                            + " | Est. Change=" + defaultMoney(changeEstimate.max(BigDecimal.ZERO)));
                },
                sellScreenCoordinator.cartStateProperty(),
                cashAmount.textProperty(),
                cardAmount.textProperty(),
                cashTendered.textProperty(),
                i18n.languageProperty()
        ));

        PosButton submitCheckout = primary(i18n, "Complete Checkout");
        submitCheckout.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sellScreenCoordinator.busyProperty().get()
                        || sellScreenCoordinator.cartState() == null
                        || sellScreenCoordinator.cartState().status() != SaleCartStatus.ACTIVE,
                sellScreenCoordinator.busyProperty(),
                sellScreenCoordinator.cartStateProperty()
        ));
        submitCheckout.setOnAction(event -> sellScreenCoordinator.checkout(
                parseLong(cashierUserId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseMoney(cashAmount.getText()),
                parseMoney(cashTendered.getText()),
                parseMoney(cardAmount.getText()),
                cardReference.getText()
        ));

        PosButton backToSell = accent(i18n, "Back To Sell");
        backToSell.disableProperty().bind(sellScreenCoordinator.busyProperty());
        backToSell.setOnAction(event -> navigationState.navigate(NavigationTarget.SELL));

        if (shiftControlCoordinator.shiftState() != null) {
            cashierUserId.setText(Long.toString(shiftControlCoordinator.shiftState().cashierUserId()));
            terminalDeviceId.setText(Long.toString(shiftControlCoordinator.shiftState().terminalDeviceId()));
        } else if (sellScreenCoordinator.cartState() != null) {
            cashierUserId.setText(Long.toString(sellScreenCoordinator.cartState().cashierUserId()));
            terminalDeviceId.setText(Long.toString(sellScreenCoordinator.cartState().terminalDeviceId()));
        }

        screenBody.getChildren().addAll(
                label(i18n, "Checkout workstation: capture tenders, verify due/change, and commit sale"),
                cartSummary,
                checkoutSummary,
                feedback,
                label(i18n, "Checkout context"),
                cashierUserId,
                terminalDeviceId,
                label(i18n, "Tenders"),
                cashAmount,
                cashTendered,
                cardAmount,
                cardReference,
                tenderPreview,
                new HBox(8, submitCheckout, backToSell)
        );
    }

    private static void renderReturns(UiI18n i18n, VBox screenBody,
                                      ReturnsScreenCoordinator returnsScreenCoordinator) {
        Label lookupSummary = new Label();
        lookupSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toReturnLookupSummary(i18n, returnsScreenCoordinator.lookupState()),
                returnsScreenCoordinator.lookupStateProperty(),
                i18n.languageProperty()
        ));

        Label returnSummary = new Label();
        returnSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toReturnSubmitSummary(i18n, returnsScreenCoordinator.submitState()),
                returnsScreenCoordinator.submitStateProperty(),
                i18n.languageProperty()
        ));

        Label feedback = new Label();
        feedback.textProperty().bind(i18n.bindTranslated(returnsScreenCoordinator.returnsMessageProperty()));

        Label managerHint = label(i18n, "Manager approval required: have a manager sign in and retry this return.");
        managerHint.visibleProperty().bind(returnsScreenCoordinator.managerApprovalRequiredProperty());
        managerHint.managedProperty().bind(managerHint.visibleProperty());

        PosTextField receiptNumber = input(i18n, "Receipt number");
        PosButton lookup = primary(i18n, "Lookup Receipt");
        lookup.disableProperty().bind(returnsScreenCoordinator.busyProperty());
        lookup.setOnAction(event -> returnsScreenCoordinator.lookupByReceipt(receiptNumber.getText()));
        receiptNumber.setOnAction(event -> returnsScreenCoordinator.lookupByReceipt(receiptNumber.getText()));

        ListView<SaleReturnLookupLineResponse> returnLines = new ListView<>();
        returnLines.setPrefHeight(180);
        returnsScreenCoordinator.lookupStateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.lines() == null) {
                returnLines.setItems(FXCollections.emptyObservableList());
                return;
            }
            returnLines.setItems(FXCollections.observableArrayList(newValue.lines()));
        });
        returnLines.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(SaleReturnLookupLineResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(i18n.translate("#" + item.saleLineId()
                        + " | line=" + item.lineNumber()
                        + " | product=" + item.productId()
                        + " | sold=" + item.quantitySold()
                        + " | returned=" + item.quantityReturned()
                        + " | available=" + item.quantityAvailable()
                        + " | gross=" + defaultMoney(item.grossAmount())));
            }
        });

        PosTextField saleLineId = input(i18n, "Sale line ID");
        PosTextField quantity = input(i18n, "Return quantity");
        PosTextField reasonCode = input(i18n, "Reason code (e.g. DAMAGED)");
        PosTextField refundTenderType = input(i18n, "Refund tender type: CASH or CARD");
        refundTenderType.setText(TenderType.CASH.name());
        PosTextField refundReference = input(i18n, "Refund reference (optional)");
        PosTextField note = input(i18n, "Note (optional)");

        PosButton useSelected = accent(i18n, "Use Selected Line");
        useSelected.disableProperty().bind(returnsScreenCoordinator.busyProperty());
        useSelected.setOnAction(event -> {
            SaleReturnLookupLineResponse selected = returnLines.getSelectionModel().getSelectedItem();
            if (selected == null) {
                returnsScreenCoordinator.returnsMessageProperty().set(i18n.translate("Select a return line first."));
                return;
            }
            saleLineId.setText(Long.toString(selected.saleLineId()));
            quantity.setText(selected.quantityAvailable().toPlainString());
        });

        PosButton submit = primary(i18n, "Submit Return");
        submit.disableProperty().bind(returnsScreenCoordinator.busyProperty());
        submit.setOnAction(event -> returnsScreenCoordinator.submitReturn(
                parseLong(saleLineId.getText()),
                parseDecimal(quantity.getText()),
                reasonCode.getText(),
                parseTenderType(refundTenderType.getText()),
                refundReference.getText(),
                note.getText()
        ));

        screenBody.getChildren().addAll(
                label(i18n, "Returns workstation: lookup receipt, review eligible lines, and submit refunds"),
                lookupSummary,
                returnSummary,
                feedback,
                managerHint,
                receiptNumber,
                lookup,
                returnLines,
                new HBox(8, useSelected, saleLineId),
                quantity,
                reasonCode,
                refundTenderType,
                refundReference,
                note,
                submit
        );
    }

    private static void renderBackoffice(UiI18n i18n, VBox screenBody,
                                         BackofficeCoordinator backofficeCoordinator) {
        Label feedback = new Label();
        feedback.textProperty().bind(i18n.bindTranslated(backofficeCoordinator.backofficeMessageProperty()));

        Label priceSummary = new Label();
        priceSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toPriceSummary(i18n, backofficeCoordinator.priceResolutionProperty().get()),
                backofficeCoordinator.priceResolutionProperty(),
                i18n.languageProperty()
        ));

        Label supplierReturnSummary = new Label();
        supplierReturnSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toSupplierReturnSummary(i18n, backofficeCoordinator.supplierReturnProperty().get()),
                backofficeCoordinator.supplierReturnProperty(),
                i18n.languageProperty()
        ));

        PosTextField catalogMerchantId = input(i18n, "Catalog merchant ID");
        PosTextField productQuery = input(i18n, "Catalog search (SKU/name)");
        PosButton loadProducts = primary(i18n, "Load Products");
        loadProducts.disableProperty().bind(backofficeCoordinator.busyProperty());
        loadProducts.setOnAction(event -> backofficeCoordinator.loadProducts(
                parseLong(catalogMerchantId.getText()),
                productQuery.getText()
        ));

        ListView<ProductResponse> productList = new ListView<>();
        productList.setPrefHeight(140);
        backofficeCoordinator.productsProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                productList.setItems(FXCollections.emptyObservableList());
                return;
            }
            productList.setItems(FXCollections.observableArrayList(newValue));
        });
        productList.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ProductResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(i18n.translate(item.id()
                        + " | " + item.sku()
                        + " | " + item.name()
                        + " | " + defaultMoney(item.basePrice())
                        + " | " + (item.active() ? "ACTIVE" : "INACTIVE")));
            }
        });

        PosTextField productId = input(i18n, "Product ID (for update)");
        PosTextField productSku = input(i18n, "SKU");
        PosTextField productName = input(i18n, "Name");
        PosTextField productBasePrice = input(i18n, "Base price");
        PosTextField productBarcodes = input(i18n, "Barcode(s), comma-separated");
        PosButton createProduct = primary(i18n, "Create Product");
        PosButton updateProduct = accent(i18n, "Update Product");
        createProduct.disableProperty().bind(backofficeCoordinator.busyProperty());
        updateProduct.disableProperty().bind(backofficeCoordinator.busyProperty());
        createProduct.setOnAction(event -> backofficeCoordinator.saveProduct(
                null,
                parseLong(catalogMerchantId.getText()),
                productSku.getText(),
                productName.getText(),
                parseMoney(productBasePrice.getText()),
                productBarcodes.getText()
        ));
        updateProduct.setOnAction(event -> backofficeCoordinator.saveProduct(
                parseLong(productId.getText()),
                parseLong(catalogMerchantId.getText()),
                productSku.getText(),
                productName.getText(),
                parseMoney(productBasePrice.getText()),
                productBarcodes.getText()
        ));

        PosTextField customerMerchantId = input(i18n, "Customer merchant ID");
        PosButton loadCustomers = primary(i18n, "Load Customers");
        loadCustomers.disableProperty().bind(backofficeCoordinator.busyProperty());
        loadCustomers.setOnAction(event -> backofficeCoordinator.loadCustomers(parseLong(customerMerchantId.getText())));

        PosTextField lookupDocumentType = input(i18n, "Lookup document type");
        PosTextField lookupDocumentValue = input(i18n, "Lookup document value");
        PosTextField lookupEmail = input(i18n, "Lookup email");
        PosTextField lookupPhone = input(i18n, "Lookup phone");
        PosButton lookupCustomers = accent(i18n, "Lookup Customers");
        lookupCustomers.disableProperty().bind(backofficeCoordinator.busyProperty());
        lookupCustomers.setOnAction(event -> backofficeCoordinator.lookupCustomers(
                parseLong(customerMerchantId.getText()),
                lookupDocumentType.getText(),
                lookupDocumentValue.getText(),
                lookupEmail.getText(),
                lookupPhone.getText()
        ));

        ListView<CustomerResponse> customerList = new ListView<>();
        customerList.setPrefHeight(140);
        backofficeCoordinator.customersProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                customerList.setItems(FXCollections.emptyObservableList());
                return;
            }
            customerList.setItems(FXCollections.observableArrayList(newValue));
        });
        customerList.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(CustomerResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(i18n.translate(item.id()
                        + " | " + defaultText(item.displayName())
                        + " | invoiceRequired=" + item.invoiceRequired()
                        + " | creditEnabled=" + item.creditEnabled()
                        + " | " + (item.active() ? "ACTIVE" : "INACTIVE")));
            }
        });

        PosTextField customerId = input(i18n, "Customer ID (for update)");
        PosTextField customerName = input(i18n, "Display name");
        PosTextField customerInvoiceRequired = input(i18n, "Invoice required (true/false)");
        customerInvoiceRequired.setText("false");
        PosTextField customerCreditEnabled = input(i18n, "Credit enabled (true/false)");
        customerCreditEnabled.setText("false");
        PosTextField customerDocumentType = input(i18n, "Document type (optional)");
        PosTextField customerDocumentValue = input(i18n, "Document value (optional)");
        PosTextField customerEmail = input(i18n, "Primary email (optional)");
        PosTextField customerPhone = input(i18n, "Primary phone (optional)");
        PosButton createCustomer = primary(i18n, "Create Customer");
        PosButton updateCustomer = accent(i18n, "Update Customer");
        createCustomer.disableProperty().bind(backofficeCoordinator.busyProperty());
        updateCustomer.disableProperty().bind(backofficeCoordinator.busyProperty());
        createCustomer.setOnAction(event -> backofficeCoordinator.saveCustomer(
                null,
                parseLong(customerMerchantId.getText()),
                customerName.getText(),
                parseBoolean(customerInvoiceRequired.getText()),
                parseBoolean(customerCreditEnabled.getText()),
                customerDocumentType.getText(),
                customerDocumentValue.getText(),
                customerEmail.getText(),
                customerPhone.getText()
        ));
        updateCustomer.setOnAction(event -> backofficeCoordinator.saveCustomer(
                parseLong(customerId.getText()),
                parseLong(customerMerchantId.getText()),
                customerName.getText(),
                parseBoolean(customerInvoiceRequired.getText()),
                parseBoolean(customerCreditEnabled.getText()),
                customerDocumentType.getText(),
                customerDocumentValue.getText(),
                customerEmail.getText(),
                customerPhone.getText()
        ));

        PosTextField priceStoreId = input(i18n, "Store location ID");
        PosTextField priceProductId = input(i18n, "Product ID");
        PosTextField priceCustomerId = input(i18n, "Customer ID (optional)");
        PosButton resolvePrice = primary(i18n, "Resolve Store Price");
        resolvePrice.disableProperty().bind(backofficeCoordinator.busyProperty());
        resolvePrice.setOnAction(event -> backofficeCoordinator.resolveStorePrice(
                parseLong(priceStoreId.getText()),
                parseLong(priceProductId.getText()),
                parseLong(priceCustomerId.getText())
        ));

        PosTextField lotStoreLocationId = input(i18n, "Lot balance store location ID");
        PosTextField lotProductId = input(i18n, "Lot balance product ID (optional)");
        CheckBox lotLevel = checkBox(i18n, "Lot-level balances");
        lotLevel.setSelected(true);
        PosButton loadBalances = primary(i18n, "Load Lot/Expiry Balances");
        loadBalances.disableProperty().bind(backofficeCoordinator.busyProperty());
        loadBalances.setOnAction(event -> backofficeCoordinator.loadInventoryBalances(
                parseLong(lotStoreLocationId.getText()),
                parseLong(lotProductId.getText()),
                lotLevel.isSelected()
        ));

        ListView<InventoryStockBalanceResponse> balanceList = new ListView<>();
        balanceList.setPrefHeight(120);
        backofficeCoordinator.inventoryBalancesProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                balanceList.setItems(FXCollections.emptyObservableList());
                return;
            }
            balanceList.setItems(FXCollections.observableArrayList(newValue));
        });
        balanceList.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(InventoryStockBalanceResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(i18n.translate("store=" + item.storeLocationId()
                        + " | product=" + item.productId()
                        + " | lot=" + defaultText(item.lotCode())
                        + " | expiry=" + (item.expiryDate() == null ? "-" : item.expiryDate().toString())
                        + " | state=" + (item.expiryState() == null ? "-" : item.expiryState().name())
                        + " | qty=" + defaultMoney(item.quantityOnHand())));
            }
        });

        PosTextField supplierReturnId = input(i18n, "Supplier return ID");
        PosTextField supplierReturnSupplierId = input(i18n, "Supplier ID");
        PosTextField supplierReturnStoreId = input(i18n, "Return store location ID");
        PosTextField supplierReturnProductId = input(i18n, "Return product ID");
        PosTextField supplierReturnQuantity = input(i18n, "Return quantity");
        PosTextField supplierReturnUnitCost = input(i18n, "Return unit cost");
        PosTextField supplierReturnNote = input(i18n, "Supplier return note");

        PosButton createSupplierReturn = primary(i18n, "Create Supplier Return");
        PosButton loadSupplierReturn = accent(i18n, "Load Supplier Return");
        PosButton approveSupplierReturn = accent(i18n, "Approve Return");
        PosButton postSupplierReturn = accent(i18n, "Post Return");
        createSupplierReturn.disableProperty().bind(backofficeCoordinator.busyProperty());
        loadSupplierReturn.disableProperty().bind(backofficeCoordinator.busyProperty());
        approveSupplierReturn.disableProperty().bind(backofficeCoordinator.busyProperty());
        postSupplierReturn.disableProperty().bind(backofficeCoordinator.busyProperty());

        createSupplierReturn.setOnAction(event -> backofficeCoordinator.createSupplierReturn(
                parseLong(supplierReturnSupplierId.getText()),
                parseLong(supplierReturnStoreId.getText()),
                parseLong(supplierReturnProductId.getText()),
                parseDecimal(supplierReturnQuantity.getText()),
                parseMoney(supplierReturnUnitCost.getText()),
                supplierReturnNote.getText()
        ));
        loadSupplierReturn.setOnAction(event -> backofficeCoordinator.loadSupplierReturn(parseLong(supplierReturnId.getText())));
        approveSupplierReturn.setOnAction(event -> backofficeCoordinator.approveSupplierReturn(
                parseLong(supplierReturnId.getText()),
                supplierReturnNote.getText()
        ));
        postSupplierReturn.setOnAction(event -> backofficeCoordinator.postSupplierReturn(
                parseLong(supplierReturnId.getText()),
                supplierReturnNote.getText()
        ));

        screenBody.getChildren().addAll(
                label(i18n, "Backoffice workspace: catalog, pricing, customers, lot/expiry inventory, and supplier returns"),
                feedback,
                label(i18n, "Catalog"),
                catalogMerchantId,
                productQuery,
                new HBox(8, loadProducts),
                productList,
                productId,
                productSku,
                productName,
                productBasePrice,
                productBarcodes,
                new HBox(8, createProduct, updateProduct),
                label(i18n, "Customers"),
                customerMerchantId,
                new HBox(8, loadCustomers),
                lookupDocumentType,
                lookupDocumentValue,
                lookupEmail,
                lookupPhone,
                new HBox(8, lookupCustomers),
                customerList,
                customerId,
                customerName,
                customerInvoiceRequired,
                customerCreditEnabled,
                customerDocumentType,
                customerDocumentValue,
                customerEmail,
                customerPhone,
                new HBox(8, createCustomer, updateCustomer),
                label(i18n, "Pricing"),
                priceStoreId,
                priceProductId,
                priceCustomerId,
                new HBox(8, resolvePrice),
                priceSummary,
                label(i18n, "Lot/Expiry inventory"),
                lotStoreLocationId,
                lotProductId,
                lotLevel,
                new HBox(8, loadBalances),
                balanceList,
                label(i18n, "Supplier returns"),
                supplierReturnId,
                supplierReturnSupplierId,
                supplierReturnStoreId,
                supplierReturnProductId,
                supplierReturnQuantity,
                supplierReturnUnitCost,
                supplierReturnNote,
                new HBox(8, createSupplierReturn, loadSupplierReturn, approveSupplierReturn, postSupplierReturn),
                supplierReturnSummary
        );
    }

    private static void renderReporting(UiI18n i18n, VBox screenBody,
                                        ReportingCoordinator reportingCoordinator) {
        Label feedback = new Label();
        feedback.textProperty().bind(i18n.bindTranslated(reportingCoordinator.reportingMessageProperty()));

        Label summary = new Label();
        summary.textProperty().bind(i18n.bindTranslated(reportingCoordinator.reportSummaryProperty()));

        PosTextField from = input(i18n, "From (ISO-8601, optional)");
        PosTextField to = input(i18n, "To (ISO-8601, optional)");
        PosTextField storeLocationId = input(i18n, "Store location ID (optional)");
        PosTextField terminalDeviceId = input(i18n, "Terminal device ID (optional)");
        PosTextField cashierUserId = input(i18n, "Cashier user ID (optional)");
        PosTextField categoryId = input(i18n, "Category ID (optional)");
        PosTextField taxGroupId = input(i18n, "Tax group ID (optional)");
        PosTextField supplierId = input(i18n, "Supplier ID (inventory optional)");
        PosTextField reasonCode = input(i18n, "Exception reason code (optional)");
        PosTextField eventType = input(i18n, "Exception event type (optional)");

        ListView<String> reportRows = new ListView<>();
        reportRows.setPrefHeight(280);
        reportingCoordinator.tableRowsProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                reportRows.setItems(FXCollections.emptyObservableList());
                return;
            }
            reportRows.setItems(FXCollections.observableArrayList(newValue.stream().map(i18n::translate).toList()));
        });

        PosButton loadSales = primary(i18n, "Load Sales/Returns");
        loadSales.disableProperty().bind(reportingCoordinator.busyProperty());
        loadSales.setOnAction(event -> reportingCoordinator.loadSalesReturns(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseLong(cashierUserId.getText()),
                parseLong(categoryId.getText()),
                parseLong(taxGroupId.getText())
        ));

        PosButton loadInventoryMovements = accent(i18n, "Load Inventory Moves");
        loadInventoryMovements.disableProperty().bind(reportingCoordinator.busyProperty());
        loadInventoryMovements.setOnAction(event -> reportingCoordinator.loadInventoryMovements(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(categoryId.getText()),
                parseLong(supplierId.getText())
        ));

        PosButton loadCashShifts = accent(i18n, "Load Cash Shifts");
        loadCashShifts.disableProperty().bind(reportingCoordinator.busyProperty());
        loadCashShifts.setOnAction(event -> reportingCoordinator.loadCashShifts(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseLong(cashierUserId.getText())
        ));

        PosButton loadExceptions = accent(i18n, "Load Exceptions");
        loadExceptions.disableProperty().bind(reportingCoordinator.busyProperty());
        loadExceptions.setOnAction(event -> reportingCoordinator.loadExceptions(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseLong(cashierUserId.getText()),
                reasonCode.getText(),
                parseExceptionEventType(eventType.getText())
        ));

        PosButton exportSales = primary(i18n, "Export Sales CSV");
        exportSales.disableProperty().bind(reportingCoordinator.busyProperty());
        exportSales.setOnAction(event -> reportingCoordinator.exportSalesReturns(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseLong(cashierUserId.getText()),
                parseLong(categoryId.getText()),
                parseLong(taxGroupId.getText())
        ));

        PosButton exportInventory = accent(i18n, "Export Inventory CSV");
        exportInventory.disableProperty().bind(reportingCoordinator.busyProperty());
        exportInventory.setOnAction(event -> reportingCoordinator.exportInventoryMovements(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(categoryId.getText()),
                parseLong(supplierId.getText())
        ));

        PosButton exportCash = accent(i18n, "Export Cash CSV");
        exportCash.disableProperty().bind(reportingCoordinator.busyProperty());
        exportCash.setOnAction(event -> reportingCoordinator.exportCashShifts(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseLong(cashierUserId.getText())
        ));

        PosButton exportExceptions = accent(i18n, "Export Exceptions CSV");
        exportExceptions.disableProperty().bind(reportingCoordinator.busyProperty());
        exportExceptions.setOnAction(event -> reportingCoordinator.exportExceptions(
                parseInstant(from.getText()),
                parseInstant(to.getText()),
                parseLong(storeLocationId.getText()),
                parseLong(terminalDeviceId.getText()),
                parseLong(cashierUserId.getText()),
                reasonCode.getText(),
                parseExceptionEventType(eventType.getText())
        ));

        screenBody.getChildren().addAll(
                label(i18n, "Reporting workspace: filtered report loads with streaming preview and CSV export actions"),
                feedback,
                summary,
                from,
                to,
                storeLocationId,
                terminalDeviceId,
                cashierUserId,
                categoryId,
                taxGroupId,
                supplierId,
                reasonCode,
                eventType,
                new HBox(8, loadSales, loadInventoryMovements),
                new HBox(8, loadCashShifts, loadExceptions),
                new HBox(8, exportSales, exportInventory),
                new HBox(8, exportCash, exportExceptions),
                reportRows
        );
    }

    private static void renderHardware(UiI18n i18n, VBox screenBody,
                                       HardwareCoordinator hardwareCoordinator,
                                       AppStateStore appStateStore) {
        if (appStateStore.isAuthenticated() && hardwareCoordinator.printStatusProperty().get() == HardwareActionStatus.IDLE) {
            hardwareCoordinator.refreshPermissions();
        }

        Label feedback = new Label();
        feedback.textProperty().bind(i18n.bindTranslated(hardwareCoordinator.hardwareMessageProperty()));

        Label printStatus = new Label();
        printStatus.textProperty().bind(Bindings.createStringBinding(
                () -> toPrintSummary(i18n,
                        hardwareCoordinator.printStatusProperty().get(),
                        hardwareCoordinator.printResponseProperty().get()),
                hardwareCoordinator.printStatusProperty(),
                hardwareCoordinator.printResponseProperty(),
                i18n.languageProperty()
        ));

        Label drawerStatus = new Label();
        drawerStatus.textProperty().bind(Bindings.createStringBinding(
                () -> toDrawerSummary(i18n,
                        hardwareCoordinator.drawerStatusProperty().get(),
                        hardwareCoordinator.drawerResponseProperty().get()),
                hardwareCoordinator.drawerStatusProperty(),
                hardwareCoordinator.drawerResponseProperty(),
                i18n.languageProperty()
        ));

        Label journalSummary = new Label();
        journalSummary.textProperty().bind(Bindings.createStringBinding(
                () -> toReceiptJournalSummary(i18n, hardwareCoordinator.receiptJournalProperty().get()),
                hardwareCoordinator.receiptJournalProperty(),
                i18n.languageProperty()
        ));

        PosButton refreshPermissions = accent(i18n, "Refresh Hardware Access");
        refreshPermissions.disableProperty().bind(hardwareCoordinator.busyProperty());
        refreshPermissions.setOnAction(event -> hardwareCoordinator.refreshPermissions());

        PosTextField receiptNumber = input(i18n, "Receipt number");
        CheckBox copy = checkBox(i18n, "Print as copy");
        PosButton printReceipt = primary(i18n, "Print Receipt");
        printReceipt.disableProperty().bind(hardwareCoordinator.busyProperty());
        printReceipt.setOnAction(event -> hardwareCoordinator.printReceipt(receiptNumber.getText(), copy.isSelected()));

        PosTextField journalReceiptNumber = input(i18n, "Journal lookup receipt number");
        PosTextField journalSaleId = input(i18n, "Journal lookup sale ID");
        PosButton lookupByReceipt = accent(i18n, "Lookup by Receipt");
        lookupByReceipt.disableProperty().bind(hardwareCoordinator.busyProperty());
        lookupByReceipt.setOnAction(event -> hardwareCoordinator.lookupReceiptJournalByNumber(journalReceiptNumber.getText()));
        PosButton lookupBySale = accent(i18n, "Lookup by Sale");
        lookupBySale.disableProperty().bind(hardwareCoordinator.busyProperty());
        lookupBySale.setOnAction(event -> hardwareCoordinator.lookupReceiptJournalBySaleId(parseLong(journalSaleId.getText())));

        PosButton reprintReceipt = primary(i18n, "Reprint Receipt");
        reprintReceipt.disableProperty().bind(Bindings.or(
                hardwareCoordinator.busyProperty(),
                Bindings.not(hardwareCoordinator.reprintAuthorizedProperty())
        ));
        reprintReceipt.visibleProperty().bind(hardwareCoordinator.reprintAuthorizedProperty());
        reprintReceipt.managedProperty().bind(reprintReceipt.visibleProperty());
        reprintReceipt.setOnAction(event -> hardwareCoordinator.reprintReceipt(journalReceiptNumber.getText()));

        Label reprintHiddenHint = label(i18n, "Receipt reprint is hidden because this user is not authorized.");
        reprintHiddenHint.visibleProperty().bind(Bindings.not(hardwareCoordinator.reprintAuthorizedProperty()));
        reprintHiddenHint.managedProperty().bind(reprintHiddenHint.visibleProperty());

        PosTextField terminalDeviceId = input(i18n, "Terminal device ID");
        PosTextField reasonCode = input(i18n, "Reason code");
        PosTextField note = input(i18n, "Note (optional)");
        PosTextField referenceNumber = input(i18n, "Reference number (optional)");
        PosButton openDrawer = primary(i18n, "Open Drawer");
        openDrawer.disableProperty().bind(Bindings.or(
                hardwareCoordinator.busyProperty(),
                Bindings.not(hardwareCoordinator.drawerAuthorizedProperty())
        ));
        openDrawer.visibleProperty().bind(hardwareCoordinator.drawerAuthorizedProperty());
        openDrawer.managedProperty().bind(openDrawer.visibleProperty());
        openDrawer.setOnAction(event -> hardwareCoordinator.openDrawer(
                parseLong(terminalDeviceId.getText()),
                reasonCode.getText(),
                note.getText(),
                referenceNumber.getText()
        ));

        Label drawerHiddenHint = label(i18n, "Drawer controls are hidden because this user is not authorized.");
        drawerHiddenHint.visibleProperty().bind(Bindings.not(hardwareCoordinator.drawerAuthorizedProperty()));
        drawerHiddenHint.managedProperty().bind(drawerHiddenHint.visibleProperty());

        screenBody.getChildren().addAll(
                label(i18n, "Hardware workspace: receipt print status and role-gated drawer controls"),
                feedback,
                refreshPermissions,
                label(i18n, "Receipt print"),
                receiptNumber,
                copy,
                printReceipt,
                printStatus,
                label(i18n, "Receipt journal + reprint"),
                journalReceiptNumber,
                journalSaleId,
                new HBox(8, lookupByReceipt, lookupBySale),
                reprintReceipt,
                reprintHiddenHint,
                journalSummary,
                label(i18n, "Cash drawer"),
                terminalDeviceId,
                reasonCode,
                note,
                referenceNumber,
                openDrawer,
                drawerHiddenHint,
                drawerStatus
        );
    }

    private static boolean isOpenShift(CashShiftResponse shift) {
        return shift != null && shift.status() == CashShiftStatus.OPEN;
    }

    private static String toCartSummary(UiI18n i18n, SaleCartResponse cart) {
        if (cart == null) {
            return i18n.translate("No cart loaded.");
        }
        int lineCount = cart.lines() == null ? 0 : cart.lines().size();
        return i18n.translate("Cart #" + cart.id()
                + " | status=" + cart.status()
                + " | lines=" + lineCount
                + " | subtotal=" + defaultMoney(cart.subtotalNet())
                + " | tax=" + defaultMoney(cart.totalTax())
                + " | rounding=" + defaultMoney(cart.roundingAdjustment())
                + " | payable=" + defaultMoney(cart.totalPayable()));
    }

    private static String toShiftSummary(UiI18n i18n, CashShiftResponse shift) {
        if (shift == null) {
            return i18n.translate("No shift loaded.");
        }
        return i18n.translate("Shift #" + shift.id()
                + " | status=" + shift.status()
                + " | opening=" + defaultMoney(shift.openingCash())
                + " | paidIn=" + defaultMoney(shift.totalPaidIn())
                + " | paidOut=" + defaultMoney(shift.totalPaidOut())
                + " | expectedClose=" + defaultMoney(shift.expectedCloseCash())
                + " | counted=" + defaultMoney(shift.countedCloseCash())
                + " | variance=" + defaultMoney(shift.varianceCash()));
    }

    private static String toCheckoutSummary(UiI18n i18n, SaleCheckoutResponse checkout) {
        if (checkout == null) {
            return i18n.translate("No completed checkout yet.");
        }
        return i18n.translate("Sale #" + checkout.saleId()
                + " | receipt=" + checkout.receiptNumber()
                + " | status=" + checkout.paymentStatus()
                + " | payable=" + defaultMoney(checkout.totalPayable())
                + " | allocated=" + defaultMoney(checkout.totalAllocated())
                + " | tendered=" + defaultMoney(checkout.totalTendered())
                + " | change=" + defaultMoney(checkout.changeAmount()));
    }

    private static String toReturnLookupSummary(UiI18n i18n, SaleReturnLookupResponse lookup) {
        if (lookup == null) {
            return i18n.translate("No receipt lookup loaded.");
        }
        int lineCount = lookup.lines() == null ? 0 : lookup.lines().size();
        return i18n.translate("Sale #" + lookup.saleId()
                + " | receipt=" + lookup.receiptNumber()
                + " | lines=" + lineCount
                + " | soldAt=" + lookup.soldAt());
    }

    private static String toReturnSubmitSummary(UiI18n i18n, SaleReturnResponse response) {
        if (response == null) {
            return i18n.translate("No return submitted yet.");
        }
        int lineCount = response.lines() == null ? 0 : response.lines().size();
        return i18n.translate("Return #" + response.saleReturnId()
                + " | ref=" + response.returnReference()
                + " | tender=" + response.refundTenderType()
                + " | lines=" + lineCount
                + " | gross=" + defaultMoney(response.totalGross()));
    }

    private static String toPriceSummary(UiI18n i18n, PriceResolutionResponse response) {
        if (response == null) {
            return i18n.translate("No price resolution requested yet.");
        }
        return i18n.translate("Store=" + response.storeLocationId()
                + " | product=" + response.productId()
                + " | resolved=" + defaultMoney(response.resolvedPrice())
                + " | source=" + response.source()
                + " | sourceId=" + response.sourceId());
    }

    private static String toSupplierReturnSummary(UiI18n i18n, SupplierReturnResponse response) {
        if (response == null) {
            return i18n.translate("No supplier return selected.");
        }
        int lineCount = response.lines() == null ? 0 : response.lines().size();
        return i18n.translate("SupplierReturn #" + response.id()
                + " | ref=" + defaultText(response.referenceNumber())
                + " | status=" + response.status()
                + " | lines=" + lineCount
                + " | totalCost=" + defaultMoney(response.totalCost()));
    }

    private static String toReceiptJournalSummary(UiI18n i18n, ReceiptJournalResponse response) {
        if (response == null) {
            return i18n.translate("No receipt journal lookup loaded.");
        }
        return i18n.translate("Sale #" + response.saleId()
                + " | receipt=" + defaultText(response.receiptNumber())
                + " | store=" + defaultText(response.storeLocationCode())
                + " | terminal=" + defaultText(response.terminalCode())
                + " | cashier=" + defaultText(response.cashierUsername())
                + " | soldAt=" + response.soldAt()
                + " | payable=" + defaultMoney(response.totalPayable()));
    }

    private static String toPrintSummary(UiI18n i18n, HardwareActionStatus status, ReceiptPrintResponse response) {
        if (status == null || status == HardwareActionStatus.IDLE) {
            return i18n.translate("Print status: idle.");
        }
        if (status == HardwareActionStatus.QUEUED) {
            return i18n.translate("Print status: queued.");
        }
        if (response == null) {
            return i18n.translate("Print status: " + status.name().toLowerCase());
        }
        return i18n.translate("Print status: " + status.name().toLowerCase()
                + " | receipt=" + defaultText(response.receiptNumber())
                + " | adapter=" + defaultText(response.adapter())
                + " | retryable=" + response.retryable());
    }

    private static String toDrawerSummary(UiI18n i18n, HardwareActionStatus status, CashDrawerOpenResponse response) {
        if (status == null || status == HardwareActionStatus.IDLE) {
            return i18n.translate("Drawer status: idle.");
        }
        if (status == HardwareActionStatus.QUEUED) {
            return i18n.translate("Drawer status: queued.");
        }
        if (response == null) {
            return i18n.translate("Drawer status: " + status.name().toLowerCase());
        }
        return i18n.translate("Drawer status: " + status.name().toLowerCase()
                + " | terminal=" + defaultText(response.terminalCode())
                + " | eventId=" + response.eventId()
                + " | retryable=" + response.retryable());
    }

    private static Label label(UiI18n i18n, String text) {
        return new Label(i18n.translate(text));
    }

    private static PosTextField input(UiI18n i18n, String promptText) {
        return new PosTextField(i18n.translate(promptText));
    }

    private static PosButton primary(UiI18n i18n, String text) {
        return PosButton.primary(i18n.translate(text));
    }

    private static PosButton accent(UiI18n i18n, String text) {
        return PosButton.accent(i18n.translate(text));
    }

    private static CheckBox checkBox(UiI18n i18n, String text) {
        return new CheckBox(i18n.translate(text));
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

    private static TenderType parseTenderType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TenderType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private static ExceptionReportEventType parseExceptionEventType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ExceptionReportEventType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("yes")
                || normalized.equals("y")
                || normalized.equals("si")
                || normalized.equals("s");
    }

    private static String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String formatExpiryValue(AuthSessionState session, UiI18n i18n) {
        if (session == null || session.accessTokenExpiresAt() == null) {
            return i18n.translate("n/a");
        }
        return EXPIRY_FORMATTER.format(session.accessTokenExpiresAt());
    }
}
