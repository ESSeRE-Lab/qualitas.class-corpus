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
 * Contributor(s): Julie Marguerite, Greg Ward, Nicolas Modrzyk, Vadim Kassin, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.requestmanager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import javax.management.NotCompliantMBeanException;

import org.objectweb.cjdbc.common.exceptions.BackupException;
import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.RollbackException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.jmx.mbeans.RequestManagerMBean;
import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.shared.BackendState;
import org.objectweb.cjdbc.common.shared.DumpInfo;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.AlterRequest;
import org.objectweb.cjdbc.common.sql.CreateRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.RequestType;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.controller.backend.BackendStateListener;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.backup.BackupManager;
import org.objectweb.cjdbc.controller.backup.Backuper;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.cache.parsing.ParsingCache;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean;
import org.objectweb.cjdbc.controller.jmx.MBeanServerManager;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.recoverylog.BackendRecoveryInfo;
import org.objectweb.cjdbc.controller.recoverylog.RecoverThread;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * This class defines the Request Manager.
 * <p>
 * The RM is composed of a Request Scheduler, an optional Query Cache, and a
 * Load Balancer and an optional Recovery Log.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:vadim@kase.kz">Vadim Kassin </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RequestManager extends AbstractStandardMBean
    implements
      XmlComponent,
      RequestManagerMBean
{

  //
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Request handling
  // 4. Transaction handling
  // 5. Database backend management
  // 6. Getter/Setter (possibly in alphabetical order)
  //

  /** begin timeout in ms */
  protected long                 beginTimeout;

  /** commit timeout in ms */
  protected long                 commitTimeout;

  /** rollback timeout in ms */
  protected long                 rollbackTimeout;

  /** The virtual database owning this Request Manager */
  protected VirtualDatabase      vdb;

  /** The request scheduler to order and schedule requests */
  protected AbstractScheduler    scheduler;

  /** An optional request cache to cache responses to SQL requests */
  protected AbstractResultCache  resultCache;

  /** The request load balancer to use to send requests to the databases */
  protected AbstractLoadBalancer loadBalancer;

  /** An optional recovery log */
  protected RecoveryLog          recoveryLog;

  /** The backup manager responsible for backup and restore of backends */
  protected BackupManager        backupManager;

  // The virtual dabase schema
  protected DatabaseSchema       dbs;

  /** <code>true</code> if schema is no more up-to-date and needs a refresh */
  private boolean                schemaIsDirty                 = false;

  private boolean                schemaIsStatic                = false;

  private boolean                isCaseSensitiveParsing        = false;

  protected ParsingCache         parsingCache                  = null;

  private MetadataCache          metadataCache                 = null;

  // SQL queries parsing granularity according to Scheduler, ResultCache and
  // LoadBalancer required granularity
  protected int                  schedulerParsingranularity    = ParsingGranularities.NO_PARSING;

  private int                    cacheParsingranularity        = ParsingGranularities.NO_PARSING;

  private int                    loadBalancerParsingranularity = ParsingGranularities.NO_PARSING;

  protected int                  requiredParsingGranularity    = ParsingGranularities.NO_PARSING;

  /** Transaction id/Login mapping */
  protected Hashtable            tidLoginTable;

  /** Transaction id/Savepoints mapping */
  protected Hashtable            tidSavepoints;

  protected static Trace         logger                        = null;

  private BackendStateListener   backendStateListener;

  //
  // Constructors
  //

  /**
   * Creates a new <code>RequestManager</code> instance.
   * 
   * @param vdb the virtual database this request manager belongs to
   * @param scheduler the Request Scheduler to use
   * @param cache a Query Cache implementation
   * @param loadBalancer the Request Load Balancer to use
   * @param recoveryLog the Log Recovery to use
   * @param beginTimeout timeout in seconds for begin
   * @param commitTimeout timeout in seconds for commit
   * @param rollbackTimeout timeout in seconds for rollback
   * @throws SQLException if an error occurs
   * @throws NotCompliantMBeanException if the MBean is not JMX compliant
   */
  public RequestManager(VirtualDatabase vdb, AbstractScheduler scheduler,
      AbstractResultCache cache, AbstractLoadBalancer loadBalancer,
      RecoveryLog recoveryLog, long beginTimeout, long commitTimeout,
      long rollbackTimeout) throws SQLException, NotCompliantMBeanException
  {
    super(RequestManagerMBean.class);
    this.vdb = vdb;
    assignAndCheckSchedulerLoadBalancerValidity(scheduler, loadBalancer);
    // requiredParsingGranularity is the maximum of each component granularity
    this.resultCache = cache;
    if (resultCache != null)
    {
      cacheParsingranularity = cache.getParsingGranularity();
      if (cacheParsingranularity > requiredParsingGranularity)
        requiredParsingGranularity = cacheParsingranularity;
    }
    setRecoveryLog(recoveryLog);
    initRequestManagerVariables(vdb, beginTimeout, commitTimeout,
        rollbackTimeout);
    setBackendsLastKnownCheckpointFromRecoveryLog();
    logger.info(Translate.get("requestmanager.parsing.granularity",
        ParsingGranularities.getInformation(requiredParsingGranularity)));

    if (MBeanServerManager.isJmxEnabled())
    {
      try
      {
        MBeanServerManager.registerMBean(this, JmxConstants
            .getRequestManagerObjectName(vdb.getVirtualDatabaseName()));

      }
      catch (Exception e)
      {
        logger.error(Translate.get("jmx.failed.register.mbean.requestmanager"));
      }
    }
  }

  /**
   * Retrieve the last known checkpoint from the recovery log and set it for
   * each backend.
   */
  private void setBackendsLastKnownCheckpointFromRecoveryLog()
  {

    if (recoveryLog == null)
      return;
    String databaseName = vdb.getVirtualDatabaseName();
    ArrayList backends = vdb.getBackends();
    int size = backends.size();
    DatabaseBackend backend;
    BackendRecoveryInfo info;
    for (int i = 0; i < size; i++)
    {
      backend = (DatabaseBackend) backends.get(i);
      info = recoveryLog
          .getBackendRecoveryInfo(databaseName, backend.getName());
      backend.setLastKnownCheckpoint(info.getCheckpoint());
    }

  }

  /**
   * Check that Scheduler and Load Balancer are not null and have compatible
   * RAIDb levels.
   * 
   * @param scheduler
   * @param loadBalancer
   * @throws SQLException if an error occurs
   */
  private void assignAndCheckSchedulerLoadBalancerValidity(
      AbstractScheduler scheduler, AbstractLoadBalancer loadBalancer)
      throws SQLException
  {
    if (scheduler == null)
      throw new SQLException(Translate.get("requestmanager.null.scheduler"));

    if (loadBalancer == null)
      throw new SQLException(Translate.get("requestmanager.null.loadbalancer"));

    if (scheduler.getRAIDbLevel() != loadBalancer.getRAIDbLevel())
      throw new SQLException(Translate.get(
          "requestmanager.incompatible.raidb.levels",
          new String[]{"" + scheduler.getRAIDbLevel(),
              "" + loadBalancer.getRAIDbLevel()}));

    // requiredParsingGranularity is the maximum of each component granularity
    setScheduler(scheduler);
    schedulerParsingranularity = scheduler.getParsingGranularity();
    requiredParsingGranularity = schedulerParsingranularity;
    setLoadBalancer(loadBalancer);
    loadBalancerParsingranularity = loadBalancer.getParsingGranularity();
    if (loadBalancerParsingranularity > requiredParsingGranularity)
      requiredParsingGranularity = loadBalancerParsingranularity;
  }

  /**
   * Method initRequestManagerVariables.
   * 
   * @param vdb
   * @param beginTimeout
   * @param commitTimeout
   * @param rollbackTimeout
   */
  private void initRequestManagerVariables(VirtualDatabase vdb,
      long beginTimeout, long commitTimeout, long rollbackTimeout)
  {
    this.tidLoginTable = new Hashtable();
    this.tidSavepoints = new Hashtable();
    this.beginTimeout = beginTimeout;
    this.commitTimeout = commitTimeout;
    this.rollbackTimeout = rollbackTimeout;
    this.vdb = vdb;
    logger = Trace.getLogger("org.objectweb.cjdbc.controller.RequestManager."
        + vdb.getDatabaseName());
  }

  //
  // Request Handling
  //

  /**
   * Perform a read request and return the reply. Call first the scheduler, then
   * the cache (if defined) and finally the load balancer.
   * 
   * @param request the request to execute
   * @return a <code>ControllerResultSet</code> value
   * @exception SQLException if an error occurs
   */
  public ControllerResultSet execReadRequest(SelectRequest request)
      throws SQLException
  {
    // Sanity check
    if (!request.isAutoCommit())
    { // Check that the transaction has been
      // started
      long tid = request.getTransactionId();
      if (!tidLoginTable.containsKey(new Long(tid)))
        throw new SQLException(Translate.get("transaction.not.started", tid));
    }

    // If we need to parse the request, try to get the parsing from the
    // cache.
    // Note that if we have a cache miss but backgroundParsing has been
    // turned
    // on, then this call will start a ParsedThread in background.
    if ((requiredParsingGranularity != ParsingGranularities.NO_PARSING)
        && (!request.isParsed()))
    {
      if (parsingCache == null)
        request.parse(getDatabaseSchema(), requiredParsingGranularity,
            isCaseSensitiveParsing);
      else
        parsingCache.getParsingFromCache(request);
    }

    //
    // SCHEDULER
    //

    // Get the parsing now if the request is not yet parsed. The parsing is
    // handled by the ParsingCache that may already have parsed the request
    // in background (if backgroundParsing is set).
    if ((schedulerParsingranularity != ParsingGranularities.NO_PARSING)
        && !request.isParsed())
    {
      if (parsingCache == null)
        request.parse(dbs, requiredParsingGranularity, isCaseSensitiveParsing);
      else
        parsingCache.getParsingFromCacheAndParseIfMissing(request);
    }

    if (logger.isDebugEnabled())
      logger.debug(Translate.get("requestmanager.read.request.schedule",
          new String[]{String.valueOf(request.getId()),
              request.getSQLShortForm(vdb.getSQLShortFormLength())}));

    // Wait for the scheduler to give us the authorization to execute
    scheduler.scheduleReadRequest(request);

    //
    // CACHE
    //

    ControllerResultSet result = null;
    try
    { // Check cache if any
      if (resultCache != null)
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("requestmanager.read.request.cache.get",
              new String[]{String.valueOf(request.getId()),
                  request.getSQLShortForm(vdb.getSQLShortFormLength())}));

        AbstractResultCacheEntry qce = resultCache.getFromCache(request, true);
        if (qce != null)
        {
          result = qce.getResult();
          if (result != null)
          { // Cache hit !
            if (vdb.getSQLMonitor() != null)
              vdb.getSQLMonitor().logCacheHit(request);

            scheduler.readCompleted(request);
            return result;
          }
        }
      }

      //
      // LOAD BALANCER
      //

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("requestmanager.read.request.balance",
            new String[]{String.valueOf(request.getId()),
                request.getSQLShortForm(vdb.getSQLShortFormLength())}));

      // At this point, we have a result cache miss.
      // If we had a parsing cache miss too, wait for the parsing to be
      // done if
      // needed.
      if ((loadBalancerParsingranularity != ParsingGranularities.NO_PARSING)
          && !request.isParsed())
      {
        if (parsingCache == null)
          request
              .parse(dbs, requiredParsingGranularity, isCaseSensitiveParsing);
        else
          parsingCache.getParsingFromCacheAndParseIfMissing(request);
      }

      // Send the request to the load balancer
      result = loadBalancer.execReadRequest(request, metadataCache);

      //
      // UPDATES & NOTIFICATIONS
      //

      // Update cache
      if ((resultCache != null)
          && (request.getCacheAbility() != RequestType.UNCACHEABLE))
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get(
              "requestmanager.read.request.cache.update", new String[]{
                  String.valueOf(request.getId()),
                  request.getSQLShortForm(vdb.getSQLShortFormLength())}));

        if (!request.isParsed()
            && (cacheParsingranularity != ParsingGranularities.NO_PARSING))
        { // The cache was the only one to need parsing and the request was not
          // previously in the cache
          if (parsingCache == null)
            request.parse(dbs, requiredParsingGranularity,
                isCaseSensitiveParsing);
          else
            parsingCache.getParsingFromCacheAndParseIfMissing(request);
        }
        resultCache.addToCache(request, result);
      }
    }
    catch (Exception failed)
    {
      if (resultCache != null)
        resultCache.removeFromPendingQueries(request);
      scheduler.readCompleted(request);
      if (failed instanceof NoMoreBackendException)
        throw (NoMoreBackendException) failed;
      String msg = Translate.get("requestmanager.request.failed", new String[]{
          request.getSQLShortForm(vdb.getSQLShortFormLength()),
          failed.getMessage()});
      if (failed instanceof RuntimeException)
        logger.warn(msg, failed);
      else
        logger.warn(msg);
      if (failed instanceof SQLException)
        throw (SQLException) failed;

      throw new SQLException(msg);
    }

    // Notify scheduler of completion
    scheduler.readCompleted(request);

    return result;
  }

  /**
   * Perform a write request and return the number of rows affected Call first
   * the scheduler (if defined), then notify the cache (if defined) and finally
   * call the load balancer.
   * 
   * @param request the request to execute
   * @return number of rows affected
   * @exception SQLException if an error occurs
   */
  public int execWriteRequest(AbstractWriteRequest request) throws SQLException
  {
    scheduleExecWriteRequest(request);
    int execWriteRequestResult = 0;
    try
    {
      execWriteRequestResult = loadBalanceExecWriteRequest(request);
    }
    catch (AllBackendsFailedException e)
    {
      String msg = Translate
          .get("requestmanager.write.request.failed.unexpected");
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
    updateAndNotifyExecWriteRequest(request);
    return execWriteRequestResult;
  }

  /**
   * Perform a write request and return the auto generated keys. Call first the
   * scheduler (if defined), then notify the cache (if defined) and finally call
   * the load balancer.
   * 
   * @param request the request to execute
   * @return auto generated keys.
   * @exception SQLException if an error occurs
   */
  public ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request) throws SQLException
  {
    scheduleExecWriteRequest(request);
    ControllerResultSet execWriteRequestWithKeysResult = null;
    try
    {
      execWriteRequestWithKeysResult = loadBalanceExecWriteRequestWithKeys(request);
    }
    catch (AllBackendsFailedException e)
    {
      String msg = Translate
          .get("requestmanager.write.request.keys.failed.unexpected");
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
    updateAndNotifyExecWriteRequest(request);
    return execWriteRequestWithKeysResult;
  }

  /**
   * Schedule a request for execution.
   * 
   * @param request the request to execute
   * @throws SQLException if an error occurs
   */
  public void scheduleExecWriteRequest(AbstractWriteRequest request)
      throws SQLException
  {
    // Sanity check
    if (!request.isAutoCommit())
    { // Check that the transaction has been
      // started
      long tid = request.getTransactionId();
      if (!tidLoginTable.containsKey(new Long(tid)))
        throw new SQLException(Translate.get("transaction.not.started", tid));
    }

    // If we need to parse the request, try to get the parsing from the
    // cache.
    // Note that if we have a cache miss but backgroundParsing has been
    // turned
    // on, then this call will start a ParsedThread in background.
    if ((requiredParsingGranularity != ParsingGranularities.NO_PARSING)
        && (!request.isParsed()))
    {
      if (parsingCache == null)
        request.parse(getDatabaseSchema(), requiredParsingGranularity,
            isCaseSensitiveParsing);
      else
        parsingCache.getParsingFromCache(request);
    }

    //
    // SCHEDULER
    //

    // Get the parsing now if the request is not yet parsed. The parsing is
    // handled by the ParsingCache that may already have parsed the request
    // in background (if backgroundParsing is set).
    if ((schedulerParsingranularity != ParsingGranularities.NO_PARSING)
        && !request.isParsed())
    {
      if (parsingCache == null)
        request.parse(getDatabaseSchema(), requiredParsingGranularity,
            isCaseSensitiveParsing);
      else
        parsingCache.getParsingFromCacheAndParseIfMissing(request);
    }

    if (logger.isDebugEnabled())
      logger.debug(Translate.get("requestmanager.write.request.schedule",
          new String[]{String.valueOf(request.getId()),
              request.getSQLShortForm(vdb.getSQLShortFormLength())}));

    // Wait for the scheduler to give us the authorization to execute
    try
    {
      scheduler.scheduleWriteRequest(request);
    }
    catch (RollbackException e)
    { // Something bad happened and we need to rollback this transaction
      rollback(request.getTransactionId(), true);
      throw new SQLException(e.getMessage());
    }

    // If we have a parsing cache miss, wait for the parsing to be done if
    // needed. Note that even if the cache was the only one to require
    // parsing,
    // we wait for the parsing result here, because if it fails, we must not
    // execute the query.
    try
    {
      if ((requiredParsingGranularity != ParsingGranularities.NO_PARSING)
          && !request.isParsed())
      {
        if (parsingCache == null)
          request.parse(getDatabaseSchema(), requiredParsingGranularity,
              isCaseSensitiveParsing);
        else
          parsingCache.getParsingFromCacheAndParseIfMissing(request);
      }
    }
    catch (SQLException e)
    {
      // If the parsing fail, we must release the lock acquired ...
      scheduler.writeCompleted(request);
      throw e;
    }
  }

  /**
   * Send the given query to the load balancer. If the request fails, the
   * scheduler is properly notified.
   * 
   * @param request the request to execute
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           query
   * @throws SQLException if an error occurs
   * @return auto-generated keys
   */
  public ControllerResultSet loadBalanceExecWriteRequestWithKeys(
      AbstractWriteRequest request) throws AllBackendsFailedException,
      SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("requestmanager.write.request.balance",
          new String[]{String.valueOf(request.getId()),
              request.getSQLShortForm(vdb.getSQLShortFormLength())}));

    try
    { // Send the request to the load balancer
      return loadBalancer.execWriteRequestWithKeys(request, metadataCache);
    }
    catch (Exception failed)
    {
      scheduler.writeCompleted(request);
      String msg = Translate.get("requestmanager.request.failed", new String[]{
          request.getSQLShortForm(vdb.getSQLShortFormLength()),
          failed.getMessage()});
      if (failed instanceof RuntimeException)
        logger.warn(msg, failed);
      else
        logger.warn(msg);
      if (failed instanceof AllBackendsFailedException)
        throw (AllBackendsFailedException) failed;
      else
        throw new SQLException(msg);
    }
  }

  /**
   * Send the given query to the load balancer. If the request fails, the
   * scheduler is properly notified.
   * 
   * @param request the request to execute
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           query
   * @throws SQLException if an error occurs
   * @return number of modified lines
   */
  public int loadBalanceExecWriteRequest(AbstractWriteRequest request)
      throws AllBackendsFailedException, SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("requestmanager.write.request.balance",
          new String[]{String.valueOf(request.getId()),
              request.getSQLShortForm(vdb.getSQLShortFormLength())}));

    try
    { // Send the request to the load balancer
      if (request.isUpdate() && (resultCache != null))
      { // Try the optimization if we try to update values that are already
        // up-to-date.
        if (!resultCache.isUpdateNecessary((UpdateRequest) request))
          return 0;
      }
      return loadBalancer.execWriteRequest(request);
    }
    catch (Exception failed)
    {
      scheduler.writeCompleted(request);
      String msg = Translate.get("requestmanager.request.failed", new String[]{
          request.getSQLShortForm(vdb.getSQLShortFormLength()),
          failed.getMessage()});

      // Logging
      if (failed instanceof RuntimeException)
        logger.warn(msg, failed);
      else
        logger.warn(msg);

      // Rethrow exception
      if (failed instanceof AllBackendsFailedException)
        throw (AllBackendsFailedException) failed;
      else if (failed instanceof SQLException)
        throw (SQLException) failed;
      else
        throw new SQLException(msg);
    }
  }

  /**
   * Update the cache, notify the recovery log, update the database schema if
   * needed and finally notify the scheduler. Note that if an error occurs, the
   * scheduler is always notified.
   * 
   * @param request the request to execute
   * @throws SQLException if an error occurs
   */
  public void updateAndNotifyExecWriteRequest(AbstractWriteRequest request)
      throws SQLException
  {
    try
    { // Notify cache if any
      if (resultCache != null)
      { // Update cache
        if (logger.isDebugEnabled())
          logger.debug(Translate.get(
              "requestmanager.write.request.cache.update", new String[]{
                  String.valueOf(request.getId()),
                  request.getSQLShortForm(vdb.getSQLShortFormLength())}));

        resultCache.writeNotify(request);
      }

      if (metadataCache != null)
      {
        if (request.isAlter() || request.isDrop())
          // Note that we also have to flush on Drop in case the same table is
          // re-created with another schema.
          metadataCache.flushCache();
      }

      // Log the request
      if (recoveryLog != null)
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("requestmanager.write.request.log",
              new String[]{String.valueOf(request.getId()),
                  request.getSQLShortForm(vdb.getSQLShortFormLength())}));

        recoveryLog.logRequest(request);
      }

      // Update the schema if needed
      if ((request.isDDL())
          && (requiredParsingGranularity != ParsingGranularities.NO_PARSING))
      {
        if (request.isCreate())
        { // Add the table to the schema
          CreateRequest createRequest = (CreateRequest) request;
          if (createRequest.altersDatabaseSchema())
          {
            if (createRequest.getDatabaseTable() != null)
            {
              getDatabaseSchema().addTable(
                  ((CreateRequest) request).getDatabaseTable());
              if (logger.isDebugEnabled())
                logger.debug(Translate.get("requestmanager.schema.add.table",
                    request.getTableName()));
            }
            else
              // Some other create statement that modifies the schema, force
              // refresh
              schemaIsDirty = true;
          }
        }
        else if (request.isDrop())
        { // Delete the table from the schema
          if (getDatabaseSchema().removeTable(
              getDatabaseSchema().getTable(request.getTableName())))
            if (logger.isDebugEnabled())
              logger.debug(Translate.get("requestmanager.schema.remove.table",
                  request.getTableName()));
            else
              // Table not found, force refresh
              schemaIsDirty = true;
        }
        else if (request.isAlter()
            && (requiredParsingGranularity > ParsingGranularities.TABLE))
        { // Add or drop the column from the table
          AlterRequest req = (AlterRequest) request;
          DatabaseTable alteredTable = getDatabaseSchema().getTable(
              req.getTableName());
          if ((alteredTable != null) && (req.getColumn() != null))
          {
            if (req.isDrop())
              alteredTable.remove(req.getColumn().getName());
            else if (req.isAdd())
              alteredTable.addColumn(req.getColumn());
            else
              // Unsupported, force refresh
              schemaIsDirty = true;
          }
          else
            // Table not found, force refresh
            schemaIsDirty = true;
        }
        else
          // Unsupported, force refresh
          schemaIsDirty = true;
      }
    }
    catch (Exception failed)
    {
      scheduler.writeCompleted(request);
      String msg = Translate.get("requestmanager.request.failed", new String[]{
          request.getSQLShortForm(vdb.getSQLShortFormLength()),
          failed.getMessage()});
      if (failed instanceof RuntimeException)
        logger.warn(msg, failed);
      else
        logger.warn(msg);
      throw new SQLException(msg);
    }
    finally
    {
      // Notify scheduler
      scheduler.writeCompleted(request);
    }
  }

  /**
   * Call a stored procedure that returns a ResultSet.
   * 
   * @param proc the stored procedure call
   * @return a <code>ControllerResultSet</code> value
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           stored procedure
   * @exception SQLException if an error occurs
   */
  public ControllerResultSet execReadStoredProcedure(StoredProcedure proc)
      throws AllBackendsFailedException, SQLException
  {
    ControllerResultSet result = null;
    try
    {
      scheduleStoredProcedure(proc);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("requestmanager.read.stored.procedure",
            new String[]{String.valueOf(proc.getId()),
                proc.getSQLShortForm(vdb.getSQLShortFormLength())}));

      result = loadBalanceReadStoredProcedure(proc);

      flushCacheAndLogStoredProcedure(proc, true);

      // Notify scheduler of completion
      scheduler.storedProcedureCompleted(proc);

      return result;
    }
    catch (AllBackendsFailedException e)
    {
      throw e;
    }
    catch (Exception failed)
    {
      scheduler.storedProcedureCompleted(proc);
      String msg = Translate.get("requestmanager.stored.procedure.failed",
          new String[]{proc.getSQLShortForm(vdb.getSQLShortFormLength()),
              failed.getMessage()});
      logger.warn(msg);
      if (failed instanceof SQLException)
      {
        throw (SQLException) failed;
      }
      throw new SQLException(msg);
    }
  }

  /**
   * Call a stored procedure that performs an update.
   * 
   * @param proc the stored procedure call
   * @return number of rows affected
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           stored procedure
   * @exception SQLException if an error occurs
   */
  public int execWriteStoredProcedure(StoredProcedure proc)
      throws AllBackendsFailedException, SQLException
  {
    int result;
    try
    {
      // Wait for the scheduler to give us the authorization to execute
      scheduleStoredProcedure(proc);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("requestmanager.write.stored.procedure",
            new String[]{String.valueOf(proc.getId()),
                proc.getSQLShortForm(vdb.getSQLShortFormLength())}));

      result = loadBalanceWriteStoredProcedure(proc);

      flushCacheAndLogStoredProcedure(proc, false);

      // Notify scheduler of completion
      scheduler.storedProcedureCompleted(proc);

      return result;
    }
    catch (AllBackendsFailedException e)
    {
      throw e;
    }
    catch (Exception failed)
    {
      scheduler.storedProcedureCompleted(proc);
      String msg = Translate.get("requestmanager.stored.procedure.failed",
          new String[]{proc.getSQLShortForm(vdb.getSQLShortFormLength()),
              failed.getMessage()});
      logger.warn(msg);
      throw new SQLException(msg);
    }
  }

  /**
   * This method does some sanity check on the given stored procedure and then
   * tries to schedule it. Note that it is more likely that on a stored
   * procedure the scheduler will lock in write the entire database as it does
   * not know which tables are accessed by the procedure.
   * 
   * @param proc the stored procedure to schedule
   * @throws SQLException if an error occurs
   */
  public void scheduleStoredProcedure(StoredProcedure proc) throws SQLException
  {
    // Sanity check
    if (!proc.isAutoCommit())
    { // Check that the transaction has been
      // started
      long tid = proc.getTransactionId();
      if (!tidLoginTable.containsKey(new Long(tid)))
        throw new SQLException(Translate.get("transaction.not.started", tid));
    }

    //
    // SCHEDULER
    //

    // Wait for the scheduler to give us the authorization to execute
    try
    {
      scheduler.scheduleStoredProcedure(proc);
    }
    catch (RollbackException e)
    { // Something bad happened and we need to rollback this transaction
      rollback(proc.getTransactionId(), true);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Execute a read stored procedure on the load balancer. Note that we flush
   * the cache before calling the load balancer.
   * 
   * @param proc the stored procedure to call
   * @return the corresponding ControllerResultSet
   * @throws SQLException if an error occurs
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           stored procedure
   */
  public ControllerResultSet loadBalanceReadStoredProcedure(StoredProcedure proc)
      throws SQLException, AllBackendsFailedException
  {
    ControllerResultSet result;
    //
    // CACHE
    //

    // Cache is always flushed unless the user has explicitely set the
    // connection to read-only mode in which case we assume that the
    // users deliberately forces the cache not to be flushed when calling
    // this stored procedure.
    if ((resultCache != null) && (!proc.isReadOnly()))
      resultCache.flushCache();

    //
    // LOAD BALANCER
    //

    // Send the request to the load balancer
    if (proc.isReadOnly())
      result = loadBalancer
          .execReadOnlyReadStoredProcedure(proc, metadataCache);
    else
      result = loadBalancer.execReadStoredProcedure(proc, metadataCache);
    return result;
  }

  /**
   * Execute a write stored procedure on the load balancer. Note that we flush
   * the cache before calling the load balancer.
   * 
   * @param proc the stored procedure to call
   * @return the number of updated rows
   * @throws SQLException if an error occurs
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           stored procedure
   */
  public int loadBalanceWriteStoredProcedure(StoredProcedure proc)
      throws AllBackendsFailedException, SQLException
  {
    int result;
    //
    // CACHE
    //

    // Flush cache (if any) before as we don't properly lock the tables
    if (resultCache != null)
      resultCache.flushCache();

    //
    // LOAD BALANCER
    //

    // Send the request to the load balancer
    result = loadBalancer.execWriteStoredProcedure(proc);
    return result;
  }

  /**
   * Flush the cache and log the stored procedure execution in the recovery log.
   * 
   * @param proc the stored procedure to log
   * @param isRead true is this is an execReadStoredProcedure, false for an
   *          execWriteStoredProcedure
   */
  public void flushCacheAndLogStoredProcedure(StoredProcedure proc,
      boolean isRead)
  {
    // Schema might have been updated, force refresh
    schemaIsDirty = true;

    //
    // CACHE
    //

    // Flush cache (if any) after for consistency (we don't know what has been
    // modified by the stored procedure)
    if (resultCache != null)
      resultCache.flushCache();

    if (metadataCache != null)
      metadataCache.flushCache();

    //
    // RECOVERY LOG
    //

    if (recoveryLog != null)
      recoveryLog.logRequest(proc, isRead);
  }

  //
  // Transaction management
  //

  /**
   * Begin a new transaction and return the corresponding transaction
   * identifier. This method is called from the driver when setAutoCommit(false)
   * is called.
   * <p>
   * Note that the transaction begin is not logged in the recovery log by this
   * method, you will have to call logLazyTransactionBegin.
   * 
   * @param login the login used by the connection
   * @return int a unique transaction identifier
   * @throws SQLException if an error occurs
   * @see #logLazyTransactionBegin(long)
   */
  public long begin(String login) throws SQLException
  {
    try
    {
      TransactionMarkerMetaData tm = new TransactionMarkerMetaData(0,
          beginTimeout, login);

      // Wait for the scheduler to give us the authorization to execute
      long tid = scheduler.begin(tm);
      tm.setTransactionId(tid);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.begin", String.valueOf(tid)));

      try
      {
        // Send to load balancer
        loadBalancer.begin(tm);
      }
      catch (SQLException e)
      {
        throw e;
      }
      finally
      {
        // Notify scheduler for completion in any case
        scheduler.beginCompleted(tid);
      }

      tidLoginTable.put(new Long(tid), tm);
      return tid;
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.begin"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Log the begin of a transaction that is started lazily. In fact, we just log
   * the begin when we execute the first write request in a transaction to
   * prevent logging begin/commit for read-only transactions. This also prevents
   * a problem with backends that are disabled with a checkpoint when no request
   * has been played in the transaction but the begin statement has already been
   * logged. In that case, the transaction would not be properly replayed at
   * restore time.
   * 
   * @param transactionId the transaction id begin to log
   * @throws SQLException if an error occurs
   */
  public void logLazyTransactionBegin(long transactionId) throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.begin.log", String
            .valueOf(transactionId)));

      // Log the begin
      if (recoveryLog != null)
        recoveryLog.logBegin(tm);
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.begin.log"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Abort a transaction that has been started but in which no query was
   * executed. As we use lazy transaction begin, there is no need to rollback
   * such transaction but just to cleanup the metadata associated with this not
   * effectively started transaction.
   * 
   * @param transactionId id of the transaction to abort
   * @param logAbort true if the abort (in fact rollback) should be logged in
   *          the recovery log
   * @throws SQLException if an error occurs
   */
  public void abort(long transactionId, boolean logAbort) throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.abort", String
            .valueOf(transactionId)));

      try
      {
        // Notify the scheduler to abort which is the same as a rollback
        // from a
        // scheduler point of view.
        scheduler.rollback(tm);

        // Notify the recovery log manager
        if (logAbort && (recoveryLog != null))
        {
          recoveryLog.logAbort(tm);
        }
      }
      catch (SQLException e)
      {
        throw e;
      }
      finally
      {
        // Notify scheduler for completion
        scheduler.rollbackCompleted(transactionId);

        // Scheduler will add the rollback in the total order queue if any, so
        // we have to remove it from the queue to prevent blocking subsequent
        // queries.
        loadBalancer.removeHeadFromAndNotifyTotalOrderQueue();

        completeTransaction(tid);
      }
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.abort"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Get the TransactionMarkerMetaData for the given transaction id.
   * 
   * @param tid transaction id
   * @return the TransactionMarkerMetaData
   * @throws SQLException if no marker has been found for this transaction
   */
  public TransactionMarkerMetaData getTransactionMarker(Long tid)
      throws SQLException
  {
    TransactionMarkerMetaData tm = (TransactionMarkerMetaData) tidLoginTable
        .get(tid);

    if (tm == null)
      throw new SQLException(Translate.get("transaction.marker.not.found", ""
          + tid));

    tm.setTimeout(commitTimeout);
    return tm;
  }

  /**
   * Complete the transaction by removing it from the tidLoginTable.
   * 
   * @param tid transaction id
   */
  public void completeTransaction(Long tid)
  {
    tidLoginTable.remove(tid);
    tidSavepoints.remove(tid);
  }

  /**
   * Commit a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param logCommit true if the commit should be logged in the recovery log
   * @throws SQLException if an error occurs
   */
  public void commit(long transactionId, boolean logCommit) throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      // Wait for the scheduler to give us the authorization to execute
      scheduler.commit(tm);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.commit", String.valueOf(tid)));

      try
      {
        // Send to load balancer
        loadBalancer.commit(tm);

        // Notify the cache
        if (resultCache != null)
          resultCache.commit(tm.getTransactionId());

        // Notify the recovery log manager
        if (logCommit && (recoveryLog != null))
          recoveryLog.logCommit(tm);
      }
      catch (SQLException e)
      {
        throw e;
      }
      catch (AllBackendsFailedException e)
      {
        String msg = "All backends failed to commit transaction "
            + transactionId + " (" + e + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }
      finally
      {
        // Notify scheduler for completion
        scheduler.commitCompleted(transactionId);

        completeTransaction(tid);
      }
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.commit"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Rollback a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param logRollback true if the rollback should be logged in the recovery
   *          log
   * @throws SQLException if an error occurs
   */
  public void rollback(long transactionId, boolean logRollback)
      throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      // Wait for the scheduler to give us the authorization to execute
      scheduler.rollback(tm);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.rollback", String
            .valueOf(transactionId)));

      try
      {
        // Send to load balancer
        loadBalancer.rollback(tm);

        // Send to cache
        if (this.resultCache != null)
          resultCache.rollback(transactionId);

        // Notify the recovery log manager
        if (logRollback && (recoveryLog != null))
        {
          recoveryLog.logRollback(tm);
        }
      }
      catch (SQLException e)
      {
        throw e;
      }
      catch (AllBackendsFailedException e)
      {
        String msg = Translate.get("requestmanager.rollback.failed.all",
            new String[]{String.valueOf(transactionId), e.getMessage()});
        logger.error(msg);
        throw new SQLException(msg);
      }
      finally
      {
        // Notify scheduler for completion
        scheduler.rollbackCompleted(transactionId);

        completeTransaction(tid);
      }
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.rollback"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Rollback a transaction given its id to a savepoint given its name.
   * 
   * @param transactionId the transaction id
   * @param savepointName the name of the savepoint
   * @throws SQLException if an error occurs
   */
  public void rollback(long transactionId, String savepointName)
      throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      // Check that a savepoint with given name has been set
      if (!hasSavepoint(tid, savepointName))
        throw new SQLException(Translate.get("transaction.savepoint.not.found",
            new String[]{savepointName, String.valueOf(transactionId)}));

      // Wait for the scheduler to give us the authorization to execute
      scheduler.rollback(tm, savepointName);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.rollbacksavepoint",
            new String[]{String.valueOf(transactionId), savepointName}));
      try
      {
        // Send to loadbalancer
        loadBalancer.rollback(tm, savepointName);

        // Notify the recovery log manager
        if (recoveryLog != null)
        {
          recoveryLog.logRollback(tm, savepointName);
        }
      }
      catch (AllBackendsFailedException e)
      {
        String msg = Translate.get(
            "requestmanager.rollbackavepoint.failed.all", new String[]{
                String.valueOf(transactionId), savepointName, e.getMessage()});
        logger.error(msg);
        throw new SQLException(msg);
      }
      finally
      {
        // Notify scheduler for completion
        scheduler.savepointCompleted(transactionId);

        // Remove all the savepoints set after the savepoint we rollback to
        removeSavepoints(tid, savepointName);
      }
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.rollbacksavepoint"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Sets a unnamed savepoint to a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @return the generated id of the new savepoint
   * @throws SQLException if an error occurs
   */
  public int setSavepoint(long transactionId) throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      // Wait for the scheduler to give us the authorization to execute
      int savepointId = scheduler.setSavepoint(tm);
      String savepointName = String.valueOf(savepointId);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.setsavepoint", new String[]{
            savepointName, String.valueOf(transactionId)}));

      try
      {
        // Send to loadbalancer
        loadBalancer.setSavepoint(tm, savepointName);

        // Notify the recovery log manager
        if (recoveryLog != null)
        {
          recoveryLog.logSetSavepoint(tm, savepointName);
        }
      }
      catch (AllBackendsFailedException e)
      {
        String msg = Translate.get("requestmanager.setsavepoint.failed.all",
            new String[]{savepointName, String.valueOf(transactionId),
                e.getMessage()});
        logger.error(msg);
        throw new SQLException(msg);
      }
      finally
      {
        // Notify scheduler for completion
        scheduler.savepointCompleted(transactionId);
      }

      // Add savepoint name to list of savepoints for this transaction
      addSavepoint(tid, savepointName);
      return savepointId;
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.setsavepoint"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Sets a savepoint given its desired name to a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param name the desired name of the savepoint
   * @throws SQLException if an error occurs
   */
  public void setSavepoint(long transactionId, String name) throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      // Wait for the scheduler to give us the authorization to execute
      scheduler.setSavepoint(tm, name);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.setsavepoint", new String[]{
            name, String.valueOf(transactionId)}));

      try
      {
        // Send to loadbalancer
        loadBalancer.setSavepoint(tm, name);

        // Notify the recovery log manager
        if (recoveryLog != null)
        {
          recoveryLog.logSetSavepoint(tm, name);
        }
      }
      catch (AllBackendsFailedException e)
      {
        String msg = Translate.get("requestmanager.setsavepoint.failed.all",
            new String[]{name, String.valueOf(transactionId), e.getMessage()});
        logger.error(msg);
        throw new SQLException(msg);
      }
      finally
      {
        // Notify scheduler for completion
        scheduler.savepointCompleted(transactionId);
      }

      // Add savepoint name to list of savepoints for this transaction
      addSavepoint(tid, name);
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.setsavepoint"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Releases a savepoint given its name from a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param name the name of the savepoint
   * @exception SQLException if an error occurs
   */
  public void releaseSavepoint(long transactionId, String name)
      throws SQLException
  {
    try
    {
      Long tid = new Long(transactionId);
      TransactionMarkerMetaData tm = getTransactionMarker(tid);

      // Check that a savepoint with given name has been set
      if (!hasSavepoint(tid, name))
        throw new SQLException(Translate.get("transaction.savepoint.not.found",
            new String[]{name, String.valueOf(transactionId)}));

      // Wait for the scheduler to give us the authorization to execute
      scheduler.releaseSavepoint(tm, name);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.releasesavepoint",
            new String[]{name, String.valueOf(transactionId)}));

      try
      {
        // Send to loadbalancer
        loadBalancer.releaseSavepoint(tm, name);

        // Notify the recovery log manager
        if (recoveryLog != null)
        {
          recoveryLog.logReleaseSavepoint(tm, name);
        }
      }
      catch (AllBackendsFailedException e)
      {
        String msg = Translate.get(
            "requestmanager.releasesavepoint.failed.all", new String[]{name,
                String.valueOf(transactionId), e.getMessage()});
        logger.error(msg);
        throw new SQLException(msg);
      }
      finally
      {
        // Notify scheduler for completion
        scheduler.savepointCompleted(transactionId);

        // Remove savepoint for the transaction
        removeSavepoint(tid, name);
      }
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate
          .get("fatal.runtime.exception.requestmanager.releasesavepoint"), e);
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Adds a given savepoint to a given transaction
   * 
   * @param tid transaction id
   * @param savepointName name of the savepoint
   */
  public void addSavepoint(Long tid, String savepointName)
  {
    LinkedList savepoints = (LinkedList) tidSavepoints.get(tid);
    if (savepoints == null)
    { // Lazy list creation
      savepoints = new LinkedList();
      tidSavepoints.put(tid, savepoints);
    }

    savepoints.addLast(savepointName);
  }

  /**
   * Removes a given savepoint for a given transaction
   * 
   * @param tid transaction id
   * @param savepointName name of the savepoint
   */
  public void removeSavepoint(Long tid, String savepointName)
  {
    LinkedList savepoints = (LinkedList) tidSavepoints.get(tid);
    if (savepoints == null)
      logger.error("No savepoints found for transaction " + tid);
    else
      savepoints.remove(savepointName);
  }

  /**
   * Removes all the savepoints set after a given savepoint for a given
   * transaction
   * 
   * @param tid transaction id
   * @param savepointName name of the savepoint
   */
  public void removeSavepoints(Long tid, String savepointName)
  {
    LinkedList savepoints = (LinkedList) tidSavepoints.get(tid);
    if (savepoints == null)
    {
      logger.error("No savepoints found for transaction " + tid);
      return;
    }

    int index = savepoints.indexOf(savepointName);
    if (index == -1)
      logger.error("No savepoint with name " + savepointName + " found "
          + "for transaction " + tid);
    else
      savepoints.subList(index, savepoints.size()).clear();
  }

  /**
   * Check if a given savepoint has been set for a given transaction
   * 
   * @param tid transaction id
   * @param savepointName name of the savepoint
   * @return true if the savepoint exists
   */
  public boolean hasSavepoint(Long tid, String savepointName)
  {
    LinkedList savepoints = (LinkedList) tidSavepoints.get(tid);
    if (savepoints == null)
      return false;

    return savepoints.contains(savepointName);
  }

  //
  // Database Backends management
  //

  /**
   * Enable a backend that has been previously added to this virtual database
   * and that is in the disabled state.
   * <p>
   * The backend is enabled without further check.
   * <p>
   * The enableBackend method of the load balancer is called.
   * 
   * @param db The database backend to enable
   * @throws SQLException if an error occurs
   */
  public void enableBackend(DatabaseBackend db) throws SQLException
  {
    db
        .setSchemaIsNeededByVdb(requiredParsingGranularity != ParsingGranularities.NO_PARSING);
    loadBalancer.enableBackend(db, true);
    logger.info(Translate.get("backend.state.enabled", db.getName()));
  }

  /**
   * The backend must have been previously added to this virtual database and be
   * in the disabled state.
   * <p>
   * All the queries since the given checkpoint are played and the backend state
   * is set to enabled when it is completely synchronized.
   * <p>
   * Note that the job is performed in background by a
   * <code>RecoverThread</code>. You can synchronize on thread termination if
   * you need to wait for completion of this task and listen to JMX
   * notifications for the success status.
   * 
   * @param db The database backend to enable
   * @param checkpointName The checkpoint name to restart from
   * @return the JDBC reocver thread synchronizing the backend
   * @throws SQLException if an error occurs
   */
  public RecoverThread enableBackendFromCheckpoint(DatabaseBackend db,
      String checkpointName) throws SQLException
  {
    // Sanity checks
    if (recoveryLog == null)
    {
      String msg = Translate.get(
          "recovery.restore.checkpoint.failed.cause.null", checkpointName);
      logger.error(msg);
      throw new SQLException(msg);
    }

    if (db.getStateValue() != BackendState.DISABLED)
      throw new SQLException(Translate.get(
          "recovery.restore.backend.state.invalid", db.getName()));

    db
        .setSchemaIsNeededByVdb(requiredParsingGranularity != ParsingGranularities.NO_PARSING);

    RecoverThread recoverThread = new RecoverThread(scheduler, recoveryLog, db,
        loadBalancer, checkpointName);

    // fire the thread and forget
    // exception will be reported in a jmx notification on the backend.
    recoverThread.start();
    return recoverThread;
  }

  /**
   * Disable a backend that is currently enabled on this virtual database.
   * <p>
   * The backend is disabled without further check.
   * <p>
   * The load balancer disabled method is called on the specified backend.
   * 
   * @param db The database backend to disable
   * @throws SQLException if an error occurs
   */
  public void disableBackend(DatabaseBackend db) throws SQLException
  {
    if (db.isReadEnabled() || db.isWriteEnabled())
    {
      loadBalancer.disableBackend(db);
      logger.info(Translate.get("backend.state.disabled", db.getName()));
    }
    else
    {
      throw new SQLException(Translate.get("backend.already.disabled", db
          .getName()));
    }
  }

  /**
   * The backend must belong to this virtual database and be in the enabled
   * state.
   * <p>
   * The backend is disabled once all the pending write queries are executed. A
   * checkpoint is inserted in the recovery log.
   * 
   * @param db The database backend to enable
   * @param checkpointName The checkpoint name to restart from
   * @throws SQLException if an error occurs
   */
  public void disableBackendForCheckpoint(DatabaseBackend db,
      String checkpointName) throws SQLException
  {
    // Sanity checks
    if (recoveryLog == null)
    {
      String msg = Translate.get("recovery.store.checkpoint.failed.cause.null",
          checkpointName);
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Wait for all pending writes to finish
    logger.info(Translate.get("requestmanager.wait.pending.writes"));
    scheduler.suspendWrites();

    // Store checkpoint
    recoveryLog.storeCheckpoint(checkpointName);
    logger.info(Translate.get("recovery.checkpoint.stored", checkpointName));

    // Signal the backend should not begin any new transaction
    db.setState(BackendState.DISABLING);
    logger.info(Translate.get("backend.state.disabling", db.getName()));

    // Resume writes
    logger.info(Translate.get("requestmanager.resume.pending.writes"));
    scheduler.resumeWrites();

    // Wait for all current transactions on the backend to finish
    db.waitForAllTransactionsToComplete();

    // Now we can safely disable the backend
    db.setLastKnownCheckpoint(checkpointName);
    loadBalancer.disableBackend(db);
    logger.info(Translate.get("backend.state.disabled", db.getName()));
  }

  /**
   * Disable a list of backends. Only to store only one checkpoint, and to
   * disable all the backends at the same time so the the system is in a
   * coherent state. Consider only the backends that were enabled. The others
   * are left in the state they were before.
   * 
   * @param backendsArrayList backends to disable
   * @param checkpointName to store
   * @throws SQLException if an error occurs
   */
  public void disableBackendsForCheckpoint(ArrayList backendsArrayList,
      String checkpointName) throws SQLException
  {
    // Sanity checks
    if (recoveryLog == null)
    {
      String msg = Translate.get("recovery.store.checkpoint.failed.cause.null",
          checkpointName);
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Wait for all pending writes to finish
    logger.info(Translate.get("requestmanager.wait.pending.writes"));
    scheduler.suspendWrites();

    // Store checkpoint
    recoveryLog.storeCheckpoint(checkpointName);
    logger.info(Translate.get("recovery.checkpoint.stored", checkpointName));

    // Copy the list and consider only the backends that are enabled
    DatabaseBackend db;
    ArrayList backendList = (ArrayList) backendsArrayList.clone();
    for (int i = 0; i < backendList.size(); i++)
    {
      db = (DatabaseBackend) backendList.get(i);
      if (!db.isWriteEnabled())
        backendList.remove(i);
    }

    // Signal all backends that they should not begin any new transaction
    int size = backendList.size();
    for (int i = 0; i < size; i++)
    {
      db = (DatabaseBackend) backendList.get(i);
      db.setState(BackendState.DISABLING);
      logger.info(Translate.get("backend.state.disabling", db.getName()));
    }

    // Resume writes
    logger.info(Translate.get("requestmanager.resume.pending.writes"));
    scheduler.resumeWrites();

    // Wait for all current transactions on backends to finish
    for (int i = 0; i < size; i++)
    {
      db = (DatabaseBackend) backendList.get(i);
      db.waitForAllTransactionsToComplete();
    }

    // Now we can safely disable all backends
    for (int i = 0; i < size; i++)
    {
      db = (DatabaseBackend) backendList.get(i);
      db.setLastKnownCheckpoint(checkpointName);
      loadBalancer.disableBackend(db);
      logger.info(Translate.get("backend.state.disabled", db.getName()));
    }
  }

  /**
   * Create a backup from the content of a backend.
   * 
   * @param backend the target backend to backup
   * @param login the login to use to connect to the database for the backup
   *          operation
   * @param password the password to use to connect to the database for the
   *          backup operation
   * @param dumpName the name of the dump to create
   * @param backuperName the logical name of the backuper to use
   * @param path the path where to store the dump
   * @param tables the list of tables to backup, null means all tables
   * @throws SQLException if the backup fails
   */
  public void backupBackend(DatabaseBackend backend, String login,
      String password, String dumpName, String backuperName, String path,
      ArrayList tables) throws SQLException
  {
    Backuper backuper = backupManager.getBackuperByName(backuperName);
    if (backuper == null)
      throw new SQLException("No backuper named " + backuperName
          + " was found.");

    boolean enableAfter = false;
    String checkpointName = null;
    if (backend.isReadEnabled())
    { // Disable backend and store checkpoint
      checkpointName = "cp_for_" + dumpName + "_"
          + new Date(System.currentTimeMillis()).toString();
      disableBackendForCheckpoint(backend, checkpointName);
      logger.info(Translate.get("backend.state.disabled", backend.getName()));
      enableAfter = true;
    }
    else
    {
      if (backend.getLastKnownCheckpoint() == null)
      {
        throw new SQLException(Translate.get(
            "controller.backup.no.lastknown.checkpoint", backend.getName()));
      }
    }
    // else backend is already disabled, no checkpoint is stored here, it should
    // have been done at disable time.

    try
    {
      logger.info(Translate.get("controller.backup.start", backend.getName()));

      // Sanity check to be sure that no pending request is in the pipe for the
      // backend
      Vector pending = backend.getPendingRequests();
      if (pending.size() != 0)
      {
        if (logger.isDebugEnabled())
        {
          for (int i = 0; i < pending.size(); i++)
            logger.debug("Pending:" + pending.get(i).toString());

          logger.debug("Pending Requests:"
              + backend.getPendingRequests().size());
          logger.debug("Read enabled:" + backend.isReadEnabled());
          logger.debug("Write enabled:" + backend.isWriteEnabled());
        }
        throw new BackupException(Translate.get("backend.not.ready.for.backup"));
      }

      // Let's start the backup
      backend.setState(BackendState.BACKUPING);
      Date dumpDate = backuper.backup(backend, login, password, dumpName, path,
          tables);
      if (recoveryLog != null)
      {
        DumpInfo dump = new DumpInfo(dumpName, dumpDate.toString(), path,
            backuper.getDumpFormat(), backend.getLastKnownCheckpoint(), backend
                .getName(), "*");
        recoveryLog.storeDump(dump);
      }

      // Notify that a new dump is available
      backend.notifyJmx(CjdbcNotificationList.VIRTUALDATABASE_NEW_DUMP_LIST);

      // Swtich from BACKUPING to DISABLED STATE
      backend.setState(BackendState.DISABLED);
    }
    catch (BackupException be)
    {
      // Backend is now in an unknown state
      backend.setState(BackendState.UNKNOWN);
      logger.error(Translate.get("controller.backup.failed"), be);
      throw new SQLException(be.getMessage());
    }

    logger.info(Translate.get("controller.backup.complete", backend.getName()));

    if (enableAfter)
    {
      RecoverThread thread = enableBackendFromCheckpoint(backend,
          checkpointName);
      try
      {
        thread.join();
      }
      catch (InterruptedException e)
      {
        logger.error("Recovery thread has been interrupted", e);
      }
    }

  }

  /**
   * Restore a dump on a specific backend. The proper Backuper is retrieved
   * automatically according to the dump format stored in the recovery log dump
   * table.
   * <p>
   * This method disables the backend and leave it disabled after recovery
   * process. The user has to call the <code>enableBackendFromCheckpoint</code>
   * after this.
   * 
   * @param backend the backend to restore
   * @param login the login to use to connect to the database for the restore
   *          operation
   * @param password the password to use to connect to the database for the
   *          restore operation
   * @param dumpName the name of the dump to restore
   * @param tables the list of tables to restore, null means all tables
   * @throws BackupException if the restore operation failed
   */
  public void restoreBackendFromBackupCheckpoint(DatabaseBackend backend,
      String login, String password, String dumpName, ArrayList tables)
      throws BackupException
  {
    DumpInfo dumpInfo;
    try
    {
      dumpInfo = recoveryLog.getDumpInfo(dumpName);
    }
    catch (SQLException e)
    {
      throw new BackupException(
          "Recovery log error access occured while retrieving information for dump "
              + dumpName, e);
    }
    if (dumpInfo == null)
      throw new BackupException(
          "No information was found in the dump table for dump " + dumpName);

    Backuper backuper = backupManager.getBackuperByFormat(dumpInfo
        .getDumpFormat());
    if (backuper == null)
      throw new BackupException("No backuper was found to handle dump format "
          + dumpInfo.getDumpFormat());

    try
    {
      // no check for disable as we are going to overwrite
      // all the database data
      if (backend.isReadEnabled())
        loadBalancer.disableBackend(backend);

      backend.setState(BackendState.RECOVERING);

      backuper.restore(backend, login, password, dumpName, dumpInfo
          .getDumpPath(), tables);

      // Set the checkpoint name corresponding to this database dump
      backend.setLastKnownCheckpoint(dumpInfo.getCheckpointName());
      backend.setState(BackendState.DISABLED);
    }
    catch (SQLException e1)
    {
      // This comes from the loadbalancer
      backend.setState(BackendState.UNKNOWN);
      throw new BackupException("Backend cannot be enabled", e1);
    }
    catch (BackupException be)
    {
      backend.setState(BackendState.UNKNOWN);
      logger.error(Translate.get("controller.backup.recovery.failed"), be);
      throw be;
    }
    finally
    {
      logger.info(Translate.get("controller.backup.recovery.done", backend
          .getName()));
    }
  }

  /**
   * Store all the backends checkpoint in the recoverylog
   * 
   * @param databaseName the virtual database name
   * @param backends the <code>Arraylist</code> of backends
   */
  public void storeBackendsInfo(String databaseName, ArrayList backends)
  {
    if (recoveryLog == null)
      return;
    int size = backends.size();
    DatabaseBackend backend;
    for (int i = 0; i < size; i++)
    {
      backend = (DatabaseBackend) backends.get(i);
      try
      {
        recoveryLog.storeBackendRecoveryInfo(databaseName,
            new BackendRecoveryInfo(backend.getName(), backend
                .getLastKnownCheckpoint(), backend.getStateValue(),
                databaseName));
      }
      catch (SQLException e)
      {
        logger.error(Translate.get("recovery.store.checkpoint.failed",
            new String[]{backend.getName(), e.getMessage()}), e);
      }
    }
  }

  /**
   * Remove a checkpoint and corresponding entries from the log table
   * 
   * @param checkpointName to remove
   * @throws SQLException if fails
   */
  public void removeCheckpoint(String checkpointName) throws SQLException
  {
    recoveryLog.removeCheckpoint(checkpointName);
  }

  //
  // Database schema management
  //

  /**
   * Get the <code>DatabaseSchema</code> used by this Request Manager.
   * 
   * @return a <code>DatabaseSchema</code> value
   */
  public synchronized DatabaseSchema getDatabaseSchema()
  {
    try
    {
      // Refresh schema if needed. Note that this will break static schemas if
      // any
      if (schemaIsDirty)
      {
        dbs = vdb.getDatabaseSchemaFromActiveBackends();
        schemaIsDirty = false;
      }
    }
    catch (SQLException e)
    {
      logger.error("Unable to refresh schema", e);
    }
    return dbs;
  }

  /**
   * Merge the given schema with the existing database schema.
   * 
   * @param backendSchema The virtual database schema to merge.
   */
  public synchronized void mergeDatabaseSchema(DatabaseSchema backendSchema)
  {
    try
    {
      if (dbs == null)
        setDatabaseSchema(new DatabaseSchema(backendSchema), false);
      else
      {
        dbs.mergeSchema(backendSchema);
        logger.info(Translate
            .get("requestmanager.schema.virtualdatabase.merged.new"));

        if (schedulerParsingranularity != ParsingGranularities.NO_PARSING)
          scheduler.mergeDatabaseSchema(dbs);

        if (cacheParsingranularity != ParsingGranularities.NO_PARSING)
          resultCache.mergeDatabaseSchema(dbs);
      }
    }
    catch (SQLException e)
    {
      logger.error(Translate.get("requestmanager.schema.merge.failed", e
          .getMessage()), e);
    }
  }

  /**
   * Sets the <code>DatabaseSchema</code> to be able to parse the requests and
   * find dependencies.
   * 
   * @param schema a <code>DatabaseSchema</code> value
   * @param isStatic true if the given schema is static
   */
  public synchronized void setDatabaseSchema(DatabaseSchema schema,
      boolean isStatic)
  {
    if (schemaIsStatic)
    {
      if (isStatic)
      {
        logger.warn(Translate
            .get("requestmanager.schema.replace.static.with.new"));
        this.dbs = schema;
      }
      else
        logger.info(Translate.get("requestmanager.schema.ignore.new.dynamic"));
    }
    else
    {
      schemaIsStatic = isStatic;
      this.dbs = schema;
      logger.info(Translate
          .get("requestmanager.schema.set.new.virtualdatabase"));
    }

    if (schedulerParsingranularity != ParsingGranularities.NO_PARSING)
      scheduler.setDatabaseSchema(dbs);

    if (cacheParsingranularity != ParsingGranularities.NO_PARSING)
      resultCache.setDatabaseSchema(dbs);

    // Load balancers do not have a specific database schema to update
  }

  /**
   * Sets the schemaIsDirty value if the backend schema needs to be refreshed.
   * 
   * @param schemaIsDirty The schemaIsDirty to set.
   */
  public void setSchemaIsDirty(boolean schemaIsDirty)
  {
    this.schemaIsDirty = schemaIsDirty;
  }

  //
  // Getter/Setter methods
  //

  /**
   * Returns the vdb value.
   * 
   * @return Returns the vdb.
   */
  public VirtualDatabase getVirtualDatabase()
  {
    return vdb;
  }

  /**
   * Sets the backup manager for this recovery log
   * 
   * @param currentBackupManager an instance of <code>BackupManager</code>
   */
  public void setBackupManager(BackupManager currentBackupManager)
  {
    this.backupManager = currentBackupManager;
  }

  /**
   * Returns the backupManager value.
   * 
   * @return Returns the backupManager.
   */
  public BackupManager getBackupManager()
  {
    return backupManager;
  }

  /**
   * Get the Request Load Balancer used in this Request Controller.
   * 
   * @return an <code>AbstractLoadBalancer</code> value
   */
  public AbstractLoadBalancer getLoadBalancer()
  {
    return loadBalancer;
  }

  /**
   * Set the Request Load Balancer to use in this Request Controller.
   * 
   * @param loadBalancer a Request Load Balancer implementation
   */
  public void setLoadBalancer(AbstractLoadBalancer loadBalancer)
  {
    if (this.loadBalancer != null)
      throw new RuntimeException(
          "It is not possible to dynamically change a load balancer.");
    this.loadBalancer = loadBalancer;
    if (loadBalancer == null)
      return;
    loadBalancerParsingranularity = loadBalancer.getParsingGranularity();
    if (loadBalancerParsingranularity > requiredParsingGranularity)
      requiredParsingGranularity = loadBalancerParsingranularity;

    if (MBeanServerManager.isJmxEnabled())
    {
      try
      {
        MBeanServerManager.registerMBean(loadBalancer, JmxConstants
            .getLoadBalancerObjectName(vdb.getVirtualDatabaseName()));
      }
      catch (Exception e)
      {
        logger.error(Translate.get("jmx.failed.register.mbean.loadbalancer"));
      }
    }

  }

  /**
   * Get the result cache (if any) used in this Request Manager.
   * 
   * @return an <code>AbstractResultCache</code> value or null if no Reqsult
   *         Cache has been defined
   */
  public AbstractResultCache getResultCache()
  {
    return resultCache;
  }

  /**
   * Returns the metadataCache value.
   * 
   * @return Returns the metadataCache.
   */
  public MetadataCache getMetadataCache()
  {
    return metadataCache;
  }

  /**
   * Sets the metadataCache value.
   * 
   * @param metadataCache The metadataCache to set.
   */
  public void setMetadataCache(MetadataCache metadataCache)
  {
    this.metadataCache = metadataCache;
  }

  /**
   * Sets the ParsingCache.
   * 
   * @param parsingCache The parsingCache to set.
   */
  public void setParsingCache(ParsingCache parsingCache)
  {
    parsingCache.setRequestManager(this);
    parsingCache.setGranularity(requiredParsingGranularity);
    parsingCache.setCaseSensitiveParsing(isCaseSensitiveParsing);
    this.parsingCache = parsingCache;
  }

  /**
   * Returns the Recovery Log Manager.
   * 
   * @return RecoveryLog
   */
  public RecoveryLog getRecoveryLog()
  {
    return recoveryLog;
  }

  /**
   * Sets the Recovery Log Manager.
   * 
   * @param recoveryLog The log recovery to set
   */
  public void setRecoveryLog(RecoveryLog recoveryLog)
  {
    if (recoveryLog == null)
      return;
    this.recoveryLog = recoveryLog;
    ArrayList backends = vdb.getBackends();
    int size = backends.size();
    backendStateListener = new BackendStateListener(vdb
        .getVirtualDatabaseName(), recoveryLog);
    for (int i = 0; i < size; i++)
      ((DatabaseBackend) backends.get(i))
          .setStateListener(backendStateListener);
  }

  /**
   * Set the Request Cache to use in this Request Controller.
   * 
   * @param cache a Request Cache implementation
   */
  public void setResultCache(AbstractResultCache cache)
  {
    resultCache = cache;
    cacheParsingranularity = cache.getParsingGranularity();
    if (cacheParsingranularity > requiredParsingGranularity)
      requiredParsingGranularity = cacheParsingranularity;
  }

  /**
   * Get the Request Scheduler (if any) used in this Request Controller.
   * 
   * @return an <code>AbstractScheduler</code> value or null if no Request
   *         Scheduler has been defined
   */
  public AbstractScheduler getScheduler()
  {
    return scheduler;
  }

  /**
   * Set the Request Scheduler to use in this Request Controller.
   * 
   * @param scheduler a Request Scheduler implementation
   */
  public void setScheduler(AbstractScheduler scheduler)
  {
    this.scheduler = scheduler;
    schedulerParsingranularity = scheduler.getParsingGranularity();
    if (schedulerParsingranularity > requiredParsingGranularity)
      requiredParsingGranularity = schedulerParsingranularity;
  }

  /**
   * Sets the parsing case sensitivity. If true the request are parsed in a case
   * sensitive way (table/column name must match exactly the case of the names
   * fetched from the database or enforced by a static schema).
   * 
   * @param isCaseSensitiveParsing true if parsing is case sensitive
   */
  public void setCaseSensitiveParsing(boolean isCaseSensitiveParsing)
  {
    this.isCaseSensitiveParsing = isCaseSensitiveParsing;
    if (parsingCache != null)
      parsingCache.setCaseSensitiveParsing(isCaseSensitiveParsing);
  }

  //
  // Debug/Monitoring
  //

  /**
   * Get xml information about this Request Manager
   * 
   * @return <code>String</code> in xml formatted text
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RequestManager + " "
        + DatabasesXmlTags.ATT_caseSensitiveParsing + "=\""
        + isCaseSensitiveParsing + "\" " + DatabasesXmlTags.ATT_beginTimeout
        + "=\"" + beginTimeout / 1000 + "\" "
        + DatabasesXmlTags.ATT_commitTimeout + "=\"" + commitTimeout / 1000
        + "\" " + DatabasesXmlTags.ATT_rollbackTimeout + "=\""
        + rollbackTimeout / 1000 + "\">");
    if (scheduler != null)
      info.append(scheduler.getXml());

    if (metadataCache != null || parsingCache != null || resultCache != null)
    {
      info.append("<" + DatabasesXmlTags.ELT_RequestCache + ">");
      if (metadataCache != null)
        info.append(metadataCache.getXml());
      if (parsingCache != null)
        info.append(parsingCache.getXml());
      if (resultCache != null)
        info.append(resultCache.getXml());
      info.append("</" + DatabasesXmlTags.ELT_RequestCache + ">");
    }

    if (loadBalancer != null)
      info.append(loadBalancer.getXml());
    if (recoveryLog != null)
      info.append(this.recoveryLog.getXml());
    info.append("</" + DatabasesXmlTags.ELT_RequestManager + ">");
    return info.toString();
  }

  /**
   * Returns the backendStateListener value.
   * 
   * @return Returns the backendStateListener.
   */
  public BackendStateListener getBackendStateListener()
  {
    return backendStateListener;
  }

  /**
   * Returns the beginTimeout value.
   * 
   * @return Returns the beginTimeout.
   */
  public long getBeginTimeout()
  {
    return beginTimeout;
  }

  /**
   * Sets the beginTimeout value.
   * 
   * @param beginTimeout The beginTimeout to set.
   */
  public void setBeginTimeout(long beginTimeout)
  {
    this.beginTimeout = beginTimeout;
  }

  /**
   * Returns the cacheParsingranularity value.
   * 
   * @return Returns the cacheParsingranularity.
   */
  public int getCacheParsingranularity()
  {
    return cacheParsingranularity;
  }

  /**
   * Sets the cacheParsingranularity value.
   * 
   * @param cacheParsingranularity The cacheParsingranularity to set.
   */
  public void setCacheParsingranularity(int cacheParsingranularity)
  {
    this.cacheParsingranularity = cacheParsingranularity;
  }

  /**
   * Returns the commitTimeout value.
   * 
   * @return Returns the commitTimeout.
   */
  public long getCommitTimeout()
  {
    return commitTimeout;
  }

  /**
   * Sets the commitTimeout value.
   * 
   * @param commitTimeout The commitTimeout to set.
   */
  public void setCommitTimeout(long commitTimeout)
  {
    this.commitTimeout = commitTimeout;
  }

  /**
   * Returns the loadBalancerParsingranularity value.
   * 
   * @return Returns the loadBalancerParsingranularity.
   */
  public int getLoadBalancerParsingranularity()
  {
    return loadBalancerParsingranularity;
  }

  /**
   * Sets the loadBalancerParsingranularity value.
   * 
   * @param loadBalancerParsingranularity The loadBalancerParsingranularity to
   *          set.
   */
  public void setLoadBalancerParsingranularity(int loadBalancerParsingranularity)
  {
    this.loadBalancerParsingranularity = loadBalancerParsingranularity;
  }

  /**
   * Returns the requiredParsingGranularity value.
   * 
   * @return Returns the requiredParsingGranularity.
   */
  public int getRequiredParsingGranularity()
  {
    return requiredParsingGranularity;
  }

  /**
   * Sets the requiredParsingGranularity value.
   * 
   * @param requiredGranularity The requiredParsingGranularity to set.
   */
  public void setRequiredParsingGranularity(int requiredGranularity)
  {
    this.requiredParsingGranularity = requiredGranularity;
  }

  /**
   * Returns the rollbackTimeout value.
   * 
   * @return Returns the rollbackTimeout.
   */
  public long getRollbackTimeout()
  {
    return rollbackTimeout;
  }

  /**
   * Sets the rollbackTimeout value.
   * 
   * @param rollbackTimeout The rollbackTimeout to set.
   */
  public void setRollbackTimeout(long rollbackTimeout)
  {
    this.rollbackTimeout = rollbackTimeout;
  }

  /**
   * Returns the schedulerParsingranularity value.
   * 
   * @return Returns the schedulerParsingranularity.
   */
  public int getSchedulerParsingranularity()
  {
    return schedulerParsingranularity;
  }

  /**
   * Sets the schedulerParsingranularity value.
   * 
   * @param schedulerParsingranularity The schedulerParsingranularity to set.
   */
  public void setSchedulerParsingranularity(int schedulerParsingranularity)
  {
    this.schedulerParsingranularity = schedulerParsingranularity;
  }

  /**
   * Returns the schemaIsStatic value.
   * 
   * @return Returns the schemaIsStatic.
   */
  public boolean isSchemaIsStatic()
  {
    return schemaIsStatic;
  }

  /**
   * Sets the schemaIsStatic value.
   * 
   * @param schemaIsStatic The schemaIsStatic to set.
   */
  public void setSchemaIsStatic(boolean schemaIsStatic)
  {
    this.schemaIsStatic = schemaIsStatic;
  }

  /**
   * Returns the isCaseSensitiveParsing value.
   * 
   * @return Returns the isCaseSensitiveParsing.
   */
  public boolean isCaseSensitiveParsing()
  {
    return isCaseSensitiveParsing;
  }

  /**
   * @see org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean#getAssociatedString()
   */
  public String getAssociatedString()
  {
    return "requestmanager";
  }
}
