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
 * Contributor(s): 
 */

package org.objectweb.cjdbc.common.exceptions;


/**
 * Octopus Exception class in case errors happen while octopus is working
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 *  
 */
public class OctopusException extends CJDBCException
{
  private static final long serialVersionUID = 4283344859653349315L;

  /**
   * Creates a new <code>OctopusException</code> instance.
   *  
   */
  public OctopusException()
  {
  }

  /**
   * Creates a new <code>OctopusException</code> instance.
   * 
   * @param message the error message
   */
  public OctopusException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>OctopusException</code> instance.
   * 
   * @param cause the root cause
   */
  public OctopusException(Throwable cause)
  {
    this.cause = cause;
  }

  /**
   * Creates a new <code>OctopusException</code> instance.
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public OctopusException(String message, Throwable cause)
  {
    super(message);
    this.cause = cause;
  }

}