package com.saulpos.client.ui.components;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public final class ToastHost extends StackPane {

    private final Label messageLabel = new Label();

    public ToastHost() {
        messageLabel.getStyleClass().add("pos-toast");
        messageLabel.setVisible(false);
        getChildren().add(messageLabel);
        setPickOnBounds(false);
    }

    public void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setOpacity(1.0);
        messageLabel.setVisible(true);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1800), messageLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> messageLabel.setVisible(false));
        fadeTransition.playFromStart();
    }
}
