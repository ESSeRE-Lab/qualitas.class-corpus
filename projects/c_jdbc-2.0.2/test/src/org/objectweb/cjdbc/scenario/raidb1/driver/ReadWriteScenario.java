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
 *  Initial developer(s): Marck Wick.
 *  Contributor(s): Emmanuel Cecchet.
 */
package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * Read after Write test class. Problem submitted by Mark Wick on the mailing
 * list: http://www.objectweb.org/wws/arc/c-jdbc/2003-09/msg00072.html
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 */
public class ReadWriteScenario extends Raidb1Template
{
  
  /**
   * Test method for clobs.
   *@throws Exception possibly ...
   */
  public void testReadWrite() throws Exception
  {
      Connection con = getCJDBCConnection();
      Statement stmt = con.createStatement();
      String tableName = "test"+ System.currentTimeMillis();
      stmt.executeUpdate("create table "+tableName+" (id INTEGER)");
      stmt.executeUpdate("delete from "+tableName);
      Hashtable ht = new Hashtable();
      for (int i = 1; i < 100; i++)
      {
        int counter = 1;
        stmt.executeUpdate("insert into "+tableName+" (id) values (" + i + ")");
        while (counter < 50)
        {
          ResultSet rs = stmt.executeQuery("select max(id) as id from "+tableName);
          rs.next();
          if (rs.getInt("id") != i)
            fail(
              "Read after write broken: id value ("
                + rs.getInt("id")
                + ") different from expected value ("
                + i
                + ")");
          rs.close();
          counter++;
        }
        Integer tot = (Integer) ht.get(new Integer(counter));
        if (tot == null)
        {
          tot = new Integer(1);
          ht.put(new Integer(counter), tot);
        }
        else
        {
          ht.put(new Integer(counter), new Integer(tot.intValue() + 1));
        }
      }
      con.createStatement().executeUpdate("drop table "+tableName);
      con.close();
  }

}
