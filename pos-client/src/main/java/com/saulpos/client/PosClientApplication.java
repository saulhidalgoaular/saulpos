package com.saulpos.client;

import com.saulpos.client.app.NavigationState;
import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.ui.layout.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PosClientApplication extends Application {

    @Override
    public void start(Stage stage) {
        AppStateStore appStateStore = new AppStateStore();
        NavigationState navigationState = new NavigationState();

        Scene scene = new Scene(AppShell.createRoot(appStateStore, navigationState), 1180, 760);
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
