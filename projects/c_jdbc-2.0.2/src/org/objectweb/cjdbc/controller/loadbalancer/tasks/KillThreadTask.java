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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Julie Marguerite.
 */

package org.objectweb.cjdbc.controller.loadbalancer.tasks;

import java.sql.SQLException;

import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;

/**
 * This task is used to kill backend worker threads.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class KillThreadTask extends AbstractTask
{

  /**
   * Creates a new <code>KillThreadTask</code> instance that must be executed
   * by <code>nbToComplete</code> backend threads
   * 
   * @param nbToComplete number of threads that must succeed before returning
   * @param totalNb total number of threads
   */
  public KillThreadTask(int nbToComplete, int totalNb)
  {
    super(nbToComplete, totalNb);
  }

  /**
   * This function call the backendThread kill function and notifies the task
   * completion success.
   * 
   * @param backendThread the backend thread that will execute the task
   * @throws SQLException if an error occurs
   */
  public void executeTask(BackendWorkerThread backendThread)
      throws SQLException
  {
    backendThread.killWithoutDisablingBackend();
    notifySuccess();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "KillThreadTask";
  }
}