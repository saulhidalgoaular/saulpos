package com.saulpos;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.saulpos.model.MenuModel;
import com.saulpos.model.bean.Product;
import com.saulpos.model.dao.DatabaseConnection;
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
        Form form = Form.of(
                Group.of(
                        Field.ofStringType("")
                                .label("Username"),
                        Field.ofPasswordType("")
                                .label("Password")
                                .required("This field can’t be empty")
                )

        ).title("Login");


        DatabaseConnection.getInstance().initialize();

        Product product = new Product();
        product.setArea("Test");
        product.setBarcode("123123");
        product.save();

        product = new Product();
        product.setArea("Test Salah");
        product.setBarcode("Te");
        product.save();

        List allProducts = DatabaseConnection.getInstance().listAll("Product");
        for (Object p :
                allProducts) {
            Product pr = (Product)p;
            System.out.println(pr.getId() + " " + pr.getArea() + " "  + pr.getBarcode());
        }

        product.setArea("Test Georgy");
        product.saveOrUpdate();

        System.out.println("----------------");

        allProducts = DatabaseConnection.getInstance().listAll("Product");
        for (Object p :
                allProducts) {
            Product pr = (Product) p;
            System.out.println(pr.getId() + " " + pr.getArea() + " " + pr.getBarcode());
        }

        // todo: after finishing initial concept for menu, move it from below
        MenuModel[] menuMock;
        menuMock = new MenuModel[10];
        menuMock[0] = new MenuModel(0, "Main", null);
        menuMock[1] = new MenuModel(1, "Product", "0");
        menuMock[2] = new MenuModel(2, "Discount", "0");
        menuMock[3] = new MenuModel(3, "Price", "0");
        menuMock[4] = new MenuModel(4, "Storage", "0");
        menuMock[5] = new MenuModel(5, "Unit", "0");
        menuMock[6] = new MenuModel(6, "Cashier", "0");
        menuMock[7] = new MenuModel(7, "Administration", null);
        menuMock[8] = new MenuModel(8, "Profile", "7");
        menuMock[9] = new MenuModel(9, "User", "7");

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 640);

        HelloController controller = fxmlLoader.<HelloController>getController();
        controller.mainVBox.getChildren().add(
                new FormRenderer(form)
        );
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}