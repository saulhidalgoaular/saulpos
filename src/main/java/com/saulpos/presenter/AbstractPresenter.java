package com.saulpos.presenter;

import com.saulpos.model.AbstractModel;
import com.saulpos.view.AbstractView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Saul on 10/23/2016.
 */
public abstract class AbstractPresenter
        <M extends AbstractModel, V extends AbstractView> implements Initializable{
    final protected M model;
    final protected V view;
    protected AbstractPresenter father;

    @FXML
    public AnchorPane mainPane;

    public AbstractPresenter(M model, V view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    public void initialize(URL location, ResourceBundle resources) {
        initializeComponents();
        addComponents();
        addBinding();
        entryActions();
    }

    public AnchorPane getMainPane() {
        return mainPane;
    }

    public void setMainPane(AnchorPane mainPane) {
        this.mainPane = mainPane;
    }

    public AbstractPresenter setFather(AbstractPresenter father){
        this.father = father;
        return this;
    }

    public AbstractPresenter getFather() {
        return father;
    }

    public abstract void addBinding();
    public abstract void addComponents();
    public abstract void initializeComponents();
    public abstract void entryActions();

    public M getModel(){
        return model;
    }

}
