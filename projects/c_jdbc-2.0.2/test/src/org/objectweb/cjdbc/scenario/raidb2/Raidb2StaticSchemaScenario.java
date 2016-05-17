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

package org.objectweb.cjdbc.scenario.raidb2;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb2Template;

/**
 * This class defines a Raidb2StaticSchemaScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class Raidb2StaticSchemaScenario extends SimpleRaidb2Template
{
  public void testDrop() throws Exception
  {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb2-static-schema.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      
      Connection con = getCJDBCConnection();
      con.createStatement().executeUpdate("Drop table REGION");
      
      con.createStatement().executeUpdate("create table region ( r_regionkey INTEGER NOT NULL, r_name CHAR(25), r_comment VARCHAR(152), PRIMARY KEY(r_regionkey))");
  }
}
