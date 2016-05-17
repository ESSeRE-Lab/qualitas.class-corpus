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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools.testlet;

import java.sql.Connection;

/**
 * This class defines a AbstractTestLet. This expands the test let to connection
 * oriented test lets.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class AbstractConnectionTestLet extends AbstractTestLet
{

  protected Connection jdbcConnection;

  /**
   * Creates a new <code>AbstractTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public AbstractConnectionTestLet(Connection con)
  {
    super();
    this.jdbcConnection = con;
  }

  /**
   * Returns the jdbcConnection value.
   * 
   * @return Returns the jdbcConnection.
   */
  public Connection getJdbcConnection()
  {
    return jdbcConnection;
  }

  /**
   * Sets the jdbcConnection value.
   * 
   * @param jdbcConnection The jdbcConnection to set.
   */
  public void setJdbcConnection(Connection jdbcConnection)
  {
    this.jdbcConnection = jdbcConnection;
  }
}