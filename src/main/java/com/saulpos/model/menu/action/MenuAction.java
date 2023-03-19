package com.saulpos.model.menu.action;

import javafx.scene.layout.Pane;

public interface MenuAction {

    Object run(Pane mainPane) throws Exception;
}
