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
 * Contributor(s): Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.requestmanager.distributed;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.NotCompliantMBeanException;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.UnknownRequest;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.CJDBCGroupMessage;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.Commit;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ExecReadRequest;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ExecReadStoredProcedure;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ExecWriteRequest;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ExecWriteRequestWithKeys;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ExecWriteStoredProcedure;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.NotifyCompletion;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ReleaseSavepoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.Rollback;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.RollbackToSavepoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.SetSavepoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.UnlogCommit;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.UnlogRequest;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.UnlogRollback;
import org.objectweb.tribe.adapters.MulticastRequestAdapter;
import org.objectweb.tribe.adapters.MulticastResponse;
import org.objectweb.tribe.common.Member;

/**
 * This class defines a RAIDb1DistributedRequestManager
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RAIDb1DistributedRequestManager extends DistributedRequestManager
{
  /**
   * Creates a new <code>RAIDb1DistributedRequestManager</code> instance
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
  public RAIDb1DistributedRequestManager(DistributedVirtualDatabase vdb,
      AbstractScheduler scheduler, AbstractResultCache cache,
      AbstractLoadBalancer loadBalancer, RecoveryLog recoveryLog,
      long beginTimeout, long commitTimeout, long rollbackTimeout)
      throws SQLException, NotCompliantMBeanException
  {
    super(vdb, scheduler, cache, loadBalancer, recoveryLog, beginTimeout,
        commitTimeout, rollbackTimeout);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#execRemoteReadRequest(org.objectweb.cjdbc.common.sql.SelectRequest)
   */
  public ControllerResultSet execRemoteReadRequest(SelectRequest request)
      throws SQLException
  {
    try
    {
      // Iterate over controllers in members list (but us) until someone
      // successfully executes our request.
      Iterator i = dvdb.getAllMemberButUs().iterator();
      while (i.hasNext())
      {
        Member controller = (Member) i.next();
        ArrayList groupMembers = new ArrayList();
        groupMembers.add(controller);

        if (logger.isDebugEnabled())
          logger.debug("Sending request "
              + request.getSQLShortForm(dvdb.getSQLShortFormLength())
              + (request.isAutoCommit() ? "" : " transaction "
                  + request.getTransactionId()) + " to " + controller);

        // Send query to remote controller
        MulticastResponse responses;
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new ExecReadRequest(request),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut);

        if (logger.isDebugEnabled())
          logger.debug("Request "
              + request.getSQLShortForm(dvdb.getSQLShortFormLength())
              + " completed.");

        Object ret = responses.getResult(controller);
        if (ret instanceof ControllerResultSet)
          return (ControllerResultSet) ret;
      }

      // No one answered, throw
      throw new NoMoreBackendException();
    }
    catch (Exception e)
    {
      String msg = "An error occured while executing remote select request "
          + request.getId();
      logger.warn(msg, e);
      throw new SQLException(msg + " (" + e + ")");
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#execDistributedWriteRequest(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  public int execDistributedWriteRequest(AbstractWriteRequest request)
      throws SQLException
  {
    try
    {
      int execWriteRequestResult = -1;

      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();

      if (logger.isDebugEnabled())
        logger.debug("Broadcasting request "
            + request.getSQLShortForm(dvdb.getSQLShortFormLength())
            + (request.isAutoCommit() ? "" : " transaction "
                + request.getTransactionId()) + " to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new ExecWriteRequest(request),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut); // CHECK
        // request.getTimeout());
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed write request "
            + request.getId();
        logger.warn(msg, e);
        throw new SQLException(msg + " (" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("Request "
            + request.getSQLShortForm(dvdb.getSQLShortFormLength())
            + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size()
            + " controller(s) died during execution of request "
            + request.getId());
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof Integer)
        {
          if (execWriteRequestResult == -1)
            execWriteRequestResult = ((Integer) r).intValue();
          else if (execWriteRequestResult != ((Integer) r).intValue())
            logger.error("Controllers have different results for request "
                + request.getId());
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Request failed on all backends of controller "
                + member + " (" + r + ")");
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member
                + " has no more backends to execute query (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "Request " + request.getId() + " failed on controller "
              + member + " (" + r + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers of completion
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends,
              new NotifyCompletion(request, execWriteRequestResult != -1),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut); // CHECK
          // request.getTimeout());
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of failure of distributed write request "
              + request.getId();
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (execWriteRequestResult != -1)
      {
        if (logger.isDebugEnabled())
          logger.debug("Request " + request.getId()
              + " completed successfully.");
        return execWriteRequestResult;
      }

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log

        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            dvdb.getMulticastRequestAdapter().multicastMessage(dest,
                new UnlogRequest(request, nmbe.getRecoveryLogId()),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut); // CHECK
            // request.getTimeout());
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member + " to unlog failed distributed write request "
                + request.getId();
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Request '" + request + "' failed on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = Translate
          .get("loadbalancer.request.failed", new String[]{
              request.getSQLShortForm(vdb.getSQLShortFormLength()),
              e.getMessage()});
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#execDistributedWriteRequestWithKeys(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  public ControllerResultSet execDistributedWriteRequestWithKeys(
      AbstractWriteRequest request) throws SQLException
  {
    try
    {
      ControllerResultSet execWriteRequestResult = null;

      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();

      if (logger.isDebugEnabled())
        logger.debug("Broadcasting request "
            + request.getSQLShortForm(dvdb.getSQLShortFormLength())
            + (request.isAutoCommit() ? "" : " transaction "
                + request.getTransactionId()) + ") to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new ExecWriteRequestWithKeys(request),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut); // CHECK
        // request.getTimeout());
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed write request with keys "
            + request.getId();
        logger.warn(msg, e);
        throw new SQLException(msg + " (" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("Request "
            + request.getSQLShortForm(dvdb.getSQLShortFormLength())
            + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size()
            + " controller(s) died during execution of request "
            + request.getId());
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof ControllerResultSet)
        {
          if (execWriteRequestResult == null)
            execWriteRequestResult = (ControllerResultSet) r;
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Request failed on all backends of controller "
                + member + " (" + r + ")");
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member
                + " has no more backends to execute query (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "Request " + request.getId() + " failed on controller "
              + member + " (" + r + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers of completion
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends,
              new NotifyCompletion(request, execWriteRequestResult != null),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut); // CHECK
          // request.getTimeout());
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of failure of distributed write request with keys "
              + request.getId();
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (execWriteRequestResult != null)
      {
        if (logger.isDebugEnabled())
          logger.debug("Request " + request.getId()
              + " completed successfully.");
        return execWriteRequestResult;
      }

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log

        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            dvdb.getMulticastRequestAdapter().multicastMessage(dest,
                new UnlogRequest(request, nmbe.getRecoveryLogId()),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut);
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member + " to unlog failed distributed write request "
                + request.getId();
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Request '" + request + "' failed on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = Translate
          .get("loadbalancer.request.failed", new String[]{
              request.getSQLShortForm(vdb.getSQLShortFormLength()),
              e.getMessage()});
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#execDistributedReadStoredProcedure(StoredProcedure)
   */
  public ControllerResultSet execDistributedReadStoredProcedure(
      StoredProcedure proc) throws SQLException
  {
    try
    {
      ControllerResultSet result = null;

      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();

      if (logger.isDebugEnabled())
        logger.debug("Broadcasting read stored procedure "
            + proc.getSQLShortForm(dvdb.getSQLShortFormLength())
            + (proc.isAutoCommit() ? "" : " transaction "
                + proc.getTransactionId()) + ") to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new ExecReadStoredProcedure(proc),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut); // CHECK proc.getTimeout());
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed read stored procedure "
            + proc.getId();
        logger.warn(msg, e);
        throw new SQLException(msg + " (" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("Stored procedure "
            + proc.getSQLShortForm(dvdb.getSQLShortFormLength())
            + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size()
            + " controller(s) died during execution of stored procedure "
            + proc.getId());
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof ControllerResultSet)
        {
          if (result == null)
            result = (ControllerResultSet) r;
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member
                + " has no more backends to execute query (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "Stored procedure " + proc.getId()
              + " failed on controller " + member + " (" + r + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers of completion
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends, new NotifyCompletion(proc, result != null),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut); // CHECK
          // proc.getTimeout());
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of failure of read stored procedure "
              + proc.getId();
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (result != null)
      {
        if (logger.isDebugEnabled())
          logger.debug("Stored procedure " + proc.getId()
              + " completed successfully.");
        return result; // Success
      }

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log

        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            dvdb.getMulticastRequestAdapter().multicastMessage(dest,
                new UnlogRequest(proc, nmbe.getRecoveryLogId()),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut); // CHECK
            // proc.getTimeout());
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member
                + " to unlog failed distributed read stored procedure "
                + proc.getId();
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Stored procedure '" + proc
            + "' failed on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = Translate.get("loadbalancer.request.failed", new String[]{
          proc.getSQLShortForm(vdb.getSQLShortFormLength()), e.getMessage()});
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#execDistributedWriteStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public int execDistributedWriteStoredProcedure(StoredProcedure proc)
      throws SQLException
  {
    try
    {
      int execWriteStoredProcedureResult = NO_RESULT;

      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();

      if (logger.isDebugEnabled())
        logger.debug("Broadcasting write store procedure "
            + proc.getSQLShortForm(dvdb.getSQLShortFormLength())
            + (proc.isAutoCommit() ? "" : " transaction "
                + proc.getTransactionId()) + ") to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new ExecWriteStoredProcedure(proc),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut); // CHECK proc.getTimeout());
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed write stored procedure "
            + proc.getId();
        logger.warn(msg, e);
        throw new SQLException(msg + " (" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("Stored procedure "
            + proc.getSQLShortForm(dvdb.getSQLShortFormLength())
            + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size()
            + " controller(s) died during execution of stored procedure "
            + proc.getId());
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof Integer)
        {
          if (execWriteStoredProcedureResult == NO_RESULT)
            execWriteStoredProcedureResult = ((Integer) r).intValue();
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member
                + " has no more backends to execute query (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "Stored procedure " + proc.getId()
              + " failed on controller " + member + " (" + r + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers of completion
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends,
              new NotifyCompletion(proc,
                  execWriteStoredProcedureResult != NO_RESULT),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut); // CHECK
          // proc.getTimeout());
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of failure of write stored procedure "
              + proc.getId();
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (execWriteStoredProcedureResult != NO_RESULT)
      {
        if (logger.isDebugEnabled())
          logger.debug("Stored procedure " + proc.getId()
              + " completed successfully.");
        return execWriteStoredProcedureResult; // Success
      }

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log

        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            dvdb.getMulticastRequestAdapter().multicastMessage(dest,
                new UnlogRequest(proc, nmbe.getRecoveryLogId()),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut); // CHECK
            // proc.getTimeout());
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member
                + " to unlog failed distributed write stored procedure "
                + proc.getId();
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Stored procedure '" + proc
            + "' failed on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = Translate.get("loadbalancer.request.failed", new String[]{
          proc.getSQLShortForm(vdb.getSQLShortFormLength()), e.getMessage()});
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#distributedCommit(String,
   *      long)
   */
  public void distributedCommit(String login, long transactionId)
      throws SQLException
  {
    try
    {
      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();
      if (logger.isDebugEnabled())
        logger.debug("Broadcasting transaction " + transactionId
            + " commit to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new Commit(login, transactionId),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut); // CHECK this.commitTimeout
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed rollback for transaction "
            + transactionId;
        logger.warn(msg, e);
        throw new SQLException(msg + "(" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("Commit of transaction " + transactionId + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size()
            + " controller(s) died during execution of commit for transaction "
            + transactionId);
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      boolean success = false;
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof Boolean)
        {
          if (((Boolean) r).booleanValue())
            success = true;
          else
            logger.error("Unexpected result for controller  " + member);
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member
                + " has no more backends to commit transaction  "
                + transactionId + " (" + r + ")");
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Commit failed on all backends of controller "
                + member + " (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "Commit of transaction " + transactionId
              + " failed on controller " + member + " (" + r + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers where all backend failed that the commit
        // completed with 'success'
        AbstractRequest request = new UnknownRequest("commit", false, 0, "\n");
        request.setTransactionId(transactionId);
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends, new NotifyCompletion(request, success),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut); // CHECK commitTimeout);
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of failure to commit transaction "
              + transactionId;
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (success)
        return; // This is a success if at least one controller has succeeded

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log

        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            // Here we use the commit timeout field to transport the recovery
            // log id. This is ugly but convenient.
            dvdb.getMulticastRequestAdapter().multicastMessage(
                dest,
                new UnlogCommit(new TransactionMarkerMetaData(transactionId,
                    nmbe.getRecoveryLogId(), nmbe.getLogin())),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut); // CHECK
            // this.commitTimeout);
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member + " to unlog failed commit for transaction "
                + transactionId;
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Transaction " + transactionId
            + " failed to commit on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = "Transaction " + transactionId + " commit failed (" + e
          + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#distributedRollback(String,
   *      long)
   */
  public void distributedRollback(String login, long transactionId)
      throws SQLException
  {
    try
    {
      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();
      if (logger.isDebugEnabled())
        logger.debug("Broadcasting transaction " + transactionId
            + " rollback to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new Rollback(login, transactionId),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut); // CHECK
        // this.rollbackTimeout);
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed rollback for transaction "
            + transactionId;
        logger.warn(msg, e);
        throw new SQLException(msg + "(" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger
            .debug("rollback of transaction " + transactionId + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger
            .warn(responses.getFailedMembers().size()
                + " controller(s) died during execution of rollback for transaction "
                + transactionId);
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      boolean success = false;
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof Boolean)
        {
          if (((Boolean) r).booleanValue())
            success = true;
          else
            logger.error("Unexpected result for controller  " + member);
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member
                + " has no more backends to rollback transaction  "
                + transactionId + " (" + r + ")");
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("rollback failed on all backends of controller "
                + member + " (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "rollback of transaction " + transactionId
              + " failed on controller " + member + " (" + r + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers where all backend failed that the rollback
        // completed with 'success'
        AbstractRequest request = new UnknownRequest("rollback", false, 0, "\n");
        request.setTransactionId(transactionId);
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends, new NotifyCompletion(request, success),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut); // CHECK rollbackTimeout);
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of failure to rollback transaction "
              + transactionId;
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (success)
        return; // This is a success if at least one controller has succeeded

      if (exception != null)
        throw exception;

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log

        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            // Here we use the commit timeout field to transport the recovery
            // log id. This is ugly but convenient.
            dvdb.getMulticastRequestAdapter().multicastMessage(
                dest,
                new UnlogRollback(new TransactionMarkerMetaData(transactionId,
                    nmbe.getRecoveryLogId(), nmbe.getLogin())),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut); // CHECK
            // this.commitTimeout);
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member + " to unlog failed rollback for transaction "
                + transactionId;
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Transaction " + transactionId
            + " failed to rollback on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = "Transaction " + transactionId + " rollback failed (" + e
          + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#distributedRollback(long,
   *      String)
   */
  public void distributedRollback(long transactionId, String savepointName)
      throws SQLException
  {
    try
    {
      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();
      if (logger.isDebugEnabled())
        logger.debug("Broadcasting rollback to savepoint " + savepointName
            + " for transaction " + transactionId + " to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers,
            new RollbackToSavepoint(transactionId, savepointName),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut);
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed rollback to"
            + " savepoint " + savepointName + " for transaction "
            + transactionId;
        logger.warn(msg, e);
        throw new SQLException(msg + "(" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("rollback to savepoint " + savepointName + " for "
            + "transaction " + transactionId + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size() + " controller(s) died"
            + " during execution of rollback to savepoint " + savepointName
            + " for transaction " + transactionId);
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      boolean success = false;
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof Boolean)
        {
          if (((Boolean) r).booleanValue())
            success = true;
          else
            logger.error("Unexpected result for controller  " + member);
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member + " has no more backends to "
                + "rollback to savepoint " + savepointName + " for "
                + "transaction " + transactionId + " (" + r + ")");
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("rollback to savepoint failed on all backends of "
                + "controller " + member + " (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "rollback to savepoint " + savepointName + " for "
              + "transaction " + transactionId + " failed on controller "
              + member + " (" + r + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers where all backend failed that the rollback
        // completed with 'success'
        AbstractRequest request = new UnknownRequest("rollback "
            + savepointName, false, 0, "\n");
        request.setTransactionId(transactionId);
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends, new NotifyCompletion(request, success),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut);
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of "
              + "failure to rollback to savepoint " + savepointName + " for "
              + "transaction " + transactionId;
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (success)
        return; // This is a success if at least one controller has succeeded

      if (exception != null)
        throw exception;

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log
        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            // Here we use the commit timeout field to transport the recovery
            // log id. This is ugly but convenient.
            dvdb.getMulticastRequestAdapter().multicastMessage(
                dest,
                new UnlogRollback(new TransactionMarkerMetaData(transactionId,
                    nmbe.getRecoveryLogId(), nmbe.getLogin())),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut);
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member + " to unlog failed rollback to savepoint "
                + savepointName + " for transaction " + transactionId;
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Rollback to savepoint " + savepointName + " for "
            + "transaction " + transactionId + " failed on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = "Rollback to savepoint " + savepointName + " for "
          + "transaction " + transactionId + " failed (" + e + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#distributedSetSavepoint(long,
   *      String)
   */
  public void distributedSetSavepoint(long transactionId, String name)
      throws SQLException
  {
    try
    {
      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();
      if (logger.isDebugEnabled())
        logger.debug("Broadcasting set savepoint " + name + " to transaction "
            + transactionId + " to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new SetSavepoint(transactionId, name),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut);
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed set "
            + "savepoint " + name + " to transaction " + transactionId;
        logger.warn(msg, e);
        throw new SQLException(msg + "(" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("set savepoint " + name + " to transaction "
            + transactionId + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size() + " controller(s) died"
            + " during execution of set savepoint " + name + " to transaction "
            + transactionId);
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      boolean success = false;
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof Boolean)
        {
          if (((Boolean) r).booleanValue())
            success = true;
          else
            logger.error("Unexpected result for controller  " + member);
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member + " has no more backends to "
                + "set savepoint " + name + " to transaction " + transactionId
                + " (" + r + ")");
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("set savepoint failed on all backends of controller "
                + member + " (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "set savepoint " + name + " to transaction "
              + transactionId + " failed on controller " + member + " (" + r
              + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers where all backend failed that the rollback
        // completed with 'success'
        AbstractRequest request = new UnknownRequest("savepoint " + name,
            false, 0, "\n");
        request.setTransactionId(transactionId);
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends, new NotifyCompletion(request, success),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut);
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of "
              + "failure to set savepoint " + name + " to transaction "
              + transactionId;
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (success)
        return; // This is a success if at least one controller has succeeded

      if (exception != null)
        throw exception;

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log
        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            // Here we use the commit timeout field to transport the recovery
            // log id. This is ugly but convenient.
            dvdb.getMulticastRequestAdapter().multicastMessage(
                dest,
                new UnlogRollback(new TransactionMarkerMetaData(transactionId,
                    nmbe.getRecoveryLogId(), nmbe.getLogin())),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut);
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member + " to unlog failed set savepoint " + name + " to "
                + "transaction " + transactionId;
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Set savepoint " + name + " to transaction "
            + transactionId + " failed on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = "Set savepoint " + name + " to transaction " + transactionId
          + " failed (" + e + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#distributedReleaseSavepoint(long,
   *      String)
   */
  public void distributedReleaseSavepoint(long transactionId, String name)
      throws SQLException
  {
    try
    {
      ArrayList groupMembers = dvdb.getCurrentGroup().getMembers();
      if (logger.isDebugEnabled())
        logger.debug("Broadcasting release savepoint " + name + " from "
            + "transaction " + transactionId + " to all controllers ("
            + dvdb.getChannel().getLocalMembership() + "->"
            + groupMembers.toString() + ")");

      // Send the query to everybody including us
      MulticastResponse responses;
      try
      {
        responses = dvdb.getMulticastRequestAdapter().multicastMessage(
            groupMembers, new ReleaseSavepoint(transactionId, name),
            MulticastRequestAdapter.WAIT_ALL,
            CJDBCGroupMessage.defaultCastTimeOut);
      }
      catch (Exception e)
      {
        String msg = "An error occured while executing distributed release "
            + "savepoint " + name + " from transaction " + transactionId;
        logger.warn(msg, e);
        throw new SQLException(msg + "(" + e + ")");
      }

      if (logger.isDebugEnabled())
        logger.debug("release savepoint " + name + " from transaction "
            + transactionId + " completed.");

      if (responses.getFailedMembers() != null)
      { // Some controllers failed ... too bad !
        logger.warn(responses.getFailedMembers().size() + " controller(s) died"
            + " during execution of release savepoint " + name + " from "
            + "transaction " + transactionId);
      }

      // List of controllers that gave a AllBackendsFailedException
      ArrayList failedOnAllBackends = null;
      // List of controllers that have no more backends to execute queries
      ArrayList controllersWithoutBackends = null;
      SQLException exception = null;
      int size = groupMembers.size();
      boolean success = false;
      // Get the result of each controller
      for (int i = 0; i < size; i++)
      {
        Member member = (Member) groupMembers.get(i);
        if ((responses.getFailedMembers() != null)
            && responses.getFailedMembers().contains(member))
        {
          logger.warn("Controller " + member + " is suspected of failure.");
          continue;
        }
        Object r = responses.getResult(member);
        if (r instanceof Boolean)
        {
          if (((Boolean) r).booleanValue())
            success = true;
          else
            logger.error("Unexpected result for controller  " + member);
        }
        else if (r instanceof NoMoreBackendException)
        {
          if (controllersWithoutBackends == null)
            controllersWithoutBackends = new ArrayList();
          controllersWithoutBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("Controller " + member + " has no more backends to "
                + "release savepoint " + name + " from transaction "
                + transactionId + " (" + r + ")");
        }
        else if (r instanceof AllBackendsFailedException)
        {
          if (failedOnAllBackends == null)
            failedOnAllBackends = new ArrayList();
          failedOnAllBackends.add(member);
          if (logger.isDebugEnabled())
            logger.debug("release savepoint failed on all backends of "
                + "controller " + member + " (" + r + ")");
        }
        else if (r instanceof SQLException)
        {
          String msg = "release savepoint " + name + " from transaction "
              + transactionId + " failed on controller " + member + " (" + r
              + ")";
          logger.warn(msg);
          exception = (SQLException) r;
        }
      }

      if (failedOnAllBackends != null)
      { // Notify all controllers where all backend failed that the rollback
        // completed with 'success'
        AbstractRequest request = new UnknownRequest("release " + name, false,
            0, "\n");
        request.setTransactionId(transactionId);
        try
        {
          dvdb.getMulticastRequestAdapter().multicastMessage(
              failedOnAllBackends, new NotifyCompletion(request, success),
              MulticastRequestAdapter.WAIT_NONE,
              CJDBCGroupMessage.defaultCastTimeOut);
        }
        catch (Exception e)
        {
          String msg = "An error occured while notifying all controllers of "
              + "failure to release savepoint " + name + " from transaction "
              + transactionId;
          logger.warn(msg, e);
          throw new SQLException(msg + " (" + e + ")");
        }
      }

      if (success)
        return; // This is a success if at least one controller has succeeded

      if (exception != null)
        throw exception;

      // At this point, all controllers failed

      if (controllersWithoutBackends != null)
      { // Notify all controllers without backend that have already logged the
        // request that they must remove it from the log
        int nbOfControllers = controllersWithoutBackends.size();
        for (int i = 0; i < nbOfControllers; i++)
        {
          Member member = (Member) controllersWithoutBackends.get(i);
          NoMoreBackendException nmbe = (NoMoreBackendException) responses
              .getResult(member);
          try
          {
            ArrayList dest = new ArrayList();
            dest.add(member);
            // Here we use the commit timeout field to transport the recovery
            // log id. This is ugly but convenient.
            dvdb.getMulticastRequestAdapter().multicastMessage(
                dest,
                new UnlogRollback(new TransactionMarkerMetaData(transactionId,
                    nmbe.getRecoveryLogId(), nmbe.getLogin())),
                MulticastRequestAdapter.WAIT_NONE,
                CJDBCGroupMessage.defaultCastTimeOut);
          }
          catch (Exception e)
          {
            String msg = "An error occured while notifying controllers "
                + member + " to unlog failed release savepoint " + name
                + " from transaction " + transactionId;
            logger.error(msg, e);
          }
        }
      }

      if (exception != null)
        throw exception;
      else
      {
        String msg = "Release savepoint " + name + " from transaction "
            + transactionId + " failed on all controllers";
        logger.warn(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      String msg = "Release savepoint " + name + " from transaction "
          + transactionId + " failed (" + e + ")";
      logger.warn(msg);
      throw e;
    }
  }
}