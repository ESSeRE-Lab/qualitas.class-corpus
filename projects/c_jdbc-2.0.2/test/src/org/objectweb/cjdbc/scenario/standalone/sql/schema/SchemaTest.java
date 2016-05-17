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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.sql.schema;

import java.sql.Types;

import org.objectweb.cjdbc.common.sql.schema.AliasedDatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * Test schema validation of the database.
 */
public class SchemaTest extends NoTemplate
{
  /**
   * Test <code>AliasedDatabaseTable</code> objects
   *
   */
  public void testAliasedDatabaseTable()
  {
    AliasedDatabaseTable at1, at2, at3, at4;
    DatabaseTable t1 = new DatabaseTable("buy_now", 5);
    DatabaseColumn c1 = new DatabaseColumn("id", true);
    DatabaseColumn c2 = new DatabaseColumn("buyer_id", false);
    DatabaseColumn c3 = new DatabaseColumn("item_id", false);
    DatabaseColumn c4 = new DatabaseColumn("qty", false);
    DatabaseColumn c5 = new DatabaseColumn("date", false);

    t1.addColumn(c1);
    t1.addColumn(c2);
    t1.addColumn(c3);
    t1.addColumn(c4);
    t1.addColumn(c5);

    DatabaseTable t2 = new DatabaseTable("buy_now2", 5);
    t2.addColumn(c1);
    t2.addColumn(c2);
    t2.addColumn(c3);
    t2.addColumn(c4);
    t2.addColumn(c5);

    at1 = new AliasedDatabaseTable(t1, "alias1");
    at2 = new AliasedDatabaseTable(t1, "alias2");
    at3 = new AliasedDatabaseTable(t2, "alias1");
    at4 = new AliasedDatabaseTable(t1, "alias1");

    assertTrue(at1.equals(at1));
    assertFalse(at1.equals(at2));
    assertFalse(at1.equals(at3));
    assertTrue(at1.equals(at4));
  }

  /**
   * Test validity of columns in schema
   *
   */
  public void testDatabaseColumn()
  {
    DatabaseColumn c1, c2, c3, c4, c5, c6;
    c1 = new DatabaseColumn("foo", true);
    c2 = new DatabaseColumn("foo", true);
    c3 = new DatabaseColumn("foo", false);

    c4 = new DatabaseColumn("foo", true, Types.INTEGER);
    c5 = new DatabaseColumn("foo", true, Types.VARCHAR);
    c6 = new DatabaseColumn("foo", true, Types.NULL);

    // Test equals
    assertTrue(c1.equals(c2));
    assertFalse(c1.equals(c3));
    assertFalse(c1.equals(c4));
    assertFalse(c1.equals(c5));
    assertTrue(c1.equals(c6));

    // Test equals Ignore Types
    assertTrue(c1.equalsIgnoreType(c2));
    assertFalse(c1.equalsIgnoreType(c3));
    assertTrue(c1.equalsIgnoreType(c4));
    assertTrue(c1.equalsIgnoreType(c5));
    assertTrue(c1.equalsIgnoreType(c6));
    assertTrue(c4.equalsIgnoreType(c5));
    assertTrue(c4.equalsIgnoreType(c6));

  }

  /**
   * Test validity of column in schema
   *
   */
  public void testTableColumn()
  {
    TableColumn t1, t2, t3, t4;
    t1 = new TableColumn("table1", "name1");
    t2 = new TableColumn("table1", "name1");
    t3 = new TableColumn("table1", "name2");
    t4 = new TableColumn("table2", "name1");

    assertTrue(t1.equals(t1));
    assertTrue(t1.equals(t2));
    assertFalse(t1.equals(t3));
    assertFalse(t1.equals(t4));
  }
}