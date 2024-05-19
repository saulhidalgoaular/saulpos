/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.AbstractView;
import com.saulpos.view.Utils;
import javafx.scene.layout.Pane;

public class ManageUserMenuAction extends CrudMenuAction{

    public ManageUserMenuAction() {
        super(UserB.class);
    }

    @Override
    public void run(MainModel mainModel, Pane mainPane) throws Exception {
        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(crudClass);
        crudGeneratorParameter.setBeforeSave(new Function() {
            @Override
            public Object[] run(Object[] objects) throws Exception {
                if(objects == null || objects.length == 0 || !(objects[0] instanceof UserB user)){
                    return null;
                }

                user.hashPassword();
                return null;
            }
        });
        HibernateDataProvider dataProvider = new HibernateDataProvider();
        dataProvider.registerClass(Profile.class);
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();
        Utils.goForward(new AbstractView(crud.getView().getMainView()), mainPane);
    }
}
