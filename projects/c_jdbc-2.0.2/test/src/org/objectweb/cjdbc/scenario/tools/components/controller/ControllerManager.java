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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools.components.controller;

import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.scenario.tools.ScenarioConstants;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.ComponentManager;

/**
 * This class defines a ControllerManager
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ControllerManager extends ComponentManager
{
  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentManager#getDefaultConfigurationFile()
   */
  public String getDefaultConfigurationFile()
  {
    return ScenarioConstants.DATABASE_CONFIG_FILE;
  }
  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentManager#instanciateProcess(java.lang.String, java.lang.String)
   */
  public ComponentInterface instanciateProcess(String port, String configurationFile)
      throws Exception
  {
    return new ControllerProcess(port,configurationFile);
  }
  
  /**
   * 
   * This loads virtual database on the given contoller
   * 
   * @param controller to load the database into
   * @param vdb the virtual db name to load
   * @param file the template to use
   * @throws Exception if fails
   */
  public void loadVirtualDatabases(Controller controller,String vdb,String file) throws Exception
  {
    ((ComponentInterface) processes.get(""+controller.getPortNumber())).loadDatabase(file,vdb);
  }
}