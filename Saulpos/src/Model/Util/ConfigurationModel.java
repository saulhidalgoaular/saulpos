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
package Model.Util;

import Controller.ErrHandler.ConfigManagerException;
import Controller.Util.MainController;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ConfigurationModel {
    
    
    /**
     * Parse and load the configuration file
     */
    public void loadConfigurationFile() throws ConfigManagerException{
        try {
            //<editor-fold defaultstate="collapsed" desc="Parsing">
            IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
            IXMLReader reader = StdXMLReader.fileReader( MainController.getInstance().getConstants().getFileName4XMLConfig() );
            parser.setReader(reader);
            IXMLElement xmlFile = (IXMLElement) parser.parse();
            //</editor-fold>
            
            // TODO: IS CORRECT HAVING HARD CODED THOSE TAGS??
            assert(xmlFile.getName().equals("saulpos"));
            
            //<editor-fold defaultstate="collapsed" desc="Load Attributes">
            for (Object childObj : xmlFile.getChildren()) {
                XMLElement xmlChild = (XMLElement)childObj;
                Enumeration<String> attrEnum = xmlChild.enumerateAttributeNames();
                while( attrEnum.hasMoreElements() ){
                    String key = attrEnum.nextElement();
                    MainController.getInstance().getConfigurationController().set(key, xmlChild.getAttribute(key, ""));
                }
            }
            //</editor-fold>
        } catch (XMLException | ClassNotFoundException | InstantiationException |
                IllegalAccessException | IOException ex) {
            throw new ConfigManagerException("ErrLoadingConfigFile");
        }
    }
    
    /**
     * Read the configuration from the database
     * @throws SQLException 
     */
    public void loadDatabaseConfiguration() throws SQLException{
        Connection c = MainController.getInstance().getConnectionModel().getConnection();
        PreparedStatement stmt = c.prepareCall("select SettingName , SettingValue , SettingType from configuration");
        
        ResultSet rs = stmt.executeQuery();
        while( rs.next() ){
            MainController
                    .getInstance()
                    .getConfigurationController()
                    .set(
                        rs.getString("SettingName") , 
                        parse( 
                            MainController
                                .getInstance()
                                .getConstants()
                                .getConfigurationClass()[rs.getInt("SettingType")] ,
                            rs.getString("SettingValue")));
            MainController.getInstance().getLogger().debug(ConfigurationModel.class, "Loaded configuration key " +rs.getString("SettingName") + "->" + rs.getString("SettingValue"));
        }
        
        rs.close();
        c.close();
    }
    
    /**
     * TODO: IMPROVE THIS PLEASE =(
     * @param c Class of the object
     * @param value Value to parse if it is necessary
     * @return Parsed value
     */
    private Object parse(Class c, String value){
        try{
            if ( c.equals(String.class) ){
                return value;
            }else if ( c.equals(Double.class) ){
                return Double.parseDouble(value);
            }else if ( c.equals(Integer.class) ){
                return Integer.parseInt(value);
            }else{
                return null;
            }
        }catch (Exception ex){
            MainController.getInstance().getLogger().debug(ConfigurationModel.class, "Failed loading configuration key " + value );
            return null;
        }
    }
}
