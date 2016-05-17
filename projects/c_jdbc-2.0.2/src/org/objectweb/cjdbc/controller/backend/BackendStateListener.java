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

package org.objectweb.cjdbc.controller.backend;

import java.sql.SQLException;

import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.recoverylog.BackendRecoveryInfo;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;

/**
 * This class defines a BackendStateListener
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BackendStateListener
{

  Trace                   logger = Trace.getLogger(BackendStateListener.class
                                     .getName());
  private String          virtualDatabaseName;
  private RecoveryLog recoveryLog;

  /**
   * Creates a new <code>BackendStateListener</code> object
   * 
   * @param vdbName virtual database name
   * @param recoveryLog recovery log
   */
  public BackendStateListener(String vdbName, RecoveryLog recoveryLog)
  {
    this.virtualDatabaseName = vdbName;
    this.recoveryLog = recoveryLog;
  }

  /**
   * Update the persistent state of the backend in the recovery log
   * 
   * @param backend the backend to update information from
   */
  public synchronized void changeState(DatabaseBackend backend)
  {
    try
    {
      recoveryLog.storeBackendRecoveryInfo(virtualDatabaseName,
          new BackendRecoveryInfo(backend.getName(), backend
              .getLastKnownCheckpoint(), backend.getStateValue(),
              virtualDatabaseName));
    }
    catch (SQLException e)
    {
      logger.error("Could not store informatione for backend:"
          + backend.getName(), e);
    }
  }
}