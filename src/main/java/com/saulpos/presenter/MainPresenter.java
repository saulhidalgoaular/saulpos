package com.saulpos.presenter;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.saulpos.Main;
import com.saulpos.model.AbstractModel;
import com.saulpos.model.LoginModel;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.menu.DefaultMenuGenerator;
import com.saulpos.view.MainView;
import com.saulpos.view.menu.MenuBarGenerator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class MainPresenter extends AbstractPresenter<MainModel, MainView> {

    @FXML
    public VBox mainVBox;
    private Pane pane = new Pane();

    public MainPresenter(MainModel nModel) {
        super(nModel);
    }

    @Override
    public void addBinding() {

    }

    @Override
    public void addComponents() {
        pane.setMinHeight(640);
        pane.setMinWidth(640);
        DefaultMenuGenerator dmg = new DefaultMenuGenerator();
        ArrayList<MenuModel> mb = dmg.generateMenu();
        //MenuModel[] menuArray = (MenuModel[]) mb.toArray();
        MenuModel[] menuArray = mb.toArray(new MenuModel[mb.size()]);
        MenuBar menuBar = MenuBarGenerator.generateMenuNode(menuArray, mainVBox);
        Label saulPOSLabel = new Label("SAUL POS");
        pane.getChildren().add(saulPOSLabel);
        mainVBox.getChildren().add(menuBar);
        mainVBox.getChildren().add(pane);
    }

    @Override
    public void initializeComponents() {

    }

    @Override
    public void entryActions() {

    }
}
