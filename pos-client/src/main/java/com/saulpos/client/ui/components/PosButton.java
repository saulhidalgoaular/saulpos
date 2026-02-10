package com.saulpos.client.ui.components;

import javafx.scene.control.Button;

public final class PosButton extends Button {

    private PosButton(String text) {
        super(text);
        getStyleClass().add("pos-button");
        setFocusTraversable(true);
    }

    public static PosButton primary(String text) {
        return new PosButton(text);
    }

    public static PosButton accent(String text) {
        PosButton button = new PosButton(text);
        button.getStyleClass().add("pos-button-accent");
        return button;
    }
}
