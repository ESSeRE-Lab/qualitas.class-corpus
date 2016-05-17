/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 Emic Networks
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
 * Initial developer(s): Marc Herbert
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.driver.protocol;

import java.io.IOException;
import java.sql.Types;

import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * This class implements protocol type tags with an internal String, but offers
 * an abstract interface on top of it in order to be transparently substituted
 * some day (with enums for instance).
 * <p>
 * Advantages of using string types is human-readability (debugging, trace
 * analysis, etc.) and earlier detection in case of protocol corruption.
 * Drawback maybe a small performance cost.
 * <p>
 * Check "the importance of being textual" - by Eric S. Raymond.
 * http://www.faqs.org/docs/artu/ch05s01.html
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @version 1.0
 */
public final class TypeTag
{
  private String              internalString;


  /** ** Actual Types *** */
  private static final String TPREFIX          = "T-";

  /* SQL "objects" */
  /*
   * The reference is table 47.9.3 "JDBC Types Mapped to Java Object Types" in
   * 2nd edition (JDBC 2.1) of the book "JDBC API Tutorial and reference", or
   * table 50.3 in the 3rd edition (JDBC 3.0). Also available online in Sun's
   * "JDBC Technology Guide: Getting Started", section "Mapping SQL and Java
   * Types".
   */
  /* unsupported are: DISTINCT, Array, Struct/SQLData, Ref, JAVA_OBJECT */
  /* JDBC 3.0 added the type: java.net.URL, also currently unsupported */

  /** Constant for a SQL/Java type */
  public static final TypeTag TYPE_ERROR       = new TypeTag(
                                                   TPREFIX
                                                       + "Type not found or unsupported");
  /** Constant for a SQL/Java type */
  public static final TypeTag STRING           = new TypeTag(TPREFIX + "String");
  /** Constant for a SQL/Java type */
  public static final TypeTag BIGDECIMAL       = new TypeTag(TPREFIX
                                                   + "BigDecimal");
  /** Constant for a SQL/Java type */
  public static final TypeTag BOOLEAN          = new TypeTag(TPREFIX
                                                   + "Boolean");
  /** Constant for a SQL/Java type */
  public static final TypeTag INTEGER          = new TypeTag(TPREFIX
                                                   + "Integer");
  /** Constant for a SQL/Java type */
  public static final TypeTag LONG             = new TypeTag(TPREFIX + "Long");
  /** Constant for a SQL/Java type */
  public static final TypeTag FLOAT            = new TypeTag(TPREFIX + "Float");
  /** Constant for a SQL/Java type */
  public static final TypeTag DOUBLE           = new TypeTag(TPREFIX + "Double");
  /** Constant for a SQL/Java type */
  public static final TypeTag BYTE_ARRAY       = new TypeTag(TPREFIX + "Byte[]");
  /** Constant for a SQL/Java type */
  public static final TypeTag SQL_DATE         = new TypeTag(TPREFIX
                                                   + "SqlDate");
  /** Constant for a SQL/Java type */
  public static final TypeTag SQL_TIME         = new TypeTag(TPREFIX
                                                   + "SqlTime");
  /** Constant for a SQL/Java type */
  public static final TypeTag SQL_TIMESTAMP    = new TypeTag(TPREFIX
                                                   + "SqlTimestamp");
  /** Constant for a SQL/Java type */
  public static final TypeTag CLOB             = new TypeTag(TPREFIX + "Clob");
  /** Constant for a SQL/Java type */
  public static final TypeTag BLOB             = new TypeTag(TPREFIX + "Blob");

  /* Structs */
  
  /** Constant for a SQL structure */
  public static final TypeTag RESULTSET        = new TypeTag(TPREFIX
                                                   + "ResultSet");
  /** Null ResultSet */
  public static final TypeTag NULL_RESULTSET   = new TypeTag("null ResultSet");

  /** Constant for a SQL structure */
  public static final TypeTag FIELD            = new TypeTag(TPREFIX + "Field");
  /** Constant for a SQL structure */
  public static final TypeTag COL_TYPES        = new TypeTag(TPREFIX
                                                   + "Column types");
  /** Constant for a SQL structure */
  public static final TypeTag ROW              = new TypeTag(TPREFIX + "Row");

  
  /** used when there is no type ambiguity; no need to type */
  public static final TypeTag NOT_EXCEPTION    = new TypeTag("OK");

  /** Constant for an exception */
  public static final TypeTag EXCEPTION    = new TypeTag(TPREFIX
                                                   + "Exception");
  /** Constant for an exception */
  public static final TypeTag BACKEND_EXCEPTION    = new TypeTag(TPREFIX
                                                   + "BackendException");
  /** Constant for an exception */
  public static final TypeTag CORE_EXCEPTION  = new TypeTag(TPREFIX
                                                   + "CoreException");

  /** Constant for internal protocol data */
  public static final TypeTag CONTROLLER_READY = new TypeTag("Ready");

  private TypeTag(String init)
  {
    this.internalString = init;
  }

  /**
   * Read/deserialize/construct a TypeTag from a stream.
   * 
   * @param in input stream
   * @throws IOException stream error
   */
  public TypeTag(CJDBCInputStream in) throws IOException
  {
    this(in.readUTF());
  }

  /**
   * Serialize "this" tag on the stream.
   * 
   * @param out output stream
   * @throws IOException stream error
   */
  public void sendToStream(CJDBCOutputStream out) throws IOException
  {
    out.writeUTF(this.internalString);
  }

  /**
   * Calling this method is a bug, check the type of your argument.
   * 
   * @param o compared object (which should be a TypeTag!)
   * @return a buggy result
   * @see java.lang.Object#equals(java.lang.Object)
   * @deprecated
   */
  public boolean equals(Object o)
  {
    System.err
        .println("internal bug: TypeTag was compared with something else at:");
    (new Throwable()).printStackTrace();
    return false;
  }

  /**
   * Compares two TypeTags.
   * 
   * @param b compared TypeTag
   * @return true if same value
   */
  public boolean equals(TypeTag b)
  {
    /**
     * Even if the constants above are static, we cannot use "==" since we also
     * have to consider objects built from the stream
     * {@link #TypeTag(CJDBCInputStream)}
     */
    /*
     * We could reimplement String.equals here without the instanceof in order
     * to get back a couple of CPU cycles.
     */
    return this.internalString.equals(b.internalString);
  }

  /**
   * Returns a string representation, useful for logging and debugging.
   * 
   * @return string representation of the tag
   */
  public String toSring()
  {
    return internalString;
  }

  /**
   * Gives the standard JDBC type to Java Object type conversion according to
   * table "JDBC type to Java Object Type" of the JDBC reference book. (Table
   * 47.9.3 in 2nd Edition, table 50.3 in 3rd edition). This is the conversion
   * that the getObject() method of every JDBC driver should perform by default.
   * 
   * @param jdbcType the JDBC type to convert
   * @return the Java Object type resulting from the standard type conversion.
   * @see java.sql.Types
   */

  public static TypeTag jdbcToJavaObjectType(int jdbcType)
  {
    switch (jdbcType)
    {

      case Types.CHAR :
      case Types.VARCHAR :
      case Types.LONGVARCHAR :
        return TypeTag.STRING;

      case Types.NUMERIC :
      case Types.DECIMAL :
        return TypeTag.BIGDECIMAL;

      case Types.BIT :
        return TypeTag.BOOLEAN;

      case Types.TINYINT :
      case Types.SMALLINT :
      case Types.INTEGER :
        return TypeTag.INTEGER;

      case Types.BIGINT :
        return TypeTag.LONG;

      case Types.REAL :
        return TypeTag.FLOAT;

      case Types.FLOAT :
      case Types.DOUBLE :
        return TypeTag.DOUBLE;

      case Types.BINARY :
      case Types.VARBINARY :
      case Types.LONGVARBINARY :
        return TypeTag.BYTE_ARRAY;

      case Types.DATE :
        return TypeTag.SQL_DATE;

      case Types.TIME :
        return TypeTag.SQL_TIME;

      case Types.TIMESTAMP :
        return TypeTag.SQL_TIMESTAMP;

      // DISTINCT unsupported

      case Types.CLOB :
        return TypeTag.CLOB;

      case Types.BLOB :
        return TypeTag.BLOB;

      // ARRAY, STRUCT/SQLData, REF, JAVA_OBJECT unsupported

      // JDBC 3.0 java.net.URL unsupported
      
      default :
        return TypeTag.TYPE_ERROR;
    }
  }
}
