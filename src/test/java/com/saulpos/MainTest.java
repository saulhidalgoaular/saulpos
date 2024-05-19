package com.saulpos;

import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.DatabaseConnection;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    public void mainTest() throws Exception {

        DatabaseConnection.getInstance().initialize();

        UserB admin = new UserB();
        admin.setUserName("admin");
        admin.setPassword("admin");
        admin.hashPassword();
        admin.saveOrUpdate();
    }
}