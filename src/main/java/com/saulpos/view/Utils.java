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
    public static void goForward(final String fxmlPath, final AbstractPresenter controller,
                                 final Class classInstance, final Pane mainPane) throws IOException {
        goTo(fxmlPath, controller, classInstance, mainPane, mainPane.getScene().getWidth(), .0, false);
    }

    public static void goBack(final String fxmlPath, final AbstractPresenter controller,
                       final Class classInstance, final Pane mainPane) throws IOException {
        goTo(fxmlPath, controller, classInstance, mainPane, (mainPane.getScene().getWidth()) * (-1), .0, false);
    }

    public static void goBackRemove(final String fxmlPath, final AbstractPresenter controller,
                             final Class classInstance, final Pane mainPane) throws IOException {
        goTo(fxmlPath, controller, classInstance, mainPane,.0, mainPane.getScene().getWidth(), true);
    }

    public static void goForwardRemove(final String fxmlPath, final AbstractPresenter controller,
                                final Class classInstance, final Pane mainPane) throws IOException {
        goTo(fxmlPath, controller, classInstance, mainPane, mainPane.getScene().getWidth(), .0, true);
    }

    public static void goTo(final String fxmlPath, final AbstractPresenter controller,
                     final Class classInstance, final Pane mainPane, final double from,
                     final double to, final boolean remove) throws IOException {

        Pane rootPane = mainPane;
        Parent tempPane;
        while ( (tempPane = rootPane.getParent()) != null ){
            if ( tempPane instanceof  Pane ){
                rootPane = (Pane) tempPane;
            }
        }
        final Pane rootPaneFinal = rootPane;

        final ArrayList<Node> childrenToRemove = new ArrayList<>(rootPaneFinal.getChildren());

        FXMLLoader loader = new FXMLLoader(classInstance.getResource(fxmlPath), controller.getModel().getLanguage());

        loader.setController(controller);

        final Parent root = loader.load();
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
        //After completing animation, remove first scene
        timeline.setOnFinished(t -> {
            if ( remove ){
                rootPaneFinal.getChildren().removeAll(childrenToRemove);
            }
        });
        timeline.play();
    }
}
