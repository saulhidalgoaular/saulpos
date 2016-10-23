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

import Controller.Util.MainController;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ConnectionModel {

    private ComboPooledDataSource pool;
    
    public ConnectionModel() {
    }

    /**
     * Initialize the Pool of Connections
     * @param serverUrl Server URL
     * @param dbUser Username
     * @param dbPassword Password
     * @param maxCheckoutTime Max number of seconds a connection can be checked out from the pool.
     * @throws PropertyVetoException Driver couldn't be found
     */
    public void initializePool
            (String serverUrl,
             String dbUser,
             String dbPassword,
             int maxCheckoutTime)
            
                throws PropertyVetoException{
        
        MainController.getInstance().getLogger().info(ConnectionModel.class, "Starting Connection Pool...");
        String addr = "jdbc:mysql://" + serverUrl;
        
        pool = new ComboPooledDataSource();
        pool.setDriverClass("com.mysql.jdbc.Driver");
        // TODO: FIX THIS
        //pool.setCheckoutTimeout(maxCheckoutTime);
        pool.setUser(dbUser);
        pool.setPassword(dbPassword);
        pool.setJdbcUrl(addr);
        
        MainController.getInstance().getLogger().info(ConnectionModel.class , "Connection Pool started!");
    }
    
    /**
     * Testing connection
     * @param serverUrl Server URL
     * @param user Usename
     * @param password Password
     * @throws SQLException Any connection err
     */
    public void testConnection(String serverUrl , String user , String password) throws SQLException, Exception{
        
        // TODO
        // FIX THIS BUG
        /*if ( !serverUrl.matches(ConstantsController.getInstance().getUrlDatabasePattern()) ){
            throw new Exception(MainController.getInstance().getLanguage().get("FormatErr") + " " + MainController.getInstance().getLanguage().get("dbConfigFrame"));
        }*/
        
        String addr = "jdbc:mysql://" + serverUrl;
        Connection c = DriverManager.getConnection(addr,user,password);
        
        /**
         * Testing if database exists
         */
        DatabaseMetaData meta = c.getMetaData();
        ResultSet rs = meta.getCatalogs();
        
        String[] urlSplot = serverUrl.split("/");
        String databaseName = urlSplot[urlSplot.length-1];
        
        boolean exists = false;
        while ( rs.next() && !exists ) {
            if ( rs.getString("TABLE_CAT").equals(databaseName) ){
                exists = true;
            }
        }
        
        c.close();
        
        if ( !exists ){
            throw new Exception("Database not found");
        }
        
    }
    
    public Connection getConnection() throws SQLException{
        return pool.getConnection();
    }
}
