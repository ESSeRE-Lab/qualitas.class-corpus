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
 * Contributor(s): 
 */
package org.objectweb.cjdbc.common.monitor.virtualdatabase;

import org.objectweb.cjdbc.common.exceptions.DataCollectorException;
import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * Abstract class for virtual databases collectors
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 */
public abstract class AbstractVirtualDatabaseDataCollector
    extends
      AbstractDataCollector
{
  private String virtualDatabaseName;

  /**
   * abstract collector contructor
   * @param virtualDatabaseName to collect data from
   */
  public AbstractVirtualDatabaseDataCollector(String virtualDatabaseName)
  {
    super();
    this.virtualDatabaseName = virtualDatabaseName;
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long collectValue() throws DataCollectorException
  {
    VirtualDatabase vdb = ((Controller)controller).getVirtualDatabase(
        virtualDatabaseName);
    return this.getValue(vdb);
  }

  /**
   * We have the database object so let's get the value we want from ot
   * @param database as an object to allow it through RMI, but IS a <code>VirtualDatabase</code>
   * @return the collected value 
   */
  public abstract long getValue(Object database);
  
  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getTargetName()
   */
  public String getTargetName()
  {
    return virtualDatabaseName;
  }
}
