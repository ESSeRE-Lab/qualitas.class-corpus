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
 * Created on 07.03.2003
 *
 */
package at.bestsolution.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.Attributes;

/***
 * This class provides a single instance of a ClassLoader, using the <i>Factory Method</i>
 * and <i>Singleton</i> design patterns.
 * The ClassLoader provided will be able to search for extension classes in the <b>lib</b> and <b>classes</b>
 * subdirectories in the application path as described by the system property <code>sitaci.home</code>.
 * <b>Important note:</b><br>
 * <ul>
 * <li><b>lib</b> directory is intended for extension <i>.jar</i> files.</li>
 * <li><b>classes</b> directory is intended for extension <i>.class</i> files.</li>
 * </ul>
 * Be aware that this class is not a class loader, but only a mean to obtain an instance of a 
 * ClassLoader object adecuately configured for its purpose.<br>
 * This class also implements the <i>Double Checked Locking</i> design pattern, so it is safe to
 * call the <code>getClassLoader()</code> method concurrently and only one instance of the
 * class loader will be created.
 * @author <a href="mailto:ezavalla@yahoo.com.ar">Eduardo M. Zavalla</a> 06/2002
 */
public class ExtensionsClassLoader
{

    //   /** Home folder property name. */
    //public static final String HOME_DIR_PROP_NAME = "sitaci.home";

    //    /** Additional library folder name. */
    //public static final String DEFAULT_LIB_DIR = "lib";

    /** Default LIB files extension. */
    public static final String DEFAULT_LIB_EXT = ".jar";

    //    /** Additional classes folder name. */
    //    public static final String DEFAULT_CLASS_DIR = "classes";

    /** The unique class loader returned by this class. */
    private static URLClassLoader cl_ = null;

    private static URL[] urls_;

    /***
     * Private constructor to avoid the creation of instances of this class.
     */
    private ExtensionsClassLoader()
    {
    }

    /***
     * Factory method for ClassLoader objects. Be aware that the returned object is an instance of
     * an custom configured URLClassLoader which is able to search for classes in the <b>lib</b>
     * and <b>classes</b> subdirectories. The <b>classes</b> directory is always included
     * in the classpath, but only <b>.jar</b> files present in the <b>lib</b> directory are
     * included in the classpath.
     */
    public static ClassLoader getClassLoader(String plugindir)
    {
        System.out.println(plugindir);

        if (cl_ == null)
        {
            synchronized (ExtensionsClassLoader.class)
            {
                ArrayList urls = new ArrayList();

                // Add jar files in the plugindir
                File libDir = new File( plugindir );
                File[] jars = libDir.listFiles(new FileFilter()
                {
                    public boolean accept(File pathname)
                    {
                        return (pathname.getName().endsWith(DEFAULT_LIB_EXT)) ? true : false;
                    }
                });

                for (int i = 0; i < jars.length; i++)
                {
                    try
                    {
                        urls.add(jars[i].toURL());
                    }
                    catch (MalformedURLException mue)
                    {
                        mue.printStackTrace();
                    }
                }

                // Build URLs list
                urls_ = new URL[urls.size()];
                Iterator it = urls.listIterator();
                int i = 0;
                while (it.hasNext())
                {
                    urls_[i++] = (URL) it.next();
                }

                // Build the class loader
                cl_ = URLClassLoader.newInstance(urls_);
            }
        }

        return cl_;
    }

    public static URL[] getJars()
    {
        return urls_;
    }

    public static String getMainClassName(URL url) throws IOException
    {
        URL u = new URL("jar", "", url + "!/");
        JarURLConnection uc = (JarURLConnection) u.openConnection();
        Attributes attr = uc.getMainAttributes();
        return attr != null ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
    }

}