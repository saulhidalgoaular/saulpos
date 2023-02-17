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

package com.saulpos.javafxcrudgenerator.model.dao;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Saul on 11/16/2016.
 */
public interface AbstractBean < S extends AbstractBean > extends Cloneable {

    Integer save() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException;

    void update() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException;

    void saveOrUpdate() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException;

    void receiveChanges(S currentBean);

    S clone();

    void delete() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException;
}
