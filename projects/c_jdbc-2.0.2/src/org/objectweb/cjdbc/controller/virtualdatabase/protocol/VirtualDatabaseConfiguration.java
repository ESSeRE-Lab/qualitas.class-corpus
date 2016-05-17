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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.virtualdatabase.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.shared.BackendInfo;
import org.objectweb.cjdbc.controller.jmx.RmiConnector;
import org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase;

/**
 * Transports the configuration of a virtual database to remote controllers so
 * that compatibility checking can be performed.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class VirtualDatabaseConfiguration implements Serializable
{
  private static final long serialVersionUID = -4753828540599620782L;

  private String            controllerName;
  private String            controllerJmxName;
  private String            vdbName;
  private String            groupName        = null;
  private ArrayList         vLogins;
  private int               schedulerRAIDbLevel;
  private int               loadBalancerRAIDbLevel;
  private ArrayList         backends;

  // Jmx Information
  private String            rmiHostname;
  private String            rmiPort;

  /**
   * @return Returns the controllerName.
   */
  public String getControllerName()
  {
    return controllerName;
  }

  /**
   * Returns the controllerJmxName value.
   * 
   * @return Returns the controllerJmxName.
   */
  public String getControllerJmxName()
  {
    return controllerJmxName;
  }

  /**
   * Constructs a new <code>VirtualDatabaseConfiguration</code> object from a
   * <code>DistributedVirtualDatabase</code>.
   * 
   * @param dvdb The distributed virtual database to get configuration from.
   */
  public VirtualDatabaseConfiguration(DistributedVirtualDatabase dvdb)
  {
    this.controllerName = dvdb.getControllerName();
    this.controllerJmxName = dvdb.viewOwningController();
    this.vdbName = dvdb.getVirtualDatabaseName();
    this.groupName = dvdb.getGroupName();
    this.vLogins = dvdb.getAuthenticationManager().getVirtualLogins();
    this.schedulerRAIDbLevel = dvdb.getRequestManager().getScheduler()
        .getRAIDbLevel();
    this.loadBalancerRAIDbLevel = dvdb.getRequestManager().getLoadBalancer()
        .getRAIDbLevel();
    this.backends = dvdb.getBackendsInfo(dvdb.getBackends());

    List connectors = RmiConnector.getRmiConnectors();
    if (connectors.size() > 0)
    {
      RmiConnector rmi = (RmiConnector) connectors.get(0);
      rmiHostname = rmi.getHostName();
      rmiPort = String.valueOf(rmi.getPort());
    }
    else
    {
      rmiHostname = controllerName.substring(0, controllerName.indexOf(":"));
      rmiPort = String.valueOf(JmxConstants.DEFAULT_JMX_RMI_PORT);
    }
  }

  /**
   * @return Returns the rmiHostname.
   */
  public String getRmiHostname()
  {
    return rmiHostname;
  }

  /**
   * @return Returns the rmiPort.
   */
  public String getRmiPort()
  {
    return rmiPort;
  }

  /**
   * Check if the local distributed virtual database is compatible with this
   * virtual database configuration.
   * 
   * @param localDvdb The local distributed virtual database
   * @return true if both configurations are compatible, false otherwise
   */
  public boolean isCompatible(DistributedVirtualDatabase localDvdb)
  {
    try
    {
      if (controllerName.equals(localDvdb.getControllerName()))
      {
        localDvdb.logger
            .warn(Translate
                .get("virtualdatabase.distributed.configuration.checking.duplicate.controller.name"));
        return false;
      }

      // Sanity checks for virtual database name and group name
      if (!vdbName.equals(localDvdb.getVirtualDatabaseName()))
      {
        localDvdb.logger
            .warn(Translate
                .get("virtualdatabase.distributed.configuration.checking.mismatch.name"));
        return false;
      }
      if (!groupName.equals(localDvdb.getGroupName()))
      {
        localDvdb.logger
            .warn(Translate
                .get("virtualdatabase.distributed.configuration.checking.mismatch.groupname"));
        return false;
      }

      // Authentication managers must contains the same set of elements but
      // possibly in different orders (equals require the element to be in the
      // same order).
      if (!vLogins.containsAll(localDvdb.getAuthenticationManager()
          .getVirtualLogins())
          || !localDvdb.getAuthenticationManager().getVirtualLogins()
              .containsAll(vLogins))
      {
        localDvdb.logger
            .warn(Translate
                .get("virtualdatabase.distributed.configuration.checking.mismatch.vlogins"));
        return false;
      }

      // Scheduler and Load Balancer checking
      if (schedulerRAIDbLevel != localDvdb.getRequestManager().getScheduler()
          .getRAIDbLevel())
      {
        localDvdb.logger
            .warn(Translate
                .get("virtualdatabase.distributed.configuration.checking.mismatch.scheduler"));
        return false;
      }

      if (loadBalancerRAIDbLevel != localDvdb.getRequestManager()
          .getLoadBalancer().getRAIDbLevel())
      {
        localDvdb.logger
            .warn(Translate
                .get("virtualdatabase.distributed.configuration.checking.mismatch.loadbalancer"));
        return false;
      }

      // Checking backends
      int size = backends.size();
      for (int i = 0; i < size; i++)
      {
        BackendInfo b = (BackendInfo) backends.get(i);
        if (!localDvdb.isCompatibleBackend(b))
        {
          localDvdb.logger
              .warn(Translate
                  .get(
                      "virtualdatabase.distributed.configuration.checking.mismatch.backend.shared",
                      b.getName()));
          return false;
        }
      }

      // Ok, all tests succeeded, configuration is compatible
      return true;
    }
    catch (Exception e)
    {
      localDvdb.logger.error(Translate
          .get("virtualdatabase.distributed.configuration.checking.error"), e);
      return false;
    }
  }

}