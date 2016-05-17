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
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * <code>DatabaseSchema</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema
 */
public class DatabaseSchemaTest extends NoTemplate
{
  /** Database schema. */
  private DatabaseSchema s1, s2, s3, s4, s5, s6;

  /** Database table. */
  private DatabaseTable  t1, t2, t3, t4, t5, t6;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    t1 = new DatabaseTable("categories", 2);
    t1.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t1.addColumn(new DatabaseColumn("name", false, Types.VARCHAR));

    t2 = new DatabaseTable("regions", 2);
    t2.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t2.addColumn(new DatabaseColumn("name", false, Types.VARCHAR));

    t3 = new DatabaseTable("users", 10);
    t3.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t3.addColumn(new DatabaseColumn("firstname", false, Types.VARCHAR));
    t3.addColumn(new DatabaseColumn("lastname", false, Types.VARCHAR));
    t3.addColumn(new DatabaseColumn("nickname", false, Types.VARCHAR));
    t3.addColumn(new DatabaseColumn("password", false, Types.VARCHAR));
    t3.addColumn(new DatabaseColumn("email", false, Types.VARCHAR));
    t3.addColumn(new DatabaseColumn("rating", false, Types.INTEGER));
    t3.addColumn(new DatabaseColumn("balance", false, Types.FLOAT));
    t3.addColumn(new DatabaseColumn("creation_date", false, Types.TIMESTAMP));
    t3.addColumn(new DatabaseColumn("region", false, Types.INTEGER));

    t4 = new DatabaseTable("regions", 2);
    t4.addColumn(new DatabaseColumn("id", true, Types.FLOAT));
    t4.addColumn(new DatabaseColumn("nickname", false, Types.VARCHAR));

    t5 = new DatabaseTable("regions", 2);
    t5.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t5.addColumn(new DatabaseColumn("name", true, Types.VARCHAR));

    t6 = new DatabaseTable("regions", 1);
    t6.addColumn(new DatabaseColumn("country", true, Types.VARCHAR));

    s1 = new DatabaseSchema();

    s2 = new DatabaseSchema(2);
    s2.addTable(t1);
    s2.addTable(t2);

    s3 = new DatabaseSchema(1);
    s3.addTable(t1);

    s4 = new DatabaseSchema(2);
    s4.addTable(t1);
    s4.addTable(t4);

    s5 = new DatabaseSchema(3);
    s5.addTable(t1);
    s5.addTable(t2);
    s5.addTable(t5);

    s6 = new DatabaseSchema(1);
    s6.addTable(t3);
    s6.addTable(t6);
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#addTable(DatabaseTable)
   */
  public void testAddTable()
  {
    assertTrue(s2.hasTable("categories"));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#removeTable(DatabaseTable)
   */
  public void testRemoveTable()
  {
    s2.removeTable(t2);
    assertFalse(s2.hasTable("regions"));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#getTables()
   */
  public void testGetTables()
  {
    ArrayList tables;
    tables = new ArrayList();
    assertEquals(tables, s1.getTables());

    tables = new ArrayList();
    tables.add(t1);
    tables.add(t2);
    assertEquals(tables, s2.getTables());
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#getTable(String)
   */
  public void testGetTable()
  {
    assertEquals(t1, s2.getTable("categories"));
    assertEquals(t2, s2.getTable("regions"));
    assertNull(s2.getTable("users"));
    assertNull(s2.getTable(null));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#hasTable(String)
   */
  public void testHasTable()
  {
    assertTrue(s2.hasTable("categories"));
    assertTrue(s2.hasTable("regions"));
    assertFalse(s2.hasTable("users"));
    assertFalse(s2.hasTable(null));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#isCompatibleSubset(DatabaseSchema)
   */
  public void testIsCompatibleSubset()
  {
    assertTrue(s1.isCompatibleSubset(s1));
    assertTrue(s2.isCompatibleSubset(s2));
    assertFalse(s2.isCompatibleSubset(s3));
    assertFalse(s2.isCompatibleSubset(s4));
    assertTrue(s2.isCompatibleSubset(s5));
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#mergeSchema(DatabaseSchema)
   */
  public void testMergeSchema()
  {
    try
    {
      s2.mergeSchema(s2);
      s2.mergeSchema(s6);
    }
    catch (SQLException e)
    {
      fail("Exception thrown while merging two compatible schemas: " + e);
    }
    assertEquals(t3, s2.getTable("users"));
    assertEquals(new DatabaseColumn("country", true, Types.VARCHAR), s2
        .getTable("regions").getColumn("country"));

    try
    {
      s2.mergeSchema(s4);
      s2.mergeSchema(s5);
      fail("Exception not thrown while merging two not compatible schemas");
    }
    catch (SQLException ignore)
    {
    }
  }

  /**
   * org.objectweb.cjdbc.common.sql..schema.DatabaseSchema#equals(Object)
   */
  public void testEquals()
  {
    assertFalse(s1.equals(s2));
    assertFalse(s1.equals(s3));
    assertFalse(s1.equals(s4));
    assertFalse(s1.equals(s5));

    s1.addTable(t1);
    s1.addTable(t2);
    s1.addTable(t3);
    assertFalse(s1.equals(s2));

    s2.addTable(t3);
    assertTrue(s1.equals(s2));
  }
}
