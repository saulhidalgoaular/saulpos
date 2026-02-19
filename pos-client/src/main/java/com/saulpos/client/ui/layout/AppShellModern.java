package com.saulpos.client.ui.layout;

import com.saulpos.client.app.NavigationState;
import com.saulpos.client.app.NavigationTarget;
import com.saulpos.client.app.ScreenDefinition;
import com.saulpos.client.app.ScreenRegistry;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.state.AuthSessionCoordinator;
import com.saulpos.client.state.AuthSessionState;
import com.saulpos.client.state.BackofficeCoordinator;
import com.saulpos.client.state.ConnectivityCoordinator;
import com.saulpos.client.state.HardwareCoordinator;
import com.saulpos.client.state.ReportingCoordinator;
import com.saulpos.client.state.ReturnsScreenCoordinator;
import com.saulpos.client.state.SellScreenCoordinator;
import com.saulpos.client.state.ShiftControlCoordinator;
import com.saulpos.client.ui.components.PosButton;
import com.saulpos.client.ui.i18n.UiI18n;
import com.saulpos.client.ui.i18n.UiLanguage;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

public final class AppShellModern {

    private static final Map<NavigationTarget, String> ICONS = Map.of(
            NavigationTarget.LOGIN, "LI",
            NavigationTarget.SHIFT_CONTROL, "SH",
            NavigationTarget.SELL, "SL",
            NavigationTarget.CHECKOUT, "CO",
            NavigationTarget.RETURNS, "RT",
            NavigationTarget.BACKOFFICE, "BO",
            NavigationTarget.REPORTING, "RP",
            NavigationTarget.HARDWARE, "HW"
    );

    private AppShellModern() {
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
        root.getStyleClass().add("pos2-shell");

        VBox nav = createNav(i18n, stateStore, navigationState);
        root.setLeft(nav);

        HBox topBar = createTopBar(i18n, stateStore, authSessionCoordinator, connectivityCoordinator);
        root.setTop(topBar);

        Label title = new Label();
        title.getStyleClass().add("pos2-title");

        Label description = new Label();
        description.getStyleClass().add("pos2-description");
        description.setWrapText(true);

        VBox screenBody = new VBox(10);

        ScrollPane contentScroll = new ScrollPane(screenBody);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.getStyleClass().add("pos2-content-scroll");

        VBox card = new VBox(12, title, description, contentScroll);
        card.getStyleClass().add("pos2-card");
        VBox.setVgrow(contentScroll, Priority.ALWAYS);

        StackPane center = new StackPane(card);
        center.setPadding(new Insets(16));
        root.setCenter(center);

        AppShell.updateContent(
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

        Map<NavigationTarget, Button> navButtons = mapNavButtons(nav);
        applyActiveNavClass(navButtons, navigationState.activeTarget());

        navigationState.activeTargetProperty().addListener((obs, oldValue, newValue) -> {
            authSessionCoordinator.onNavigationChanged(newValue);
            AppShell.updateContent(
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
            applyActiveNavClass(navButtons, newValue);
        });

        i18n.languageProperty().addListener((obs, oldValue, newValue) -> AppShell.updateContent(
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

        return root;
    }

    private static VBox createNav(UiI18n i18n, AppStateStore stateStore, NavigationState navigationState) {
        VBox nav = new VBox(10);
        nav.getStyleClass().add("pos2-nav");
        nav.setPrefWidth(88);
        nav.setMinWidth(88);

        Label navBrand = new Label("SP");
        navBrand.getStyleClass().add("pos2-nav-brand");
        nav.getChildren().add(navBrand);

        for (ScreenDefinition screen : ScreenRegistry.orderedScreens()) {
            Button button = new Button();
            button.getStyleClass().add("pos2-nav-btn");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setPrefHeight(66);
            button.setFocusTraversable(true);
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            Label icon = new Label(ICONS.getOrDefault(screen.target(), "--"));
            icon.getStyleClass().add("pos2-nav-icon");

            Label caption = new Label();
            caption.getStyleClass().add("pos2-nav-caption");
            caption.textProperty().bind(Bindings.createStringBinding(
                    () -> compactLabel(i18n.translate(screen.title())),
                    i18n.languageProperty()
            ));

            VBox graphic = new VBox(2, icon, caption);
            graphic.setAlignment(Pos.CENTER);
            button.setGraphic(graphic);

            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(Bindings.createStringBinding(
                    () -> i18n.translate(screen.title()),
                    i18n.languageProperty()
            ));
            button.setTooltip(tooltip);

            button.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> screen.requiresAuthenticatedSession() && !stateStore.isAuthenticated(),
                    stateStore.authenticatedProperty()
            ));

            button.setOnAction(event -> navigationState.navigate(screen.target()));
            button.setUserData(screen.target());
            nav.getChildren().add(button);
        }

        Region pushDown = new Region();
        VBox.setVgrow(pushDown, Priority.ALWAYS);
        nav.getChildren().add(pushDown);

        return nav;
    }

    private static HBox createTopBar(UiI18n i18n,
                                     AppStateStore stateStore,
                                     AuthSessionCoordinator authSessionCoordinator,
                                     ConnectivityCoordinator connectivityCoordinator) {
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("pos2-topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        String branchName = System.getProperty("saulpos.branch", "MAIN");
        Label brand = new Label("SaulPOS");
        brand.getStyleClass().add("pos2-brand");

        Label branch = new Label("Branch: " + branchName);
        branch.getStyleClass().add("pos2-branch");

        VBox brandBlock = new VBox(2, brand, branch);

        Label connectivityPill = new Label();
        connectivityPill.getStyleClass().addAll("pos2-pill", "pos2-pill-online");
        connectivityPill.textProperty().bind(Bindings.createStringBinding(
                () -> connectivityCoordinator.isOnline() ? "ONLINE" : "OFFLINE",
                connectivityCoordinator.onlineProperty()
        ));
        Tooltip connectivityTooltip = new Tooltip();
        connectivityTooltip.textProperty().bind(i18n.bindTranslated(connectivityCoordinator.connectivityMessageProperty()));
        connectivityPill.setTooltip(connectivityTooltip);
        connectivityCoordinator.onlineProperty().addListener((obs, oldValue, newValue) ->
                updateConnectivityPillClass(connectivityPill, newValue));
        updateConnectivityPillClass(connectivityPill, connectivityCoordinator.isOnline());

        PosButton refreshConnectivity = PosButton.accent(i18n.translate("Retry Connectivity"));
        refreshConnectivity.textProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("Retry Connectivity"),
                i18n.languageProperty()
        ));
        refreshConnectivity.disableProperty().bind(connectivityCoordinator.checkingProperty());
        refreshConnectivity.setOnAction(event -> connectivityCoordinator.refresh());

        Label authFeedback = new Label();
        authFeedback.getStyleClass().add("pos2-feedback");
        authFeedback.textProperty().bind(i18n.bindTranslated(authSessionCoordinator.sessionMessageProperty()));
        authFeedback.setMaxWidth(280);

        TextField searchField = new TextField();
        searchField.getStyleClass().add("pos2-search");
        searchField.setPromptText(i18n.translate("Search"));
        searchField.promptTextProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("Search"),
                i18n.languageProperty()
        ));

        Label searchShortcut = new Label("Ctrl+K");
        searchShortcut.getStyleClass().add("pos2-kbd");

        HBox searchWrap = new HBox(8, searchField, searchShortcut);
        searchWrap.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchWrap, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<UiLanguage> languagePicker = new ComboBox<>(FXCollections.observableArrayList(UiLanguage.values()));
        languagePicker.valueProperty().bindBidirectional(i18n.languageProperty());
        languagePicker.setPrefWidth(120);

        Label userBadge = new Label();
        userBadge.getStyleClass().addAll("pos2-pill", "pos2-user-badge");
        userBadge.textProperty().bind(Bindings.createStringBinding(
                () -> currentUserLabel(stateStore.sessionState(), stateStore.isAuthenticated()),
                stateStore.sessionStateProperty(),
                stateStore.authenticatedProperty()
        ));

        PosButton signOut = PosButton.accent(i18n.translate("Sign Out"));
        signOut.textProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("Sign Out"),
                i18n.languageProperty()
        ));
        signOut.disableProperty().bind(Bindings.not(stateStore.authenticatedProperty()));
        signOut.setOnAction(event -> authSessionCoordinator.logout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(
                brandBlock,
                connectivityPill,
                refreshConnectivity,
                authFeedback,
                searchWrap,
                spacer,
                languagePicker,
                userBadge,
                signOut
        );
        return topBar;
    }

    private static Map<NavigationTarget, Button> mapNavButtons(VBox nav) {
        Map<NavigationTarget, Button> navButtons = new EnumMap<>(NavigationTarget.class);
        for (javafx.scene.Node child : nav.getChildren()) {
            if (!(child instanceof Button button)) {
                continue;
            }
            if (button.getUserData() instanceof NavigationTarget target) {
                navButtons.put(target, button);
            }
        }
        return navButtons;
    }

    private static void applyActiveNavClass(Map<NavigationTarget, Button> navButtons, NavigationTarget activeTarget) {
        for (Map.Entry<NavigationTarget, Button> entry : navButtons.entrySet()) {
            Button button = entry.getValue();
            if (entry.getKey() == activeTarget) {
                if (!button.getStyleClass().contains("pos2-nav-btn-active")) {
                    button.getStyleClass().add("pos2-nav-btn-active");
                }
            } else {
                button.getStyleClass().remove("pos2-nav-btn-active");
            }
        }
    }

    private static void updateConnectivityPillClass(Label connectivityPill, boolean online) {
        connectivityPill.getStyleClass().remove("pos2-pill-online");
        connectivityPill.getStyleClass().remove("pos2-pill-offline");
        connectivityPill.getStyleClass().add(online ? "pos2-pill-online" : "pos2-pill-offline");
    }

    private static String compactLabel(String label) {
        if (label == null || label.isBlank()) {
            return "";
        }
        String[] parts = label.trim().split("\\s+");
        if (parts.length == 1) {
            return clip(parts[0], 6).toUpperCase();
        }
        return (clip(parts[0], 3) + clip(parts[1], 3)).toUpperCase();
    }

    private static String clip(String value, int size) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= size) {
            return value;
        }
        return value.substring(0, size);
    }

    private static String currentUserLabel(AuthSessionState sessionState, boolean authenticated) {
        if (!authenticated || sessionState == null || sessionState.username() == null || sessionState.username().isBlank()) {
            return "GUEST";
        }
        return sessionState.username().trim().toUpperCase();
    }
}
