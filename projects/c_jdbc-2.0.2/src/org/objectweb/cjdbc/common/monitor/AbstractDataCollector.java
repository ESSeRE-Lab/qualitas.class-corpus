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

package org.objectweb.cjdbc.common.monitor;

import java.io.Serializable;

import org.objectweb.cjdbc.common.exceptions.DataCollectorException;

/**
 * This defines the abstract hierachy to collect monitoring information. All
 * monitored information from the controller should extends this class.
 * <code>collectValue</code> can therefore NOT be called directly on the
 * client side. Instead, the client should be only given the returned result.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 */
public abstract class AbstractDataCollector implements Serializable
{
  protected transient Object controller;

  /**
   * This is used on the controller side to collect information
   * 
   * @return the value collected by this collectorsardes@inrialpes.fr
   * @throws DataCollectorException if fails to collect the information
   */
  public abstract long collectValue() throws DataCollectorException;

  /**
   * Get a string description for this collector
   * 
   * @return translated string
   */
  public abstract String getDescription();

  /**
   * Return the name of the target of this collector
   * 
   * @return target name
   */
  public abstract String getTargetName();

  /**
   * associated a controller to this data collector
   * 
   * @param controller to associate
   */
  public void setController(Object controller)
  {
    this.controller = controller;
  }

}
