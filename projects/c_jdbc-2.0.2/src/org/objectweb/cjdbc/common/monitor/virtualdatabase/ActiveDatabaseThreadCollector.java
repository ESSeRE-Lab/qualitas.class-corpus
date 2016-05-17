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
 * Contributor(s): 
 */

package org.objectweb.cjdbc.common.monitor.virtualdatabase;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * Count active threads for a <code>VirtualDatabase</code>
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 */
public class ActiveDatabaseThreadCollector
    extends AbstractVirtualDatabaseDataCollector
{
  private static final long serialVersionUID = -5197418723162228403L;

  /**
   * create collector
   * 
   * @param virtualDatabaseName to get info from
   */
  public ActiveDatabaseThreadCollector(String virtualDatabaseName)
  {
    super(virtualDatabaseName);
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long getValue(Object database)
  {
    return ((VirtualDatabase) database).getActiveThreads().size();
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getDescription()
   */
  public String getDescription()
  {
    return Translate.get("monitoring.virtualdatabase.active.threads");
  }
}
