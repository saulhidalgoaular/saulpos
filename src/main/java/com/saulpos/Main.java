package com.saulpos;

import com.dlsc.formsfx.model.structure.*;
import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.model.LoginModel;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Product;
import com.saulpos.model.bean.User;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.model.menu.DefaultMenuGenerator;
import com.saulpos.presenter.LoginPresenter;
import com.saulpos.view.menu.MenuBarGenerator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConnection.getInstance().initialize();

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