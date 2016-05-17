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
package org.objectweb.cjdbc.common.monitor.scheduler;

import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * Abstract class to factor code for scheduler collectors
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 */
public abstract class AbstractSchedulerDataCollector
    extends
      AbstractDataCollector
{
  private String virtualDatabaseName;

  /**
   * create a new collector
   * 
   * @param virtualDatabaseName database accessed to get data
   */
  public AbstractSchedulerDataCollector(String virtualDatabaseName)
  {
    super();
    this.virtualDatabaseName = virtualDatabaseName;
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long collectValue()
  {
    VirtualDatabase vdb = ((Controller)controller).getVirtualDatabase(
        virtualDatabaseName);
    AbstractScheduler scheduler = vdb.getRequestManager().getScheduler();
    return this.getValue(scheduler);
  }

  /**
   * Get information on the scheduler retrieved by <code>collectValue()</code>
   * 
   * @param scheduler to get value from
   * @return collected value
   */
  public abstract long getValue(Object scheduler);
  
  

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getTargetName()
   */
  public String getTargetName()
  {
    return virtualDatabaseName;
  }
}
