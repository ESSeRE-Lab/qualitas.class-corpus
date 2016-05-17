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

package org.objectweb.cjdbc.common.shared;

import java.io.Serializable;

import javax.management.NotCompliantMBeanException;

import org.objectweb.cjdbc.controller.backend.DatabaseBackend;

/**
 * This class defines a BackendInfo. We cannot use DatabaseBackend as a
 * serializable object because it is used as an MBean interface. We use this
 * class to share configuration information on backends between distributed
 * virtual database.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class BackendInfo implements Serializable
{
  private static final long serialVersionUID   = 235252034979233679L;

  private String            name;
  private String            url;
  private String            driverPath;
  private String            driverClassName;
  private String            virtualDatabaseName;
  private String            connectionTestStatement;
  private int               dynamicPrecision;
  private boolean           gatherSystemTables = false;
  private String            schemaName;
  private String            xml;

  /**
   * Creates a new <code>BackendInfo</code> object. Extract configuration
   * information from the original backend object
   * 
   * @param backend DatabaseBackend to extract information from
   */
  public BackendInfo(DatabaseBackend backend)
  {
    this.url = backend.getURL();
    this.name = backend.getName();
    this.driverPath = backend.getDriverPath();
    this.driverClassName = backend.getDriverClassName();
    this.virtualDatabaseName = backend.getVirtualDatabaseName();
    this.connectionTestStatement = backend.getConnectionTestStatement();
    this.dynamicPrecision = backend.getDynamicPrecision();
    this.gatherSystemTables = backend.isGatherSystemTables();
    this.schemaName = backend.getSchemaName();
    this.xml = backend.getXml();
  }

  /**
   * Create a corresponding DatabaseBackend object from the information stored
   * in this object.
   * 
   * @return a <code>DatabaseBackend</code>
   */
  public DatabaseBackend getDatabaseBackend()
  {
    try
    {
      return new DatabaseBackend(name, driverPath, driverClassName, url,
          virtualDatabaseName, true, connectionTestStatement);
    }
    catch (NotCompliantMBeanException e)
    {
      throw new RuntimeException(
          "Unable to recreate backend from BackendInfo object", e);
    }
  }

  /**
   * Returns the xml value.
   * 
   * @return Returns the xml.
   */
  public String getXml()
  {
    return xml;
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
   * Returns the driverClassName value.
   * 
   * @return Returns the driverClassName.
   */
  public String getDriverClassName()
  {
    return driverClassName;
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
   * Returns the dynamicPrecision value.
   * 
   * @return Returns the dynamicPrecision.
   */
  public int getDynamicPrecision()
  {
    return dynamicPrecision;
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
   * Returns the schemaName value.
   * 
   * @return Returns the schemaName.
   */
  public String getSchemaName()
  {
    return schemaName;
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
   * Returns the virtualDatabaseName value.
   * 
   * @return Returns the virtualDatabaseName.
   */
  public String getVirtualDatabaseName()
  {
    return virtualDatabaseName;
  }

  /**
   * Returns the gatherSystemTables value.
   * 
   * @return Returns the gatherSystemTables.
   */
  public boolean isGatherSystemTables()
  {
    return gatherSystemTables;
  }

  /**
   * Set the xml information on that BackendInfo object
   * 
   * @param xml new XML to set
   */
  public void setXml(String xml)
  {
    this.xml = null;
  }

}