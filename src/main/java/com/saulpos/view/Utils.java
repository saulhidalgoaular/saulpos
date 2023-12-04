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
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;

public class Utils {

    public static class ViewDef{
        private String fxmlPath;

        private AbstractPresenter presenter;
        private Node rootNode;
        
        public ViewDef(String fxmlPath, AbstractPresenter presenter){
            this.fxmlPath = fxmlPath;
            this.presenter = presenter;
        }

        public ViewDef(Node rootNode) {
            this.rootNode = rootNode;
        }
        
        public Node getRoot() throws IOException {
            if (fxmlPath != null){
                FXMLLoader loader = new FXMLLoader(presenter.getClass().getResource(fxmlPath), presenter.getModel().getLanguage());

                loader.setController(presenter);

                final Parent root = loader.load();
                return root;
            }else{
                return rootNode;
            }
        }
    }

    public static void goForward(final ViewDef viewDef,
                                 final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane, mainPane.getScene().getWidth(), .0, true);
    }

    public static void goBack(final ViewDef viewDef,
                       final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane, (mainPane.getScene().getWidth()) * (-1), .0, false);
    }

    public static void goBackRemove(final ViewDef viewDef,
                             final Class classInstance, final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane,.0, mainPane.getScene().getWidth(), true);
    }

    public static void goForwardRemove(final ViewDef viewDef,
                                final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane, mainPane.getScene().getWidth(), .0, true);
    }

    public static void goTo(final ViewDef viewDef,
                     final Pane mainPane, final double from,
                     final double to, final boolean remove) throws IOException {
        Parent rootPane = mainPane;
        while ( rootPane != null && !(rootPane instanceof ParentPane) ){
            rootPane = rootPane.getParent();
        }

        final Pane rootPaneFinal = (Pane) rootPane;

        final ArrayList<Node> childrenToRemove = new ArrayList<>(rootPaneFinal.getChildren());
        if ( remove ){
            rootPaneFinal.getChildren().removeAll(childrenToRemove);
        }
        final Node root = viewDef.getRoot();

        AnchorPane.setBottomAnchor(root, .0);
        AnchorPane.setLeftAnchor(root, .0);
        AnchorPane.setRightAnchor(root, .0);
        AnchorPane.setTopAnchor(root, .0);


        //Set X of second scene to Height of window
        root.translateXProperty().set(from);
        //Add second scene. Now both first and second scene is present
        rootPaneFinal.getChildren().add(root);

        //Create new TimeLine animation
        Timeline timeline = new Timeline();
        //Animate Y property
        KeyValue kv = new KeyValue(root.translateXProperty(), to, Interpolator.EASE_IN);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }


}
