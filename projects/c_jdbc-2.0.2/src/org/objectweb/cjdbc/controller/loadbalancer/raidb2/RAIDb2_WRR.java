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

package org.objectweb.cjdbc.controller.loadbalancer.raidb2;

import java.sql.SQLException;
import java.util.HashMap;

import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.loadbalancer.WeightedBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTablePolicy;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * RAIDb-2 Weighted Round Robin load balancer.
 * <p>
 * The read requests coming from the request manager are sent to the backend
 * nodes using a weighted round robin. Write requests are broadcasted to all
 * backends.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @version 1.0
 */
public class RAIDb2_WRR extends RAIDb2
{
  /*
   * How the code is organized ? 1. Member variables 2. Constructor(s) 3.
   * Request handling 4. Debug/Monitoring
   */

  private HashMap weights;

  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-2 Weighted Round Robin request load balancer.
   * 
   * @param vdb The virtual database this load balancer belongs to.
   * @param waitForCompletionPolicy How many backends must complete before
   *          returning the result?
   * @param createTablePolicy The policy defining how 'create table' statements
   *          should be handled
   * @exception Exception if an error occurs
   */
  public RAIDb2_WRR(VirtualDatabase vdb,
      WaitForCompletionPolicy waitForCompletionPolicy,
      CreateTablePolicy createTablePolicy) throws Exception
  {
    super(vdb, waitForCompletionPolicy, createTablePolicy);
  }

  /*
   * Request Handling
   */

  /**
   * Performs a read request. It is up to the implementation to choose to which
   * backend node(s) this request should be sent.
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
    throw new NotImplementedException(this.getClass().getName()
        + ":execReadRequest");
  }

  /**
   * Chooses the node to execute the stored procedure using a round-robin
   * algorithm. If the next node has not the needed stored procedure, we try the
   * next one and so on until a suitable backend is found.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadOnlyReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadOnlyReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache) throws SQLException
  {
    throw new NotImplementedException(this.getClass().getName()
        + ":execReadStoredProcedure");
  }

  /*
   * Backends management
   */

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#setWeight(String,
   *      int)
   */
  public void setWeight(String name, int w) throws SQLException
  {
    throw new SQLException("Weight is not supported with this load balancer");
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
    if (weights == null)
      return "RAIDb-2 Weighted Round Robin Request load balancer: !!!Warning!!! No backend nodes found\n";
    else
      return "RAIDb-2 Weighted Round Robin Request load balancer balancing over "
          + weights.size() + " nodes\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb2.RAIDb2#getRaidb2Xml
   */
  public String getRaidb2Xml()
  {
    return WeightedBalancer.getRaidbXml(weights,
        DatabasesXmlTags.ELT_RAIDb_2_WeightedRoundRobin);
  }
}