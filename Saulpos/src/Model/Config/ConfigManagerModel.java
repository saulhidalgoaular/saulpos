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
package Model.Config;

import Controller.Util.MainController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ConfigManagerModel {
    private SimpleStringProperty serverUrl = new SimpleStringProperty();
    private SimpleStringProperty userName = new SimpleStringProperty();
    private SimpleStringProperty password = new SimpleStringProperty();
    private SimpleStringProperty serverOfflineUrl = new SimpleStringProperty();
    private SimpleStringProperty userNameOffline = new SimpleStringProperty();
    private SimpleStringProperty passwordOffline = new SimpleStringProperty();
    private SimpleStringProperty posId = new SimpleStringProperty();
    private SimpleStringProperty comPort = new SimpleStringProperty();
    private SimpleStringProperty fiscalPrinterModel = new SimpleStringProperty();
    private Boolean isPos = false;

    public ConfigManagerModel() {
    }
    
    public SimpleStringProperty getServerUrl() {
        return serverUrl;
    }

    public SimpleStringProperty getUserName() {
        return userName;
    }

    public SimpleStringProperty getPassword() {
        return password;
    }

    public SimpleStringProperty getServerOfflineUrl() {
        return serverOfflineUrl;
    }

    public SimpleStringProperty getUserNameOffline() {
        return userNameOffline;
    }

    public SimpleStringProperty getPasswordOffline() {
        return passwordOffline;
    }

    public SimpleStringProperty getPosId() {
        return posId;
    }

    public SimpleStringProperty getComPort() {
        return comPort;
    }

    public SimpleStringProperty getFiscalPrinterModel() {
        return fiscalPrinterModel;
    }

    public void setIsPos(boolean isPos) {
        this.isPos = isPos;
    }
    
    /**
     * Create the XML config file
     */
    public void save() throws FileNotFoundException, IOException{
        IXMLElement xml = new XMLElement("saulpos");
        
        IXMLElement xmlChild = xml.createElement("BasicConfig");
        xml.addChild(xmlChild);
        
        xmlChild.setAttribute("isPos", isPos.toString());
        xmlChild.setAttribute("serverUrl", serverUrl.getValue());
        xmlChild.setAttribute("userName", userName.getValue());
        xmlChild.setAttribute("password", password.getValue());
        
        if ( isPos ){
            
            xmlChild.setAttribute("offlineServerUrl", serverOfflineUrl.getValue());
            xmlChild.setAttribute("offlineUserName", userNameOffline.getValue());
            xmlChild.setAttribute("offlinePassword", passwordOffline.getValue());
            
            xmlChild.setAttribute("posId", posId.getValue());
            xmlChild.setAttribute("comPort", comPort.getValue());
            xmlChild.setAttribute("fiscalPrinterModel", fiscalPrinterModel.getValue());
        }
        
        File configFile = new File(MainController.getInstance().getConstants().getFileName4XMLConfig());
        configFile.delete();
        
        FileOutputStream outputStream = new FileOutputStream(configFile);
        XMLWriter xmlWriter = new XMLWriter(outputStream);
        
        xmlWriter.write(xml);
    }
}
