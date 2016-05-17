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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.sql.schema;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * <code>DatabaseTable</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * org.objectweb.cjdbc.common.sql..schema.DatabaseTable
 */
public class DatabaseTableTest extends NoTemplate
{
  /** Database table. */
  private DatabaseTable t1, t2, t3, t4, t5, t6;

  /** Database column. */
  private DatabaseColumn c1, c2, c3, c4, c5, c6, c7, c8;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    c1 = new DatabaseColumn("id", true, Types.INTEGER);
    c2 = new DatabaseColumn("name", false, Types.VARCHAR);
    c3 = new DatabaseColumn("rating", false, Types.INTEGER);
    c4 = new DatabaseColumn("balance", false, Types.FLOAT);
    c5 = new DatabaseColumn("creation_date", false, Types.TIMESTAMP);

    c6 = new DatabaseColumn("nickname", false, Types.VARCHAR);
    c7 = new DatabaseColumn("comment", false, Types.VARCHAR);
    c8 = new DatabaseColumn("name", true, Types.VARCHAR);

    t1 = new DatabaseTable("users", 5);
    t1.addColumn(c1);
    t1.addColumn(c2);
    t1.addColumn(c3);
    t1.addColumn(c4);
    t1.addColumn(c5);

    t2 = new DatabaseTable("users-merge-compatible", 2);
    t2.addColumn(c6);
    t2.addColumn(c7);

    t3 = new DatabaseTable("users-merge-incompatible", 3);
    t3.addColumn(c6);
    t3.addColumn(c7);
    t3.addColumn(c8);

    // same as users table
    t4 = new DatabaseTable("users", 5);
    t4.addColumn(c1);
    t4.addColumn(c2);
    t4.addColumn(c3);
    t4.addColumn(c4);
    t4.addColumn(c5);

    // same as users table but name is unique
    t5 = new DatabaseTable("users", 5);
    t5.addColumn(c1);
    t5.addColumn(c2);
    t5.addColumn(c3);
    t5.addColumn(c8);
    t5.addColumn(c5);

    t6 = new DatabaseTable("users", 1);
    t6.addColumn(c6);
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseTable#mergeColumns(DatabaseTable)
   */
  public void testMergeColumns()
  {
    try
    {
      t1.mergeColumns(t3);
      fail("SQLException not thrown with two incompatible tables");
    }
    catch (SQLException ignore)
    {
    }

    try
    {
      t1.mergeColumns(t2);
      t1.mergeColumns(t1);
    }
    catch (SQLException e)
    {
      fail("SQLException thrown with two compatible tables (" + e + ")");
    }

    assertTrue(t1.getColumn("nickname").equals(c6));
    assertTrue(t1.getColumn("comment").equals(c7));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseTable#getColumns()
   */
  public void testGetColumns()
  {
    ArrayList columns = new ArrayList();
    columns.add(c1);
    columns.add(c2);
    columns.add(c3);
    columns.add(c4);
    columns.add(c5);

    assertEquals(columns, t1.getColumns());
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseTable#getColumn(String)
   */
  public void testGetColumn()
  {
    assertEquals(t1.getColumn("id"), c1);
    assertEquals(t1.getColumn("ID"), c1);
    assertNull(t1.getColumn("not_found"));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseTable#getName()
   */
  public void testGetName()
  {
    assertEquals("users", t1.getName());
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseTable#equals(Object)
   */
  public void testEquals()
  {
    assertTrue(t1.equals(t1));
    assertFalse(t1.equals(t2));
    assertFalse(t1.equals(t3));
    assertTrue(t1.equals(t4));
    assertFalse(t1.equals(t5));
    assertFalse(t1.equals(t6));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseTable#equalsIgnoreType(Object)
   */
  public void testEqualsIgnoreType()
  {
    assertTrue(t1.equalsIgnoreType(t1));
    assertFalse(t1.equalsIgnoreType(t2));
    assertFalse(t1.equalsIgnoreType(t3));
    assertTrue(t1.equalsIgnoreType(t4));
    assertFalse(t1.equalsIgnoreType(t5));
    assertFalse(t1.equalsIgnoreType(t6));
  }
}
