package com.saulpos.presenter;

import com.saulpos.model.MainModel;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.menu.DefaultMenuGenerator;
import com.saulpos.view.MainView;
import com.saulpos.view.ParentPane;
import com.saulpos.view.menu.MenuBarGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class MainPresenter extends AbstractPresenter<MainModel, MainView> {

    @FXML
    public VBox mainVBox;
    private ParentPane pane = new ParentPane();

    public MainPresenter(MainModel model, MainView view) {
        super(model, view);
    }

    @Override
    public void addBinding() {

    }

    @Override
    public void addComponents() {
        MenuBar menuBar = MenuBarGenerator.generateMenuNode(model, pane);

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
