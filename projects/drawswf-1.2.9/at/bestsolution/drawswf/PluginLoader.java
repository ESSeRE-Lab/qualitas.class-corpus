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
 * Created on 21.02.2003
 *
 */
package at.bestsolution.drawswf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.util.DrawSWFConfig;
import at.bestsolution.util.ExtensionsClassLoader;

/**
 * @author tom
 */
public class PluginLoader
{
    public PluginLoader()
    {
    }

    public ArrayList loadPlugins()
    {
        ArrayList plugins = new ArrayList();

        ClassLoader loader = ExtensionsClassLoader.getClassLoader(DrawSWFConfig.getInstance().getProperty("pluginpath"));
        URL[] jars = ExtensionsClassLoader.getJars();

        if (jars != null)
        {
            for (int i = 0; i < jars.length; i++)
            {
                try
                {
                    System.out.println(ExtensionsClassLoader.getMainClassName(jars[i]));
                    Class user_class = loader.loadClass(ExtensionsClassLoader.getMainClassName(jars[i]));

                    if (user_class != null)
                    {
                        AbstractPlugin plugin = (AbstractPlugin) user_class.newInstance();
                        plugin.init(this, MainWindow.getDrawingPanel(), MainWindow.MAIN_WINDOW);
                        plugin.loadSelf();
                        plugins.add(plugin);
                    }
                    else
                    {
                        System.err.println("Couldn't find main class for "+jars[i].getFile()+"! Please specify in MANIFEST");
                    }
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch (InstantiationException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return plugins;
    }

    public void addToMenu(String menu_name, AbstractDrawAction abstract_action, int position)
    {
        MainWindow.MAIN_WINDOW.getDrawMenuBar().addGenericMenuItem(menu_name, abstract_action, position);
    }

    public DrawSWFConfig getConfig()
    {
        return DrawSWFConfig.getInstance();
    }

    //    public void addToToolbar( String toolbar_section_name, AbstractDrawAction abstract_action, int position )
    //    {
    //        MainWindow.MAIN_WINDOW.getDrawToolbar().addGenericMenuItem(toolbar_section_name,abstract_action, position);
    //    }
}
