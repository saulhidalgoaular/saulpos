package com.saulpos;

import com.saulpos.model.LoginModel;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.presenter.LoginPresenter;
import com.saulpos.view.LoginView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    public void mainTest() throws Exception {

        Stage stage = new Stage();

        DatabaseConnection.getInstance().initialize();

        UserB admin = new UserB();
        admin.setUserName("admin");
        admin.setPassword("admin");
        admin.hashPassword();
        admin.save();

        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/login.fxml"));
        LoginModel loginModel = new LoginModel();
        LoginPresenter loginPresenter = new LoginPresenter(loginModel, new LoginView());
        fxmlLoader.setController(loginPresenter);

        Scene scene = new Scene(fxmlLoader.load(), 700, 200);

        stage.setTitle("Login!");
        stage.setScene(scene);
        stage.show();
    }
}