package com.saulpos;

import com.saulpos.model.LoginModel;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.presenter.LoginPresenter;
import com.saulpos.view.LoginView;
import com.saulpos.view.Utils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConnection.getInstance().initialize();
        DatabaseConnection.getInstance().saveDefaultValues();

        LoginModel loginModel = new LoginModel();
        LoginPresenter loginPresenter = new LoginPresenter(loginModel, new LoginView());

        AnchorPane anchorPane = new AnchorPane();
        stage.setTitle("Saul POS");
        stage.setScene(new Scene(anchorPane));
        stage.setMaximized(true);
        stage.show();

        Utils.goForward(new Utils.ViewDef("/login.fxml", loginPresenter), anchorPane);
    }

    public static void main(String[] args) {
        launch();
    }
}