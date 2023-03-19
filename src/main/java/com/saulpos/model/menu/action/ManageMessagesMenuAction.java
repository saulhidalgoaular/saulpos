package com.saulpos.model.menu.action;

import com.saulpos.model.bean.Message;

public class ManageMessagesMenuAction extends CrudMenuAction{

    public ManageMessagesMenuAction() {
        super(Message.class);
    }
}
