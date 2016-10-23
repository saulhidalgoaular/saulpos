/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package View.Util;

import Controller.Util.MainController;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * CLASS DERIVED FROM https://github.com/4ntoine/JavaFxDialog/
 * @author Anton Smirnov (dev@antonsmirnov.name)
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class DialogBuilder {
    /**
    * Dialog builder
    */
    protected static final int STACKTRACE_LABEL_MAXHEIGHT = 240;
    protected static final int MESSAGE_MIN_WIDTH = 180;
    protected static final int MESSAGE_MAX_WIDTH = 800;
    protected static final int BUTTON_WIDTH = 60;
    protected static final double MARGIN = 10;
    protected static final String ICON_PATH = MainController.getInstance().getConstants().getDefaultImagesDir() + File.separator;

    protected Dialog stage;

    public DialogBuilder create() {
        stage = new Dialog();
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);                        
        stage.setIconified(false);
        stage.centerOnScreen();
        stage.borderPanel = new BorderPane();

        // icon
        stage.icon = new ImageView();
        stage.borderPanel.setLeft(stage.icon);
        BorderPane.setMargin(stage.icon, new Insets(MARGIN));

        // message
        stage.messageBox = new VBox();
        stage.messageBox.setAlignment(Pos.CENTER_LEFT);

        stage.messageLabel = new Label();
        stage.messageLabel.setWrapText(true);
        stage.messageLabel.setMinWidth(MESSAGE_MIN_WIDTH);
        stage.messageLabel.setMaxWidth(MESSAGE_MAX_WIDTH);

        stage.messageBox.getChildren().add(stage.messageLabel);
        stage.borderPanel.setCenter(stage.messageBox);
        BorderPane.setAlignment(stage.messageBox, Pos.CENTER);
        BorderPane.setMargin(stage.messageBox, new Insets(MARGIN, MARGIN, MARGIN, 0));

        // buttons
        stage.buttonsPanel = new HBox();
        stage.buttonsPanel.setSpacing(MARGIN);
        stage.buttonsPanel.setAlignment(Pos.BOTTOM_CENTER);
        BorderPane.setMargin(stage.buttonsPanel, new Insets(0, 0, 1.5 * MARGIN, 0));
        stage.borderPanel.setBottom(stage.buttonsPanel);
        stage.borderPanel.widthProperty().addListener(new ChangeListener<Number> () {

            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                stage.buttonsPanel.layout();
            }

        });

        stage.scene = new Scene(stage.borderPanel);
        stage.setScene(stage.scene);
        return this;
    }

    public DialogBuilder setOwner(Window owner) {
        if (owner != null) {
            stage.initOwner(owner);
            stage.borderPanel.setMaxWidth(owner.getWidth());
            stage.borderPanel.setMaxHeight(owner.getHeight());
        }
        return this;
    }

    public DialogBuilder setTitle(String title) {
        stage.setTitle(title);
        return this;
    }

    public DialogBuilder setMessage(String message) {
        stage.messageLabel.setText(message);
        return this;
    }

    private void alignScrollPane() {
        stage.setWidth(
            stage.icon.getImage().getWidth()
            + Math.max(
                stage.messageLabel.getWidth(),
                (stage.stacktraceVisible 
                    ? Math.max(
                        stage.stacktraceButtonsPanel.getWidth(),
                        stage.stackTraceLabel.getWidth())
                    : stage.stacktraceButtonsPanel.getWidth()))
            + 5 * MARGIN);

        stage.setHeight(
                Math.max(
                    stage.icon.getImage().getHeight(),
                    stage.messageLabel.getHeight()
                        + stage.stacktraceButtonsPanel.getHeight()
                        + (stage.stacktraceVisible
                            ? Math.min(
                                stage.stackTraceLabel.getHeight(),
                                STACKTRACE_LABEL_MAXHEIGHT)
                            : 0))

                + stage.buttonsPanel.getHeight()
                + 3 * MARGIN);
        if (stage.stacktraceVisible) {
            stage.scrollPane.setPrefHeight(
                stage.getHeight()
                - stage.messageLabel.getHeight()
                - stage.stacktraceButtonsPanel.getHeight()
                - 2 * MARGIN);
        }

        stage.centerOnScreen();
    }

    // NOTE: invoke once during Dialog creating
    protected DialogBuilder setStackTrace(Throwable t) {
        // view button
        stage.viewStacktraceButton = new ToggleButton("View stacktrace");

        // copy button
        stage.copyStacktraceButton = new Button("Copy to clipboard");
        HBox.setMargin(stage.copyStacktraceButton, new Insets(0, 0, 0, MARGIN));

        stage.stacktraceButtonsPanel = new HBox();
        stage.stacktraceButtonsPanel.getChildren().addAll(
            stage.viewStacktraceButton, stage.copyStacktraceButton);
        VBox.setMargin(stage.stacktraceButtonsPanel, new Insets(MARGIN, MARGIN, MARGIN, 0));
        stage.messageBox.getChildren().add(stage.stacktraceButtonsPanel);

        // stacktrace text
        stage.stackTraceLabel = new Label();
        stage.stackTraceLabel.widthProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                alignScrollPane();
            }
        });

        stage.stackTraceLabel.heightProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                alignScrollPane();
            }
        });

        Dialog.StacktraceExtractor extractor = new Dialog.StacktraceExtractor();
        stage.stacktrace = extractor.extract(t);

        stage.scrollPane = new ScrollPane();
        stage.scrollPane.setContent(stage.stackTraceLabel);

        stage.viewStacktraceButton.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent t) {
                stage.stacktraceVisible = !stage.stacktraceVisible;
                if (stage.stacktraceVisible) {
                    stage.messageBox.getChildren().add(stage.scrollPane);
                    stage.stackTraceLabel.setText(stage.stacktrace);

                    alignScrollPane();
                } else {
                    stage.messageBox.getChildren().remove(stage.scrollPane);

                    //alignScrollPane();
                    stage.setWidth(stage.originalWidth);
                    stage.setHeight(stage.originalHeight);
                    stage.stackTraceLabel.setText(null);
                    stage.centerOnScreen();
                }
                stage.messageBox.layout();
            }
        });

        stage.copyStacktraceButton.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent t) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                Map<DataFormat, Object> map = new HashMap<DataFormat, Object>();
                map.put(DataFormat.PLAIN_TEXT, stage.stacktrace);
                clipboard.setContent(map);
            }
        });

        stage.showingProperty().addListener(new ChangeListener<Boolean>() {

            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    stage.originalWidth = stage.getWidth();
                    stage.originalHeight = stage.getHeight();
                }
            }
        });

        return this;
    }

    protected void setIconFromResource(String resourceName) {
        final Image image = new Image(resourceName);
        stage.icon.setImage(image);
    }

    protected DialogBuilder setWarningIcon() {
        setIconFromResource(ICON_PATH + "warningIcon.png");
        return this;
    }

    protected DialogBuilder setErrorIcon() {
        setIconFromResource(ICON_PATH + "errorIcon.png");
        return this;
    }

    protected DialogBuilder setThrowableIcon() {
        setIconFromResource(ICON_PATH + "bugIcon.png");
        return this;
    }

    protected DialogBuilder setInfoIcon() {
        setIconFromResource(ICON_PATH + "infoIcon.png");
        return this;
    }

    protected DialogBuilder setConfirmationIcon() {
        setIconFromResource(ICON_PATH + "confirmationIcon.png");
        return this;
    }

    protected DialogBuilder addOkButton() {
        stage.okButton = new Button(MainController.getInstance().getLanguage().get("OK"));
        stage.okButton.setPrefWidth(BUTTON_WIDTH);
        stage.okButton.setOnAction(new EventHandler<ActionEvent> () {

            public void handle(ActionEvent t) {
                stage.close();
            }

        });
        stage.buttonsPanel.getChildren().add(stage.okButton);
        return this;
    }

    protected DialogBuilder addConfirmationButton(String buttonCaption, final EventHandler actionHandler) {
        Button confirmationButton = new Button(buttonCaption);
        confirmationButton.setMinWidth(BUTTON_WIDTH);
        confirmationButton.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent t) {
                stage.close();
                if (actionHandler != null)
                    actionHandler.handle(t);
            }
        });

        stage.buttonsPanel.getChildren().add(confirmationButton);
        return this;
    }

    /**
     * Add Yes button to confirmation dialog
     * 
     * @param actionHandler action handler
     * @return 
     */
    public DialogBuilder addYesButton(EventHandler actionHandler) {
        return addConfirmationButton(MainController.getInstance().getLanguage().get("Yes"), actionHandler);
    }

    /**
     * Add No button to confirmation dialog
     * 
     * @param actionHandler action handler
     * @return 
     */
    public DialogBuilder addNoButton(EventHandler actionHandler) {
        return addConfirmationButton(MainController.getInstance().getLanguage().get("No"), actionHandler);
    }

    /**
     * Add Cancel button to confirmation dialog
     * 
     * @param actionHandler action handler
     * @return 
     */
    public DialogBuilder addCancelButton(EventHandler actionHandler) {
        return addConfirmationButton(MainController.getInstance().getLanguage().get("Cancel"), actionHandler);
    }

    /**
     * Build dialog
     * 
     * @return dialog instance
     */
    public Dialog build() {
        if (stage.buttonsPanel.getChildren().size() == 0)
            throw new RuntimeException("Add one dialog button at least");

        stage.buttonsPanel.getChildren().get(0).requestFocus();
        return stage;
    }

}