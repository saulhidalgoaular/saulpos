package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.Cashier;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.bean.Shift;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.Utils;
import javafx.scene.layout.Pane;

public class ManageUserMenuAction extends CrudMenuAction{

    public ManageUserMenuAction() {
        super(UserB.class);
    }

    @Override
    public Object run(MainModel mainModel, Pane mainPane) throws Exception {
        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(crudClass);
        crudGeneratorParameter.setBeforeSave(new Function() {
            @Override
            public Object[] run(Object[] objects) throws Exception {
                if(objects == null || objects.length == 0 || !(objects[0] instanceof UserB)){
                    return null;
                }

                UserB user = (UserB) objects[0];
                user.hashPassword();
                return null;
            }
        });
        HibernateDataProvider dataProvider = new HibernateDataProvider();
        dataProvider.registerClass(Profile.class);
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();
        Utils.goForward(new Utils.ViewDef(crud.getView().getMainView()), mainPane);
        return null;
    }
}
