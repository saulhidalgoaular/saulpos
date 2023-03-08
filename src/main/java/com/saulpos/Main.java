package com.saulpos;

import com.saulpos.model.LoginModel;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.presenter.LoginPresenter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConnection.getInstance().initialize();

        UserB georgy = new UserB();
        georgy.setUserName("georgy");
        georgy.setPassword("chorbov");
        georgy.save();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/login.fxml"));
        LoginModel loginModel = new LoginModel();
        LoginPresenter loginPresenter = new LoginPresenter(loginModel);
        fxmlLoader.setController(loginPresenter);

        Scene scene = new Scene(fxmlLoader.load(), 640, 200);

        stage.setTitle("Login!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}