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
