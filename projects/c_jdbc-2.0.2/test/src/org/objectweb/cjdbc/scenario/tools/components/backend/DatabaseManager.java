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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools.components.backend;

import java.io.File;

import org.objectweb.cjdbc.scenario.tools.ScenarioConstants;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.ComponentManager;
import org.objectweb.cjdbc.scenario.tools.components.backend.hsqldb.HypersonicProcess;
import org.objectweb.cjdbc.scenario.tools.util.KillJava;

/**
 * This class defines a DatabaseManager
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class DatabaseManager extends ComponentManager
{
  /** HSQLDB processes selection */
  public static final int HSQLDB = 0;
  /** MYSQL processes selection */
  public static final int MYSQL  = 1;

  private int             selection;

  /**
   * Creates a new <code>DatabaseManager</code> object
   */
  public DatabaseManager()
  {
    selection = HSQLDB;
    File file = new File(ScenarioConstants.PROCESS_DIRECTORY);
    if (file.exists())
      if (!ScenarioUtility.deleteDir(file))
        System.out
            .println("Database Manager could not delete previous files...");
  }

  /**
   * Creates a new <code>DatabaseManager</code> object
   * 
   * @param selection the process that this database manager will generate
   */
  public DatabaseManager(int selection)
  {
    this.selection = selection;
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentManager#instanciateProcess(java.lang.String,
   *             java.lang.String)
   */
  public ComponentInterface instanciateProcess(String port, String database)
      throws Exception
  {
    switch (selection)
    {
      case HSQLDB :
        ComponentInterface object = new HypersonicProcess(port, database);
        waitForStarted(port);
        return object;
      default :
        throw new Exception("Invalid process selection");
    }
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentManager#getDefaultConfigurationFile()
   */
  public String getDefaultConfigurationFile()
  {
    return "database";
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentManagerInterface#stopAll()
   *             In Linux, also kills hypersonic processes
   */
  public void stopAll()
  {
    super.stopAll();
    if (System.getProperty("os.name").equals("Linux"))
      try
      {
        new KillJava().execute();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
  }
}