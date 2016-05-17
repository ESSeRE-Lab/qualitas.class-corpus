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

package org.objectweb.cjdbc.console.wizard.objects;

import java.util.Hashtable;

import javax.swing.JComponent;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;

/**
 * A backend object. Used to transfer and collect backend data between forms
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Backend extends JComponent
{
  String    name               = WizardTranslate.get("label.backend.undefined");
  String    driver;
  String    driverPath;
  String    url;
  String    connectionTestStatement;
  String    dynamicPrecision;
  String    gatherSystemTables = "false";
  Hashtable connectionManagers = new Hashtable();

  /**
   * Sets the connectionManagers value.
   * 
   * @param connectionManagers The connectionManagers to set.
   */
  public void setConnectionManagers(Hashtable connectionManagers)
  {
    this.connectionManagers = connectionManagers;
  }

  /**
   * Returns the connectionManagers value.
   * 
   * @return Returns the connectionManagers.
   */
  public Hashtable getConnectionManagers()
  {
    return connectionManagers;
  }

  /**
   * Returns the dynamicPrecision value.
   * 
   * @return Returns the dynamicPrecision.
   */
  public String getDynamicPrecision()
  {
    return dynamicPrecision;
  }

  /**
   * Sets the dynamicPrecision value.
   * 
   * @param dynamicPrecision The dynamicPrecision to set.
   */
  public void setDynamicPrecision(String dynamicPrecision)
  {
    this.dynamicPrecision = dynamicPrecision;
  }

  /**
   * Returns the gatherSystemTables value.
   * 
   * @return Returns the gatherSystemTables.
   */
  public String getGatherSystemTables()
  {
    return gatherSystemTables;
  }

  /**
   * Sets the gatherSystemTables value.
   * 
   * @param gatherSystemTables The gatherSystemTables to set.
   */
  public void setGatherSystemTables(String gatherSystemTables)
  {
    this.gatherSystemTables = gatherSystemTables;
  }

  /**
   * Returns the connectionTestStatement value.
   * 
   * @return Returns the connectionTestStatement.
   */
  public String getConnectionTestStatement()
  {
    return connectionTestStatement;
  }

  /**
   * Sets the connectionTestStatement value.
   * 
   * @param connectionTestStatement The connectionTestStatement to set.
   */
  public void setConnectionTestStatement(String connectionTestStatement)
  {
    this.connectionTestStatement = connectionTestStatement;
  }

  /**
   * Returns the driver value.
   * 
   * @return Returns the driver.
   */
  public String getDriver()
  {
    return driver;
  }

  /**
   * Sets the driver value.
   * 
   * @param driver The driver to set.
   */
  public void setDriver(String driver)
  {
    this.driver = driver;
  }

  /**
   * Returns the driverPath value.
   * 
   * @return Returns the driverPath.
   */
  public String getDriverPath()
  {
    return driverPath;
  }

  /**
   * Sets the driverPath value.
   * 
   * @param driverPath The driverPath to set.
   */
  public void setDriverPath(String driverPath)
  {
    this.driverPath = driverPath;
  }

  /**
   * Returns the name value.
   * 
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name value.
   * 
   * @param name The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Returns the url value.
   * 
   * @return Returns the url.
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Sets the url value.
   * 
   * @param url The url to set.
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return name;
  }
}