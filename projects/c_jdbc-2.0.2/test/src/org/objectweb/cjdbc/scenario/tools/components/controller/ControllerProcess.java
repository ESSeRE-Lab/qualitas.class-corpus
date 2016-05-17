/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.tools.components.controller;

import java.io.File;

import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.core.ControllerConstants;
import org.objectweb.cjdbc.controller.core.ControllerFactory;
import org.objectweb.cjdbc.scenario.tools.ScenarioConstants;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;

/**
 * This class defines a ControllerProcess
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ControllerProcess implements ComponentInterface
{
  String     port;
  String     database;
  Controller controller;

  /**
   * Creates a new <code>ControllerProcess</code> object
   * 
   * @param port port use by the controller
   * @param database database to load with this controller
   * @throws Exception if fails
   */
  public ControllerProcess(String port, String database) throws Exception
  {
    this.port = port;
    this.database = database;
    start();
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#start()
   */
  public void start() throws Exception
  {
    String controllerConfig = getClass().getResource(
        ScenarioConstants.CONTROLLER_DIR
            + ScenarioConstants.CONTROLLER_DEFAULT_FILE).getPath();
    String[] args = new String[]{"-p", port, "-f", controllerConfig};
    ControllerFactory factory = new ControllerFactory(args);
    Controller controller = factory.getController();
    controller.setConfiguration(factory);
    if (controller != null)
      controller.launch();
    this.controller = controller;
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#getDatabase()
   */
  public String getDatabase()
  {
    return database;
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#loadDatabase(java.lang.String)
   */
  public void loadDatabase(String xml) throws Exception
  {
    this.loadDatabase(xml, ScenarioConstants.DEFAULT_VDB_NAME);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#loadDatabase(java.lang.String,
   *      java.lang.String)
   */
  public void loadDatabase(String xml, String targetDB) throws Exception
  {
    File f = new File(xml);
    if (!f.exists())
    {
      try
      {
        f = new File(getClass().getResource(
            ScenarioConstants.VIRTUALDATABASE_DIR + File.separator + xml)
            .getFile());
      }
      catch (Exception e)
      {
        throw new Exception("could not find configuration file:" + xml);
      }
      if (!f.exists())
        throw new Exception("could not find configuration file:" + xml);
    }
    if (!f.exists())
      throw new Exception("File configuration not found");
    controller.loadXmlConfiguration(f.getAbsolutePath(), targetDB,
        ControllerConstants.AUTO_ENABLE_TRUE, "");
    if (controller.hasVirtualDatabase(targetDB) == false)
      throw new Exception("Virtual Database Configuration failed");
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#loadDatabase()
   */
  public void loadDatabase() throws Exception
  {
    loadDatabase(ScenarioConstants.DATABASE_CONFIG_FILE);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#getPort()
   */
  public String getPort()
  {
    return String.valueOf(controller.getPortNumber());
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#getProcess()
   */
  public Object getProcess()
  {
    return controller;
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentInterface#release()
   */
  public void release()
  {
    try
    {
      controller.shutdown(Constants.SHUTDOWN_SAFE);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}