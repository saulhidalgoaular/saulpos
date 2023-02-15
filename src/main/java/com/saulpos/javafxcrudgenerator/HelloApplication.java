package com.saulpos.javafxcrudgenerator;

import com.saulpos.model.bean.Product;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, PropertyVetoException, URISyntaxException, ClassNotFoundException {
        DatabaseConnection.getInstance().initialize();

        Product product = new Product();
        product.setArea("Test");
        product.setBarcode("123123");
        product.save();

        List allProducts = DatabaseConnection.getInstance().listAll("Product");
        for (Object p :
                allProducts) {
            Product pr = (Product)p;
            System.out.println(pr.getId() + " " + pr.getArea());
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}