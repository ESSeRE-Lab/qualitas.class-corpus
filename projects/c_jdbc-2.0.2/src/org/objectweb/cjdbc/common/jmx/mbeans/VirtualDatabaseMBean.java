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
 * Contributor(s): Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.common.jmx.mbeans;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import org.objectweb.cjdbc.common.exceptions.VirtualDatabaseException;
import org.objectweb.cjdbc.common.monitor.backend.BackendStatistics;
import org.objectweb.cjdbc.common.shared.DumpInfo;

/**
 * JMX Interface to remotely manage a Virtual Databases.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <A href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public interface VirtualDatabaseMBean
{
  //
  // Methods are organized as follows:
  // 1. Database backends management
  // 2. Checkpoint management
  // 3. Backup management
  // 4. Virtual database management
  //  

  //
  // Database backends management
  //

  /**
   * Enables a backend that has been previously added to this virtual database
   * and that is in the disabled state. The backend is enabled without further
   * check.
   * 
   * @param databaseBackendName The database backend logical name to enable
   * @exception VirtualDatabaseException in case of communication-related error
   */
  void forceEnableBackend(String databaseBackendName)
      throws VirtualDatabaseException;

  /**
   * Enable the given backend from its last known checkpoint
   * 
   * @param backendName the name of the backend to enable
   * @throws VirtualDatabaseException if enable failed, or if there is no last
   *           known checkpoint
   */
  void enableBackendFromCheckpoint(String backendName)
      throws VirtualDatabaseException;

  /**
   * Enable all the backends without any check.
   * 
   * @throws VirtualDatabaseException if fails
   */
  void enableAllBackends() throws VirtualDatabaseException;

  // TODO: Move implem to console

  /**
   * Enable all backends from their last known states that has been recorded in
   * the recovery log, and enable only the backends which where properly
   * disabled.
   * 
   * @throws VirtualDatabaseException if fails
   */
  void enableAllBackendsFromCheckpoint() throws VirtualDatabaseException;

  // TODO: Move implem to console

  /**
   * Disables a backend that is currently enabled on this virtual database
   * (without further check).
   * 
   * @param databaseBackendName The database backend logical name to enable
   * @exception VirtualDatabaseException in case of communication-related error
   */
  void forceDisableBackend(String databaseBackendName)
      throws VirtualDatabaseException;

  /**
   * Disables a backend once all the pending write queries are executed. A
   * checkpoint is inserted in the recovery log. The backend must belong to this
   * virtual database and be in the enabled state.
   * 
   * @param databaseBackendName The database backend logical name to disable
   * @exception VirtualDatabaseException in case of communication-related error
   */
  void disableBackendWithCheckpoint(String databaseBackendName)
      throws VirtualDatabaseException;

  /**
   * Disable all backends for this virtual database
   * 
   * @throws VirtualDatabaseException if fails
   */
  void disableAllBackends() throws VirtualDatabaseException;

  // TODO: Move implem to console

  /**
   * Disable all backends and store a checkpoint
   * 
   * @param checkpoint the name of the checkpoitn
   * @throws VirtualDatabaseException if fails
   */
  void disableAllBackendsWithCheckpoint(String checkpoint)
      throws VirtualDatabaseException;

  // TODO: Move implem to console

  /**
   * Get a list of all DatabaseBackend names.
   * 
   * @return ArrayList <code>ArrayList</code> of <code>String</code>
   *         representing database backend names
   * @throws VirtualDatabaseException if an error occurs
   */
  ArrayList getAllBackendNames() throws VirtualDatabaseException;

  /**
   * Add an additionnal backend to the virtual database with connection managers
   * identical to the backend replicated.
   * 
   * @param backendName the backend to replicate and to use parameters from.
   * @param newBackendName the new backend name.
   * @param parameters parameters to override or modify when replicating to the
   *          new backend
   * @throws VirtualDatabaseException if cannot replicate backend
   */
  void replicateBackend(String backendName, String newBackendName,
      Map parameters) throws VirtualDatabaseException;

  /**
   * Remove a backend from the virtual database list. Do not check whether it is
   * enabled or not, and do not perform backup operation
   * 
   * @param backend the name of the backend to remove
   * @throws VirtualDatabaseException if the backend does not exist
   */
  void removeBackend(String backend) throws VirtualDatabaseException;

  /**
   * Transfer the backend to the destinated controller. Note that this does
   * nothing in a non-distributed environment
   * 
   * @param backend the backend to transfer
   * @param controllerDestination the controller to copy the backend to
   * @throws VirtualDatabaseException if transfer failed
   */
  void transferBackend(String backend, String controllerDestination)
      throws VirtualDatabaseException;

  //
  // Checkpoints management
  //

  /**
   * Copies a chunk of the local virtual database recovery log onto a remote
   * controller's peer virtual database log. The copy is performed from the
   * checkpoint associated to the specified dump uptil 'now' (a new global
   * checkpoint). The copy is sent to the specified remote node.
   * 
   * @param dumpName the name of the dump (which gives associated checkpoint)
   *          from which to perform the copy.
   * @param controllerName the remote controller to send the copy to
   * @throws VirtualDatabaseException if there is no recovery log, or the
   *           virtual database is not distributed, or in case of error.
   */
  void copyLogFromCheckpoint(String dumpName, String controllerName)
      throws VirtualDatabaseException;

  /**
   * Deletes the recovery log (if any) from the begining upto the specified
   * checkpoint.
   * 
   * @param checkpointName the name of the checkpoint upto which to delete the
   *          recovery log.
   * @throws VirtualDatabaseException if there is no recovery log, or in case of
   *           error.
   */
  void deleteLogUpToCheckpoint(String checkpointName)
      throws VirtualDatabaseException;

  /**
   * Disable all backends and store a checkpoint
   * 
   * @param checkpoint the name of the checkpoitn
   * @throws VirtualDatabaseException if fails
   */
  void removeCheckpoint(String checkpoint) throws VirtualDatabaseException;

  /**
   * Sets the last known checkpoint of a backend. This will also update the
   * value in the recovery log
   * 
   * @param backendName backend
   * @param checkpoint checkpoint
   * @throws VirtualDatabaseException if fails
   */
  void setBackendLastKnownCheckpoint(String backendName, String checkpoint)
      throws VirtualDatabaseException;

  /**
   * Returns an array of names of all the checkpoint available in the recovery
   * log of this virtual dabase.
   * 
   * @return <code>ArrayList</code> of checkpoint names. Can be empty
   */
  ArrayList viewCheckpointNames();

  // 
  // Backup management
  //

  /**
   * Get the names of the <code>Backupers</code> available from this
   * <code>BackupManager</code>.
   * 
   * @return an (possibly 0-sized) array of <code>String</code> representing
   *         the name of the <code>Backupers</code>
   */
  String[] getBackuperNames();

  /**
   * Get the dump format associated to a given <code>Backuper</code>
   * 
   * @param backuperName name associated to a <code>Backuper</code>
   * @return the dump format associated to a given <code>Backuper</code>
   */
  String getDumpFormatForBackuper(String backuperName);

  /**
   * Create a backup of a specific backend. Note the backend will be disabled if
   * needed during backup, and will be put back to its previous state after
   * backup.
   * 
   * @param backendName the target backend to backup
   * @param login the login to use to connect to the database for the backup
   *          operation
   * @param password the password to use to connect to the database for the
   *          backup operation
   * @param dumpName the name of the dump to create
   * @param backuperName the logical name of the backuper to use
   * @param path the path where to store the dump
   * @param tables the list of tables to backup, null means all tables
   * @throws VirtualDatabaseException if the backup fails
   */
  void backupBackend(String backendName, String login, String password,
      String dumpName, String backuperName, String path, ArrayList tables)
      throws VirtualDatabaseException;

  /**
   * Get all available dump info for this virtual database
   * 
   * @return an array of <code>DumpInfo</code> containing the available dump
   *         info for this virtual database. Cannot be null but can be empty.
   * @throws VirtualDatabaseException if cannot retrieve dump informations
   */
  DumpInfo[] getAvailableDumps() throws VirtualDatabaseException;

  /**
   * Update the path of the dump for a given dumpName.
   * 
   * @param dumpName name of the dump
   * @param newPath new path for the dump
   * @throws VirtualDatabaseException if cannot update the path
   */
  void updateDumpPath(String dumpName, String newPath)
      throws VirtualDatabaseException;

  /**
   * Remove a dump from the controller repository
   * 
   * @param dumpName name of the dump to remove
   * @return <code>true</code> if the dump was removed, <code>false</code>
   *         otherwise
   */
  boolean removeDump(String dumpName);

  /**
   * Restore a dump on a specific backend. The proper Backuper is retrieved
   * automatically according to the dump format stored in the recovery log dump
   * table.
   * <p>
   * This method disables the backend and leave it disabled after recovery
   * process. The user has to call the <code>enableBackendFromCheckpoint</code>
   * after this.
   * 
   * @param databaseBackendName the name of the backend to restore
   * @param login the login to use to connect to the database for the restore
   *          operation
   * @param password the password to use to connect to the database for the
   *          restore operation
   * @param dumpName the name of the dump to restore
   * @param tables the list of tables to restore, null means all tables
   * @throws VirtualDatabaseException if the restore operation failed
   */
  void restoreDumpOnBackend(String databaseBackendName, String login,
      String password, String dumpName, ArrayList tables)
      throws VirtualDatabaseException;

  /**
   * Copy a local dump over to a remote member of this distributed vdb, making
   * it available for restore operation. Wants a recovery log to be enabled
   * (stores dump info, and meaning less otherwize as no restore is possible
   * without a recovery log). It is pointless (and an error) to use this on a
   * non-distributed virtual db.
   * 
   * @param dumpName the name of the dump to copy. Should exist locally, and not
   *          remotely.
   * @param remoteControllerName the remote controller to talk to.
   * @throws VirtualDatabaseException in case of error.
   */
  void copyDump(String dumpName, String remoteControllerName)
      throws VirtualDatabaseException;

  /**
   * Transfer specified dump over to specified vdb's controller, making it
   * available for restore operation. The local dump is not deleted and still
   * available for local restore operations. This operation wants a recovery log
   * to be enabled for the vdb (stores dump info, and meaning less otherwize as
   * no restore is possible without a recovery log). It is pointless (and an
   * error) to use this on a non-distributed virtual db.
   * 
   * @param dumpName the name of the dump to copy. Should exist locally, and not
   *          remotely.
   * @param remoteControllerName the remote controller to talk to.
   * @param noCopy specifies whether or not to actually copy the dump. Default:
   *          false. No-copy is a useful option in case of NFS/shared dumps.
   * @throws VirtualDatabaseException in case of error
   */
  void transferDump(String dumpName, String remoteControllerName, boolean noCopy)
      throws VirtualDatabaseException;

  //
  // Administration/Monitoring functions
  //

  /**
   * Return information about the specified backend.
   * 
   * @param backendName the backend logical name
   * @return String the backend information
   * @throws VirtualDatabaseException if an error occurs
   */
  String getBackendInformation(String backendName)
      throws VirtualDatabaseException;

  // TODO: Should return a BackendInfo

  /**
   * The getXml() method does not return the schema if it is not static anymore,
   * to avoid confusion between static and dynamic schema. This method returns a
   * static view of the schema, whatever the dynamic precision is.
   * 
   * @param backendName the name of the backend to get the schema from
   * @return an xml formatted string
   * @throws VirtualDatabaseException if an error occurs while accessing the
   *           backend, or if the backend does not exist.
   */
  String getBackendSchema(String backendName) throws VirtualDatabaseException;

  /**
   * Return the state of a given database backend
   * 
   * @param backendName the name of the backend
   * @return <code>String</code> description of the database backend
   * @throws VirtualDatabaseException if fails
   */
  String getBackendState(String backendName) throws VirtualDatabaseException;

  // TODO: Dup with getBackendInformation?

  /**
   * Retrieves this <code>VirtualDatabase</code> object in xml format
   * 
   * @return xml formatted string that conforms to c-jdbc.dtd
   */
  String getXml();

  //
  // Virtual database management
  //

  /**
   * Authenticate a user for a given virtual database
   * 
   * @param adminLogin username
   * @param adminPassword password
   * @return true if authentication is a success, false otherwise
   * @throws VirtualDatabaseException if database does not exists
   */
  boolean checkAdminAuthentication(String adminLogin, String adminPassword)
      throws VirtualDatabaseException;

  /**
   * Gets the virtual database name to be used by the client (C-JDBC driver)
   * 
   * @return the virtual database name
   */
  String getVirtualDatabaseName();

  /**
   * Indicate if there is a recovery log defined for this virtual database
   * 
   * @return <code>true</code> if the recovery log is defined and can be
   *         accessed, <code>false</code> otherwise
   */
  boolean hasRecoveryLog();

  /**
   * Indicate if there is a result cache defined for this virtual database
   * 
   * @return <code>true</code> if a request cache is defined and can be
   *         accessed, <code>false</code> otherwise
   */
  boolean hasResultCache();

  /**
   * Tells whether this database is distributed or not
   * 
   * @return <code>true</code> if the database is distributed among multiple
   *         controllers <code>false</code> if it exists on a single
   *         controller only
   */
  boolean isDistributed();

  /**
   * Shutdown this virtual database. Finish all threads and stop connection to
   * backends
   * 
   * @param level Constants.SHUTDOWN_WAIT, Constants.SHUTDOWN_SAFE or
   *          Constants.SHUTDOWN_FORCE
   * @throws VirtualDatabaseException if an error occurs
   */
  void shutdown(int level) throws VirtualDatabaseException;

  /**
   * Returns an array of information on this backend The method above is not
   * used at the moment ... This one is by the GUI.
   * 
   * @param backendName the name of the backend
   * @return <code>String[]</code>
   * @throws VirtualDatabaseException if an error occurs
   */
  String[] viewBackendInformation(String backendName)
      throws VirtualDatabaseException;

  // TODO: Ugly, probably dup, to refactor

  /**
   * Returns a mapping of controller jmx names with their backends. Note the
   * method is only useful in distributed environment
   * 
   * @return <code>Hashtable</code> of controllerName -->
   *         ArrayList[BackendInfo]
   * @throws VirtualDatabaseException if cannot return the result
   */
  Hashtable viewGroupBackends() throws VirtualDatabaseException;

  /**
   * Name of the controller owning this virtual database
   * 
   * @return url of the controller
   */
  String viewOwningController();

  /**
   * Retrieves an array of data on the backends for this virtual database
   * 
   * @return <code>String[][]</code> of formatted data for all backends
   * @throws Exception if fails
   */
  String[][] retrieveBackendsData() throws Exception;

  // TODO: Ugly, to refactor

  /**
   * Retrieves an array of statistics of the given backend for this virtual
   * database
   * 
   * @param backendName name of the backend
   * @return <code>BackendStatistics[]</code> of formatted data for all
   *         backends or <code>null</code> if the backend does not exist
   * @throws Exception if fails
   */
  BackendStatistics getBackendStatistics(String backendName) throws Exception;

  // TODO: Should monitoring be in a separate interface?

  /**
   * Return the list of controllers defining this virtual database. If the
   * database is not distributed this returns the same as
   * <code>viewOwningController</code> otherwise returns an array of
   * controller configuring this <code>DistributedVirtualDatabase</code>
   * 
   * @return <code>String[]</code> of controller names.
   */
  String[] viewControllerList();

  /**
   * Returns the currentNbOfThreads.
   * 
   * @return int
   */
  int getCurrentNbOfThreads();

  // TODO: Should monitoring be in a separate interface?

  /**
   * Clean data collected by the current monitoring system, to avoid memory
   * problems. Monitor does not have to be active.
   * 
   * @throws VirtualDatabaseException if there is no monitor.
   */
  void cleanMonitoringData() throws VirtualDatabaseException;

  // TODO: Should monitoring be in a separate interface? Ugly, usefulness?

  /**
   * If a monitoring section exists, we can set the monitoring on or off by
   * calling this method. If monitoring is not defined we throw an exception.
   * 
   * @param active should set the monitor to on or off
   * @throws VirtualDatabaseException if there is no monitor.
   */
  void setMonitoringToActive(boolean active) throws VirtualDatabaseException;
  // TODO: Should monitoring be in a separate interface?

}