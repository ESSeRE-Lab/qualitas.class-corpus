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

package org.objectweb.cjdbc.scenario.tools.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Random;

import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSQLMetaData;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;

/**
 * This class defines a QueryGenerator
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class QueryGenerator
{
  Integer        maxUniqueId = new Integer(2000050);
  Random         rand;
  double         readWriteRatio;
  double         writeDeleteRatio;
  double         insertUpdateRatio;
  double         orderByRatio;
  DatabaseSchema schema;
  ArrayList      tables;
  int            tableSize;
  private double schemaUpdateRatio;
  private double createDropRatio;

  //Connection connection;

  /**
   * Test application
   * 
   * @param args not needed
   * @throws Exception yes!
   */
  public static void main(String[] args) throws Exception
  {
    Class.forName("org.objectweb.cjdbc.driver.Driver");
    Connection connection = DriverManager.getConnection(
        "jdbc:cjdbc://localhost/myDB", "user", "");

    QueryGenerator generator = new QueryGenerator(connection);
    String sql;
    for (int i = 0; i < 20000; i++)
    {
      sql = generator.generateWriteQuery();
      System.out.println(sql);
      synchronized (generator)
      {
        generator.wait(10);
      }
      connection.createStatement().execute(sql);
    }
  }

  /**
   * Creates a new <code>QueryGenerator</code> object
   */
  public QueryGenerator(DatabaseSchema schema)
  {
    rand = new Random();
    readWriteRatio = 0.5;
    writeDeleteRatio = 0.8;
    insertUpdateRatio = 0.5;
    orderByRatio = 0.9;
    schemaUpdateRatio = 0.1;
    createDropRatio = 0.8;
    this.schema = schema;
    tables = schema.getTables();
    tableSize = tables.size();
  }

  /**
   * Creates a new <code>QueryGenerator</code> object
   * 
   * @param connection used to retrieve the schema
   * @throws SQLException if fails to retrieve the schema with the given
   *                 connection
   */
  public QueryGenerator(Connection connection) throws SQLException
  {
    this(new DatabaseSQLMetaData(Trace.getLogger("niko"), connection, 4, false,null)
        .createDatabaseSchema());
    //this.connection = connection;
  }

  /**
   * Generate a read query
   * 
   * @return a query that will not udpate the data of the database
   */
  public final String generateReadQuery()
  {
    return generateSelectQuery();
  }

  /**
   * Generate a select query
   * 
   * @return "SELECT ..."
   */
  public final String generateSelectQuery()
  {
    StringBuffer buffer = new StringBuffer();
    DatabaseTable table = getTable();

    ArrayList columns = table.getColumns();
    int columnSize = columns.size();

    buffer.append("SELECT ");
    int next = rand.nextInt(columnSize + 1);
    if (next >= columnSize)
    {
      buffer.append(" * ");
    }
    else
    {
      for (int i = 0; i <= next; i++)
      {
        if (i != 0)
          buffer.append(",");
        String name = ((DatabaseColumn) columns.get(i)).getName();
        //System.out.println("Check:"+name);
        buffer.append(name);
      }
    }
    buffer.append(" FROM ");

    buffer.append(table.getName());
    if (rand.nextBoolean())
    {
      buffer.append(getRelation(table, true));
    }
    if (nextIsInRatio(orderByRatio))
      buffer.append(generateOrderBy(table));
    return buffer.toString();
  }

  /**
   * Generate order by statement of a read query
   * 
   * @param table target sql table of the read query
   * @return "ORDER BY ..."
   */
  private String generateOrderBy(DatabaseTable table)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(" ORDER BY ");
    ArrayList columns = table.getColumns();
    int colSize = columns.size();
    int nb = rand.nextInt(colSize);
    for (int i = 0; i <= nb; i++)
    {
      if (i != 0)
        buffer.append(",");
      buffer.append(((DatabaseColumn) columns.get(i)).getName());
      int aa = rand.nextInt(3);
      switch (aa)
      {
        case 0 :
          buffer.append(" ASC ");
          break;
        case 1 :
          buffer.append(" DESC ");
          break;
        case 2 :
          break;
        default :
          throw new RuntimeException("Unexpected value in generateOrderBy");
      }
    }
    return buffer.toString();
  }

  private String getRelation(DatabaseTable table, boolean useLowerAndGreater)
  {
    ArrayList unique = table.getUniqueColumns();
    int uniqueSize = unique.size();
    StringBuffer buffer = new StringBuffer();
    if (uniqueSize > 0)
    {
      buffer.append(" WHERE ");
      int nextU = rand.nextInt(uniqueSize);
      DatabaseColumn col = (DatabaseColumn) unique.get(nextU);
      buffer.append(col.getName());
      if (useLowerAndGreater)
      {
        int ee = rand.nextInt(3);
        if (ee == 0)
          buffer.append("<");
        if (ee == 1)
          buffer.append("=");
        if (ee == 2)
          buffer.append(">");
      }
      else
      {
        buffer.append("=");
      }
      buffer.append(rand.nextInt(getMaxUniqueId()));
    }
    return buffer.toString();
  }

  private int getMaxUniqueId()
  {
    synchronized (maxUniqueId)
    {
      return maxUniqueId.intValue();
    }
  }

  private int getNextMaxUniqueId()
  {
    synchronized (maxUniqueId)
    {
      int max = (maxUniqueId.intValue());
      max++;
      maxUniqueId = new Integer(max);
      return maxUniqueId.intValue();
    }
  }

  private DatabaseTable getTable()
  {
    int next = rand.nextInt(tableSize);
    return (DatabaseTable) tables.get(next);
  }

  /**
   * Generate a write query. This depends on the different ratio set for the
   * query generator instance.
   * 
   * @return an Insert, Update or Delete query
   */
  public final String generateWriteQuery()
  {
    if (nextIsInRatio(schemaUpdateRatio))
      return generateSchemaUpdateQuery();
    if (nextIsInRatio(writeDeleteRatio))
      if (nextIsInRatio(insertUpdateRatio))
        return generateInsert();
      else
        return generateUpdate();
    else
      return generateDelete();
  }

  public final String generateSchemaUpdateQuery()
  {
    if (nextIsInRatio(createDropRatio))
      return generateCreateQuery();
    else
      return generateDropQuery();
  }

  public final String generateCreateQuery()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("CREATE TABLE temp");
    buffer.append(getRandomString(20));
    buffer.append("( ");
    int columns = rand.nextInt(10)+1;
    for (int i = 0; i < columns; i++)
    {
      if (i != 0)
        buffer.append(",");
      buffer.append(getRandomString(20) + " " + getRandomType());
    }
    buffer.append(")");
    return buffer.toString();
  }

  public final String generateDropQuery()
  {
    return "DROP TABLE " + getRandomTable();
  }

  private boolean nextIsInRatio(double ratio)
  {
    double next = rand.nextDouble();
    if (next < ratio)
      return true;
    else
      return false;
  }

  /**
   * Generate a delete query
   * 
   * @return "DELETE FROM ..."
   */
  public final String generateDelete()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("DELETE FROM ");
    DatabaseTable table = getTable();
    buffer.append(table.getName());
    buffer.append(getRelation(table, false));
    return buffer.toString();
  }

  /**
   * Generate an update query
   * 
   * @return "UPDATE ..."
   */
  public final String generateUpdate()
  {
    StringBuffer buffer = new StringBuffer();
    DatabaseTable table = getTable();
    buffer.append("UPDATE ");
    buffer.append(table.getName());
    buffer.append(" SET ");
    DatabaseColumn column = getRandomNonUniqueColumn(table);
    buffer.append(column.getName() + "=" + getRandomValue(column));
    buffer.append(getRelation(table, true));
    return buffer.toString();
  }

  private DatabaseColumn getRandomNonUniqueColumn(DatabaseTable table)
  {
    return getRandomColumn(table, false);
  }

  private DatabaseColumn getRandomColumn(DatabaseTable table,
      boolean allowUnique)
  {
    ArrayList columns = table.getColumns();
    int size = columns.size();
    int next = rand.nextInt(size);
    DatabaseColumn column = (DatabaseColumn) columns.get(next);
    if (column.isUnique() && !allowUnique)
      return getRandomNonUniqueColumn(table);
    else
      return column;
  }

  /**
   * Generate an insert query
   * 
   * @return "INSERT INTO ..."
   */
  public final String generateInsert()
  {
    StringBuffer buffer = new StringBuffer();
    StringBuffer buffer2 = new StringBuffer();
    DatabaseTable table = getTable();
    buffer.append("INSERT INTO ");
    buffer.append(table.getName());
    buffer.append(" (");
    buffer2.append(" (");
    ArrayList columns = table.getColumns();
    int colSize = columns.size();
    DatabaseColumn column;
    for (int i = 0; i < colSize; i++)
    {
      if (i != 0)
      {
        buffer.append(",");
        buffer2.append(",");
      }
      column = (DatabaseColumn) columns.get(i);
      buffer.append(column.getName());
      buffer2.append(getRandomValue(column));
    }
    buffer.append(") ");
    buffer2.append(") ");
    buffer.append("VALUES " + buffer2);
    return buffer.toString();
  }

  private final String getRandomValue(DatabaseColumn column)
  {
    int type = column.getType();
    switch (type)
    {
      case Types.VARCHAR :
        return "'" + getRandomString(20) + "'";
      case Types.INTEGER :
        if (column.isUnique())
          return String.valueOf(getNextMaxUniqueId());
        else
          return String.valueOf(rand.nextInt(Integer.MAX_VALUE));
      case Types.DOUBLE :
        return String.valueOf(rand.nextDouble());
      case Types.DECIMAL :
        return String.valueOf(rand.nextDouble()
            * rand.nextInt(Integer.MAX_VALUE));
      default :
        return "0";
    }
  }

  private final String getRandomString(int size)
  {
    StringBuffer buffer = new StringBuffer();
    int length = rand.nextInt(size)+1;
    for (int i = 0; i < length; i++)
      buffer.append(Character.forDigit(rand.nextInt(Character.MAX_RADIX),
          Character.MAX_RADIX));
    return buffer.toString();
  }

  private final String getRandomType()
  {
    int type = rand.nextInt(4);
    switch (type)
    {
      case 0 :
        return "INT";
      case 1 :
        return "VARCHAR";
      case 2 :
        return "DECIMAL";
      case 3 :
        return "BOOLEAN";
      default :
        return null;
    }
  }

  private String getRandomTable()
  {
    ArrayList tables = schema.getTables();
    int size = tables.size();
    return ((DatabaseTable) tables.get(rand.nextInt(size))).getName();
  }

  /**
   * Returns the readWriteRatio value.
   * 
   * @return Returns the readWriteRatio.
   */
  public final double getReadWriteRatio()
  {
    return readWriteRatio;
  }

  /**
   * Sets the readWriteRatio value.
   * 
   * @param readWriteRatio The readWriteRatio to set.
   */
  public final void setReadWriteRatio(double readWriteRatio)
  {
    this.readWriteRatio = readWriteRatio;
  }

  /**
   * Main query generator method.
   * 
   * @return a read or write query depending on the setting of the related ratio
   */
  public final String generateQuery()
  {
    if (nextIsInRatio(readWriteRatio))
      return generateReadQuery();
    else
      return generateWriteQuery();
  }

  /**
   * Returns the insertUpdateRatio value.
   * 
   * @return Returns the insertUpdateRatio.
   */
  public final double getInsertUpdateRatio()
  {
    return insertUpdateRatio;
  }

  /**
   * Sets the insertUpdateRatio value.
   * 
   * @param insertUpdateRatio The insertUpdateRatio to set.
   */
  public final void setInsertUpdateRatio(double insertUpdateRatio)
  {
    this.insertUpdateRatio = insertUpdateRatio;
  }

  /**
   * Returns the orderByRatio value.
   * 
   * @return Returns the orderByRatio.
   */
  public final double getOrderByRatio()
  {
    return orderByRatio;
  }

  /**
   * Sets the orderByRatio value.
   * 
   * @param orderByRatio The orderByRatio to set.
   */
  public final void setOrderByRatio(double orderByRatio)
  {
    this.orderByRatio = orderByRatio;
  }

  /**
   * Returns the writeDeleteRatio value.
   * 
   * @return Returns the writeDeleteRatio.
   */
  public final double getWriteDeleteRatio()
  {
    return writeDeleteRatio;
  }

  /**
   * Sets the writeDeleteRatio value.
   * 
   * @param writeDeleteRatio The writeDeleteRatio to set.
   */
  public final void setWriteDeleteRatio(double writeDeleteRatio)
  {
    this.writeDeleteRatio = writeDeleteRatio;
  }

  /**
   * Returns the createDropRatio value.
   * 
   * @return Returns the createDropRatio.
   */
  public double getCreateDropRatio()
  {
    return createDropRatio;
  }

  /**
   * Sets the createDropRatio value.
   * 
   * @param createDropRatio The createDropRatio to set.
   */
  public void setCreateDropRatio(double createDropRatio)
  {
    this.createDropRatio = createDropRatio;
  }

  /**
   * Returns the schemaUpdateRatio value.
   * 
   * @return Returns the schemaUpdateRatio.
   */
  public double getSchemaUpdateRatio()
  {
    return schemaUpdateRatio;
  }

  /**
   * Sets the schemaUpdateRatio value.
   * 
   * @param schemaUpdateRatio The schemaUpdateRatio to set.
   */
  public void setSchemaUpdateRatio(double schemaUpdateRatio)
  {
    this.schemaUpdateRatio = schemaUpdateRatio;
  }
}
