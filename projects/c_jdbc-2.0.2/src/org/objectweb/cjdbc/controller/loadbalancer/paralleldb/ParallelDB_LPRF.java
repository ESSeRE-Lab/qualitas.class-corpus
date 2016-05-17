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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.loadbalancer.paralleldb;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * This class defines a ParallelDB_LPRF load balancer. This load balancer
 * chooses the node that has the least pending queries for read and write
 * queries execution.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ParallelDB_LPRF extends ParallelDB
{

  /**
   * Creates a new <code>ParallelDB_LPRF</code> object.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @throws Exception if an error occurs
   */
  public ParallelDB_LPRF(VirtualDatabase vdb) throws Exception
  {
    super(vdb);
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.paralleldb.ParallelDB#chooseBackendForReadRequest(org.objectweb.cjdbc.common.sql.AbstractRequest)
   */
  public DatabaseBackend chooseBackendForReadRequest(AbstractRequest request)
      throws SQLException
  {
    // Choose a backend
    try
    {
      vdb.acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    DatabaseBackend backend = null; // The backend that will execute the query

    // Note that vdb lock is released in the finally clause of this try/catch
    // block
    try
    {
      ArrayList backends = vdb.getBackends();
      int size = backends.size();

      if (size == 0)
        throw new SQLException(Translate.get(
            "loadbalancer.execute.no.backend.available", request.getId()));

      // Choose the backend that has the least pending requests
      int leastRequests = 0;
      for (int i = 0; i < size; i++)
      {
        DatabaseBackend b = (DatabaseBackend) backends.get(i);
        if (b.isReadEnabled())
        {
          int pending = b.getPendingRequests().size();
          if ((backend == null) || (pending < leastRequests))
          {
            backend = b;
            if (pending == 0)
              break; // Stop here we will never find a less loaded node
            else
              leastRequests = pending;
          }
        }
      }

      if (backend == null)
        throw new SQLException(Translate.get(
            "loadbalancer.execute.no.backend.enabled", request.getId()));
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.execute.find.backend.failed",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      vdb.releaseReadLockBackendLists();
    }

    return backend;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.paralleldb.ParallelDB#chooseBackendForWriteRequest(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  public DatabaseBackend chooseBackendForWriteRequest(
      AbstractWriteRequest request) throws SQLException
  {
    // Choose a backend
    try
    {
      vdb.acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    DatabaseBackend backend = null; // The backend that will execute the query

    // Note that vdb lock is released in the finally clause of this try/catch
    // block
    try
    {
      ArrayList backends = vdb.getBackends();
      int size = backends.size();

      if (size == 0)
        throw new SQLException(Translate.get(
            "loadbalancer.execute.no.backend.available", request.getId()));

      // Choose the backend that has the least pending requests
      int leastRequests = 0;
      for (int i = 0; i < size; i++)
      {
        DatabaseBackend b = (DatabaseBackend) backends.get(i);
        if (b.isWriteEnabled() && !b.isDisabling())
        {
          int pending = b.getPendingRequests().size();
          if ((backend == null) || (pending < leastRequests))
          {
            backend = b;
            if (pending == 0)
              break; // Stop here we will never find a less loaded node
            else
              leastRequests = pending;
          }
        }
      }

      if (backend == null)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.execute.no.backend.enabled", request.getId()));
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.execute.find.backend.failed",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      vdb.releaseReadLockBackendLists();
    }

    return backend;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getInformation()
   */
  public String getInformation()
  {
    // We don't lock since we don't need a top accurate value
    int size = vdb.getBackends().size();

    if (size == 0)
      return "ParallelDB Least Pending Request First Request load balancer: !!!Warning!!! No backend nodes found\n";
    else
      return "ParallelDB Least Pending Request First Request load balancer ("
          + size + " backends)\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.paralleldb.ParallelDB#getParallelDBXml()
   */
  public String getParallelDBXml()
  {
    return "<" + DatabasesXmlTags.ELT_ParallelDB_LeastPendingRequestsFirst
        + "/>";
  }
}