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
package at.bestsolution.drawswf.plugin.remotesave.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import at.bestsolution.util.BestsolutionConfiguration;

/**
 * @author tom
 */
public class RemoteSaveConfig extends BestsolutionConfiguration
{
    private static RemoteSaveConfig instance_ = null;
    
    private RemoteSaveConfig()
    {
        super();
    }
    
    public static RemoteSaveConfig getInstance()
    {
        if( instance_ == null)
        {
            instance_ = new RemoteSaveConfig();
            instance_.loadConfig();
        }
        
        return instance_;
    }
    
    public void loadConfig()
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream("at/bestsolution/drawswf/plugin/remotesave/DefaultRemoteSaveConfiguration.properties");
        try
        {
            loadConfig( in, System.getProperty("user.home")+ File.separator + ".drawswf" + File.separator + "remote-saveplugin.properties" );
        }
        catch( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch( IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Remote Save Plugin";
    }

}
