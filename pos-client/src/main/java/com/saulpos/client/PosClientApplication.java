package com.saulpos.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saulpos.client.api.HttpPosApiClient;
import com.saulpos.client.app.NavigationState;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.state.AuthSessionCoordinator;
import com.saulpos.client.state.BackofficeCoordinator;
import com.saulpos.client.state.ConnectivityCoordinator;
import com.saulpos.client.state.HardwareCoordinator;
import com.saulpos.client.state.ReportingCoordinator;
import com.saulpos.client.state.ReturnsScreenCoordinator;
import com.saulpos.client.state.SellScreenCoordinator;
import com.saulpos.client.state.ShiftControlCoordinator;
import com.saulpos.client.ui.i18n.UiI18n;
import com.saulpos.client.ui.layout.AppShell;
import com.saulpos.client.ui.layout.AppShellModern;
import javafx.beans.binding.Bindings;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URI;
import java.util.Locale;

public class PosClientApplication extends Application {

    @Override
    public void start(Stage stage) {
        AppStateStore appStateStore = new AppStateStore();
        NavigationState navigationState = new NavigationState();
        URI apiBaseUri = URI.create(System.getProperty("saulpos.api.base-url", "http://localhost:8080"));
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        HttpPosApiClient apiClient = new HttpPosApiClient(apiBaseUri, objectMapper);
        ConnectivityCoordinator connectivityCoordinator = new ConnectivityCoordinator(apiClient);
        AuthSessionCoordinator authSessionCoordinator =
                new AuthSessionCoordinator(apiClient, appStateStore, navigationState, connectivityCoordinator);
        ShiftControlCoordinator shiftControlCoordinator = new ShiftControlCoordinator(apiClient);
        SellScreenCoordinator sellScreenCoordinator = new SellScreenCoordinator(apiClient, connectivityCoordinator);
        ReturnsScreenCoordinator returnsScreenCoordinator = new ReturnsScreenCoordinator(apiClient);
        BackofficeCoordinator backofficeCoordinator = new BackofficeCoordinator(apiClient);
        ReportingCoordinator reportingCoordinator = new ReportingCoordinator(apiClient);
        HardwareCoordinator hardwareCoordinator = new HardwareCoordinator(apiClient);
        UiI18n i18n = new UiI18n();
        connectivityCoordinator.refresh();

        String requestedUi = System.getProperty("saulpos.ui", "modern").toLowerCase(Locale.ROOT).trim();
        boolean useClassicUi = "classic".equals(requestedUi);

        Parent root = useClassicUi
                ? AppShell.createRoot(
                        i18n,
                        appStateStore,
                        navigationState,
                        authSessionCoordinator,
                        shiftControlCoordinator,
                        sellScreenCoordinator,
                        returnsScreenCoordinator,
                        backofficeCoordinator,
                        reportingCoordinator,
                        hardwareCoordinator,
                        connectivityCoordinator
                )
                : AppShellModern.createRoot(
                        i18n,
                        appStateStore,
                        navigationState,
                        authSessionCoordinator,
                        shiftControlCoordinator,
                        sellScreenCoordinator,
                        returnsScreenCoordinator,
                        backofficeCoordinator,
                        reportingCoordinator,
                        hardwareCoordinator,
                        connectivityCoordinator
                );

        Scene scene = new Scene(root, 1180, 760);
        String stylesheetPath = useClassicUi ? "/ui/theme/saulpos-theme.css" : "/ui/theme/saulpos-modern.css";
        String stylesheet = getClass().getResource(stylesheetPath).toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.titleProperty().bind(Bindings.createStringBinding(
                () -> i18n.translate("SaulPOS v2 Client"),
                i18n.languageProperty()
        ));
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
