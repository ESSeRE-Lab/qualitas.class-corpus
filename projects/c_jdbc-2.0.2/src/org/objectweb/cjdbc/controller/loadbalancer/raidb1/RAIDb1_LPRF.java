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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): _______________________
 */

package org.objectweb.cjdbc.controller.loadbalancer.raidb1;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * RAIDb-1 Round Robin load balancer featuring (Least Pending Requests First
 * load balancing algorithm).
 * <p>
 * The read requests coming from the Request Manager are sent to the node that
 * has the least pending read requests among the nodes that can execute the
 * request. Write requests are broadcasted to all backends.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class RAIDb1_LPRF extends RAIDb1
{
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Request handling
  // 4. Debug/Monitoring

  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-1 Round Robin request load balancer.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @param waitForCompletionPolicy how many backends must complete before
   *          returning the result?
   * @throws Exception if an error occurs
   */
  public RAIDb1_LPRF(VirtualDatabase vdb,
      WaitForCompletionPolicy waitForCompletionPolicy) throws Exception
  {
    super(vdb, waitForCompletionPolicy);
  }

  /*
   * Request Handling
   */

  /**
   * Selects the backend using a least pending request first policy. The backend
   * that has the shortest queue of currently executing queries is chosen to
   * execute this query.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb1.RAIDb1#execReadRequest(SelectRequest,
   *      MetadataCache)
   */
  public ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException
  {
    return executeLPRF(request, true, "Request ", metadataCache);
  }

  /**
   * Selects the backend using a least pending request first policy. The backend
   * that has the shortest queue of currently executing queries is chosen to
   * execute this stored procedure.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadOnlyReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadOnlyReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache) throws SQLException
  {
    return executeLPRF(proc, false, "Stored procedure ", metadataCache);
  }

  /**
   * Common code to execute a SelectRequest or a StoredProcedure on a backend
   * chosen using a LPRF algorithm.
   * 
   * @param request a <code>SelectRequest</code> or
   *          <code>StoredProcedure</code>
   * @param isSelect true if it is a <code>SelectRequest</code>, false if it
   *          is a <code>StoredProcedure</code>
   * @param errorMsgPrefix the error message prefix, usually "Request " or
   *          "Stored procedure " ... failed because ...
   * @param metadataCache the metadataCache if any or null
   * @return a <code>ResultSet</code>
   * @throws SQLException if an error occurs
   */
  private ControllerResultSet executeLPRF(AbstractRequest request,
      boolean isSelect, String errorMsgPrefix, MetadataCache metadataCache)
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

    DatabaseBackend backend = null; // The
    // backend
    // that
    // will
    // execute
    // the
    // query

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
        throw new NoMoreBackendException(Translate.get(
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

    ControllerResultSet rs = null;
    // Execute the request on the chosen backend
    try
    {
      if (isSelect)
        rs = executeRequestOnBackend((SelectRequest) request, backend,
            metadataCache);
      else
        rs = executeStoredProcedureOnBackend((StoredProcedure) request,
            backend, metadataCache);
    }
    catch (UnreachableBackendException urbe)
    {
      // Try to execute query on different backend
      return executeLPRF(request, isSelect, errorMsgPrefix, metadataCache);
    }
    catch (SQLException se)
    {
      String msg = Translate.get("loadbalancer.something.failed", new String[]{
          errorMsgPrefix, String.valueOf(request.getId()), se.getMessage()});
      if (logger.isInfoEnabled())
        logger.info(msg);
      throw se;
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.something.failed.on",
          new String[]{errorMsgPrefix,
              request.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    return rs;
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
    // We don't lock since we don't need a top accurate value
    int size = vdb.getBackends().size();

    if (size == 0)
      return "RAIDb-1 Least Pending Request First load balancer: !!!Warning!!! No backend nodes found\n";
    else
      return "RAIDb-1 Least Pending Request First load balancer (" + size
          + " backends)\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb1.RAIDb1#getRaidb1Xml
   */
  public String getRaidb1Xml()
  {
    return "<" + DatabasesXmlTags.ELT_RAIDb_1_LeastPendingRequestsFirst + "/>";
  }

}
