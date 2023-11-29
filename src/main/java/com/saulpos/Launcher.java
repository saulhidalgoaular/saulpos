package com.saulpos;

import com.saulpos.model.LoginModel;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.presenter.LoginPresenter;
import com.saulpos.view.LoginView;
import com.saulpos.view.ParentPane;
import com.saulpos.view.Utils;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConnection.getInstance().initialize();
        DatabaseConnection.getInstance().saveDefaultValues();

        LoginModel loginModel = new LoginModel();
        LoginPresenter loginPresenter = new LoginPresenter(loginModel, new LoginView());

        ParentPane parentPane = new ParentPane();
        stage.setTitle("Saul POS");
        stage.setScene(new Scene(parentPane));
        stage.setMaximized(true);
        stage.show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //System.out.print("Timer Task fired!!!");
            }
        }, 10*1000);

        stage.addEventHandler(KeyEvent.ANY, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                /*timer.cancel();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.print("Timer Task fired!!!");
                    }
                }, 10*1000);*/
            }
        });

        stage.addEventHandler(MouseEvent.ANY, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                /*timer.cancel();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.print("Timer Task fired!!!");
                    }
                }, 1*1000);*/
            }
        });


        Utils.goForward(new Utils.ViewDef("/login.fxml", loginPresenter), parentPane);
    }

    public static void main(String[] args) {
        launch();
    }
}