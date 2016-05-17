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
 * Contributor(s): 
 */

package org.objectweb.cjdbc.common.jmx.mbeans;

import org.objectweb.cjdbc.common.exceptions.DataCollectorException;
import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;

/**
 * DataCollector interface to used via JMX. This interface defines the entry
 * point to collect dynamic data for all c-jdbc components.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public interface DataCollectorMBean
{

  // ****************************************//
  // *************** Controller Data ********//
  // ****************************************//

  /**
   * Get general information on the load of the controller. Get the number of
   * threads and total memory used up
   * 
   * @throws DataCollectorException if collection of information fails
   * @return array of strings
   */
  String[][] retrieveControllerLoadData() throws DataCollectorException;

  /**
   * Get dynamic data of the different virtual databases, like pending
   * connections size, currentNb of threads and number of active threads.
   * 
   * @throws DataCollectorException if collection of information fails
   * @return array of strings
   */
  String[][] retrieveVirtualDatabasesData() throws DataCollectorException;

  /**
   * Try to see if a virtual database exists from its name
   * 
   * @param name of the virtual database
   * @return true if exists, false otherwise
   */
  boolean hasVirtualDatabase(String name);

  // ****************************************//
  // *************** Database Data **********//
  // ****************************************//

  /**
   * Get the current SQL statistics for all databases
   * 
   * @throws DataCollectorException if collection of information fails
   * @return the statistics
   */
  String[][] retrieveSQLStats() throws DataCollectorException;

  /**
   * Get the current cache content for all databases
   * 
   * @throws DataCollectorException if collection of information fails
   * @return the cache content
   */
  String[][] retrieveCacheData() throws DataCollectorException;

  /**
   * Get the current cache stats content for all databases
   * 
   * @throws DataCollectorException if collection of information fails
   * @return the cache stats content
   */
  String[][] retrieveCacheStatsData() throws DataCollectorException;

  /**
   * Get the current list of backends data for all databases
   * 
   * @throws DataCollectorException if collection of information fails
   * @return the backend list content
   */
  String[][] retrieveBackendsData() throws DataCollectorException;

  /**
   * Get the current list of current users and associated data for all databases
   * 
   * @throws DataCollectorException if collection of information fails
   * @return data on users
   */
  String[][] retrieveClientsData() throws DataCollectorException;

  /**
   * Get the current SQL statistics
   * 
   * @param virtualDatabasename of the database to get the data from
   * @return the statistics
   * @throws DataCollectorException if collection of information fails
   */
  String[][] retrieveSQLStats(String virtualDatabasename)
      throws DataCollectorException;

  /**
   * Get the current cache content
   * 
   * @param virtualDatabasename of the database to get the data from
   * @return the cache content
   * @throws DataCollectorException if collection of information fails
   */
  String[][] retrieveCacheData(String virtualDatabasename)
      throws DataCollectorException;

  /**
   * Get the current cache stats content
   * 
   * @param virtualDatabasename of the database to get the data from
   * @return the cache stats content
   * @throws DataCollectorException if collection of information fails
   */
  String[][] retrieveCacheStatsData(String virtualDatabasename)
      throws DataCollectorException;

  /**
   * Get the current list of backends data
   * 
   * @param virtualDatabasename of the database to get the data from
   * @return the backend list content
   * @throws DataCollectorException if collection of information fails
   */
  String[][] retrieveBackendsData(String virtualDatabasename)
      throws DataCollectorException;

  /**
   * Retrive information about the scheduler, like number of pending requests,
   * number of writes executed and number of read executed
   * 
   * @param virtualDatabasename of the database to get the data from
   * @return data on the associated scheduler
   * @throws DataCollectorException if collection of data fails
   */
  String[][] retrieveSchedulerData(String virtualDatabasename)
      throws DataCollectorException;

  /**
   * Get the current list of current users and associated data
   * 
   * @param virtualDatabasename of the database to get the data from
   * @return data on users
   * @throws DataCollectorException if collection of information fails
   */
  String[][] retrieveClientsData(String virtualDatabasename)
      throws DataCollectorException;

  // ****************************************//
  // *************** Fine grain Data ********//
  // ****************************************//
  /**
   * Get some data information on a fine grain approach
   * 
   * @param collector for the data to be accessed
   * @return <code>long</code> value of the data
   * @throws DataCollectorException if collection of information fails
   */
  long retrieveData(AbstractDataCollector collector)
      throws DataCollectorException;

  /**
   * Get starting point for exchanging data on a particular target
   * 
   * @param dataType as given in the DataCollection interface
   * @param targetName if needed (like backendname,clientName ...)
   * @param virtualDbName if needed
   * @return collector instance
   * @throws DataCollectorException if fails to get proper collector instance
   * @see org.objectweb.cjdbc.common.monitor.DataCollection
   */
  AbstractDataCollector retrieveDataCollectorInstance(int dataType,
      String targetName, String virtualDbName) throws DataCollectorException;

  /**
   * Gets content data of the recovery log
   * 
   * @param databaseName the virtual database name
   * @return data on the recovery log
   * @throws DataCollectorException if collection of information fails
   */
  String[][] retrieveRecoveryLogData(String databaseName) throws DataCollectorException;

}