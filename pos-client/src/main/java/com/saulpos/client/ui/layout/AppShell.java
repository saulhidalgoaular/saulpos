package com.saulpos.client.ui.layout;

import com.saulpos.client.app.NavigationState;
import com.saulpos.client.app.NavigationTarget;
import com.saulpos.client.app.ScreenDefinition;
import com.saulpos.client.app.ScreenRegistry;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.state.AuthSessionCoordinator;
import com.saulpos.client.state.AuthSessionState;
import com.saulpos.client.ui.components.PosButton;
import com.saulpos.client.ui.components.PosTextField;
import com.saulpos.client.ui.components.ToastHost;
import javafx.geometry.Insets;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class AppShell {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private AppShell() {
    }

    public static Parent createRoot(AppStateStore stateStore,
                                    NavigationState navigationState,
                                    AuthSessionCoordinator authSessionCoordinator) {
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

        updateContent(navigationState.activeTarget(), title, description, screenBody, authSessionCoordinator, stateStore);
        navigationState.activeTargetProperty().addListener((obs, oldValue, newValue) -> {
            authSessionCoordinator.onNavigationChanged(newValue);
            updateContent(newValue, title, description, screenBody, authSessionCoordinator, stateStore);
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
                                      AppStateStore appStateStore) {
        ScreenDefinition screen = ScreenRegistry.byTarget(target)
                .orElseThrow(() -> new IllegalStateException("Screen not found: " + target));
        title.setText(screen.title());
        description.setText(screen.description());

        screenBody.getChildren().clear();
        if (target == NavigationTarget.LOGIN) {
            renderLogin(screenBody, authSessionCoordinator);
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

    private static String formatExpiryValue(AuthSessionState session) {
        if (session == null || session.accessTokenExpiresAt() == null) {
            return "n/a";
        }
        return EXPIRY_FORMATTER.format(session.accessTokenExpiresAt());
    }
}
