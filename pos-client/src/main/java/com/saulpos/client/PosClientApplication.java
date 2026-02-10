package com.saulpos.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saulpos.client.api.HttpPosApiClient;
import com.saulpos.client.app.NavigationState;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.state.AuthSessionCoordinator;
import com.saulpos.client.ui.layout.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URI;

public class PosClientApplication extends Application {

    @Override
    public void start(Stage stage) {
        AppStateStore appStateStore = new AppStateStore();
        NavigationState navigationState = new NavigationState();
        URI apiBaseUri = URI.create(System.getProperty("saulpos.api.base-url", "http://localhost:8080"));
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        HttpPosApiClient apiClient = new HttpPosApiClient(apiBaseUri, objectMapper);
        AuthSessionCoordinator authSessionCoordinator = new AuthSessionCoordinator(apiClient, appStateStore, navigationState);

        Scene scene = new Scene(AppShell.createRoot(appStateStore, navigationState, authSessionCoordinator), 1180, 760);
        String stylesheet = getClass().getResource("/ui/theme/saulpos-theme.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("SaulPOS v2 Client");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
