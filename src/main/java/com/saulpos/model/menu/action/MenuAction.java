package com.saulpos.model.menu.action;

import com.saulpos.model.bean.MenuModel;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public interface MenuAction {

    Object run(ArrayList<MenuModel> completeMenu, Pane mainPane) throws Exception;
}
