package com.saulpos.model.menu.action;

import javafx.scene.layout.Pane;

public class LogoutMenuAction implements MenuAction {
    @Override
    public Object run(Pane mainPane) throws Exception {
        System.exit(0);
        return null;
    }
}
