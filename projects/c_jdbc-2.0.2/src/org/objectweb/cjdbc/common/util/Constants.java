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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.util;

/**
 * Constants that are common to the console, driver and controller modules
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 */
public class Constants
{
  /** C-JDBC version. */
  public static final String VERSION = "@VERSION@";

  /**
   * C-JDBC major version
   * 
   * @return major version
   */
  public static final int getMajorVersion()
  {
    int ind = VERSION.indexOf('.');
    if (ind > 0)
      return Integer.parseInt(VERSION.substring(0, ind));
    else
      return 1;
  }

  /**
   * C-JDBC minor version
   * 
   * @return minor version
   */
  public static final int getMinorVersion()
  {
    int ind = VERSION.indexOf('.');
    if (ind > 0)
      return Integer.parseInt(VERSION.substring(ind + 1, ind + 2));
    else
      return 0;
  }

  /**
   * Maximum number of characters to display when a SQL statement is logged into
   * a Exception.
   */
  public static final int     SQL_SHORT_FORM_LENGTH  = 40;

  /** Shutdown Mode Wait: Wait for all clients to disconnect */
  public static final int     SHUTDOWN_WAIT          = 1;
  /**
   * Shutdown Mode Safe: Wait for all current transactions to complete before
   * shutdown
   */
  public static final int     SHUTDOWN_SAFE          = 2;
  /**
   * Shutdown Mode Force: Does not wait for the end of the current transactions
   * and kill all connections. Recovery will be needed on restart.
   */
  public static final int     SHUTDOWN_FORCE         = 3;

  /** C-JDBC DTD file name (must be found in classpath). */
  public static final String  C_JDBC_DTD_FILE        = "c-jdbc.dtd";

}