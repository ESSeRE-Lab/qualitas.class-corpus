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

import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.loadbalancer.policies.errorchecking.ErrorCheckingPolicy;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * RAIDb-1 Round Robin load balancer with error checking.
 * <p>
 * This load balancer tolerates byzantine failures of databases. The read
 * requests coming from the Request Manager are sent to multiple backend nodes
 * and the results are compared. Write requests are broadcasted to all backends.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @version 1.0
 */
public class RAIDb1ec_RR extends RAIDb1ec
{
  /*
   * How the code is organized ? 1. Member variables 2. Constructor(s) 3.
   * Request handling 4. Debug/Monitoring
   */

  //  private int index; // index in the backend vector the Round-Robin
  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-1 Round Robin with error checking request load
   * balancer.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @param waitForCompletionPolicy how many backends must complete before
   *          returning the result?
   * @param errorCheckingPolicy policy to apply for error checking.
   * @param nbOfConcurrentReads Number of concurrent reads allowed
   * @exception Exception if an error occurs
   */
  public RAIDb1ec_RR(VirtualDatabase vdb,
      WaitForCompletionPolicy waitForCompletionPolicy,
      ErrorCheckingPolicy errorCheckingPolicy, int nbOfConcurrentReads)
      throws Exception
  {
    super(vdb, waitForCompletionPolicy, errorCheckingPolicy,
        nbOfConcurrentReads);
    // index = -1;
  }

  /*
   * Request Handling
   */

  /**
   * Not implemented.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb1.RAIDb1#execReadRequest(SelectRequest, MetadataCache)
   */
  public ControllerResultSet execReadRequest(SelectRequest request, MetadataCache metadataCache)
      throws SQLException
  {
    throw new NotImplementedException(this.getClass().getName()
        + ":execReadRequest");
  }

  /**
   * Not implemented.
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadOnlyReadStoredProcedure(StoredProcedure, MetadataCache)
   */
  public ControllerResultSet execReadOnlyReadStoredProcedure(StoredProcedure proc, MetadataCache metadataCache)
      throws SQLException
  {
    throw new NotImplementedException(this.getClass().getName()
        + ":execReadStoredProcedure");
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
      return "RAIDb-1 Error Checking with Round-Robin Request load balancer: !!!Warning!!! No backend nodes found\n";
    else
      return "RAIDb-1 Error Checking with Round-Robin Request load balancer ("
          + size + " backends)\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.raidb1.RAIDb1#getRaidb1Xml
   */
  public String getRaidb1Xml()
  {
    return "<" + DatabasesXmlTags.ELT_RAIDb_1ec_RoundRobin + "/>";
  }

}