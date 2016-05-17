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

package org.objectweb.cjdbc.common.jmx.mbeans;

import java.sql.SQLException;

/**
 * This class defines a AbstractLoadBalancerMBean
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public interface AbstractLoadBalancerMBean
{
  /**
   * Return the load balancer RAIDb level
   * 
   * @return the RAIDb level
   */
  int getRAIDbLevel();

  /**
   * Sets the RAIDbLevel.
   * 
   * @param raidbLevel The RAIDb level to set
   */
  void setRAIDbLevel(int raidbLevel);

  /**
   * Get the needed query parsing granularity.
   * 
   * @return needed query parsing granularity
   */
  int getParsingGranularity();

  /**
   * Set the needed query parsing granularity.
   * 
   * @param parsingGranularity the granularity to set
   */
  void setParsingGranularity(int parsingGranularity);

  /**
   * Associate a weight to a backend identified by its logical name.
   * 
   * @param name the backend name
   * @param w the weight
   * @throws SQLException if an error occurs
   */
  void setWeight(String name, int w) throws SQLException;

  /**
   * Return generic information about the load balancer.
   * 
   * @return load balancer information
   */
  String getInformation();
}