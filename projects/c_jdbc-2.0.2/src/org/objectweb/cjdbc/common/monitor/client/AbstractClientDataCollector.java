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

package org.objectweb.cjdbc.common.monitor.client;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.DataCollectorException;
import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.monitoring.datacollector.DataCollector;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread;

/**
 * Collects information about C-JDBC clients. TODO: Implements proper client
 * data collection. This is not used at the moment.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public abstract class AbstractClientDataCollector extends AbstractDataCollector
{
  private String virtualDatabaseName;
  private String clientId;
  private int    clientIndex;

  /**
   * @param virtualDatabaseName of the virtualdatabase
   * @param clientId for the client
   * @throws DataCollectorException if cannot access client
   */
  public AbstractClientDataCollector(String virtualDatabaseName, String clientId)
      throws DataCollectorException
  {
    super();
    this.virtualDatabaseName = virtualDatabaseName;
    this.clientId = clientId;
    setClientIndex();
  }

  private Object setClientIndex() throws DataCollectorException
  {
    VirtualDatabase vdb = ((Controller) controller)
        .getVirtualDatabase(virtualDatabaseName);
    ArrayList activeThreads = vdb.getActiveThreads();
    int size = activeThreads.size();
    VirtualDatabaseWorkerThread client = null;
    int index = 0;
    for (index = 0; index < size; index++)
    {
      client = ((VirtualDatabaseWorkerThread) activeThreads.get(index));
      if (client.getUser().equals(clientId))
        break;
      else
        client = null;
    }

    if (client == null)
      throw new DataCollectorException(DataCollector.CLIENT_NOT_FOUND);
    else
    {
      this.clientIndex = index;
      return client;
    }
  }

  private Object checkClientIndex() throws DataCollectorException
  {
    VirtualDatabase vdb = ((Controller) controller)
        .getVirtualDatabase(virtualDatabaseName);
    ArrayList activeThreads = vdb.getActiveThreads();
    VirtualDatabaseWorkerThread client = (VirtualDatabaseWorkerThread) activeThreads
        .get(clientIndex);
    if (client.getUser().equals(clientId))
      return client;
    else
    {
      return setClientIndex();
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long collectValue() throws DataCollectorException
  {
    VirtualDatabaseWorkerThread client = (VirtualDatabaseWorkerThread) checkClientIndex();
    return this.getValue(client);
  }

  /**
   * We have the client object so let's get the value we want from ot
   * 
   * @param client as an object to allow it through RMI, but IS a
   *          <code>VirtualDatabaseWorkerThread</code>
   * @return the collected value
   */
  public abstract long getValue(Object client);

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getTargetName()
   */
  public String getTargetName()
  {
    return clientId;
  }
}
