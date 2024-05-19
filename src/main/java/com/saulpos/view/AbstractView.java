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
package com.saulpos.view;

import com.saulpos.presenter.AbstractPresenter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

public class AbstractView<P extends AbstractPresenter> {
    String fxmlPath;
    Node rootNode;
    P presenter;

    public AbstractView(String fxmlPath, P presenter) {
        this.fxmlPath = fxmlPath;
        this.presenter = presenter;
    }

    public AbstractView(Node rootNode) {
        this.rootNode = rootNode;
    }

    public AbstractView() {

    }

    public void initialize() {

    }

    public String getFxmlPath() {
        return fxmlPath;
    }

    public URL getFxmlUrl() {
        return AbstractView.class.getResource(fxmlPath);
    }

    public Node getRootNode() {
        return rootNode;
    }

    public Node getRoot() throws IOException {
        if (getFxmlPath() != null) {
            FXMLLoader loader = new FXMLLoader(getFxmlUrl(), presenter.getModel().getLanguage());

            loader.setController(presenter);

            final Parent root = loader.load();
            return root;
        } else {
            return getRootNode();
        }
    }
}
