/*
 * Copyright (C) 2012 Saúl Hidalgo <saulhidalgoaular at gmail.com>
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
package Controller.Util;

import Controller.ErrHandler.ConfigManagerException;
import Model.Util.ConfigurationModel;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the configuration option of Saulpos.
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ConfigurationController {
    
    public ConfigurationController() {
    }
    
    /**
     * Configuration is being managed in a HashMap
     */
    private Map<String, Object> configurationMap = new HashMap<>();
    
    /**
     * Configuration Model
     */
    private ConfigurationModel model = new ConfigurationModel();
    
    /**
     * Sets a configuration value
     * @param k Key
     * @param v Value
     */
    public void set(String k , Object v){
        configurationMap.put(k, v);
    }
    
    /**
     * Gets a previously setted value
     * @param k Key
     * @return Returns the value
     */
    public Object get(String k){
        return configurationMap.get(k);
    }
    
    /**
     * Returns true if the Key is in the configuration
     * @param k Key
     * @return true if and only if contains the key
     */
    public boolean contains(String k){
        return configurationMap.containsKey(k);
    }

    /**
     * Load the configuration stored in the database
     */
    public void loadDatabaseConfiguration() throws SQLException{
        model.loadDatabaseConfiguration();
    }
    
    /**
     * Parse and load the configuration file
     */
    public void loadConfigurationFile() throws ConfigManagerException{
        model.loadConfigurationFile();
    }
    
}
