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

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ConstantsController {

    public ConstantsController() {
    }

    /** Directory where are the app's icons. */
    private final String defaultLangDir = "lang/Language";
    
    /** Directory where are the app's icons. */
    private final String defaultImagesDir = "./pictures";
    
    /** Maximun number of comunication ports */
    private final int maximunComPorts = 5;
    
    /** Name of the configuration file */
    private final String fileName4XMLConfig = "config.xml";
    
    /** Compatible fiscal printer drivers. */
    private final String[] compatibleDrivers = {"tfhkaif","PrnFiscalDLL32"};
    
    /** Default language = es */
    private final String defaultLanguage = "es";
    
    /** Database URL connection pattern */
    // TODO: IMPROVE THIS REGEX
    private final String urlDatabasePattern = "\\A[A-Za-z0-9\\.]+(:[0-9]+|)\\/[A-Za-z0-9]+\\z";
    
    /** Point of Sale ID */
    private final String posIdPattern = "[0-9]{2}";
    
    /** COM pattern */
    private final String comPattern = "COM[0-9]{1}";
    
    /** Max number of seconds a connection can be checked out from the pool. */
    private final int maxCheckoutTime = 60;
    /**
     * Default country = VE
     */
    private final String defaultCountry = "VE";
    
    /** Class index for configuration */
    private final Class[] configurationClass = {String.class, Integer.class , Double.class};

    public String getDefaultLangDir() {
        return defaultLangDir;
    }

    public String getDefaultImagesDir() {
        return defaultImagesDir;
    }

    public int getMaximunComPorts() {
        return maximunComPorts;
    }

    public String getFileName4XMLConfig() {
        return fileName4XMLConfig;
    }

    public String[] getCompatibleDrivers() {
        return compatibleDrivers;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public String getDefaultCountry() {
        return defaultCountry;
    }
    
    public String getUrlDatabasePattern() {
        return urlDatabasePattern;
    }

    public String getPosIdPattern() {
        return posIdPattern;
    }

    public String getComPattern() {
        return comPattern;
    }

    public int getMaxCheckoutTime() {
        return maxCheckoutTime;
    }

    public Class[] getConfigurationClass() {
        return configurationClass;
    }
    
}
