package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.MenuModel;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public interface MenuAction {

    Object run(MainModel bean, Pane mainPane) throws Exception;
}
