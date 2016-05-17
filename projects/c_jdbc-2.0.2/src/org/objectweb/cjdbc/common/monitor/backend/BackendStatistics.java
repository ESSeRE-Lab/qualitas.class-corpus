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

package org.objectweb.cjdbc.common.monitor.backend;

import java.io.Serializable;

/**
 * This class defines a BackendStatistics
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 */
public class BackendStatistics implements Serializable
{

  /**
   * Name of the backend
   */
  private String  backendName;

  /**
   * Class name of the driver for the backend
   */
  private String  driverClassName;
  /**
   * URL of the backend
   */
  private String  url;
  /**
   * Is the backend enable for read?
   */
  private boolean readEnabled;
  /**
   * Is the backend enable for write?
   */
  private boolean writeEnabled;
  /**
   * the status of the initialization
   */
  private String  initializationStatus;
  /**
   * Is the schema of the backend static?
   */
  private boolean schemaStatic;
  /**
   * number of total active connections
   */
  private long    numberOfTotalActiveConnections;
  /**
   * Last known checkpoint of the backend
   */
  private String  lastKnownCheckpoint;
  /**
   * Number of active transactions
   */
  private int     numberOfActiveTransactions;
  /**
   * Number of pending requests
   */
  private int     numberOfPendingRequests;
  /**
   * Number of connection managers
   */
  private int     numberOfConnectionManagers;
  /**
   * Number of total requests
   */
  private int     numberOfTotalRequests;
  /**
   * Number of total transactions
   */
  private int     numberOfTotalTransactions;

  public String getBackendName()
  {
    return backendName;
  }

  public void setBackendName(String backendName)
  {
    this.backendName = backendName;
  }

  public String getDriverClassName()
  {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName)
  {
    this.driverClassName = driverClassName;
  }

  public String getInitializationStatus()
  {
    return initializationStatus;
  }

  public void setInitializationStatus(String initializationStatus)
  {
    this.initializationStatus = initializationStatus;
  }

  public String getLastKnownCheckpoint()
  {
    return lastKnownCheckpoint;
  }

  public void setLastKnownCheckpoint(String lastKnownCheckpoint)
  {
    this.lastKnownCheckpoint = lastKnownCheckpoint;
  }

  public int getNumberOfActiveTransactions()
  {
    return numberOfActiveTransactions;
  }

  public void setNumberOfActiveTransactions(int numberOfActiveTransactions)
  {
    this.numberOfActiveTransactions = numberOfActiveTransactions;
  }

  public int getNumberOfConnectionManagers()
  {
    return numberOfConnectionManagers;
  }

  public void setNumberOfConnectionManagers(int numberOfConnectionManagers)
  {
    this.numberOfConnectionManagers = numberOfConnectionManagers;
  }

  public int getNumberOfPendingRequests()
  {
    return numberOfPendingRequests;
  }

  public void setNumberOfPendingRequests(int numberOfPendingRequests)
  {
    this.numberOfPendingRequests = numberOfPendingRequests;
  }

  public long getNumberOfTotalActiveConnections()
  {
    return numberOfTotalActiveConnections;
  }

  public void setNumberOfTotalActiveConnections(
      long numberOfTotalActiveConnections)
  {
    this.numberOfTotalActiveConnections = numberOfTotalActiveConnections;
  }

  public int getNumberOfTotalRequests()
  {
    return numberOfTotalRequests;
  }

  public void setNumberOfTotalRequests(int numberOfTotalRequests)
  {
    this.numberOfTotalRequests = numberOfTotalRequests;
  }

  public int getNumberOfTotalTransactions()
  {
    return numberOfTotalTransactions;
  }

  public void setNumberOfTotalTransactions(int numberOfTotalTransactions)
  {
    this.numberOfTotalTransactions = numberOfTotalTransactions;
  }

  public boolean isReadEnabled()
  {
    return readEnabled;
  }

  public void setReadEnabled(boolean readEnabled)
  {
    this.readEnabled = readEnabled;
  }

  public boolean isSchemaStatic()
  {
    return schemaStatic;
  }

  public void setSchemaStatic(boolean schemaStatic)
  {
    this.schemaStatic = schemaStatic;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public boolean isWriteEnabled()
  {
    return writeEnabled;
  }

  public void setWriteEnabled(boolean writeEnabled)
  {
    this.writeEnabled = writeEnabled;
  }

}
