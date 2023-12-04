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

import com.saulpos.model.MainModel;
import com.saulpos.view.MainView;
import com.saulpos.view.ParentPane;
import com.saulpos.view.menu.MenuBarGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;

public class MainPresenter extends AbstractPresenter<MainModel, MainView> {

    @FXML
    public VBox mainVBox;
    private final ParentPane pane = new ParentPane();

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
