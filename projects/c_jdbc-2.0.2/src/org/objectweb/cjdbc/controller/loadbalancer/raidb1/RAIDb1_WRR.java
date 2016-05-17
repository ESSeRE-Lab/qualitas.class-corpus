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

package org.objectweb.cjdbc.controller.loadbalancer.raidb1;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.loadbalancer.WeightedBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * RAIDb-1 Weighted Round Robin load balancer
 * <p>
 * The read requests coming from the request manager are sent to the backend
 * nodes using a weighted round robin. Write requests are broadcasted to all
 * backends.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class RAIDb1_WRR extends RAIDb1
{
  /*
   * How the code is organized ? 1. Member variables 2. Constructor(s) 3.
   * Request handling 4. Debug/Monitoring
   */

  private HashMap weights = new HashMap();
  private int     index;                  // index in the backend
                                                        // vector the

  // Round-Robin

  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-1 Weighted Round Robin request load balancer.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @param waitForCompletionPolicy How many backends must complete before
   *                    returning the result?
   * @throws Exception if an error occurs
   */
  public RAIDb1_WRR(VirtualDatabase vdb,
      WaitForCompletionPolicy waitForCompletionPolicy) throws Exception
  {
    super(vdb, waitForCompletionPolicy);
    index = -1;
  }

  /*
   * Request Handling
   */

  /**
   * Selects the backend using a weighted round-robin algorithm and executes the
   * read request.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb1.RAIDb1#execReadRequest(SelectRequest,
   *             MetadataCache)
   */
  public ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException
  {
    return executeWRR(request, true, "Request ", metadataCache);
  }

  /**
   * Selects the backend using a least pending request first policy. The backend
   * that has the shortest queue of currently executing queries is chosen to
   * execute this stored procedure.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadOnlyReadStoredProcedure(StoredProcedure,
   *             MetadataCache)
   */
  public ControllerResultSet execReadOnlyReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache) throws SQLException
  {
    return executeWRR(proc, false, "Stored procedure ", metadataCache);
  }

  /**
   * Common code to execute a SelectRequest or a StoredProcedure on a backend
   * chosen using a weighted round-robin algorithm.
   * 
   * @param request a <code>SelectRequest</code> or
   *                    <code>StoredProcedure</code>
   * @param isSelect true if it is a <code>SelectRequest</code>, false if it
   *                    is a <code>StoredProcedure</code>
   * @param errorMsgPrefix the error message prefix, usually "Request " or
   *                    "Stored procedure " ... failed because ...
   * @param metadataCache a metadataCache if any or null
   * @return a <code>ResultSet</code>
   * @throws SQLException if an error occurs
   */
  private ControllerResultSet executeWRR(AbstractRequest request,
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

    DatabaseBackend backend = null;

    // Note that vdb lock is released in the finally clause of this try/catch
    // block
    try
    {
      ArrayList backends = vdb.getBackends();
      int size = backends.size();

      if (size == 0)
        throw new SQLException(Translate.get(
            "loadbalancer.execute.no.backend.available", request.getId()));

      // Choose the backend (WRR algorithm starts here)
      int w = 0; // cumulative weight
      for (int i = 0; i < size; i++)
      {
        DatabaseBackend b = (DatabaseBackend) backends.get(index);
        if (b.isReadEnabled())
        {
          if (backend == null)
            backend = b; // Fallback if no backend found

          // Add the weight of this backend
          Integer weight = (Integer) weights.get(b.getName());
          if (weight == null)
            logger.error("No weight defined for backend " + b.getName());
          else
            w += weight.intValue();

          // Ok we reached the needed weight, take this backend
          if (index <= w)
          {
            backend = b;
            index++; // Next time take the next
            break;
          }
        }
      }

      if (backend == null)
        throw new NoMoreBackendException(Translate.get(
            "loadbalancer.execute.no.backend.enabled", request.getId()));

      // We are over the total weight and we are using the
      // first available node. Let's reset the index to 1
      // since we used this first node (0++).
      if (index > w)
        index = 1;
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
      return executeWRR(request, isSelect, errorMsgPrefix, metadataCache);
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
   * Backends management
   */

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#setWeight(String,
   *             int)
   */
  public void setWeight(String name, int w) throws SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("loadbalancer.weight.set", new String[]{
          String.valueOf(w), name}));

    weights.put(name, new Integer(w));
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
      return "RAIDb-1 with Weighted Round Robin Request load balancer: !!!Warning!!! No backend nodes found\n";
    else
      return "RAIDb-1 Weighted Round-Robin Request load balancer (" + size
          + " backends)\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb1.RAIDb1#getRaidb1Xml
   */
  public String getRaidb1Xml()
  {
    return WeightedBalancer.getRaidbXml(weights,
        DatabasesXmlTags.ELT_RAIDb_1_WeightedRoundRobin);
  }

}
