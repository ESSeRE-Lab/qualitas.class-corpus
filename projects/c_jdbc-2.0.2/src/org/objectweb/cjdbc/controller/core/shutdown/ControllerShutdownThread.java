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
 * Contributor(s): Mathieu Peltier, Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.controller.core.shutdown;

import java.io.File;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.ShutdownException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.core.ControllerConstants;
import org.objectweb.cjdbc.controller.core.ControllerServerThread;
import org.objectweb.cjdbc.controller.core.ReportManager;
import org.objectweb.cjdbc.controller.jmx.MBeanServerManager;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * Abstract class for all implementations of controller shutdown strategies.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.2
 */
public abstract class ControllerShutdownThread extends ShutdownThread
{
  protected Controller controller;

  /**
   * Prepare the thread for shutting down.
   * 
   * @param controller the controller to shutdown
   * @param level Constants.SHUTDOWN_WAIT, Constants.SHUTDOWN_SAFE or
   *          Constants.SHUTDOWN_FORCE
   */
  public ControllerShutdownThread(Controller controller, int level)
  {
    super(level);
    this.controller = controller;
  }

  /**
   * Shutdown the JMX Agent.
   */
  protected void shutdownJmxAgent()
  {
    logger.info("Shutting down Jmx Agent");
    try
    {
      if (controller.getJmxEnable())
        MBeanServerManager.setJmxEnabled(false);
    }
    catch (Exception jme)
    {
      logger.error(Translate.get("controller.shutdown.jmx.error", jme
          .getMessage()), jme);
      //throw new ShutdownException(jme);
    }
  }

  /**
   * Shutdown all databases of this controller using the current shutdown level.
   */
  protected void shutdownDatabases()
  {
    logger.info("Shutting down databases");
    try
    {
      //Shutdown each virtual database with proper level
      ArrayList listvb = controller.getVirtualDatabases();
      int nbvb = listvb.size();
      for (int i = 0; i < nbvb; i++)
      {
        logger.info("Shutting down database:"
            + ((VirtualDatabase) listvb.get(i)).getVirtualDatabaseName()
            + " with level:" + this.shutdownLevel);
        ((VirtualDatabase) listvb.get(i)).shutdown(this.shutdownLevel);
        logger.info("Database:"
            + ((VirtualDatabase) listvb.get(i)).getVirtualDatabaseName()
            + " is shutdown");
      }
    }
    catch (Exception e)
    {
      logger.error(Translate.get("controller.shutdown.database.error", e));
    }
  }

  /**
   * Shutdown the ControllerServerThread and its attached connection to reject
   * new incoming connections.
   * 
   * @param joinTimeoutInMillis timeout in milliseconds to wait for controller
   *          server thread termination. A timeout of 0 means wait forever.
   * @throws ShutdownException if an error occurs
   */
  protected void shutdownServerConnectionThread(int joinTimeoutInMillis)
      throws ShutdownException
  {
    if (logger.isDebugEnabled())
      logger.debug("Shutting down ControllerServerThread");
    try
    {
      // Shutdown Server Connections Thread
      ControllerServerThread thread = controller.getConnectionThread();
      if (thread != null && !thread.isShuttingDown())
      {
        thread.shutdown();
        logger.info("Waiting for controller thread termination.");
        thread.join(joinTimeoutInMillis);
      }
    }
    catch (Exception e)
    {
      throw new ShutdownException(e);
    }
  }

  /**
   * Generate a controller report if it has been enabled in the config file.
   */
  protected void generateReportIfNeeded()
  {
    ReportManager report = controller.getReport();
    if (report != null && report.isGenerateOnShutdown())
    {
      report.startReport();
      report.generate();
      logger.info(Translate.get("fatal.report.generated", report
          .getReportLocation()
          + File.separator + ControllerConstants.REPORT_FILE));
    }
  }

}