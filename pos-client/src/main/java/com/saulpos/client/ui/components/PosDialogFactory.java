package com.saulpos.client.ui.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public final class PosDialogFactory {

    private PosDialogFactory() {
    }

    public static Alert confirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(header);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("pos-dialog");
        return alert;
    }
}
