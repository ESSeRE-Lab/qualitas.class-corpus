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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): 
 */

package org.objectweb.cjdbc.common.jmx.mbeans;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;

/**
 * MBeanInterface to the DatabaseBackend
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public interface DatabaseBackendMBean
{
  /**
   * Returns <code>true</code> if this backend has the given list of tables in
   * its schema. The caller must ensure that the database schema has been
   * defined
   * 
   * @param tables the list of table names (<code>ArrayList</code> of
   *                    <code>String</code>) to look for
   * @return <code>true</code> if all the tables are found
   */
  boolean hasTables(ArrayList tables);

  /**
   * Returns <code>true</code> if this backend has the given table in its
   * schema. The caller must ensure that the database schema has been defined,
   * 
   * @param table The table name to look for
   * @return <code>true</code> if tables is found in the schema
   */
  boolean hasTable(String table);

  /**
   * Returns <code>true</code> if this backend has the given stored procedure
   * in its schema. The caller must ensure that the database schema has been
   * defined
   * 
   * @param procedureName The stored procedure name to look for
   * @return <code>true</code> if procedure name is found in the schema
   */
  boolean hasStoredProcedure(String procedureName);

  /**
   * Tests if this backend is enabled (active and synchronized).
   * 
   * @return <code>true</code> if this backend is enabled
   * @throws Exception if an error occurs
   */
  boolean isInitialized() throws Exception;

  /**
   * Tests if this backend is read enabled (active and synchronized).
   * 
   * @return <code>true</code> if this backend is enabled.
   */
  boolean isReadEnabled();

  /**
   * Tests if this backend is write enabled (active and synchronized).
   * 
   * @return <code>true</code> if this backend is enabled.
   */
  boolean isWriteEnabled();

  /**
   * Is the backend completely disabled ? This usually means it has a known
   * state with a checkpoint associated to it.
   * 
   * @return <code>true</code> if the backend is disabled
   */
  boolean isDisabled();

  /**
   * Enables the database backend for reads. This method should only be called
   * when the backend is synchronized with the others.
   */
  void enableRead();

  /**
   * Enables the database backend for writes. This method should only be called
   * when the backend is synchronized with the others.
   */
  void enableWrite();

  /**
   * Disables the database backend for reads. This does not affect write ability
   */
  void disableRead();

  /**
   * Disables the database backend for writes. This does not affect read ability
   * although the backend will not be coherent anymore as soon as a write as
   * occured. This should be used in conjunction with a checkpoint to recover
   * missing writes.
   */
  void disableWrite();

  /**
   * Sets the database backend state to disable. This state is just an
   * indication and it has no semantic effect. It is up to the request manager
   * (especially the load balancer) to ensure that no more requests are sent to
   * this backend.
   */
  void disable();

  /**
   * Returns the SQL statement to use to check the connection validity.
   * 
   * @return a <code>String</code> containing a SQL statement
   */
  String getConnectionTestStatement();

  /**
   * Returns the database native JDBC driver class name.
   * 
   * @return the driver class name
   */
  String getDriverClassName();

  /**
   * Returns the backend logical name.
   * 
   * @return the backend logical name
   */
  String getName();

  /**
   * Returns a description of the state of the backend
   * 
   * @see org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList
   * @return a string description of the state. Can be enabled, disabled,
   *                 recovering, backuping ...
   */
  String getState();

  /**
   * Returns the list of pending requests for this backend.
   * 
   * @param count number of requests to retrieve, if 0, return all.
   * @param fromFirst count the request from first if true, or from last if false
   * @param clone should clone the pending request if true, block it if false 
   * @return <code>ArrayList</code> of <code>String</code> description of
   *                 each request.
   */
  ArrayList getPendingRequestsDescription(int count,boolean fromFirst,boolean clone);

  /**
   * Returns the list of active transactions for this backend.
   * 
   * @return <code>ArrayList</code> of <code>Long</code>, corresponding to
   *                 active transaction identifier.
   */
  ArrayList getActiveTransactions();

  /**
   * Checks that the current database schema is compatible with all schema
   * gathered from each connection manager.
   * <p>
   * If no schema has been defined, the first gathered schema is used as the
   * current database schema.
   * <p>
   * For each schema that is not compatible with the current schema, a warning
   * is issued on the logger for that backend
   * 
   * @return true if comptaible, false otherwise
   */
  boolean checkDatabaseSchema();

  /**
   * Returns the schema of this database.
   * 
   * @return the schema of this database. Returns <code>null</code> if the
   *                 schema has not been set.
   */
  DatabaseSchema getDatabaseSchema();

  /**
   * Check if the driver used by this backend is compliant with C-JDBC needs.
   * 
   * @throws Exception if the driver is not compliant
   */
  void checkDriverCompliance() throws Exception;

  /**
   * Returns the JDBC URL used to access the database.
   * 
   * @return a JDBC URL
   */
  String getURL();

  /**
   * @return Returns the schemaIsStatic.
   */
  boolean isSchemaStatic();

  /**
   * Returns the driver path.
   * 
   * @return the driver path
   */
  String getDriverPath();

  /**
   * setLastKnownCheckpoint for this backend
   * 
   * @param checkpoint the checkpoint
   */
  void setLastKnownCheckpoint(String checkpoint);

  /**
   * Returns the lastKnownCheckpoint value.
   * 
   * @return Returns the lastKnownCheckpoint.
   */
  String getLastKnownCheckpoint();

  /**
   * Is the backend accessible ?
   * 
   * @return <tt>true</tt> if a jdbc connection is still possible from the
   *                 controller, <tt>false</tt> if connectionTestStatement failed
   */
  boolean isJDBCConnected();

  /**
   * The getXml() method does not return the schema if it is not static anymore,
   * to avoid confusion between static and dynamic schema. This method returns a
   * static view of the schema, whatever the dynamic precision is.
   * 
   * @param expandSchema if we should force the schema to be expanded. This is
   *                    needed as the default getXml should call this method.
   * @return an xml formatted string
   */
  String getSchemaXml(boolean expandSchema);

  /**
   * Return a string description of the backend in xml format. This does not
   * include the schema description if the dynamic precision is not set to
   * static.
   * 
   * @return an xml formatted string
   */
  String getXml();
}