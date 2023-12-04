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
package com.saulpos.model;

import java.beans.PropertyVetoException;
import java.util.ResourceBundle;

/**
 * Created by Saul on 10/23/2016.
 */
public abstract class AbstractModel {
    ResourceBundle language;

    public AbstractModel(){

    }

    protected void initialize() throws PropertyVetoException {
        addDataSource();
        addListeners();
        addChangedListeners();
    }

    public abstract void addChangedListeners();
    public abstract void addListeners();
    public abstract void addDataSource() throws PropertyVetoException;

    public ResourceBundle getLanguage() {
        return language;
    }
}