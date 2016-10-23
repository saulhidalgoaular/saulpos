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
package Controller.Util;

import org.apache.log4j.Logger;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class LoggerController {

    private Logger logger = Logger.getLogger(MainController.class);
    
    public LoggerController() {
    }
    
    public void info( Class c , Object o ){
        logger = Logger.getLogger(c);
        logger.info(o);
    }

    public void debug( Class c , Object o ){
        logger = Logger.getLogger(c);
        logger.debug(o);
    }

    public void error( Class c , Object o ){
        logger = Logger.getLogger(c);
        logger.error(o);
    }
    
    public void trace( Class c , Object o ){
        logger = Logger.getLogger(c);
        logger.trace(o);
    }
    
    public void fatal( Class c , Object o ){
        logger = Logger.getLogger(c);
        logger.fatal(o);
    }
    
    public void warn( Class c , Object o ){
        logger = Logger.getLogger(c);
        logger.warn(o);
    }
}
