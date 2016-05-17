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
 * Contributor(s): Olivier Fambon.
 */

package org.objectweb.cjdbc.controller.virtualdatabase;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.management.NotCompliantMBeanException;

import org.objectweb.cjdbc.common.exceptions.BackupException;
import org.objectweb.cjdbc.common.exceptions.ControllerException;
import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.VirtualDatabaseException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.shared.BackendInfo;
import org.objectweb.cjdbc.common.shared.DumpInfo;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.backup.Backuper;
import org.objectweb.cjdbc.controller.backup.DumpTransferInfo;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.jmx.MBeanServerManager;
import org.objectweb.cjdbc.controller.jmx.RmiConnector;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;
import org.objectweb.cjdbc.controller.recoverylog.events.LogEntry;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.requestmanager.RequestManager;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.BackendStatus;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.BackendTransfer;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.CJDBCGroupMessage;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ControllerName;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.CopyLogEntry;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.DisableBackend;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedTransactionMarker;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.EnableBackend;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.InitiateDumpCopy;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ReplicateLogEntries;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.SetCheckpoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.VirtualDatabaseConfiguration;
import org.objectweb.tribe.adapters.MulticastRequestAdapter;
import org.objectweb.tribe.adapters.MulticastRequestListener;
import org.objectweb.tribe.adapters.MulticastResponse;
import org.objectweb.tribe.channel.JGroupsReliableChannelWithGms;
import org.objectweb.tribe.channel.ReliableGroupChannelWithGms;
import org.objectweb.tribe.common.Address;
import org.objectweb.tribe.common.Group;
import org.objectweb.tribe.common.GroupIdentifier;
import org.objectweb.tribe.common.Member;
import org.objectweb.tribe.exceptions.ChannelException;
import org.objectweb.tribe.exceptions.NotConnectedException;
import org.objectweb.tribe.exceptions.TimeoutException;
import org.objectweb.tribe.gms.GroupMembershipListener;
import org.objectweb.tribe.gms.JGroupsMembershipService;
import org.objectweb.tribe.messages.MessageListener;

/**
 * A <code>DistributedVirtualDatabase</code> is a virtual database hosted by
 * several controllers. Communication between the controllers is achieved with
 * reliable multicast provided by Javagroups.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class DistributedVirtualDatabase extends VirtualDatabase
    implements
      MessageListener,
      MulticastRequestListener,
      GroupMembershipListener
{
  //
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Request handling
  // 4. Transaction handling
  // 5. Database backend management
  // 6. Distribution management (multicast)
  // 7. Getter/Setter (possibly in alphabetical order)
  //

  // Distribution

  /** Group name */
  private String                      groupName                  = null;
  /** Controller name mapping: Member --> controller JMX name */
  private Hashtable                   controllersMap;
  /** Tribe Member --> <code>ArrayList[BackendInfo]</code> */
  private Hashtable                   backendsPerController;

  /** JGroups channel */
  private ReliableGroupChannelWithGms channel                    = null;
  /** MessageDispatcher to communicate with the group */
  private MulticastRequestAdapter     multicastRequestAdapter;
  private Group                       currentGroup               = null;
  private ArrayList                   allMemberButUs             = null;
  private static final long           INCOMPATIBLE_CONFIGURATION = -1;
  private boolean                     isVirtualDatabaseStarted   = false;

  /**
   * Our view of the request manager, same as super.requestManager, only just
   * typed properly.
   */
  private DistributedRequestManager   distributedRequestManager;

  /** Logger for distributed request execution */
  private static Trace                distributedRequestLogger;

  //
  // Constructors
  //
  /**
   * Creates a new <code>DistributedVirtualDatabase</code> instance.
   * 
   * @param controller the controller we belong to
   * @param name the virtual database name
   * @param groupName the virtual database group name
   * @param maxConnections maximum number of concurrent connections.
   * @param pool should we use a pool of threads for handling connections?
   * @param minThreads minimum number of threads in the pool
   * @param maxThreads maximum number of threads in the pool
   * @param maxThreadIdleTime maximum time a thread can remain idle before being
   *          removed from the pool.
   * @param sqlShortFormLength maximum number of characters of an SQL statement
   *          to display in traces or exceptions
   * @param blobFilter encoding method for blobs
   * @exception NotCompliantMBeanException in case the bean does not comply with
   *              jmx
   * @exception JmxException the bean could not be registered
   */
  public DistributedVirtualDatabase(Controller controller, String name,
      String groupName, int maxConnections, boolean pool, int minThreads,
      int maxThreads, long maxThreadIdleTime, int sqlShortFormLength,
      AbstractBlobFilter blobFilter) throws NotCompliantMBeanException,
      JmxException
  {
    super(controller, name, maxConnections, pool, minThreads, maxThreads,
        maxThreadIdleTime, sqlShortFormLength, blobFilter);

    this.groupName = groupName;
    backendsPerController = new Hashtable();
    controllersMap = new Hashtable();
    isVirtualDatabaseStarted = false;
    distributedRequestLogger = Trace
        .getLogger("org.objectweb.cjdbc.controller.distributedvirtualdatabase.request."
            + name);
  }

  /**
   * Disconnect the channel and close it.
   * 
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable
  {
    quitChannel();
    super.finalize();
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#addBackend(org.objectweb.cjdbc.controller.backend.DatabaseBackend)
   */
  public void addBackend(DatabaseBackend db) throws VirtualDatabaseException
  {
    // Add the backend to the virtual database.
    super.addBackend(db);

    // Send a group message if already joined group
    try
    {
      broadcastBackendInformation(getAllMemberButUs());
    }
    catch (Exception e)
    {
      String msg = "Error while broadcasting backend information when adding backend";
      logger.error(msg, e);
      throw new VirtualDatabaseException(msg, e);
    }
  }

  /**
   * Terminate the multicast request adapter and quit the jgroups channel.
   * 
   * @throws NotConnectedException if the channel is not connected
   * @throws ChannelException if an error occurred while closing the channel
   */
  public void quitChannel() throws ChannelException, NotConnectedException
  {
    if (multicastRequestAdapter != null)
    {
      multicastRequestAdapter.stop();
    }
    if (channel != null)
    {
      channel.close();
    }
  }

  /**
   * Returns the controllerName value.
   * 
   * @return Returns the controllerName.
   */
  public String getControllerName()
  {
    return controller.getControllerName();
  }

  /**
   * Returns the group name this virtual database belongs to.
   * 
   * @return a <code>String</code> value. Returns <code>null</code> if this
   *         virtual database is standalone
   */
  public String getGroupName()
  {
    return groupName;
  }

  /**
   * Sets the group name used by the controllers hosting this virtual database.
   * 
   * @param groupName the group name to set
   */
  public void setGroupName(String groupName)
  {
    this.groupName = groupName;
  }

  /**
   * Sets a new distributed request manager for this database.
   * 
   * @param requestManager the new request manager.
   */
  public void setRequestManager(RequestManager requestManager)
  {
    if (!(requestManager instanceof DistributedRequestManager))
      throw new RuntimeException(
          "A distributed virtual database can only work with a distributed request manager.");

    distributedRequestManager = (DistributedRequestManager) requestManager;
    // really, this is super.requestManager
    this.requestManager = distributedRequestManager;
  }

  //
  // Distribution handling
  //

  /**
   * Makes this virtual database join a virtual database group. Those groups are
   * mapped to JavaGroups groups.
   * 
   * @exception Exception if an error occurs
   */
  public void joinGroup() throws Exception
  {
    try
    {
      // Read the protocol stack configuration from jgroups.xml
      URL jgroupConfigFile = DistributedVirtualDatabase.class
          .getResource("/jgroups.xml");
      if (jgroupConfigFile == null)
        logger.warn(Translate
            .get("virtualdatabase.distributed.jgroups.xml.not.found"));
      else
        logger.info(Translate.get("virtualdatabase.distributed.jgroups.using",
            jgroupConfigFile.toString()));
      JGroupsMembershipService gms = new JGroupsMembershipService(
          jgroupConfigFile);
      gms.registerGroupMembershipListener(this);
      channel = new JGroupsReliableChannelWithGms(gms);
      if (logger.isDebugEnabled())
        logger.debug("Group communication channel is configured as follows: "
            + ((JGroupsReliableChannelWithGms) channel).getProperties());

      // Join the group
      channel.join(new Group(new GroupIdentifier(groupName)));
      multicastRequestAdapter = new MulticastRequestAdapter(channel // group
          // channel
          , this /* MessageListener */
          , this /* MulticastRequestListener */
      );

      // Let the MulticastRequestAdapter thread pump the membership out of the
      // JGroups channel.
      Thread.sleep(2000);

      logger.info("Group " + groupName + " connected to "
          + channel.getLocalMembership());

      // Add ourselves to the list of controllers
      controllersMap.put(channel.getLocalMembership(), controller.getJmxName());

      // Check if we are alone or not
      currentGroup = channel.getCurrentGroup();
      ArrayList currentGroupMembers = currentGroup.getMembers();
      int groupSize = currentGroupMembers.size();
      if (groupSize == 1)
      {
        logger.info(Translate.get(
            "virtualdatabase.distributed.configuration.first.in.group",
            groupName));
        allMemberButUs = new ArrayList();
        distributedRequestManager.setControllerId(0);
        isVirtualDatabaseStarted = true;
        return;
      }

      logger.info("Group now contains " + groupSize + " controllers.");
      if (logger.isDebugEnabled())
      {
        logger.debug("Current list of controllers is as follows:");
        for (Iterator iter = currentGroupMembers.iterator(); iter.hasNext();)
          logger.debug("Controller " + iter.next());
      }

      refreshGroupMembership(); // also updates allMemberButUs

      // Check with the other controller that our config is compatible
      long controllerId = checkConfigurationCompatibilityAndReturnControllerId(getAllMemberButUs());
      if (controllerId == INCOMPATIBLE_CONFIGURATION)
      {
        String msg = Translate
            .get("virtualdatabase.distributed.configuration.not.compatible");
        logger.error(msg);
        throw new ControllerException(msg);
      }
      else
      {
        // In case several controllers join at the same time they would get the
        // same highest controller id value and here we discriminate them by
        // adding their position in the membership. This assumes that the
        // membership is ordered the same way at all nodes.
        controllerId += currentGroupMembers.indexOf(channel
            .getLocalMembership());

        if (logger.isInfoEnabled())
        {
          logger.info(Translate
              .get("virtualdatabase.distributed.configuration.compatible"));
          logger.info("Controller identifier is set to: " + controllerId);
        }
        // Set the controller Id
        distributedRequestManager.setControllerId(controllerId);
      }

      // Distribute backends among controllers knowing that at this point
      // there is no conflict on the backend distribution policies.
      broadcastBackendInformation(getAllMemberButUs());
    }
    catch (Exception e)
    {
      String msg = Translate.get("virtualdatabase.distributed.joingroup.error",
          groupName);
      if (e instanceof RuntimeException)
        logger.error(msg, e);
      throw new Exception(msg + " (" + e + ")");
    }
    isVirtualDatabaseStarted = true;
  }

  /**
   * Synchronized access to current group members.
   * 
   * @return a clone of the list of all members (never null).
   */
  public ArrayList getAllMembers()
  {
    synchronized (controllersMap)
    {
      if (currentGroup == null) // this happens if we did not #joinGroup()
        return new ArrayList();
      return ((ArrayList) currentGroup.getMembers().clone());
    }
  }

  /**
   * Returns the list of all members in the group except us. Consider the value
   * read-only (do not alter).
   * 
   * @return the allMembersButUs field (never null).
   */
  public ArrayList getAllMemberButUs()
  {
    synchronized (controllersMap)
    {
      if (allMemberButUs == null) // this happens if we did not #joinGroup()
        return new ArrayList();

      /**
       * This synchronized block might seem loussy, but actually it's enough, as
       * long as no caller alters the returned value: field allMembersButUs is
       * replaced (as opposed to updated) by refreshGroupMembership(). So
       * someone who has called this lives with a (possibly) outdated list, but,
       * still, with a safe list (never updated concurently by vdb threads). If
       * clients/callers are not trusted to leave the returned value un-touched,
       * use a clone
       */
      return allMemberButUs;
    }
  }

  /**
   * Get the group channel used for group communications
   * 
   * @return a <code>JChannel</code>
   */
  public ReliableGroupChannelWithGms getChannel()
  {
    return channel;
  }

  /**
   * Returns the currentGroup value.
   * 
   * @return Returns the currentGroup.
   */
  public Group getCurrentGroup()
  {
    return currentGroup;
  }

  /**
   * Return the group communication multicast request adapter.
   * 
   * @return the group communication multicast request adapter
   */
  public MulticastRequestAdapter getMulticastRequestAdapter()
  {
    return multicastRequestAdapter;
  }

  /**
   * Send the configuration of this controller to remote controller. All remote
   * controllers must agree on the compatibility of the local controller
   * configuration with their own configuration. Compatibility checking include
   * Authentication Manager, Scheduler and Load Balancer settings.
   * 
   * @param dest List of <code>Address</code> to send the message to
   * @return INCOMPATIBLE_CONFIGURATION if the configuration is not compatible
   *         with other controllers or the controller id to use otherwise.
   */
  private long checkConfigurationCompatibilityAndReturnControllerId(
      ArrayList dest)
  {
    if (logger.isInfoEnabled())
      logger.info(Translate
          .get("virtualdatabase.distributed.configuration.checking"));

    // Send our configuration
    MulticastResponse rspList;
    try
    {
      rspList = multicastRequestAdapter.multicastMessage(dest,
          new VirtualDatabaseConfiguration(this),
          MulticastRequestAdapter.WAIT_ALL,
          CJDBCGroupMessage.defaultCastTimeOut);
    }
    catch (TimeoutException e)
    {
      logger.error(
          "Timeout occured while checking configuration compatibility", e);
      return INCOMPATIBLE_CONFIGURATION;
    }
    catch (ChannelException e)
    {
      logger
          .error(
              "Communication error occured while checking configuration compatibility",
              e);
      return INCOMPATIBLE_CONFIGURATION;
    }
    catch (NotConnectedException e)
    {
      logger.error(
          "Channel unavailable while checking configuration compatibility", e);
      return INCOMPATIBLE_CONFIGURATION;
    }

    // Check that everybody agreed
    HashMap results = rspList.getResults();
    int size = results.size();
    if (size == 0)
      logger.warn(Translate
          .get("virtualdatabase.distributed.configuration.checking.noanswer"));

    long highestRemoteControllerId = 0;
    for (Iterator iter = results.values().iterator(); iter.hasNext();)
    {
      Object response = iter.next();
      if (response instanceof Long)
      {
        // These highestRemotecontrollerId and remoteControllerId are returned
        // directly by the remote controller, and are 'thus' of 'shifted
        // nature': effective bits = upper 16 bits. See
        // DistributedRequestManager.CONTROLLER_ID_BITS
        long remoteControllerId = ((Long) response).longValue();
        if (remoteControllerId == INCOMPATIBLE_CONFIGURATION)
          return INCOMPATIBLE_CONFIGURATION;
        else if (highestRemoteControllerId < remoteControllerId)
          highestRemoteControllerId = remoteControllerId;
      }
      else
      {
        logger
            .error("Unexpected response while checking configuration compatibility: "
                + response);
        return INCOMPATIBLE_CONFIGURATION;
      }
    }

    // Ok, everybody agreed that our configuration is compatible.
    // Take the highest controller id + 1 as our id. (non-shifted, this is used
    // to pass in setControllerId which expects 16 bits)
    return ((highestRemoteControllerId >> DistributedRequestManager.CONTROLLER_ID_SHIFT_BITS) & DistributedRequestManager.CONTROLLER_ID_BITS) + 1;
  }

  /**
   * Broadcast backend information among controllers.
   * 
   * @param dest List of <code>Address</code> to send the message to
   * @throws NotConnectedException if the channel is not connected
   * @throws ChannelException if the channel reported an error
   * @throws TimeoutException if a timeout occured
   */
  private void broadcastBackendInformation(ArrayList dest)
      throws TimeoutException, ChannelException, NotConnectedException
  {
    logger
        .debug(Translate
            .get("virtualdatabase.distributed.configuration.querying.remote.status"));

    // Send our backend status
    MulticastResponse rspList = multicastRequestAdapter.multicastMessage(dest,
        new BackendStatus(getBackendsInfo(backends)),
        MulticastRequestAdapter.WAIT_ALL, CJDBCGroupMessage.defaultCastTimeOut);

    int size = dest.size();
    for (int i = 0; i < size; i++)
    {
      // Add the backend configuration of every remote controller
      Member m = (Member) dest.get(i);
      if (rspList.getResult(m) != null)
      {
        if (logger.isDebugEnabled())
          logger
              .debug(Translate
                  .get(
                      "virtualdatabase.distributed.configuration.updating.backend.list",
                      m.toString()));
      }
      else
        logger.warn(Translate.get(
            "virtualdatabase.distributed.unable.get.remote.status", m
                .toString()));
    }
  }

  /**
   * Check if the given backend definition is compatible with the backend
   * definitions of this distributed virtual database. Not that if the given
   * backend does not exist in the current configuration, it is considered as
   * compatible. Incompatibility results from 2 backends with the same JDBC URL.
   * 
   * @param backend the backend to check
   * @return true if the backend is compatible with the local definition
   * @throws VirtualDatabaseException if locking the local backend list fails
   */
  public boolean isCompatibleBackend(BackendInfo backend)
      throws VirtualDatabaseException
  {
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = "Unable to acquire read lock on backend list in isCompatibleBackend ("
          + e + ")";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    try
    {
      // Find the backend
      String backendURL = backend.getUrl();
      int size = backends.size();
      DatabaseBackend b = null;
      for (int i = 0; i < size; i++)
      {
        b = (DatabaseBackend) backends.get(i);
        if (b.getURL().equals(backendURL))
          return false;
      }
    }
    catch (RuntimeException re)
    {
      throw new VirtualDatabaseException(re);
    }
    finally
    {
      releaseReadLockBackendLists();
    }
    // This backend does not exist here
    return true;
  }

  /**
   * Return true if the provided schema is compatible with the existing schema
   * of this distributed virtual database. Note that if the given schema is
   * null, this function returns true.
   * 
   * @param dbs the database schema to compare with
   * @return true if dbs is compatible with the current schema (according to
   *         RAIDb level)
   */
  public boolean isCompatibleDatabaseSchema(DatabaseSchema dbs)
  {
    // Database schema checking (if any)
    if (dbs == null)
    {
      logger.warn(Translate
          .get("virtualdatabase.distributed.configuration.checking.noschema"));
    }
    else
    {
      // Check database schemas compatibility
      switch (getRequestManager().getLoadBalancer().getRAIDbLevel())
      {
        case RAIDbLevels.RAIDb0 :
          // There must be no overlap between schemas
          if (dbs.equals(getRequestManager().getDatabaseSchema()))
          {
            logger
                .warn(Translate
                    .get("virtualdatabase.distributed.configuration.checking.mismatch.databaseschema"));
            return false;
          }
          break;
        case RAIDbLevels.RAIDb1 :
          // Schemas must be identical
          if (!dbs.equals(getRequestManager().getDatabaseSchema()))
          {
            logger
                .warn(Translate
                    .get("virtualdatabase.distributed.configuration.checking.mismatch.databaseschema"));
            return false;
          }
          break;
        case RAIDbLevels.RAIDb2 :
          // Common parts of the schema must be identical
          if (!dbs.isCompatibleWith(getRequestManager().getDatabaseSchema()))
          {
            logger
                .warn(Translate
                    .get("virtualdatabase.distributed.configuration.checking.mismatch.databaseschema"));
            return false;
          }
          break;
        case RAIDbLevels.SingleDB :
        default :
          logger.error("Unsupported RAIDb level: "
              + getRequestManager().getLoadBalancer().getRAIDbLevel());
          return false;
      }
    }
    return true;
  }

  //
  // Message dispatcher request handling
  //

  /**
   * @see org.objectweb.tribe.messages.MessageListener#receive(java.io.Serializable)
   */
  public void receive(Serializable msg)
  {
    logger.error("Distributed virtual database received unhandled message: "
        + msg);
  }

  /**
   * This method handle the scheduling part of the queries to be sure that the
   * query is scheduled in total order before letting other queries to execute.
   * 
   * @see org.objectweb.tribe.adapters.MulticastRequestListener#handleMessageSingleThreaded(java.io.Serializable,
   *      org.objectweb.tribe.common.Member)
   */
  public Object handleMessageSingleThreaded(Serializable msg, Member sender)
  {
    try
    {
      if (msg != null)
      {
        if (logger.isDebugEnabled())
          logger.debug("handleMessageSingleThreaded (" + msg.getClass() + "): "
              + msg);

        // This has to be executed in total order
        if (msg instanceof DistributedRequest)
        { // Distributed request execution
          if (!isVirtualDatabaseStarted)
            return null; // Not ready yet to handle requests

          if (logger.isDebugEnabled())
            logger.debug(getControllerName()
                + ": Scheduling distributedRequest "
                + ((DistributedRequest) msg).getRequest().getId() + " from "
                + sender);

          if (distributedRequestLogger.isInfoEnabled())
          {
            AbstractRequest request = ((DistributedRequest) msg).getRequest();
            distributedRequestLogger.info((request.isSelect() ? "S " : "W ")
                + request.getTransactionId() + " " + request.getSQL());
          }
          ((DistributedRequest) msg).scheduleRequest(distributedRequestManager);
          return null;
        }
        if (msg instanceof DistributedTransactionMarker)
        { // Distributed commit/rollback/savepoint
          if (!isVirtualDatabaseStarted)
            return null; // Not ready yet to handle requests

          if (logger.isDebugEnabled())
            logger.debug(getControllerName() + ": Scheduling " + msg + " from "
                + sender);

          if (distributedRequestLogger.isInfoEnabled())
            distributedRequestLogger.info(msg);

          ((DistributedTransactionMarker) msg)
              .scheduleCommand(distributedRequestManager);
          return null;
        }
        if (msg instanceof SetCheckpoint)
          setCheckpoint(((SetCheckpoint) msg).getCheckpointName());
        else if (msg instanceof ReplicateLogEntries)
          handleReplicateLogEntries((ReplicateLogEntries) msg);
        else if (msg instanceof CopyLogEntry)
          handleCopyLogEntry((CopyLogEntry) msg);
        // Other message types will be handled in multithreaded handler
      }
      else
      {
        String errorMsg = "Invalid null message";
        logger.error(errorMsg);
        return new ControllerException(errorMsg);
      }

      return null;
    }
    catch (Exception e)
    {
      if (e instanceof RuntimeException)
        logger.warn("Error while handling group message:" + msg.getClass(), e);
      return e;
    }
  }

  /**
   * @see org.objectweb.tribe.adapters.MulticastRequestListener#handleMessageMultiThreaded(Serializable,
   *      Member, Object)
   */
  public Serializable handleMessageMultiThreaded(Serializable msg,
      Member sender, Object handleMessageSingleThreadedResult)
  {
    try
    {
      if (msg != null)
      {
        if (logger.isDebugEnabled())
          logger.debug("handleMessageMultiThreaded (" + msg.getClass() + "): "
              + msg);
        if (msg instanceof SetCheckpoint)
        {
          return null; // ignore, handled in the single-threaded stage.
        }
        if (msg instanceof DistributedRequest)
        { // Distributed request execution
          if (!isVirtualDatabaseStarted)
            return new NoMoreBackendException(
                "Controller is in intialization phase");

          if (logger.isDebugEnabled())
            logger.debug(getControllerName()
                + ": Executing distributedRequest "
                + ((DistributedRequest) msg).getRequest().getId() + " from "
                + sender);
          Serializable result = ((Serializable) ((DistributedRequest) msg)
              .executeScheduledRequest(distributedRequestManager));
          return result;
        }
        if (msg instanceof DistributedTransactionMarker)
        { // Distributed commit/rollback/savepoint execution
          if (!isVirtualDatabaseStarted)
            return new NoMoreBackendException(
                "Controller is in intialization phase");

          if (logger.isDebugEnabled())
            logger.debug(getControllerName() + ": " + msg + " from " + sender);
          return ((Serializable) ((DistributedTransactionMarker) msg)
              .executeCommand(distributedRequestManager));
        }
        if (msg instanceof ControllerName)
        {
          addRemoteController(sender, ((ControllerName) msg).getJmxName());
          return Boolean.TRUE;
        }
        else if (msg instanceof VirtualDatabaseConfiguration)
        { // Check if given configuration is compatible with the local one
          return handleVirtualDatabaseConfiguration(
              (VirtualDatabaseConfiguration) msg, sender);
        }
        else if (msg instanceof BackendTransfer)
        { // Receive a backend and enable it
          return handleBackendTransfer((BackendTransfer) msg);
        }
        else if (msg instanceof BackendStatus)
          handleBackendStatus((BackendStatus) msg, sender);
        else if (msg instanceof EnableBackend)
          handleEnableBackend((EnableBackend) msg, sender);
        else if (msg instanceof DisableBackend)
          handleDisableBackend((DisableBackend) msg, sender);
        else if (msg instanceof ReplicateLogEntries)
          return null; // handled in single-threaded
        else if (msg instanceof CopyLogEntry)
          return null; // handled in single-threaded
        else if (msg instanceof InitiateDumpCopy)
          handleInitiateDumpCopy((InitiateDumpCopy) msg);
        else
          logger.warn("Unhandled message type received: " + msg.getClass()
              + "(" + msg + ")");
      }
      else
      {
        String errorMsg = "Invalid null message";
        logger.error(errorMsg);
        return new ControllerException(errorMsg);
      }
      return null;
    }
    catch (Exception e)
    {
      if (e instanceof RuntimeException)
        logger.warn("Error while handling group message:" + msg.getClass(), e);
      return e;
    }
    finally
    {
      if (msg != null)
      {
        // Just in case something bad happen and the request was not properly
        // removed from the queue.
        if (msg instanceof DistributedRequest)
        {
          synchronized (totalOrderQueue)
          {
            if (totalOrderQueue.remove(((DistributedRequest) msg).getRequest()))
            {
              logger.warn("Distributed request "
                  + ((DistributedRequest) msg).getRequest()
                  + " did not remove itself from the total order queue");
              totalOrderQueue.notifyAll();
            }
          }
        }
        else if (msg instanceof DistributedTransactionMarker)
        {
          synchronized (totalOrderQueue)
          {
            if (totalOrderQueue.remove(msg))
            {
              logger.warn("Distributed " + msg.toString() + " did not remove "
                  + "itself from the total order queue");
              totalOrderQueue.notifyAll();
            }
          }
        }
      }
    }
  }

  private RecoveryLog getRecoveryLog() throws VirtualDatabaseException
  {
    if (!hasRecoveryLog())
      throw new VirtualDatabaseException(Translate
          .get("virtualdatabase.no.recovery.log"));

    return getRequestManager().getRecoveryLog();
  }

  private void handleInitiateDumpCopy(InitiateDumpCopy msg)
      throws VirtualDatabaseException, BackupException, IOException
  {
    // hand-off copy to backuper, if a copy is required
    if (msg.getDumpTransferInfo() != null)
    {
      Backuper backuper = getRequestManager().getBackupManager()
          .getBackuperByFormat(msg.getDumpInfo().getDumpFormat());

      backuper.fetchDump(msg.getDumpTransferInfo(), msg.getDumpInfo()
          .getDumpPath(), msg.getDumpInfo().getDumpName());
    }

    // update local recovery log dump tables
    getRecoveryLog().setDumpInfo(msg.getDumpInfo());
  }

  /**
   * Handle CopyLogEntry messages.
   * 
   * @param msg the CopyLogEntry message.
   */
  private void handleCopyLogEntry(CopyLogEntry msg)
  {
    if (!hasRecoveryLog())
    {
      logger.warn("Tentative handleCopyLogEntry on vdb with no recovery log");
      return;
    }

    getRequestManager().getRecoveryLog().logLogEntry(msg.getEntry());
  }

  /**
   * Handle ReplicateLogEntries messages. There are two cases: initialization
   * and termination of the replication process. Initialization asynchronously
   * and ato,ically wipes-out the recovery log entries before the 'now'
   * checkpointand. Termination sets the dump checkpoint name, thus making it
   * available so that a recovery can be executed.
   * 
   * @param msg the ReplicateLogEntries message
   * @throws SQLException in case of error when resetting the recovery log.
   */
  private void handleReplicateLogEntries(ReplicateLogEntries msg)
      throws SQLException
  {
    if (!hasRecoveryLog())
    {
      logger
          .warn("Tentative handleReplicateLogEntries on vdb with no recovery log");
      return;
    }

    if (msg.getDumpCheckpointName() != null)
    { // Termination
      getRequestManager().getRecoveryLog().storeDumpCheckpointName(
          msg.getDumpCheckpointName(), msg.getCheckpointId());
    }
    else
    { // Initialization
      getRequestManager().getRecoveryLog().resetLogTableIdAndDeleteRecoveryLog(
          msg.getCheckpointName(), msg.getCheckpointId());
    }
  }

  /**
   * Handles BackendStatus messages.
   * 
   * @param msg the BackendStatus message
   * @param sender the message sender
   */
  private void handleBackendStatus(BackendStatus msg, Member sender)
  {
    // Update backend list from sender
    ArrayList remoteBackendInfoList = msg.getBackends();
    // Convert BackendInfo arraylist to real DatabaseBackend objects
    ArrayList remoteBackendList = new ArrayList(remoteBackendInfoList.size());
    for (Iterator iter = remoteBackendInfoList.iterator(); iter.hasNext();)
    {
      BackendInfo info = (BackendInfo) iter.next();
      remoteBackendList.add(info.getDatabaseBackend());
    }
    backendsPerController.put(sender, remoteBackendList);
    if (logger.isInfoEnabled())
      logger.info(Translate.get(
          "virtualdatabase.distributed.configuration.updating.backend.list",
          sender));
  }

  /**
   * Handles BackendTransfer messages.
   * 
   * @param msg the BackendTransfer message
   * @return true on success
   * @throws NotCompliantMBeanException if an error occurs while creating the
   *           DatabaseBackend
   * @throws VirtualDatabaseException if an error occurs while adding the new
   *           backend
   */
  private Serializable handleBackendTransfer(BackendTransfer msg)
      throws NotCompliantMBeanException, VirtualDatabaseException
  {
    if (logger.isInfoEnabled())
      logger.info(getControllerName()
          + ": Received transfer command. Checkpoint: "
          + msg.getCheckpointName());
    BackendInfo info = msg.getInfo();
    DatabaseBackend backend = new DatabaseBackend(info);
    this.addBackend(backend);
    if (logger.isInfoEnabled())
      logger.info(getControllerName() + ": Enable backend");
    enableBackendFromCheckpoint(backend.getName(), msg.getCheckpointName());
    return Boolean.TRUE;
  }

  /**
   * Handles DisableBackend messages.
   * 
   * @param msg the DisableBackend message
   * @param sender the message sender
   */
  private void handleDisableBackend(DisableBackend msg, Member sender)
  {
    ArrayList remoteBackendList = (ArrayList) backendsPerController.get(sender);
    if (remoteBackendList == null)
    { // This case was reported by Alessandro Gamboz on April 1, 2005.
      // It looks like the EnableBackend message arrives before membership
      // has been properly updated.
      logger.warn("No information has been found for remote controller "
          + sender);
      remoteBackendList = new ArrayList();
      backendsPerController.put(sender, remoteBackendList);
    }
    DatabaseBackend disabledBackend = msg.getDatabaseBackend();
    int size = remoteBackendList.size();
    boolean backendFound = false;
    for (int i = 0; i < size; i++)
    {
      DatabaseBackend b = (DatabaseBackend) remoteBackendList.get(i);
      if (b.equals(disabledBackend))
      {
        logger.info("Backend " + b.getName() + " disabled on controller "
            + sender);
        remoteBackendList.set(i, disabledBackend);
        backendFound = true;
        break;
      }
    }
    if (!backendFound)
    {
      logger.warn("Updating backend list with unknown backend "
          + disabledBackend.getName() + " disabled on controller " + sender);
      remoteBackendList.add(disabledBackend);
    }
  }

  /**
   * Handles EnableBackend messages.
   * 
   * @param msg the EnableBackend message
   * @param sender the message sender
   */
  private void handleEnableBackend(EnableBackend msg, Member sender)
  {
    ArrayList remoteBackendList = (ArrayList) backendsPerController.get(sender);
    if (remoteBackendList == null)
    { // This case was reported by Alessandro Gamboz on April 1, 2005.
      // It looks like the EnableBackend message arrives before membership
      // has been properly updated.
      logger.warn("No information has been found for remote controller "
          + sender);
      remoteBackendList = new ArrayList();
      backendsPerController.put(sender, remoteBackendList);
    }
    DatabaseBackend enabledBackend = msg.getDatabaseBackend();
    int size = remoteBackendList.size();
    boolean backendFound = false;
    for (int i = 0; i < size; i++)
    {
      DatabaseBackend b = (DatabaseBackend) remoteBackendList.get(i);
      if (b.equals(enabledBackend))
      {
        logger.info("Backend " + b.getName() + " enabled on controller "
            + sender);
        remoteBackendList.set(i, enabledBackend);
        backendFound = true;
        break;
      }
    }
    if (!backendFound)
    {
      logger.warn("Updating backend list with unknown backend "
          + enabledBackend.getName() + " enabled on controller " + sender);
      remoteBackendList.add(enabledBackend);
    }
  }

  /**
   * Handles VirtualDatabaseConfiguration messages.
   * 
   * @param msg the VirtualDatabaseConfiguration message
   * @param sender the membership of the message sender
   * @return a long, controllerId on success, INCOMPATIBLE_CONFIGURATION on
   *         error
   * @throws TimeoutException if a group communication timeout occurs
   * @throws ChannelException if the group communication fails
   * @throws NotConnectedException if the group communication channel is not
   *           conected
   */
  private Serializable handleVirtualDatabaseConfiguration(
      VirtualDatabaseConfiguration msg, Member sender) throws TimeoutException,
      ChannelException, NotConnectedException
  {

    if (logger.isInfoEnabled())
      logger.info("Checking virtual database configuration from "
          + msg.getControllerName());

    if (!isLocalSender(sender) && !msg.isCompatible(this))
      // Failure
      return new Long(INCOMPATIBLE_CONFIGURATION);

    // Send JMX notification
    if (MBeanServerManager.isJmxEnabled())
    {
      Hashtable data = new Hashtable();
      data.put("controllerName", msg.getControllerName());
      data.put("rmiconnector", new String[]{msg.getRmiHostname(),
          msg.getRmiPort()});
      RmiConnector.broadcastNotification(this,
          CjdbcNotificationList.DISTRIBUTED_CONTROLLER_ADDED,
          CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
              "notification.distributed.controller.added", this
                  .getVirtualDatabaseName()), data);
    }

    addRemoteController(sender, msg.getControllerJmxName());

    // Send controller name to new comer
    if (logger.isDebugEnabled())
      logger.debug("Sending local controller name to joining controller ("
          + sender + ")");
    ArrayList target = new ArrayList();
    target.add(sender);
    multicastRequestAdapter.multicastMessage(target, new ControllerName(
        controller.getControllerName(), controller.getJmxName()),
        MulticastRequestAdapter.WAIT_ALL, CJDBCGroupMessage.defaultCastTimeOut);

    // Send backend status
    if (logger.isDebugEnabled())
      logger.debug("Sending backend status name to joining controller ("
          + sender + ")");
    multicastRequestAdapter.multicastMessage(target, new BackendStatus(
        getBackendsInfo(backends)), MulticastRequestAdapter.WAIT_ALL,
        CJDBCGroupMessage.defaultCastTimeOut);

    if (logger.isInfoEnabled())
      logger.info("Controller " + msg.getControllerName()
          + " is compatible with the local configuration");
    return new Long(distributedRequestManager.getControllerId());
  }

  /**
   * Returns true if the given member is ourselves.
   * 
   * @param sender the sender
   * @return true if we are the sender, false otherwise
   */
  private boolean isLocalSender(Member sender)
  {
    return channel.getLocalMembership().equals(sender);
  }

  /**
   * Get the status of all remote controllers
   * 
   * @throws NotConnectedException if the channel is not connected
   * @throws ChannelException if the channel reported an error
   * @throws TimeoutException if a timeout occured
   */
  public void getBackendStatus() throws TimeoutException, ChannelException,
      NotConnectedException
  {
    if (logger.isDebugEnabled())
      logger.debug("Requesting remote controllers status");
    MulticastResponse rspList = multicastRequestAdapter.multicastMessage(null,
        new BackendStatus(getBackendsInfo(backends)),
        MulticastRequestAdapter.WAIT_ALL, CJDBCGroupMessage.defaultCastTimeOut);

    HashMap results = rspList.getResults();
    for (Iterator iter = results.values().iterator(); iter.hasNext();)
    {
      ArrayList b = (ArrayList) iter.next();
      int bSize = b.size();
      if (bSize == 0)
        logger.debug("No Database backends");
      else
        for (int j = 0; j < bSize; j++)
          logger.debug(((DatabaseBackend) b.get(j)).getXml());
    }
  }

  //
  // Group Membership Management
  //

  /**
   * Add a new controller name to the controllerMap list and refresh the group
   * membership.
   * 
   * @param remoteControllerMembership the membership identifying the remote
   *          controller
   * @param remoteControllerJmxName the JMX name of the remote controller
   */
  public void addRemoteController(Member remoteControllerMembership,
      String remoteControllerJmxName)
  {
    controllersMap.put(remoteControllerMembership, remoteControllerJmxName);
    if (logger.isDebugEnabled())
      logger.debug("Adding new controller " + remoteControllerJmxName);
    refreshGroupMembership();
  }

  /**
   * Remove a remote controller (usually because it has failed) from the
   * controllerMap list and refresh the group membership.
   * 
   * @param remoteControllerMembership the membership identifying the remote
   *          controller
   * @return the JMX name of the removed controller (or null if this controller
   *         was not in the list)
   */
  public String removeRemoteController(Member remoteControllerMembership)
  {
    String remoteControllerJmxName = (String) controllersMap
        .remove(remoteControllerMembership);
    if (logger.isDebugEnabled())
      logger.debug("Removing controller " + remoteControllerJmxName);

    // Remove the list of remote backends since they are no more reachable
    backendsPerController.remove(remoteControllerMembership);
    refreshGroupMembership();
    return remoteControllerJmxName;
  }

  /*
   * Set a raw checkpoint in local recoveryLog (if any, or yell) to reflect
   * group-comm events. Local, raw operation: this is not a cluster-wide,
   * synchronous checkpoint (although group-comm event message is sent to every
   * member of the group). Intended to be used by external recovery schemes, or
   * as debugging tags.
   */
  private void setEventCheckpoint(String name, Member m, GroupIdentifier gid)
  {
    if (!hasRecoveryLog())
    {
      logger.warn("Trying to set checkpoint with no recoveryLog. Ignored.");
      return;
    }

    try
    {
      getRequestManager().getRecoveryLog().storeCheckpoint(
          (name + " member:" + m + " gid:" + gid + " " + new Date())
              .replaceAll(" ", "_"));
    }
    catch (SQLException e)
    {
      logger.warn("setEventCheckpoint: " + name, e);
    }
  }

  /**
   * @see org.objectweb.tribe.gms.GroupMembershipListener#joinMember(org.objectweb.tribe.common.Member,
   *      org.objectweb.tribe.common.GroupIdentifier)
   */
  public void joinMember(Member m, GroupIdentifier gid)
  {
    if (hasRecoveryLog())
      setEventCheckpoint("joining", m, gid);
  }

  /**
   * @see org.objectweb.tribe.gms.GroupMembershipListener#quitMember(org.objectweb.tribe.common.Member,
   *      org.objectweb.tribe.common.GroupIdentifier)
   */
  public void quitMember(Member m, GroupIdentifier gid)
  {
    if (hasRecoveryLog())
      setEventCheckpoint("quitting", m, gid);

    // Notify adapter that we do not expect responses anymore from this member
    int failures = multicastRequestAdapter.memberFailsOnAllReplies(m);
    logger.info(failures + " requests were waiting responses from " + m);

    // Remove controller from list and notify JMX listeners
    String remoteControllerName = removeRemoteController(m);
    if (remoteControllerName != null)
    {
      logger.warn("Controller " + m + " has left the cluster.");
      // Send JMX notification
      if (MBeanServerManager.isJmxEnabled())
      {
        Hashtable data = new Hashtable();
        data.put("controllerName", remoteControllerName);
        RmiConnector.broadcastNotification(this,
            CjdbcNotificationList.DISTRIBUTED_CONTROLLER_REMOVED,
            CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
                "notification.distributed.controller.removed", this
                    .getVirtualDatabaseName()), data);
      }
    }
  }

  /**
   * @see org.objectweb.tribe.gms.GroupMembershipListener#groupComposition(org.objectweb.tribe.common.Group,
   *      org.objectweb.tribe.common.Address)
   */
  public void groupComposition(Group g, Address sender)
  {
    // Just ignore
  }

  /**
   * @see org.objectweb.tribe.gms.GroupMembershipListener#failedMember(org.objectweb.tribe.common.Member,
   *      org.objectweb.tribe.common.GroupIdentifier,
   *      org.objectweb.tribe.common.Member)
   */
  public void failedMember(Member failed, GroupIdentifier gid, Member sender)
  {
    // Just ignore
  }

  /**
   * Refresh the current group membership when someone has joined or left the
   * group.
   */
  private void refreshGroupMembership()
  {
    if (logger.isDebugEnabled())
      logger.debug("Refreshing members list:" + currentGroup.getMembers());

    synchronized (controllersMap)
    {
      allMemberButUs = ((ArrayList) currentGroup.getMembers().clone());
      allMemberButUs.remove(channel.getLocalMembership());
    }
  }

  //
  // Getter/Setter and tools (equals, ...)
  //

  /**
   * Is this virtual database distributed ?
   * 
   * @return true
   */
  public boolean isDistributed()
  {
    return true;
  }

  /**
   * Two virtual databases are equal if they have the same name, login and
   * password.
   * 
   * @param other an object
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object other)
  {
    if ((other == null)
        || (!(other instanceof org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase)))
      return false;
    else
    {
      DistributedVirtualDatabase db = (org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase) other;
      return name.equals(db.getDatabaseName())
          && groupName.equals(db.getGroupName());
    }
  }

  /**
   * Get the XML dump of the Distribution element if any.
   * 
   * @return XML dump of the Distribution element
   */
  protected String getDistributionXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_Distribution + " "
        + DatabasesXmlTags.ATT_groupName + "=\"" + groupName + "\">");

    info.append("</" + DatabasesXmlTags.ELT_Distribution + ">");
    return info.toString();
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#viewControllerList()
   */
  public String[] viewControllerList()
  {
    if (logger.isInfoEnabled())
    {
      logger.info(channel.getLocalMembership() + " see members:"
          + currentGroup.getMembers() + " and has mapping:" + controllersMap);
    }
    String[] members = new String[controllersMap.keySet().size()];
    Iterator iter = controllersMap.keySet().iterator();
    int i = 0;
    while (iter.hasNext())
    {
      members[i] = (String) controllersMap.get(iter.next());
      i++;
    }
    return members;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#viewGroupBackends()
   */
  public Hashtable viewGroupBackends() throws VirtualDatabaseException
  {
    Hashtable map = new Hashtable(controllersMap.size());
    Iterator iter = backendsPerController.keySet().iterator();
    Member member;
    while (iter.hasNext())
    {
      member = (Member) iter.next();
      map.put(controllersMap.get(member), backendsPerController.get(member));
    }
    return map;
  }

  /**
   * We have to convert the backends list from an array of
   * <code>DatabaseBackend</code> object to an ArrayList of
   * <code>BackendInfo</code> objects. The DatabaseBackend objects cannot be
   * serialized because they are used as MBean and notification emitters, so we
   * want to extract the info out of them.
   * 
   * @param backendsObject the list of DatabaseBackend object
   * @see BackendInfo
   * @return a list of BackendInfo objects
   */
  public ArrayList getBackendsInfo(ArrayList backendsObject)
  {
    int size = backendsObject.size();
    ArrayList infos = new ArrayList(size);
    DatabaseBackend backend;
    for (int i = 0; i < size; i++)
    {
      backend = (DatabaseBackend) backendsObject.get(i);
      infos.add(createBackendInfo(backend, false));
    }
    return infos;
  }

  /**
   * Create backend information object from a DatabaseBackend object. This will
   * get only static information.
   * 
   * @param backend the <code>DatabaseBackend</code> object to get info from
   * @param useXml should we add xml for extensive backend description
   * @return <code>BackendInfo</code>
   */
  public BackendInfo createBackendInfo(DatabaseBackend backend, boolean useXml)
  {
    BackendInfo info = new BackendInfo(backend);
    if (!useXml)
      info.setXml(null);
    return info;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#removeBackend(java.lang.String)
   */
  public void removeBackend(String backend) throws VirtualDatabaseException
  {
    super.removeBackend(backend);

    try
    {
      // Send a group message to update backend list
      broadcastBackendInformation(getAllMemberButUs());
    }
    catch (Exception e)
    {
      String msg = "An error occured while multicasting new backedn information";
      logger.error(msg, e);
      throw new VirtualDatabaseException(msg, e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#transferBackend(java.lang.String,
   *      java.lang.String)
   */
  public void transferBackend(String backend, String controllerDestination)
      throws VirtualDatabaseException
  {
    Member targetMember = getControllerByName(controllerDestination);

    // Get reference on backend
    DatabaseBackend db = getAndCheckBackend(backend, CHECK_BACKEND_DISABLE);
    String transfertCheckpointName = makeTransferCheckpointName(db,
        targetMember);

    if (logger.isDebugEnabled())
      logger.debug("**** Disabling backend for transfer");

    // Disable local backend (distributed operation)
    try
    {
      if (!hasRecoveryLog())
        throw new VirtualDatabaseException(
            "Transfer is not supported on virtual databases without a recovery log");

      distributedRequestManager.disableBackendForCheckpoint(db,
          transfertCheckpointName);
    }
    catch (SQLException e)
    {
      throw new VirtualDatabaseException(e.getMessage());
    }

    // Enable remote transfered backend.
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("**** Sending transfer message to:" + targetMember);

      ArrayList dest = new ArrayList(1);
      dest.add(targetMember);

      multicastRequestAdapter.multicastMessage(dest, new BackendTransfer(
          controllerDestination, transfertCheckpointName, createBackendInfo(db,
              true)), MulticastRequestAdapter.WAIT_ALL,
          CJDBCGroupMessage.defaultCastTimeOut);

      if (logger.isDebugEnabled())
        logger.debug("**** Removing local backend");

      // Remove backend from this controller
      removeBackend(db);

      // Broadcast updated backend list
      broadcastBackendInformation(getAllMemberButUs());
    }
    catch (Exception e)
    {
      String msg = "An error occured while transfering the backend";
      logger.error(msg, e);
      throw new VirtualDatabaseException(msg, e);
    }
  }

  /**
   * Gets a Controller specified by its name as a Member object suitable for
   * group communication.
   * 
   * @param controllerName the name of the target controller
   * @return a Member representing the target controller
   * @throws VirtualDatabaseException
   */
  private Member getControllerByName(String controllerName)
      throws VirtualDatabaseException
  {
    // Get the target controller
    Iterator iter = controllersMap.entrySet().iterator();
    Member targetMember = null;
    while (iter.hasNext())
    {
      Entry entry = (Entry) iter.next();
      if (entry.getValue().equals(controllerName))
      {
        targetMember = (Member) entry.getKey();
        break;
      }
    }
    if (targetMember == null)
      throw new VirtualDatabaseException("Cannot find controller:"
          + controllerName + " in group");
    return targetMember;
  }

  private String makeTransferCheckpointName(DatabaseBackend db, Member dest)
  {
    return ("transfer-checkpoint: " + db.getName() + " from "
        + this.controller.getControllerName() + " to " + dest.getUid() + " " + new Date())
        .replaceAll(" ", "_");
  }

  /**
   * Atomically checkpoint across specified group members. Group-wide atomicity
   * is (and can only be) achieved with a globally ordered message, possibly
   * also sent to ourselves. The local operation must block writes group-wide
   * (in scheduler), to allow a clean write of the checkpoint mark. TODO CHECK
   * this requirement (group-wide lock).
   * 
   * @param checkpointName the name of the (transfer) checkpoint to create
   * @param groupMembers an ArrayList of target Members
   * @throws VirtualDatabaseException in case of scheduler or recoveryLog
   *           exceptions
   * @see VirtualDatabase#setCheckpoint(String)
   * @see DistributedVirtualDatabase#handleMessageSingleThreaded(Serializable,
   *      Member)
   */
  public void setGroupCheckpoint(String checkpointName, ArrayList groupMembers)
      throws VirtualDatabaseException
  {
    try
    {
      getMulticastRequestAdapter().multicastMessage(groupMembers,
          new SetCheckpoint(checkpointName), MulticastRequestAdapter.WAIT_ALL,
          CJDBCGroupMessage.defaultCastTimeOut);
    }
    catch (Exception e)
    {
      String msg = "Set group checkpoint failed: checkpointName="
          + checkpointName;
      logger.error(msg, e);
      throw new VirtualDatabaseException(msg);
    }
  }

  /**
   * Sets an atomic (group-wide) checkpoint on local & target controllers.
   * 
   * @param controllerName the target remote controller
   * @return the 'now' checkpoint name.
   * @throws VirtualDatabaseException in case of error (whatever error, wraps
   *           the underlying error)
   */
  private String setLogReplicationCheckpoint(String controllerName)
      throws VirtualDatabaseException
  {
    String checkpointName = "now-" + new Date();

    ArrayList dest = new ArrayList();
    dest.add(getControllerByName(controllerName));
    dest.add(channel.getLocalMembership());
    setGroupCheckpoint(checkpointName, dest);
    return checkpointName;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#copyLogFromCheckpoint(java.lang.String,
   *      java.lang.String)
   */
  public void copyLogFromCheckpoint(String dumpName, String controllerName)
      throws VirtualDatabaseException
  {
    // perform basic error checks (in particular, on sucksess, we have a
    // recovery log)
    super.copyLogFromCheckpoint(dumpName, controllerName);

    // get the checkpoint name from the dump info, or die
    String dumpCheckpointName;
    DumpInfo dumpInfo;

    try
    {
      dumpInfo = getRecoveryLog().getDumpInfo(dumpName);
    }
    catch (SQLException e)
    {
      throw new VirtualDatabaseException(
          "Recovery log error access occured while checking for dump"
              + dumpName, e);
    }

    if (dumpInfo == null)
      throw new VirtualDatabaseException(
          "No information was found in the dump table for dump " + dumpName);

    dumpCheckpointName = dumpInfo.getCheckpointName();

    // set a global 'now' checkpoint (temporary)
    String nowCheckpointName = setLogReplicationCheckpoint(controllerName);

    // get it's id (ewerk) so that we can replicate it on the other side
    // TODO: find a way to hide those ids in the recovery log interface...
    long nowCheckpointId;
    RecoveryLog recoveryLog = getRequestManager().getRecoveryLog();
    try
    {
      nowCheckpointId = recoveryLog.getCheckpointRequestId(nowCheckpointName);
    }
    catch (SQLException e)
    {
      String errorMessage = "Can not find 'now checkpoint' log entry";
      logger.error(errorMessage);
      throw new VirtualDatabaseException(errorMessage);
    }

    // initiate the replication - clears the remote recovery log.
    sendMessageToController(controllerName, new ReplicateLogEntries(
        nowCheckpointName, null, nowCheckpointId));

    // protect from concurrent log updates: fake a recovery (increments
    // semaphore)
    recoveryLog.beginRecovery();

    // copy the entries over to the remote controller.
    // Send them one by one over to the remote controller, coz each LogEntry can
    // potentially be huge (e.g. if it contains a blob)

    // Note: ugly iteration over Ids, but that's all we have.
    // TODO: provide itertator in terms of checkpoint names
    try
    {
      long dumpId = recoveryLog.getCheckpointRequestId(dumpCheckpointName);
      long nowId = recoveryLog.getCheckpointRequestId(nowCheckpointName);

      for (long id = dumpId; id != nowId; id++)
      {
        LogEntry entry = recoveryLog.getNextLogEntry(id);
        if (entry == null)
        {
          String errorMessage = "Can not find expected log entry: " + id;
          logger.error(errorMessage);
          throw new VirtualDatabaseException(errorMessage);
        }

        // Because 'getNextLogEntry()' will hunt for the next valid log entry,
        // we need to update the iterator with the new id value - 1
        id = entry.getId() - 1;
        sendMessageToController(controllerName, new CopyLogEntry(entry));
      }

      // terminate the replication - sets the remote dump chakpoint name.
      sendMessageToController(controllerName, new ReplicateLogEntries(null,
          dumpCheckpointName, dumpId));
    }
    catch (SQLException e)
    {
      String errorMessage = "Failed to send log entries";
      logger.error(errorMessage, e);
      throw new VirtualDatabaseException(errorMessage);
    }
    finally
    {
      recoveryLog.endRecovery(); // release semaphore
    }

  }

  /**
   * Send a Message to a remote controller. This sends a point-to-point message,
   * fifo. No total order is specifically required.
   * 
   * @param controllerName name of the remote controller
   * @param message the message to send (should be Serializable)
   * @throws VirtualDatabaseException (wrapping error) in case of communication
   *           failure
   */
  private void sendMessageToController(String controllerName,
      Serializable message) throws VirtualDatabaseException
  {
    try
    {
      ArrayList dest = new ArrayList();
      dest.add(getControllerByName(controllerName));
      getMulticastRequestAdapter().multicastMessage(dest, message,
          MulticastRequestAdapter.WAIT_ALL,
          CJDBCGroupMessage.defaultCastTimeOut);
    }
    catch (Exception e)
    {
      String errorMessage = message.getClass().getName() + ": send failed";
      logger.error(errorMessage, e);
      throw new VirtualDatabaseException(errorMessage);
    }
  }

  /**
   * What this method does is really initiating the copy. It is the remote
   * controller's vdb that performs the actual copy, fetching the dump from this
   * vdb's local backuper.
   * 
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#copyDump(java.lang.String,
   *      java.lang.String)
   * @param dumpName the name of the dump to copy. Should exist locally, and not
   *          remotely.
   * @param remoteControllerName the remote controller to talk to.
   * @throws VirtualDatabaseException in case of error.
   */
  public void copyDump(String dumpName, String remoteControllerName)
      throws VirtualDatabaseException
  {
    transferDump(dumpName, remoteControllerName, false);
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#transferDump(java.lang.String,
   *      java.lang.String, boolean)
   */
  public void transferDump(String dumpName, String remoteControllerName,
      boolean noCopy) throws VirtualDatabaseException
  {
    // get the info from the backuper
    DumpInfo dumpInfo = null;
    try
    {
      dumpInfo = getRecoveryLog().getDumpInfo(dumpName);
      /*
       * getDumpInfo() is the one that throws SQLException (should it be a
       * VirtualDatabaseException instead ???)
       */
    }
    catch (SQLException e)
    {
      String msg = "getting dump info from backup manager failed";
      throw new VirtualDatabaseException(msg, e);
    }

    if (dumpInfo == null)
      throw new VirtualDatabaseException("no dump info for dump '" + dumpName
          + "'");

    // if a copy is needed, hand-off copy to backuper: setup server side of the
    // copy
    DumpTransferInfo dumpTransferInfo = null;
    if (!noCopy)
    {
      try
      {
        dumpTransferInfo = getRequestManager().getBackupManager()
            .getBackuperByFormat(dumpInfo.getDumpFormat()).setupServer();
      }
      catch (IOException e)
      {
        throw new VirtualDatabaseException(e);
      }
    }

    // send message to remote vdb instance, to act as a client
    // (see handleInitiateDumpCopy)
    sendMessageToController(remoteControllerName, new InitiateDumpCopy(
        dumpInfo, dumpTransferInfo));

  }

}