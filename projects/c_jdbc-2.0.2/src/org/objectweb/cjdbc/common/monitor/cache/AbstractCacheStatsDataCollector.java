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

package org.objectweb.cjdbc.common.monitor.cache;

import org.objectweb.cjdbc.common.exceptions.DataCollectorException;
import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.monitoring.datacollector.DataCollector;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * Abstract template to factor code for cache collectors
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public abstract class AbstractCacheStatsDataCollector
    extends AbstractDataCollector
{
  private String virtualDatabaseName;

  /**
   * new collector
   * 
   * @param virtualDatabaseName database accessed to get data
   */
  public AbstractCacheStatsDataCollector(String virtualDatabaseName)
  {
    super();
    this.virtualDatabaseName = virtualDatabaseName;
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long collectValue() throws DataCollectorException
  {
    VirtualDatabase vdb = ((Controller) controller)
        .getVirtualDatabase(virtualDatabaseName);
    AbstractResultCache cache = vdb.getRequestManager().getResultCache();
    if (cache == null)
      throw new DataCollectorException(DataCollector.NO_CACHE_ENABLED);
    return this.getValue(cache);
  }

  /**
   * We have the cache object so let's get the value we want from ot
   * 
   * @param cache as an object to allow it through RMI, but IS a
   *          <code>AbstractResultCache</code>
   * @return the collected value
   */
  public abstract long getValue(Object cache);

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getTargetName()
   */
  public String getTargetName()
  {
    return virtualDatabaseName;
  }
}
