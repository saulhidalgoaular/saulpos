package com.saulpos.model.menu.action;

import com.saulpos.model.bean.Cashier;
import com.saulpos.model.bean.Configuration;

public class ManageConfigMenuAction extends CrudMenuAction{

    public ManageConfigMenuAction() {
        super(Configuration.class);
    }
}
