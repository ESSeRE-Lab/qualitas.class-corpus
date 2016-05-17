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
 * Contributor(s): Sara Bouchenak.
 */

package org.objectweb.cjdbc.scenario.standalone.backend;

import java.sql.SQLException;

import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.controller.backend.DatabaseBackendMetaData;
import org.objectweb.cjdbc.controller.backend.DatabaseBackendSchemaConstants;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.databases.AbstractDatabase;
import org.objectweb.cjdbc.scenario.tools.databases.RUBiSDatabase;
import org.objectweb.cjdbc.scenario.tools.databases.TPCWDatabase;
import org.objectweb.cjdbc.scenario.tools.mock.MockConnectionManager;

/**
 * <code>DatabaseBackendMetaData</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.backend.DatabaseBackendMetaData
 */
public class DatabaseBackendMetaDataTest extends NoTemplate
{

  /**
   * @see org.objectweb.cjdbc.controller.backend#createDatabaseSchema()
   */
  public void testCreateDatabaseSchema()
  {
    // legacy test
    //    ArrayList tables = metaData.getDatabaseSchema().getTables();
    //    int size = tables.size();
    //    System.out.println(size + " tables found");
    //    DatabaseTable table = null;
    //    DatabaseColumn column = null;
    //    ArrayList columns = null;
    //    int nbColumns;
    //    int i, j;
    //    for (i = 0; i < size; i++)
    //    {
    //      table = (DatabaseTable) tables.get(i);
    //      System.out.println("Table found: " + table.getName());
    //      columns = table.getColumns();
    //      nbColumns = columns.size();
    //      System.out.println(nbColumns + " columns found for this table");
    //      for (j = 0; j < nbColumns; j++)
    //      {
    //        column = (DatabaseColumn) columns.get(j);
    //        System.out.println(" Column found: " + column.getName());
    //        if (column.isUnique())
    //          System.out.println(" Is unique: true");
    //        System.out.println(" Column type: " + column.getType());
    //      }
    //    }
    performTest(new RUBiSDatabase());
    performTest(new TPCWDatabase());
  }

  /**
   * Checks that the meta data of the given database.
   * 
   * @param database the <code>AbstractDatabase</code> to test.
   */
  private void performTest(AbstractDatabase database)
  {
    AbstractConnectionManager cm = new MockConnectionManager(database);
    Trace l = Trace.getLogger("org.objectweb.cjdbc.controller.backend.test");
    DatabaseBackendMetaData metaData = new DatabaseBackendMetaData(cm, l,
        DatabaseBackendSchemaConstants.DynamicPrecisionColumn, false,null);

    DatabaseSchema schema = null;
    try
    {
      schema = metaData.getDatabaseSchema();
    }
    catch (SQLException e)
    {
      fail("Failed to gather schema from database meta data (" + e + ")");
    }
    assertTrue(schema.isCompatibleWith(database.getSchema()));
    assertTrue(database.getSchema().isCompatibleWith(schema));
  }
}
