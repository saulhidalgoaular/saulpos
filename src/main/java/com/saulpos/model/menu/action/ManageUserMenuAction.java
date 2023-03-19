package com.saulpos.model.menu.action;

import com.saulpos.model.bean.UserB;
import javafx.scene.layout.Pane;

public class ManageUserMenuAction extends CrudMenuAction{

    public ManageUserMenuAction() {
        super(UserB.class);
    }
}
