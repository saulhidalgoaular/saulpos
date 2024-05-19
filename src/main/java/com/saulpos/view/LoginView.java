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
package com.saulpos.view;

import com.saulpos.presenter.AbstractPresenter;
import javafx.scene.Node;

public class LoginView extends AbstractView {

    public LoginView(String fxmlPath, AbstractPresenter presenter) {
        super(fxmlPath, presenter);
    }

    public LoginView(Node rootNode) {
        super(rootNode);
    }

    public LoginView() {
        super();
    }

    @Override
    public void initialize() {

    }
}
