package com.saulpos.javafxcrudgenerator;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.renderer.FormRenderer;
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

    // todo: after developing menu concept move somewhere else, maybe to views or own folder menus
    class MenuItem {
        private String id;
        private String name;
        private String parentId;
        public MenuItem(String id, String name, String parentId) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;
        }
    }

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
            Product pr = (Product)p;
            System.out.println(pr.getId() + " " + pr.getArea() + " "  + pr.getBarcode());
        }

        // todo: after finishing initial concept for menu, move it from below
        MenuItem[] menuMock;
        menuMock = new MenuItem[10];
        menuMock[0] = new MenuItem("0", "Main", null);
        menuMock[1] = new MenuItem("1", "Product", "0");
        menuMock[2] = new MenuItem("2", "Discount", "0");
        menuMock[3] = new MenuItem("3", "Price", "0");
        menuMock[4] = new MenuItem("4", "Stoager", "0");
        menuMock[5] = new MenuItem("5", "Unit", "0");
        menuMock[6] = new MenuItem("6", "Cashier", "0");
        menuMock[7] = new MenuItem("7", "Administration", null);
        menuMock[8] = new MenuItem("8", "Profile", "7");
        menuMock[9] = new MenuItem("9", "User", "7");

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
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