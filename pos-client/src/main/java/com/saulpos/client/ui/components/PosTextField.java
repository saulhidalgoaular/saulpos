package com.saulpos.client.ui.components;

import javafx.scene.control.TextField;

public final class PosTextField extends TextField {

    public PosTextField(String promptText) {
        super();
        setPromptText(promptText);
        getStyleClass().add("pos-input");
        setFocusTraversable(true);
    }
}
