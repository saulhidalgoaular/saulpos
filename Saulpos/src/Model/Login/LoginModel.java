/*
 * Copyright (C) 2012 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Model.Login;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class LoginModel {
    private SimpleStringProperty username = new SimpleStringProperty();
    private SimpleStringProperty password = new SimpleStringProperty();

    public LoginModel() {
    }

    public SimpleStringProperty getUsername() {
        return username;
    }

    public SimpleStringProperty getPassword() {
        return password;
    }
    
    /**
     * Returns true if and only if you have a valid username/password.
     * @return true if and only if you have a valid username/password.
     */
    public boolean isValid(){
        // TODO:...
        return true;
    }
}
