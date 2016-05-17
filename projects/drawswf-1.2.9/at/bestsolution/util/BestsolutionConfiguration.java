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
package at.bestsolution.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author tom
 */
public abstract class BestsolutionConfiguration
{
    protected Properties configuration_;
    protected String save_path_;

    public void loadConfig(URL default_config, String user_defined_config) throws FileNotFoundException, IOException
    {
        loadConfig(default_config.toString(), user_defined_config);
    }

    protected void loadConfig(URL default_config, URL user_defined_config) throws FileNotFoundException, IOException
    {
        loadConfig(default_config.toString(), user_defined_config.getFile());
    }

    protected void loadConfig(InputStream default_config_stream, String user_defined_config) throws FileNotFoundException, IOException
    {
        save_path_ = user_defined_config;
        
        Properties default_configuration = new Properties();
        default_configuration.load(default_config_stream);
        default_config_stream.close();

        File file = new File(user_defined_config);

        if (file.exists())
        {
            configuration_ = new Properties(default_configuration);
            FileInputStream in = new FileInputStream(user_defined_config);
            configuration_.load(in);
            in.close();
        }
        else
        {
            configuration_ = default_configuration;
        }
    }
    
    protected void loadConfig(String default_config, String user_defined_config) throws FileNotFoundException, IOException
    {
        FileInputStream in = new FileInputStream(default_config);
        loadConfig(in, user_defined_config);
    }

    public String getProperty(String key)
    {
        return configuration_.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        configuration_.setProperty(key, value);
    }

    public void save() throws FileNotFoundException, IOException
    {
        File save_file = new File(save_path_);

        if (!save_file.exists())
        {
            if (!save_file.getParentFile().exists())
            {
                save_file.getParentFile().mkdirs();
            }

            save_file.createNewFile();
        }

        FileOutputStream out = new FileOutputStream(save_file);
        configuration_.store(out, "");
        out.close();
    }

    public void setSavePath(String save_path)
    {
        save_path_ = save_path;
    }

    public String getSavePath()
    {
        return save_path_;
    }

    public Enumeration getProperties()
    {
        return configuration_.propertyNames();
    }

    public abstract String toString();

}