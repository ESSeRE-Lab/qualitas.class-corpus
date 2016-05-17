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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * This class defines a AbstractVdbTestLet
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class AbstractVdbTestLet
    extends
      AbstractTestLet
{

  protected VirtualDatabase vdb;

  /**
   * Creates a new <code>AbstractVdbTestLet</code> object
   * 
   * @param vdb virtual database
   */
  public AbstractVdbTestLet(VirtualDatabase vdb)
  {
    super();
    this.vdb = vdb;
  }

  /**
   * Get a sql connection
   * 
   * @param url of the backend to get a connection from
   * @return <code>Connection</code>
   * @throws SQLException if fails
   */
  public Connection getDatabaseConnection(String url) throws SQLException
  {
    Properties props = new Properties();
    props.put("user", "test");
    props.put("password", "");
    Connection con = DriverManager.getConnection(url, props);
    return con;
  }
  
  /**
   * Retrieve a connection on C-JDBC with the given properties
   * 
   * @param props additionnal properties for the connection
   * @return <code>java.sql.Connection</code> object to C-JDBC
   * @throws Exception if fails
   */
  public Connection getCJDBCConnection(Properties props) throws Exception
  {
    Properties p = new Properties();
    p.put("user", "user");
    p.put("password", "");
    p.putAll(props);
    Class.forName("org.objectweb.cjdbc.driver.Driver");
    Connection con = DriverManager.getConnection("jdbc:cjdbc://localhost/myDB",p);
    return con;
  }
  
  /**
   * Return a connection on the backend of the virtual database at given index 
   * 
   * @param index of the backend in the arraylist of backends of the vdb
   * @return <code>Connection</code>
   * @throws Exception if fails
   */
  public Connection getBackendConnection(int index) throws Exception
  {
    return getDatabaseConnection(getBackend(index).getURL());
  }
  
  /**
   * Get a backend object
   * 
   * @param index of the backend in the array list of backends of the virtual database
   * @return <code>DatabaseBackend</code>
   * @throws Exception if fails
   */
  public DatabaseBackend getBackend(int index) throws Exception
  {
    DatabaseBackend b = ((DatabaseBackend)vdb.getBackends().get(index));
    System.out.println("Accessing backend:"+b.getName());
    return b;
  }
}