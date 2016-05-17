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

package org.objectweb.cjdbc.controller.loadbalancer.raidb2;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTablePolicy;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * RAIDb-2 Round Robin load balancer featuring (Least Pending Requests First
 * load balancing algorithm).
 * <p>
 * The read requests coming from the request manager are sent to the node that
 * has the Least pending read requests among the nodes that can execute the
 * request.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class RAIDb2_LPRF extends RAIDb2
{

  /*
   * How the code is organized ? 1. Member variables 2. Constructor(s) 3.
   * Request handling 4. Debug/Monitoring
   */

  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-2 Round Robin request load balancer.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @param waitForCompletionPolicy how many backends must complete before
   *          returning the result?
   * @param createTablePolicy the policy defining how 'create table' statements
   *          should be handled
   * @exception Exception if an error occurs
   */
  public RAIDb2_LPRF(VirtualDatabase vdb,
      WaitForCompletionPolicy waitForCompletionPolicy,
      CreateTablePolicy createTablePolicy) throws Exception
  {
    super(vdb, waitForCompletionPolicy, createTablePolicy);
  }

  /*
   * Request Handling
   */

  /**
   * Chooses the node to execute the request using a round-robin algorithm. If
   * the next node has not the tables needed to execute the requests, we try the
   * next one and so on until a suitable backend is found.
   * 
   * @param request an <code>SelectRequest</code>
   * @param metadataCache cached metadata to use to construct the result set
   * @return the corresponding <code>java.sql.ResultSet</code>
   * @exception SQLException if an error occurs
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb2.RAIDb2#execReadRequest(SelectRequest,
   *      MetadataCache)
   */
  public ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException
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
      int enabledBackends = 0;
      ArrayList tables = request.getFrom();

      for (int i = 0; i < size; i++)
      {
        DatabaseBackend b = (DatabaseBackend) backends.get(i);
        if (b.isReadEnabled())
        {
          enabledBackends++;
          if (b.hasTables(tables))
          {
            int pending = b.getPendingRequests().size();
            if (((backend == null) || (pending < leastRequests)))
            {
              backend = b;
              if (pending == 0)
                break; // Stop here we will never find a less loaded node
              else
                leastRequests = pending;
            }
          }
        }
      }

      if (backend == null)
      {
        if (enabledBackends == 0)
          throw new NoMoreBackendException(Translate.get(
              "loadbalancer.execute.no.backend.enabled", request.getId()));
        else
          throw new SQLException(Translate.get(
              "loadbalancer.backend.no.required.tables", tables.toString()));
      }

    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.request.failed.on.backend",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      vdb.releaseReadLockBackendLists();
    }

    // Execute the request on the chosen backend
    ControllerResultSet rs = null;
    try
    {
      rs = executeRequestOnBackend(request, backend, metadataCache);
    }
    catch (UnreachableBackendException se)
    {
      // Try on another backend
      return execReadRequest(request, metadataCache);
    }
    catch (SQLException se)
    {
      String msg = Translate.get("loadbalancer.request.failed", new String[]{
          String.valueOf(request.getId()), se.getMessage()});
      if (logger.isInfoEnabled())
        logger.info(msg);
      throw new SQLException(msg);
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.request.failed.on.backend",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    return rs;
  }

  /**
   * Chooses the node to execute the stored procedure using a LPRF algorithm. If
   * the next node has not the needed stored procedure, we try the next one and
   * so on until a suitable backend is found.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadOnlyReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadOnlyReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache) throws SQLException
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
      DatabaseBackend failedBackend = null;
      SQLException failedException = null;
      ControllerResultSet rs = null;
      do
      {
        ArrayList backends = vdb.getBackends();
        int size = backends.size();

        if (size == 0)
          throw new SQLException(Translate.get(
              "loadbalancer.execute.no.backend.available", proc.getId()));

        // Choose the backend that has the least pending requests
        int leastRequests = 0;
        int enabledBackends = 0;

        for (int i = 0; i < size; i++)
        {
          DatabaseBackend b = (DatabaseBackend) backends.get(i);
          if (b.isReadEnabled())
          {
            enabledBackends++;
            if (b.hasStoredProcedure(proc.getProcedureName()))
            {
              int pending = b.getPendingRequests().size();
              if ((b != failedBackend)
                  && ((backend == null) || (pending < leastRequests)))
              {
                backend = b;
                if (pending == 0)
                  break; // Stop here we will never find a less loaded node
                else
                  leastRequests = pending;
              }
            }
          }
        }

        if (backend == null)
        {
          if (enabledBackends == 0)
            throw new SQLException(Translate
                .get("loadbalancer.storedprocedure.backend.no.enabled", proc
                    .getId()));
          else if (failedBackend == null)
            throw new SQLException(Translate.get(
                "loadbalancer.backend.no.required.storedprocedure", proc
                    .getProcedureName()));
          else
            // Bad query, the only backend that could execute it has failed
            throw failedException;
        }

        // Execute the request on the chosen backend
        boolean toDisable = false;
        try
        {
          rs = executeStoredProcedureOnBackend(proc, backend, metadataCache);
          if (failedBackend != null)
          { // Previous backend failed
            if (logger.isWarnEnabled())
              logger.warn(Translate.get("loadbalancer.storedprocedure.status",
                  new String[]{String.valueOf(proc.getId()), backend.getName(),
                      failedBackend.getName()}));
            toDisable = true;
          }
        }
        catch (UnreachableBackendException se)
        {
          // Retry on an other backend.
          continue;
        }
        catch (SQLException se)
        {
          if (failedBackend != null)
          { // Bad query, no backend can execute it
            String msg = Translate.get(
                "loadbalancer.storedprocedure.failed.twice", new String[]{
                    String.valueOf(proc.getId()), se.getMessage()});
            if (logger.isInfoEnabled())
              logger.info(msg);
            throw new SQLException(msg);
          }
          else
          { // We are the first to fail on this query
            failedBackend = backend;
            failedException = se;
            if (logger.isInfoEnabled())
              logger.info(Translate.get(
                  "loadbalancer.storedprocedure.failed.on.backend",
                  new String[]{
                      proc.getSQLShortForm(vdb.getSQLShortFormLength()),
                      backend.getName(), se.getMessage()}));
            continue;
          }
        }

        if (toDisable)
        { // retry has succeeded and we need to disable the first node that
          // failed
          try
          {
            if (logger.isWarnEnabled())
              logger.warn(Translate.get("loadbalancer.backend.disabling",
                  failedBackend.getName()));
            disableBackend(failedBackend);
          }
          catch (SQLException ignore)
          {
          }
          finally
          {
            failedBackend = null; // to exit the do{}while
          }
        }
      }
      while (failedBackend != null);
      return rs;
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get(
          "loadbalancer.storedprocedure.failed.on.backend", new String[]{
              proc.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      vdb.releaseReadLockBackendLists();
    }
  }

  /*
   * Debug/Monitoring
   */

  /**
   * Gets information about the request load balancer.
   * 
   * @return <code>String</code> containing information
   */
  public String getInformation()
  {
    // We don't lock since we don't need a completely accurate value
    int size = vdb.getBackends().size();

    if (size == 0)
      return "RAIDb-2 Least Pending Requests First load balancer: !!!Warning!!! No backend nodes found\n";
    else
      return "RAIDb-2 Least Pending Requests First load balancer (" + size
          + " backends)\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb2.RAIDb2#getRaidb2Xml
   */
  public String getRaidb2Xml()
  {
    return "<" + DatabasesXmlTags.ELT_RAIDb_2_LeastPendingRequestsFirst + "/>";
  }
}