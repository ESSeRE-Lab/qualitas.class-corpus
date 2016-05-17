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

package org.objectweb.cjdbc.common.monitor.backend;

import org.objectweb.cjdbc.common.exceptions.DataCollectorException;
import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.monitoring.datacollector.DataCollector;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * Abstract class to factor code for collecting data from backends
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public abstract class AbstractBackendDataCollector
    extends AbstractDataCollector
{
  private String backendName;
  private String virtualDatabaseName;

  /**
   * Create new collector
   * 
   * @param backendName of the backend to get data from
   * @param virtualDatabaseName that contains reference to this backend
   */
  public AbstractBackendDataCollector(String backendName,
      String virtualDatabaseName)
  {
    super();
    this.backendName = backendName;
    this.virtualDatabaseName = virtualDatabaseName;
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long collectValue() throws DataCollectorException
  {
    try
    {
      VirtualDatabase vdb = ((Controller) controller)
          .getVirtualDatabase(virtualDatabaseName);
      DatabaseBackend db = vdb.getAndCheckBackend(backendName,
          VirtualDatabase.NO_CHECK_BACKEND);
      return this.getValue(db);
    }
    catch (Exception e)
    {
      throw new DataCollectorException(DataCollector.BACKEND_NOT_ACCESSIBLE);
    }
  }

  /**
   * get the proper collected value when we have instace of the backend
   * 
   * @param backend <code>DatabaseBackend</code> instance
   * @return collected value
   */
  public abstract long getValue(Object backend);

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getTargetName()
   */
  public String getTargetName()
  {
    return backendName;
  }
}
