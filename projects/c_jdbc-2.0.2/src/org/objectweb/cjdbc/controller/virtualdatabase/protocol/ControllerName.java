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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.controller.virtualdatabase.protocol;

import java.io.Serializable;

/**
 * This class defines a ControllerName class to send to new group members
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 */
public class ControllerName implements Serializable
{
  private static final long serialVersionUID = -2380753151132303045L;

  private String            controllerName;
  private String            jmxName;

  /**
   * Creates a new <code>ControllerName</code> object
   * 
   * @param controllerName the controller name
   * @param controllerJmxName the jmx name of the controller
   */
  public ControllerName(String controllerName, String controllerJmxName)
  {
    this.controllerName = controllerName;
    this.jmxName = controllerJmxName;
  }

  /**
   * @return Returns the controllerName.
   */
  public String getControllerName()
  {
    return controllerName;
  }

  /**
   * @param controllerName The controllerName to set.
   */
  public void setControllerName(String controllerName)
  {
    this.controllerName = controllerName;
  }

  /**
   * Returns the jmxName value.
   * 
   * @return Returns the jmxName.
   */
  public String getJmxName()
  {
    return jmxName;
  }

  /**
   * Sets the jmxName value.
   * 
   * @param jmxName The jmxName to set.
   */
  public void setJmxName(String jmxName)
  {
    this.jmxName = jmxName;
  }
}