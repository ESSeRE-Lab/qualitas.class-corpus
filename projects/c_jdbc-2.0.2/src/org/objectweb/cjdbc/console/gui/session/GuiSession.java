/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.gui.session;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.objectweb.cjdbc.common.util.ReadWrite;

/**
 * This class defines a GUISession
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GuiSession
{
  private final boolean saveDatabaseInfoToDisk = true;
  ArrayList             controllerItems;
  ArrayList             configurationFiles;
  Hashtable             databaseItems;

  /**
   * Creates a new <code>GUISession.java</code> object no recorded values
   */
  public GuiSession()
  {
    controllerItems = new ArrayList();
    configurationFiles = new ArrayList();
    databaseItems = new Hashtable();
  }

  /**
   * Save the current gui session into a file
   * 
   * @param sessionFile the file to save the session in
   * @throws IOException if writing causes a problem
   */
  public void saveSessionToFile(File sessionFile) throws IOException
  {
    BufferedWriter writer = new BufferedWriter(new FileWriter(sessionFile));
    if (saveDatabaseInfoToDisk)
    {
      writer.write("### DATABASES           ###"
          + System.getProperty("line.separator"));
      writer.write(ReadWrite.write(databaseItems, false));
    }
    writer.write("### CONTROLLERS         ###"
        + System.getProperty("line.separator"));
    writer.write(ReadWrite.write(controllerItems, "controller", false));
    writer.write("### CONFIGURATION FILES ###"
        + System.getProperty("line.separator"));
    writer.write(ReadWrite.write(configurationFiles, "configuration", false));
    writer.close();
  }

  /**
   * Load a gui session from the give file
   * 
   * @param sessionFile the file to load the session from
   * @throws IOException if loading causes a problem
   */
  public void loadSessionFromFile(File sessionFile) throws IOException
  {
    if (sessionFile.exists())
    {
      Properties session = new Properties();
      session.load(new FileInputStream(sessionFile));
      Enumeration enume = session.keys();
      String key;
      String value;
      while (enume.hasMoreElements())
      {
        key = (String) enume.nextElement();
        value = (String) session.get(key);
        if (key.startsWith("controller"))
          controllerItems.add(value);
        else if (key.startsWith("database"))
          databaseItems.put(key, value);
        else if (key.startsWith("configuration"))
          addFileToConfigurationFiles(new File(value));
      }
    }
    else
    {
      controllerItems.add("0.0.0.0:1090");
      //      URL url = this.getClass().getResource("/virtualdatabase");
      //      System.out.println(url.getFile());
      //      if (url != null)
      //      {
      //        File f = new File(url.getFile());
      //        File[] list = f.listFiles();
      //        
      //        for (int i = 0; i < list.length; i++)
      //          addFileToConfigurationFiles(list[i]);
      //      }
    }
  }

  /**
   * Returns the controllerItems value.
   * 
   * @return Returns the controllerItems.
   */
  public ArrayList getControllerItems()
  {
    return controllerItems;
  }

  /**
   * Add a controller url to the list of controllers
   * 
   * @param controller [ipAddress]:[portNumber]
   */
  public void addControllerToList(String controller)
  {
    if (!controllerItems.contains(controller))
      controllerItems.add(controller);
  }

  /**
   * checkif a controller is in the session
   * 
   * @param controller [ipAddress]:[portNumber]
   * @return true if controller is in session
   */
  public boolean checkControllerInSession(String controller)
  {
    return controllerItems.contains(controller);
  }

  /**
   * Get the list of configuration files
   * 
   * @return the <code>Vector</code> of configuration files
   */
  public ArrayList getConfigurationFiles()
  {
    return configurationFiles;
  }

  /**
   * Add a file to the list of configuration files if it is not already in the
   * session
   * 
   * @param newFile to add to the list
   */
  public void addFileToConfigurationFiles(File newFile)
  {
    if (!configurationFiles.contains(newFile))
      configurationFiles.add(newFile);
  }

  /**
   * Add authentication to session
   * 
   * @param databaseName name of the virtual database
   * @param login login name
   * @param password password associated to the login
   */
  public void addDatabaseToSession(String databaseName, String login,
      String password)
  {
    databaseItems.put("database." + databaseName + ".login", login);
    databaseItems.put("database." + databaseName + ".password", password);
  }

  /**
   * Test if user was authenticated for this database
   * 
   * @param databaseName name of the virtual database
   * @return true if has been authenticated with success before, false
   *         otherwise
   */
  public boolean isAuthenticatedDatabase(String databaseName)
  {
    return databaseItems.containsKey("database." + databaseName + ".login");
  }

  /**
   * Retrieve the login stored for this database
   * 
   * @param databaseName name of the virtual database
   * @return password as a <code>String</code> or null
   */
  public String getAuthenticatedDatabaseLogin(String databaseName)
  {
    return (String) databaseItems.get("database." + databaseName + ".login");
  }

  /**
   * Retrieve the password stored for this database
   * 
   * @param databaseName name of the virtual database
   * @return password as a <code>String</code> or null
   */
  public String getAuthenticatedDatabasePassword(String databaseName)
  {
    return (String) databaseItems.get("database." + databaseName + ".password");
  }

  /**
   * Returns the databaseItems value.
   * 
   * @return Returns the databaseItems.
   */
  public Hashtable getDatabaseItems()
  {
    return databaseItems;
  }
}
