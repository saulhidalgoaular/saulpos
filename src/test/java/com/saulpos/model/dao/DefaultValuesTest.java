package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Permission;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.menu.DefaultMenuGenerator;
import org.junit.jupiter.api.Test;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

public class DefaultValuesTest {

    @Test
    public void mainTest() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        HibernateDataProvider hibernateDataProvider = new HibernateDataProvider();
        for (Object object : hibernateDataProvider.getAllItems(UserB.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        for (Object object : hibernateDataProvider.getAllItems(Profile.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        for (Object object : hibernateDataProvider.getAllItems(MenuModel.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        for (Object object : hibernateDataProvider.getAllItems(Permission.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        saveDefaultValues();
        System.out.println("Test");
    }

    public void saveDefaultValues() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        // default password
        UserB admin = new UserB();
        admin.setUserName("admin");
        admin.setPassword("admin");
        admin.hashPassword();
        admin.setEnabled(true);
        admin.setShouldChangePassword(true);

        // assign the profile
        Profile newProfile = new Profile();
        admin.setProfile(newProfile);
        newProfile.setName("Administrator");
        newProfile.setDescription("Administrator Profile");
        newProfile.setPermissions(new HashSet<>());

        // give permission to all menus
        DefaultMenuGenerator defaultMenuGenerator = new DefaultMenuGenerator();
        ArrayList<MenuModel> menuModels = defaultMenuGenerator.generateMenu();

        for (MenuModel menuModel : menuModels){
            Permission permission = new Permission();
            permission.setGranted(true);
            permission.setProfile(newProfile);
            permission.setNode(menuModel);

            newProfile.getPermissions().add(permission);
        }

        admin.save();
    }
}
