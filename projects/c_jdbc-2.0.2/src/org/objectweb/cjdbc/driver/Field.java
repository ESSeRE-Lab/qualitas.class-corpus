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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Nicolas Modrzyk, Marc Herbert.
 */

package org.objectweb.cjdbc.driver;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * Field is our private implementation of <code>ResultSetMetaData</code>,
 * holding the information for one column.
 * <p>
 * The first version was inspired from the MM MySQL driver by Mark Matthews.
 * 
 * @see org.objectweb.cjdbc.driver.DriverResultSet
 * @see org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @version 1.0
 */
public class Field implements Serializable
{
  //
  // This object is manually (de-)serialized below for compatibility with C.
  // It also implements Serializable for the convenience of Java-Java
  // communication (typically between controllers).
  //
  // Ideally:
  // (1) unneeded fields for Java-Java communication are all tagged as
  //    "transient"
  // (2) C-Java and Java-Java need to send the exact same fields.
  // And so:
  // (3) keeping up-to-date manual serialization below is easy: just check
  //    "transient" tags.

  private String  tableName;
  private String  fieldName;
  private int     columnDisplaySize;
  private int     sqlType;
  private String  typeName;
  private String  columnClassName;
  private boolean isAutoIncrement;
  private boolean isCaseSensitive;
  private boolean isCurrency;
  private int     isNullable;
  private boolean isReadOnly;
  private boolean isWritable;
  private boolean isDefinitelyWritable;
  private boolean isSearchable;
  private boolean isSigned;
  private int     precision;
  private int     scale;

  /**
   * Create a new field with some default common values.
   * 
   * @param table the table name
   * @param name the field name
   * @param columnDisplaySize the column display size
   * @param sqlType the SQL type
   * @param typeName the type name
   * @param columnClassName the column class name
   */
  public Field(String table, String name, int columnDisplaySize, int sqlType,
      String typeName, String columnClassName)
  {
    this(table, name, columnDisplaySize, sqlType, typeName, columnClassName,
        false, true, false, ResultSetMetaData.columnNullable, true, false,
        false, false, false, 0, 0);
  }

  /**
   * Creates a new <code>Field</code> instance.
   * 
   * @param table the table name
   * @param name the field name
   * @param columnDisplaySize the column display size
   * @param sqlType the SQL type
   * @param typeName the type name
   * @param columnClassName the column class name
   * @param isAutoIncrement true if field is auto incremented
   * @param isCaseSensitive true if field is case sensitive
   * @param isCurrency true if field is currency
   * @param isNullable indicates the nullability of the field
   * @param isReadOnly true if field is read only
   * @param isWritable true if field is writable
   * @param isDefinitelyWritable true if field is definetly writable
   * @param isSearchable true if field is searchable
   * @param isSigned true if field is signed
   * @param precision decimal precision
   * @param scale number of digits to right of decimal point
   */
  public Field(String table, String name, int columnDisplaySize, int sqlType,
      String typeName, String columnClassName, boolean isAutoIncrement,
      boolean isCaseSensitive, boolean isCurrency, int isNullable,
      boolean isReadOnly, boolean isWritable, boolean isDefinitelyWritable,
      boolean isSearchable, boolean isSigned, int precision, int scale)
  {
    if (table == null)
      this.tableName = null;
    else
      this.tableName = new String(table);
    this.fieldName = new String(name);
    this.columnDisplaySize = columnDisplaySize;
    this.sqlType = sqlType;
    this.typeName = typeName;
    this.columnClassName = columnClassName;
    this.isAutoIncrement = isAutoIncrement;
    this.isCaseSensitive = isCaseSensitive;
    this.isCurrency = isCurrency;
    this.isNullable = isNullable;
    this.isReadOnly = isReadOnly;
    this.isWritable = isWritable;
    this.isDefinitelyWritable = isDefinitelyWritable;
    this.isSearchable = isSearchable;
    this.isSigned = isSigned;
    this.precision = precision;
    this.scale = scale;
  }

  /**
   * Creates a new <code>Field</code> object, deserializing it from an input
   * stream. Has to mirror the serialization method below.
   * 
   * @param in input stream
   * @throws IOException if a stream error occurs
   */
  public Field(CJDBCInputStream in) throws IOException
  {
    if (in.readBoolean())
      this.tableName = in.readUTF();
    else
      this.tableName = null;

    this.fieldName = in.readUTF();
    this.columnDisplaySize = in.readInt();
    this.sqlType = in.readInt();
    this.typeName = in.readUTF();
    this.columnClassName = in.readUTF();
    this.isAutoIncrement = in.readBoolean();
    this.isCaseSensitive = in.readBoolean();
    this.isCurrency = in.readBoolean();
    this.isNullable = in.readInt();
    this.isReadOnly = in.readBoolean();
    this.isWritable = in.readBoolean();
    this.isDefinitelyWritable = in.readBoolean();
    this.isSearchable = in.readBoolean();
    this.isSigned = in.readBoolean();
    this.precision = in.readInt();
    this.scale = in.readInt();
  }

  /**
   * Serialize the <code>Field</code> on the output stream by sending only the
   * needed parameters to reconstruct it on the controller. Has to mirror the
   * deserialization method above.
   * 
   * @param out destination stream
   * @throws IOException if a stream error occurs
   */
  public void sendToStream(CJDBCOutputStream out) throws IOException
  {
    if (null == this.tableName)
      out.writeBoolean(false);
    else
    {
      out.writeBoolean(true);
      out.writeUTF(this.tableName);
    }

    out.writeUTF(this.fieldName);
    out.writeInt(this.columnDisplaySize);
    out.writeInt(this.sqlType);
    out.writeUTF(this.typeName);
    out.writeUTF(this.columnClassName);
    out.writeBoolean(this.isAutoIncrement);
    out.writeBoolean(this.isCaseSensitive);
    out.writeBoolean(this.isCurrency);
    out.writeInt(this.isNullable);
    out.writeBoolean(this.isReadOnly);
    out.writeBoolean(this.isWritable);
    out.writeBoolean(this.isDefinitelyWritable);
    out.writeBoolean(this.isSearchable);
    out.writeBoolean(this.isSigned);
    out.writeInt(this.precision);
    out.writeInt(this.scale);

  }

  /**
   * Gets the table name.
   * 
   * @return a <code>String</code> value
   */
  public String getTableName()
  {
    return tableName;
  }

  /**
   * Gets the field name.
   * 
   * @return a <code>String</code> value
   * @see #setFieldName
   */
  public String getFieldName()
  {
    return fieldName;
  }

  /**
   * Gets the full name: "tableName.fieldName"
   * 
   * @return a <code>String</code> value
   */
  public String getFullName()
  {
    return tableName + "." + fieldName;
  }

  /**
   * Sets the field name.
   * 
   * @param name the new field name
   * @see #getFieldName
   */
  public void setFieldName(String name)
  {
    fieldName = name;
  }

  /**
   * Returns the full name.
   * 
   * @return <code>String</code> value
   * @see #getFullName()
   */
  public String toString()
  {
    return getFullName();
  }

  /**
   * Returns the JDBC type code.
   * 
   * @return int Type according to {@link java.sql.Types}
   * @see java.sql.ResultSetMetaData#getColumnType(int)
   */
  public int getSqlType()
  {
    return sqlType;
  }

  /**
   * Returns the SQL type name used by the database.
   * 
   * @return the SQL type name
   * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
   */
  public String getTypeName()
  {
    return typeName;
  }

  /**
   * Returns the Java class used by the mapping.
   * 
   * @see java.sql.ResultSetMetaData#getColumnClassName(int)
   */
  public String getColumnClassName()
  {
    return columnClassName;
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
   */
  public int getColumnDisplaySize()
  {
    return columnDisplaySize;
  }

  /**
   * @see java.sql.ResultSetMetaData#isAutoIncrement(int)
   */
  public boolean isAutoIncrement()
  {
    return isAutoIncrement;
  }

  /**
   * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
   */
  public boolean isCaseSensitive()
  {
    return isCaseSensitive;
  }

  /**
   * @see java.sql.ResultSetMetaData#isCurrency(int)
   */
  public boolean isCurrency()
  {
    return isCurrency;
  }

  /**
   * @see java.sql.ResultSetMetaData#isDefinitelyWritable(int)
   */
  public boolean isDefinitelyWritable()
  {
    return isDefinitelyWritable;
  }

  /**
   * @see java.sql.ResultSetMetaData#isNullable(int)
   */
  public int isNullable()
  {
    return isNullable;
  }

  /**
   * @see java.sql.ResultSetMetaData#isReadOnly(int)
   */
  public boolean isReadOnly()
  {
    return isReadOnly;
  }

  /**
   * @see java.sql.ResultSetMetaData#isWritable(int)
   */
  public boolean isWritable()
  {
    return isWritable;
  }

  /**
   * @see java.sql.ResultSetMetaData#isSearchable(int)
   */
  public boolean isSearchable()
  {
    return isSearchable;
  }

  /**
   * @see java.sql.ResultSetMetaData#isSigned(int)
   */
  public boolean isSigned()
  {
    return isSigned;
  }

  /**
   * @see java.sql.ResultSetMetaData#getPrecision(int)
   */
  public int getPrecision()
  {
    return precision;
  }

  /**
   * @see java.sql.ResultSetMetaData#getScale(int)
   */
  public int getScale()
  {
    return scale;
  }

}