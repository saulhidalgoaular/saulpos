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

package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;


public class HibernateDataProvider implements AbstractDataProvider {

    final HashSet<Class> registeredClasses = new HashSet<>();

    @Override
    public List getAllItems(Class aClass) {
        return getAllItems(aClass, null, SearchType.LIKE);
    }

    @Override
    public List getAllItems(Class aClass, AbstractBean abstractBean, SearchType type)  {
        try {
            return DatabaseConnection.getInstance().listBySample(aClass, abstractBean, type);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isRegisteredClass(Class aClass) {
        return registeredClasses.contains(aClass);
    }

    @Override
    public void registerClass(Class aClass) {
        registeredClasses.add(aClass);
    }

    @Override
    public List<Object[]> getItems(String query) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException{
        return DatabaseConnection.getInstance().runQuery(query);
    }


}
