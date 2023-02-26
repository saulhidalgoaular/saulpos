package com.saulpos.model.menu.action;

public class LogoutMenuAction implements MenuAction {
    @Override
    public Object run() {
        System.exit(0);

        return null;
    }
}
