/*
 *  Copyright (c) 2003
 *  bestsolution EDV Systemhaus GmbH,
 *  http://www.bestsolution.at
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

/*
 * Created on 22.02.2003
 *
 */
package at.bestsolution.drawswf.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import at.bestsolution.util.BestsolutionConfiguration;

/**
 * @author tom
 */
public class DrawSWFConfig extends BestsolutionConfiguration
{
    private static DrawSWFConfig instance_ = null;

    private DrawSWFConfig()
    {
        super();
    }

    public static DrawSWFConfig getInstance()
    {
        if (instance_ == null)
        {
            instance_ = new DrawSWFConfig();
            instance_.loadConfig();
        }

        return instance_;
    }

    private void loadConfig()
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream("DefaultDrawSWFConfiguration.properties");
        
        try
        {
            loadConfig(in, System.getProperty("user.home")+ File.separator + ".drawswf" + File.separator + "drawswf.properties");
            
            if( getProperty("pluginpath").equals("") ) {
            	File file = new File( System.getProperty("user.home") + File.separator + ".drawswf" + File.separator + "plugin" );
            	
            	if( ! file.exists() ) {
            		file.mkdirs();
            	}
            	
            	setProperty("pluginpath",System.getProperty("user.home") + File.separator + ".drawswf" + File.separator + "plugin");
            	
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "DrawSWF";
    }

}
