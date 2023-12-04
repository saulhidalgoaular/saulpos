/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConnection.getInstance().initialize();

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