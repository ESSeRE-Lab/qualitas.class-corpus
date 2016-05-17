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

package org.objectweb.cjdbc.common.monitor.backend;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;

/**
 * Return total of transactions made on this backend
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 */
public class TransactionsCollector extends AbstractBackendDataCollector
{
  /**
   * 
   */
  private static final long serialVersionUID = -7454756414689230255L;

  /**
   * @param backendName of the backend to get data from
   * @param virtualDatabaseName database accessed to get data
   */
  public TransactionsCollector(String backendName, String virtualDatabaseName)
  {
    super(backendName, virtualDatabaseName);
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long getValue(Object backend)
  {
    return ((DatabaseBackend) backend).getTotalTransactions();
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getDescription()
   */
  public String getDescription()
  {
    return Translate.get("monitoring.backend.total.transactions");
  }
}
