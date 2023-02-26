package com.saulpos;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Product;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.menu.DefaultMenuGenerator;
import com.saulpos.view.menu.MenuBarGenerator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 640);

        HelloController controller = fxmlLoader.<HelloController>getController();
        DefaultMenuGenerator defaultMenuGenerator = new DefaultMenuGenerator();
        final ArrayList<MenuModel> menuModels = defaultMenuGenerator.generateMenu();
        controller.mainVBox.getChildren().add(MenuBarGenerator.generateMenuNode(menuModels.toArray(new MenuModel[]{})));
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