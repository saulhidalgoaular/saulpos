package com.saulpos;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Product;
import com.saulpos.model.bean.User;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.model.menu.DefaultMenuGenerator;
import com.saulpos.view.menu.MenuBarGenerator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Form form = Form.of(
                Group.of(
                        Field.ofStringType("")
                                .label("Username:"),
                        Field.ofPasswordType("")
                                .label("Password:")
                                .required("This field can’t be empty")
                )

        ).title("Login");

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 200);

        HelloController controller = fxmlLoader.<HelloController>getController();
        DefaultMenuGenerator defaultMenuGenerator = new DefaultMenuGenerator();
        final ArrayList<MenuModel> menuModels = defaultMenuGenerator.generateMenu();

        String exampleUser = "georgy";
        String examplePass = "chorbov";

        Button enterButton = new Button("Enter");
        enterButton.setPrefSize(100, 50);
        enterButton.setOnAction(event -> {

            String username = ((StringField) form.getGroups().get(0).getElements().get(0)).getValue();
            String password = ((PasswordField) form.getGroups().get(0).getElements().get(1)).getValue();

            if (username.equals(exampleUser) && password.equals(examplePass)) {
                //Success
                System.out.println("Welcome " + username + "!");
                enterButton.setDisable(true);
                enterButton.setVisible(false);
                controller.mainVBox.getChildren().remove(0);
                controller.mainVBox.setAlignment(Pos.TOP_LEFT);
                Node node = MenuBarGenerator.generateMenuNode(menuModels.toArray(new MenuModel[]{}));
                VBox.setMargin(node, new Insets(-20, -20, -20, -20));
                controller.mainVBox.getChildren().add(0,node);
                stage.setTitle("Main menu");
            } else {
                // Display an error message to the user
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid username or password");
                alert.setContentText("Please try again.");
                alert.showAndWait();
            }
        });

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
        //System.out.println( form.getElements().get(0).getID() );

        allProducts = DatabaseConnection.getInstance().listAll("Product");
        for (Object p :
                allProducts) {
            Product pr = (Product) p;
            System.out.println(pr.getId() + " " + pr.getArea() + " " + pr.getBarcode());
        }


        controller.mainVBox.getChildren().add(
                new FormRenderer(form)
        );
        VBox.setMargin(enterButton, new Insets(-30,0,0,0));
        controller.mainVBox.getChildren().add(enterButton);


        CrudGeneratorParameter parameter = new CrudGeneratorParameter();
        parameter.setClazz(User.class);
        parameter.setCurrentLocale(Locale.of("en", "US"));

        HibernateDataProvider dataProvider = new HibernateDataProvider();

        parameter.setDataProvider(
                dataProvider
        );
        CrudGenerator crudGenerator = new CrudGenerator(parameter);

        /*controller.mainVBox.getChildren().add(
            crudGenerator.generate().getView().getMainView()
        );*/
        stage.setTitle("Login!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}