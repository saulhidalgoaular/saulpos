package com.saulpos.client.ui.layout;

import com.saulpos.client.app.NavigationState;
import com.saulpos.client.app.NavigationTarget;
import com.saulpos.client.app.ScreenDefinition;
import com.saulpos.client.app.ScreenRegistry;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.ui.components.PosButton;
import com.saulpos.client.ui.components.PosTextField;
import com.saulpos.client.ui.components.ToastHost;
import javafx.geometry.Insets;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class AppShell {

    private AppShell() {
    }

    public static Parent createRoot(AppStateStore stateStore, NavigationState navigationState) {
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
        PosTextField searchInput = new PosTextField("Global action palette (planned)");

        updateContent(navigationState.activeTarget(), title, description);
        navigationState.activeTargetProperty().addListener((obs, oldValue, newValue) ->
                updateContent(newValue, title, description));

        content.getChildren().addAll(title, description, searchInput);

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
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(sessionBadge, spacer, toastHost);

        root.setLeft(nav);
        root.setTop(top);
        root.setCenter(content);
        return root;
    }

    private static void updateContent(NavigationTarget target, Label title, Label description) {
        ScreenDefinition screen = ScreenRegistry.byTarget(target)
                .orElseThrow(() -> new IllegalStateException("Screen not found: " + target));
        title.setText(screen.title());
        description.setText(screen.description());
    }
}
