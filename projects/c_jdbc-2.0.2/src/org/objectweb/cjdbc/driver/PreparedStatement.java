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
 * Contributor(s): Nicolas Modrzyk, Jaco Swart.
 */

package org.objectweb.cjdbc.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.sql.filters.HexaBlobFilter;
import org.objectweb.cjdbc.common.util.Strings;

/**
 * A SQL Statement is pre-compiled and stored in a
 * <code>PreparedStatement</code> object. This object can then be used to
 * efficiently execute this statement multiple times.
 * <p>
 * <b>Note: </b> The setXXX methods for setting IN parameter values must specify
 * types that are compatible with the defined SQL type of the input parameter.
 * For instance, if the IN parameter has SQL type Integer, then setInt should be
 * used.
 * <p>
 * If arbitrary parameter type conversions are required, then the setObject
 * method should be used with a target SQL type.
 * <p>
 * In the old days, this was just a dirty copy/paste from the PostgreSQL driver.
 * Some irrelevant comments are left-over here and there.
 * <p>
 * This class could maybe be splitted into DriverProcessedPreparedStatement and
 * ProxyModeProcessedStatement
 * 
 * @see DriverResultSet
 * @see java.sql.PreparedStatement
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @version 1.0
 */
public class PreparedStatement extends Statement
    implements
      java.sql.PreparedStatement
{
  /** Could we use {@link java.sql.Types} instead ? */

  /**
   * All tags must have the same length
   * {@link #setPreparedStatement(String, java.sql.PreparedStatement)}
   */
  /** Tag for a byte parameter */
  public static final String BYTE_TAG          = "b|";
  /** Tag for a bytes (used for Blob) parameter */
  public static final String BYTES_TAG         = "B|";
  /** Tag for a BLOB (used for null Blob) parameter */
  public static final String BLOB_TAG          = "c|";
  /** Tag for a CLOB (used for null Clob) parameter */
  public static final String CLOB_TAG          = "C|";
  /** Tag for a boolean parameter */
  public static final String BOOLEAN_TAG       = "0|";
  /** Tag for a big decimal parameter */
  public static final String BIG_DECIMAL_TAG   = "1|";
  /** Tag for a date parameter */
  public static final String DATE_TAG          = "d|";
  /** Tag for a double parameter */
  public static final String DOUBLE_TAG        = "D|";
  /** Tag for a float parameter */
  public static final String FLOAT_TAG         = "F|";
  /** Tag for a integer parameter */
  public static final String INTEGER_TAG       = "I|";
  /** Tag for a long parameter */
  public static final String LONG_TAG          = "L|";
  /** Tag for a setNull call */
  public static final String NULL_TAG          = "N|";
  /** Tag for a Null string parameter */
  public static final String NULL_STRING_TAG   = "n|";
  /** Tag for an object parameter */
  public static final String OBJECT_TAG        = "O|";
  /** Tag for a Ref parameter */
  public static final String REF_TAG           = "R|";
  /** Tag for a short parameter */
  public static final String SHORT_TAG         = "s|";
  /** Tag for a string parameter */
  public static final String STRING_TAG        = "S|";
  /** Tag for a time parameter */
  public static final String TIME_TAG          = "t|";
  /** Tag for a timestamp parameter */
  public static final String TIMESTAMP_TAG     = "T|";
  /** Tag for an URL parameter */
  public static final String URL_TAG           = "U|";

  /** Tag maker for parameters */
  public static final String TAG_MARKER        = "!%";
  /** Escape for tag maker */
  public static final String TAG_MARKER_ESCAPE = TAG_MARKER + ";";
  /** Tag for parameters start delimiter */
  public static final String START_PARAM_TAG   = "<" + TAG_MARKER;
  /** Tag for parameters end delimiter */
  public static final String END_PARAM_TAG     = "|" + TAG_MARKER + ">";

  /** original, untouched request (only trimmed) */
  protected String           sql;
  /** IN parameters, ready to be inlined in the request */
  private String[]           inStrings;
  /** segments: the request splitted by question marks placeholders */
  private String[]           templateStrings;

  // Some performance caches
  private StringBuffer       sbuf              = new StringBuffer();

  /**
   * Constructor. Parses/Splits the SQL statement into segments - string parts
   * separated by question mark placeholders. When we rebuild the thing with the
   * arguments, we can substitute the args by joining segments and parameters
   * back together.
   * 
   * @param connection the instanatiating connection
   * @param sqlStatement the SQL statement with ? for IN markers
   * @exception SQLException if something bad occurs
   */
  public PreparedStatement(Connection connection, String sqlStatement)
      throws SQLException
  {
    super(connection);

    /** temporary array for segments */
    ArrayList segs = new ArrayList();
    int lastParmEnd = 0;

    // The following two boolean switches are used to make sure we're not
    // counting "?" in either strings or metadata strings. For instance the
    // following query:
    // select '?' "A ? value" from dual
    // doesn't have any parameters.

    boolean inString = false;
    boolean inMetaString = false;

    this.sql = sqlStatement.trim();
    this.connection = connection;
    for (int i = 0; i < sql.length(); ++i)
    {
      if (sql.charAt(i) == '\'')
        inString = !inString;
      if (sql.charAt(i) == '"')
        inMetaString = !inMetaString;
      if ((sql.charAt(i) == '?') && (!(inString || inMetaString)))
      {
        segs.add(sql.substring(lastParmEnd, i));
        lastParmEnd = i + 1;
      }
    }
    segs.add(sql.substring(lastParmEnd, sql.length()));

    int size = segs.size();
    templateStrings = new String[size];
    inStrings = new String[size - 1];
    clearParameters();

    for (int i = 0; i < size; ++i)
      templateStrings[i] = (String) segs.get(i);
  }

  /**
   * Release objects for garbage collection and call Statement.close().
   * 
   * @throws SQLException if an error occurs
   */
  public void close() throws SQLException
  {
    sql = null;
    templateStrings = null;
    inStrings = null;

    super.close();
  }

  /**
   * A Prepared SQL query is executed and its <code>ResultSet</code> is
   * returned.
   * 
   * @return a <code>ResultSet</code> that contains the data produced by the *
   *         query - never <code>null</code>.
   * @exception SQLException if a database access error occurs
   */
  public java.sql.ResultSet executeQuery() throws SQLException
  {
    return super.executeQuery(sql, compileQuery()); // in Statement class
  }

  /**
   * Execute a SQL INSERT, UPDATE or DELETE statement. In addition, SQL
   * statements that return nothing such as SQL DDL statements can be executed.
   * 
   * @return either the row count for <code>INSERT</code>,
   *         <code>UPDATE</code> or <code>DELETE</code>; or 0 for SQL
   *         statements that return nothing.
   * @exception SQLException if a database access error occurs
   */
  public int executeUpdate() throws SQLException
  {
    return super.executeUpdateWithSkeleton(sql, compileQuery());
    // in Statement class
  }

  /**
   * Helper - this compiles the SQL query, inlining the parameters in the
   * request String. This is identical to <code>this.toString()</code> except
   * it throws an exception if a parameter was not set.
   * 
   * @return the compiled query
   * @throws SQLException if an error occurs
   */
  protected synchronized String compileQuery() throws SQLException
  {
    sbuf.setLength(0);
    int i;

    for (i = 0; i < inStrings.length; ++i)
    {
      if (inStrings[i] == null)
        throw new SQLException("Parameter " + (i + 1) + " is incorrect");
      sbuf.append(templateStrings[i]).append(inStrings[i]);
    }
    sbuf.append(templateStrings[inStrings.length]);
    return sbuf.toString();
  }

  /**
   * Escape the input string. <br>
   * <char>' </char> is replaced by <char>\' </char> <br>
   * <char>\ </char> is replaced by <char>\\' </char> <br>
   * if connection.escapeProcessing is set to true, surround the new string with
   * <char>\' </char>
   * 
   * @param x the string to process
   * @return escaped string
   */
  protected String doEscapeProcessing(String x)
  {
    // use the shared buffer object. Should never clash but this
    // makes us thread safe!
    synchronized (sbuf)
    {
      sbuf.setLength(0);
      int i;
      sbuf.append(connection.getEscapeChar());
      for (i = 0; i < x.length(); ++i)
      {
        char c = x.charAt(i);
        if ((c == '\'' && connection.isEscapeSingleQuote())
            || (c == '\\' && connection.isEscapeBackslash()))
          sbuf.append(c);
        sbuf.append(c);
      }
      sbuf.append(connection.getEscapeChar());
    }
    return sbuf.toString();
  }

  /**
   * Sets a parameter to SQL NULL.
   * <p>
   * <b>Note: </b> you must specify the parameters SQL type but we ignore it.
   * 
   * @param parameterIndex the first parameter is 1, etc...
   * @param sqlType the SQL type code defined in java.sql.Types
   * @exception SQLException if a database access error occurs
   */
  public void setNull(int parameterIndex, int sqlType) throws SQLException
  {
    if (connection.isDriverProcessed())
      set(parameterIndex, "null");
    else
      setWithTag(parameterIndex, NULL_TAG, String.valueOf(sqlType));
  }

  /**
   * Sets a parameter to a Java boolean value. The driver converts this to a SQL
   * BIT value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setBoolean(int parameterIndex, boolean x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      set(parameterIndex, x
          ? connection.getPreparedStatementBooleanTrue()
          : connection.getPreparedStatementBooleanFalse());
    }
    else
    {
      setWithTag(parameterIndex, BOOLEAN_TAG, String.valueOf(x));
    }
  }

  /**
   * Sets a parameter to a Java byte value.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setByte(int parameterIndex, byte x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      set(parameterIndex, Integer.toString(x));
    }
    else
    {
      setWithTag(parameterIndex, BYTE_TAG, Integer.toString(x));
    }
  }

  /**
   * Sets a parameter to a Java short value. The driver converts this to a SQL
   * SMALLINT value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setShort(int parameterIndex, short x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      set(parameterIndex, Integer.toString(x));
    }
    else
    {
      setWithTag(parameterIndex, SHORT_TAG, Integer.toString(x));
    }
  }

  /**
   * Sets a parameter to a Java int value. The driver converts this to a SQL
   * INTEGER value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setInt(int parameterIndex, int x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      set(parameterIndex, Integer.toString(x));
    }
    else
    {
      setWithTag(parameterIndex, INTEGER_TAG, Integer.toString(x));
    }
  }

  /**
   * Sets a parameter to a Java long value. The driver converts this to a SQL
   * BIGINT value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setLong(int parameterIndex, long x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      set(parameterIndex, Long.toString(x));
    }
    else
    {
      setWithTag(parameterIndex, LONG_TAG, Long.toString(x));
    }
  }

  /**
   * Sets a parameter to a Java float value. The driver converts this to a SQL
   * FLOAT value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setFloat(int parameterIndex, float x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      set(parameterIndex, Float.toString(x));
    }
    else
    {
      setWithTag(parameterIndex, FLOAT_TAG, Float.toString(x));
    }
  }

  /**
   * Sets a parameter to a Java double value. The driver converts this to a SQL
   * DOUBLE value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setDouble(int parameterIndex, double x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      set(parameterIndex, Double.toString(x));
    }
    else
    {
      setWithTag(parameterIndex, DOUBLE_TAG, Double.toString(x));
    }
  }

  /**
   * Sets a parameter to a java.lang.BigDecimal value. The driver converts this
   * to a SQL NUMERIC value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setBigDecimal(int parameterIndex, BigDecimal x)
      throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      if (x == null)
        setNull(parameterIndex, Types.DECIMAL);
      else
        set(parameterIndex, x.toString());
    }
    else
    {
      if (x == null)
        setWithTag(parameterIndex, BIG_DECIMAL_TAG, NULL_TAG);
      else
        setWithTag(parameterIndex, BIG_DECIMAL_TAG, x.toString());
    }
  }

  /**
   * Sets a parameter to a Java String value. The driver converts this to a SQL
   * VARCHAR or LONGVARCHAR value (depending on the arguments size relative to
   * the driver's limits on VARCHARs) when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setString(int parameterIndex, String x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      if (x == null)
        // if the passed string is null, then set this column to null
        setNull(parameterIndex, Types.VARCHAR);
      else
      {
        if (escapeProcessing
            && (connection.isEscapeBackslash() || connection
                .isEscapeSingleQuote()))
          set(parameterIndex, doEscapeProcessing(x));
        else
          // No escape processing
          set(parameterIndex, x);
      }
    }
    else
    {
      if (x == null)
        setWithTag(parameterIndex, STRING_TAG, NULL_TAG);
      else
      {
        if (NULL_TAG.equals(x))
        { // Someone is trying to set a String that matches our NULL tag, a real
          // bad luck, use our special NULL_STRING_TAG!
          setWithTag(parameterIndex, NULL_STRING_TAG, x);
        }
        else
        { // No escape processing is needed for queries not being parsed into
          // statements.
          setWithTag(parameterIndex, STRING_TAG, x);
        }
      }
    }
  }

  /**
   * Sets a parameter to a Java array of bytes.
   * <p>
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setBytes(int parameterIndex, byte[] x) throws SQLException
  {
    String blob;
    try
    {
      synchronized (sbuf)
      {
        if (connection.isDriverProcessed())
        {
          /* Encoded for database storage */
          blob = connection.getBlobFilter().encode(x);
          sbuf.setLength(0);
          sbuf.append(connection.escapeChar);
          sbuf.append(blob);
          sbuf.append(connection.escapeChar);
          set(parameterIndex, sbuf.toString());
        }
        else
        {
          /**
           * Encoded only for request inlining. Decoded right away by the
           * controller at static
           * {@link #setPreparedStatement(String, java.sql.PreparedStatement)}
           */
          blob = new HexaBlobFilter().encode(x);
          setWithTag(parameterIndex, BYTES_TAG, blob);
        }
      }
    }
    catch (OutOfMemoryError oome)
    {
      blob = null;
      sbuf = null;
      System.gc();
      throw new SQLException("Out of memory");
    }
  }

  /**
   * Sets a parameter to a java.sql.Date value. The driver converts this to a
   * SQL DATE value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setDate(int parameterIndex, java.sql.Date x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      if (x == null)
        setNull(parameterIndex, Types.DATE);
      else
        set(parameterIndex, "'" + new java.sql.Date(x.getTime()).toString()
            + "'");
    }
    else
    {
      if (x == null)
        setWithTag(parameterIndex, DATE_TAG, NULL_TAG);
      else
        setWithTag(parameterIndex, DATE_TAG, new java.sql.Date(x.getTime())
            .toString());
    }
  }

  /**
   * Sets a parameter to a <code>java.sql.Time</code> value. The driver
   * converts this to a SQL TIME value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...));
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setTime(int parameterIndex, Time x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      if (x == null)
        setNull(parameterIndex, Types.TIME);
      else
        set(parameterIndex, "{t '" + x.toString() + "'}");
    }
    else
    {
      if (x == null)
        setWithTag(parameterIndex, TIME_TAG, NULL_TAG);
      else
        setWithTag(parameterIndex, TIME_TAG, x.toString());
    }
  }

  /**
   * Sets a parameter to a <code>java.sql.Timestamp</code> value. The driver
   * converts this to a SQL TIMESTAMP value when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @exception SQLException if a database access error occurs
   */
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      if (x == null)
        setNull(parameterIndex, Types.TIMESTAMP);
      else
      {
        // Be careful don't use instanceof here since it would match derived
        // classes.
        if (x.getClass().equals(Timestamp.class))
          set(parameterIndex, "'" + x.toString() + "'");
        else
          set(parameterIndex, "'" + new Timestamp(x.getTime()).toString() + "'");
      }
    }
    else
    {
      if (x == null)
        setWithTag(parameterIndex, TIMESTAMP_TAG, NULL_TAG);
      else
      {
        if (x.getClass().equals(Timestamp.class))
          setWithTag(parameterIndex, TIMESTAMP_TAG, x.toString());
        else
          setWithTag(parameterIndex, TIMESTAMP_TAG, new Timestamp(x.getTime())
              .toString());
      }
    }
  }

  /**
   * When a very large ASCII value is input to a LONGVARCHAR parameter, it may
   * be more practical to send it via a java.io.InputStream. JDBC will read the
   * data from the stream as needed, until it reaches end-of-file. The JDBC
   * driver will do any necessary conversion from ASCII to the database char
   * format.
   * <p>
   * <b>Note: </b> this stream object can either be a standard Java stream
   * object or your own subclass that implements the standard interface.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @param length the number of bytes in the stream
   * @exception SQLException if a database access error occurs
   */
  public void setAsciiStream(int parameterIndex, InputStream x, int length)
      throws SQLException
  {
    setBinaryStream(parameterIndex, x, length);
  }

  /**
   * When a very large Unicode value is input to a LONGVARCHAR parameter, it may
   * be more practical to send it via a java.io.InputStream. JDBC will read the
   * data from the stream as needed, until it reaches end-of-file. The JDBC
   * driver will do any necessary conversion from UNICODE to the database char
   * format.
   * <p>** DEPRECIATED IN JDBC 2 **
   * <p>
   * <b>Note: </b> this stream object can either be a standard Java stream
   * object or your own subclass that implements the standard interface.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the parameter value
   * @param length the parameter length
   * @exception SQLException if a database access error occurs
   * @deprecated
   */
  public void setUnicodeStream(int parameterIndex, InputStream x, int length)
      throws SQLException
  {
    setBinaryStream(parameterIndex, x, length);
  }

  /**
   * Stores a binary stream into parameters array, using an intermediate byte[].
   * When a very large binary value is input to a LONGVARBINARY parameter, it
   * may be more practical to send it via a java.io.InputStream. JDBC will read
   * the data from the stream as needed, until it reaches end-of-file. This
   * should be more or less equivalent to setBytes(blob.getBytes()).
   * <p>
   * <b>Note: </b> This stream object can either be a standard Java stream
   * object or your own subclass that implements the standard interface.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param inStreamArg the parameter value
   * @param length the parameter length
   * @exception SQLException if a database access error occurs
   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
   *      int)
   */
  public void setBinaryStream(int parameterIndex, InputStream inStreamArg,
      int length) throws SQLException
  {
    byte[] data = new byte[length];
    try
    {
      inStreamArg.read(data, 0, length);
    }
    catch (Exception ioe)
    {
      throw new SQLException("Problem with streaming of data");
    }
    setBytes(parameterIndex, data);
  }

  /**
   * In general, parameter values remain in force for repeated used of a
   * <code>Statement</code>. Setting a parameter value automatically clears
   * its previous value. However, in coms cases, it is useful to immediately
   * release the resources used by the current parameter values; this can be
   * done by calling <code>clearParameters()</code>.
   * 
   * @exception SQLException if a database access error occurs
   */
  public void clearParameters() throws SQLException
  {
    int i;

    for (i = 0; i < inStrings.length; i++)
      inStrings[i] = null;
  }

  /**
   * Sets the value of a parameter using an object; use the
   * <code>java.lang</code> equivalent objects for integral values.
   * <p>
   * The given Java object will be converted to the targetSqlType before being
   * sent to the database.
   * <p>
   * Note that this method may be used to pass database-specific abstract data
   * types. This is done by using a Driver-specific Java type and using a
   * <code>targetSqlType</code> of <code>java.sql.Types.OTHER</code>.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the object containing the input parameter value
   * @param targetSqlType The SQL type to be send to the database
   * @param scale for <code>java.sql.Types.DECIMAL</code> or
   *          <code>java.sql.Types.NUMERIC</code> types this is the number of
   *          digits after the decimal. For all other types this value will be
   *          ignored.
   * @exception SQLException if a database access error or an incompatible type
   *              match occurs
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
   */
  public void setObject(int parameterIndex, Object x, int targetSqlType,
      int scale) throws SQLException
  {
    if (x == null)
    {
      setNull(parameterIndex, targetSqlType);
      return;
    }

    try
    {
      boolean failed = false;
      switch (targetSqlType)
      {
        /**
         * Reference is table "Conversions Performed by setObject()..." in JDBC
         * Reference Book (table 47.9.5 in 2nd edition, 50.5 in 3rd edition).
         * Also available online in Sun's "JDBC Technology Guide: Getting
         * Started", section "Mapping SQL and Java Types".
         */
        // using else setXXX( .toString()) below is a lax hack
        case Types.TINYINT :
        case Types.SMALLINT :
        case Types.INTEGER :
          if (x instanceof Number)
            setInt(parameterIndex, ((Number) x).intValue());
          else if (x instanceof Boolean)
            setInt(parameterIndex, ((Boolean) x).booleanValue() ? 1 : 0);
          else if (x instanceof String)
            setInt(parameterIndex, Integer.parseInt((String) x));
          else
            failed = true;
          break;
        case Types.BIGINT :
          if (x instanceof Number)
            setLong(parameterIndex, ((Number) x).longValue());
          else if (x instanceof String)
            setLong(parameterIndex, Long.parseLong((String) x));
          else if (x instanceof Boolean)
            setLong(parameterIndex, ((Boolean) x).booleanValue() ? 1 : 0);
          else
            failed = true;
          break;
        case Types.REAL :
        case Types.FLOAT :
        case Types.DOUBLE :
        case Types.DECIMAL :
        case Types.NUMERIC :
          // Cast to Number is not necessary. -- EC
          // Hum, this is a lax shortcut to lower level code -- MH
          if (connection.isDriverProcessed())
            set(parameterIndex, x.toString());
          else
            setWithTag(parameterIndex, STRING_TAG, x.toString());
          break;
        case Types.BIT :
        case Types.BOOLEAN :
          if (x instanceof Number)
            setBoolean(parameterIndex, 0 != ((Number) x).longValue());
          else if (x instanceof Boolean)
            setBoolean(parameterIndex, ((Boolean) x).booleanValue());
          else if (x instanceof String)
            setBoolean(parameterIndex, Boolean.valueOf((String) x)
                .booleanValue());
          else
            failed = true;
          break;
        case Types.CHAR :
        case Types.VARCHAR :
        case Types.LONGVARCHAR :
          setString(parameterIndex, x.toString());
          break;
        case Types.BINARY :
        case Types.VARBINARY :
        case Types.LONGVARBINARY :
          if (x instanceof byte[])
            setBytes(parameterIndex, (byte[]) x);
          else if (x instanceof Blob)
            setBlob(parameterIndex, (Blob) x);
          else if (x instanceof Serializable)
            // Try it as an Object (serialized in bytes in setObject below)
            setObject(parameterIndex, x);
          else
            failed = true;
          break;
        case Types.DATE :
          if (x instanceof String)
            setDate(parameterIndex, java.sql.Date.valueOf((String) x));
          else if (x instanceof java.sql.Date)
            setDate(parameterIndex, (java.sql.Date) x);
          else if (x instanceof Timestamp)
            setDate(parameterIndex,
                new java.sql.Date(((Timestamp) x).getTime()));
          else
            failed = true;
          break;
        case Types.TIME :
          if (x instanceof String)
            setTime(parameterIndex, Time.valueOf((String) x));
          else if (x instanceof Time)
            setTime(parameterIndex, (Time) x);
          else if (x instanceof Timestamp)
            setTime(parameterIndex, new Time(((Timestamp) x).getTime()));
          else
            failed = true;
          break;
        case Types.TIMESTAMP :
          if (x instanceof String)
            setTimestamp(parameterIndex, Timestamp.valueOf((String) x));
          else if (x instanceof Date)
            setTimestamp(parameterIndex, new Timestamp(((Date) x).getTime()));
          else if (x instanceof Timestamp)
            setTimestamp(parameterIndex, (Timestamp) x);
          else
            failed = true;
          break;
        case Types.BLOB :
          if (x instanceof Blob)
            setBlob(parameterIndex, (Blob) x);
          else
            failed = true;
          break;
        case Types.DATALINK :
          if (x instanceof java.net.URL)
            setURL(parameterIndex, (java.net.URL) x);
          else
            setURL(parameterIndex, new java.net.URL(x.toString()));
          break;
        case Types.JAVA_OBJECT :
        case Types.OTHER :
          setObject(parameterIndex, x);
          break;
        default :
          throw new SQLException("Unsupported type value");
      }
      if (true == failed)
        throw new IllegalArgumentException(
            "Attempt to perform an illegal conversion");
    }
    catch (Exception e)
    {
      SQLException outE = new SQLException("Exception while converting type "
          + x.getClass() + " to SQL type " + targetSqlType);
      outE.initCause(e);
      throw outE;
    }
  }

  /**
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
   */
  public void setObject(int parameterIndex, Object x, int targetSqlType)
      throws SQLException
  {
    setObject(parameterIndex, x, targetSqlType, 0);
  }

  /**
   * This stores an Object parameter into the parameters array.
   * 
   * @param parameterIndex the first parameter is 1...
   * @param x the object to set
   * @exception SQLException if a database access error occurs
   */
  public void setObject(int parameterIndex, Object x) throws SQLException
  {
    if (x == null)
    {
      if (connection.isDriverProcessed())
        setNull(parameterIndex, Types.JAVA_OBJECT);
      else
        setWithTag(parameterIndex, OBJECT_TAG, NULL_TAG);
    }
    else
    {
      if (x instanceof String)
        setString(parameterIndex, (String) x);
      else if (x instanceof BigDecimal)
        setBigDecimal(parameterIndex, (BigDecimal) x);
      else if (x instanceof Short)
        setShort(parameterIndex, ((Short) x).shortValue());
      else if (x instanceof Integer)
        setInt(parameterIndex, ((Integer) x).intValue());
      else if (x instanceof Long)
        setLong(parameterIndex, ((Long) x).longValue());
      else if (x instanceof Float)
        setFloat(parameterIndex, ((Float) x).floatValue());
      else if (x instanceof Double)
        setDouble(parameterIndex, ((Double) x).doubleValue());
      else if (x instanceof byte[])
        setBytes(parameterIndex, (byte[]) x);
      else if (x instanceof java.sql.Date)
        setDate(parameterIndex, (java.sql.Date) x);
      else if (x instanceof Time)
        setTime(parameterIndex, (Time) x);
      else if (x instanceof Timestamp)
        setTimestamp(parameterIndex, (Timestamp) x);
      else if (x instanceof Boolean)
        setBoolean(parameterIndex, ((Boolean) x).booleanValue());
      else if (x instanceof Blob)
        setBlob(parameterIndex, (Blob) x);
      else if (x instanceof java.net.URL)
        setURL(parameterIndex, (java.net.URL) x);
      else if (x instanceof Serializable)
      {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try
        {
          // Serialize object to byte array
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(
              byteOutputStream);
          objectOutputStream.writeObject(x);
          objectOutputStream.flush();
          objectOutputStream.close();
          if (connection.isDriverProcessed())
            setBytes(parameterIndex, byteOutputStream.toByteArray());
          else
            synchronized (sbuf)
            {
              sbuf.setLength(0);
              sbuf.append(byteOutputStream);
              setWithTag(parameterIndex, OBJECT_TAG, sbuf.toString());
            }
        }
        catch (IOException e)
        {
          throw new SQLException("Failed to serialize object: " + e);
        }
      }
      else
        throw new SQLException("Objects of type " + x.getClass()
            + " are not supported.");
    }
  }

  /**
   * Some prepared statements return multiple results; the execute method
   * handles these complex statements as well as the simpler form of statements
   * handled by <code>executeQuery()</code> and <code>executeUpdate()</code>.
   * 
   * @return <code>true</code> if the next result is a
   *         <code>ResultSet<code>; <code>false<code> if it is an update count
   * or there are no more results
   * @exception SQLException if a database access error occurs
   */
  public boolean execute() throws SQLException
  {
    int start = 0;
    try
    {
      // Ignore any leading parenthesis
      while (sql.charAt(start) == '(')
        start++;
    }
    catch (IndexOutOfBoundsException e)
    {
      // Probably a buggy request, let it go through and let thefollowing code
      // to report an accurate error if any.
      start = 0;
    }

    if (sql.regionMatches(true, start, "select", 0, 6)
        || (sql.regionMatches(true, start, "{call", 0, 5)))
    {
      result = executeQuery(sql, compileQuery());
      return true;
    }
    else
    {
      updateCount = executeUpdateWithSkeleton(sql, compileQuery());
      return false;
    }
  }

  /**
   * Returns the SQL statement with the current template values substituted.
   * <p>
   * <b>Note: </b>: This is identical to <code>compileQuery()</code> except
   * instead of throwing SQLException if a parameter is <code>null</code>, it
   * places ? instead.
   * 
   * @return the SQL statement
   */
  public String toString()
  {
    synchronized (sbuf)
    {
      sbuf.setLength(0);
      int i;

      for (i = 0; i < inStrings.length; ++i)
      {
        if (inStrings[i] == null)
          sbuf.append('?');
        else
          sbuf.append(templateStrings[i]);
        sbuf.append(inStrings[i]);
      }
      sbuf.append(templateStrings[inStrings.length]);
      return sbuf.toString();
    }
  }

  // ** JDBC 2 Extensions **

  /**
   * This parses the query and adds it to the current batch
   * 
   * @throws SQLException if an error occurs
   */
  public synchronized void addBatch() throws SQLException
  {
    if (batch == null)
      batch = new Vector();
    batch.addElement(new BatchElement(sql, compileQuery()));
  }

  /**
   * Execute a batch of commands
   * 
   * @return an array containing update count that corresponding to the commands
   *         that executed successfully
   * @exception BatchUpdateException if an error occurs on one statement (the
   *              number of updated rows for the successfully executed
   *              statements can be found in
   *              BatchUpdateException.getUpdateCounts())
   */
  public int[] executeBatch() throws BatchUpdateException
  {
    if (batch == null || batch.isEmpty())
      return new int[0];

    int size = batch.size();
    int[] nbsRowsUpdated = new int[size];
    int i = 0;

    try
    {
      for (i = 0; i < size; i++)
      {
        BatchElement be = (BatchElement) batch.elementAt(i);
        nbsRowsUpdated[i] = this.executeUpdateWithSkeleton(be.getSqlTemplate(),
            be.getCompiledSql());
      }
      return nbsRowsUpdated;
    }
    catch (SQLException e)
    {
      String message = "Batch failed for request " + i + ": "
          + ((BatchElement) batch.elementAt(i)).getCompiledSql() + " (" + e
          + ")";

      // shrink the returned array
      int[] updateCounts = new int[i];
      System.arraycopy(nbsRowsUpdated, 0, updateCounts, 0, i);

      throw new BatchUpdateException(message, updateCounts);
    }
    finally
    {
      batch.removeAllElements();
    }
  }

  /**
   * Returns the <code>MetaData</code> for the last <code>ResultSet</code>
   * returned.
   * 
   * @return The ResultSet Metadata
   * @throws SQLException if an error occurs
   */
  public java.sql.ResultSetMetaData getMetaData() throws SQLException
  {
    java.sql.ResultSet rs = getResultSet();
    if (rs != null)
      return rs.getMetaData();

    // Does anyone really know what this method does?
    return null;
  }

  /**
   * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
   */
  public void setArray(int i, Array x) throws SQLException
  {
    throw new NotImplementedException("setArray()");
  }

  /**
   * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
   */
  public void setBlob(int paramIndex, java.sql.Blob sqlBlobParam)
      throws SQLException
  {
    if (sqlBlobParam == null)
    {
      if (connection.isDriverProcessed())
        setNull(paramIndex, Types.BLOB);
      else
        setWithTag(paramIndex, BLOB_TAG, NULL_TAG);
      return;
    }

    // sqlBlobParam.getBytes() seems limited in size ?
    // So we use .getBinaryStream()
    InputStream blobBinStream = sqlBlobParam.getBinaryStream();

    if (connection.isDriverProcessed())
      setBinaryStream(paramIndex, blobBinStream, (int) sqlBlobParam.length());
    else
    {
      byte[] data = new byte[(int) sqlBlobParam.length()];
      try
      {
        blobBinStream.read(data, 0, (int) sqlBlobParam.length());
      }
      catch (Exception ioe)
      {
        throw new SQLException("Problem with data streaming");
      }
      try
      {
        synchronized (this.sbuf)
        {
          this.sbuf.setLength(0);
          /**
           * Encoded only for request inlining. Decoded right away by the
           * controller at static
           * {@link #setPreparedStatement(String, java.sql.PreparedStatement)}
           */
          this.sbuf.append(new HexaBlobFilter().encode(data));
          setWithTag(paramIndex, BLOB_TAG, this.sbuf.toString());
        }
      }
      catch (OutOfMemoryError oome)
      {
        this.sbuf = null;
        System.gc();
        throw new SQLException("Out of memory");
      }

    }
  }

  /**
   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
   *      int)
   */
  public void setCharacterStream(int i, java.io.Reader x, int length)
      throws SQLException
  {
    char[] data = new char[length];
    try
    {
      x.read(data, 0, length);
    }
    catch (Exception ioe)
    {
      throw new SQLException("Problem with streaming of data");
    }
    setString(i, new String(data));
  }

  /**
   * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
   */
  public void setClob(int i, java.sql.Clob clobArg) throws SQLException
  {
    if (clobArg == null)
    {
      if (connection.isDriverProcessed())
        setNull(i, Types.CLOB);
      else
        setWithTag(i, CLOB_TAG, NULL_TAG);
      return;
    }
    if (connection.isDriverProcessed())
      setString(i, clobArg.getSubString(0, (int) clobArg.length()));
    else
      setWithTag(i, CLOB_TAG, clobArg.getSubString(0, (int) clobArg.length()));
  }

  /**
   * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
   */
  public void setNull(int i, int t, String s) throws SQLException
  {
    setNull(i, t);
  }

  /**
   * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
   */
  public void setRef(int i, Ref x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      if (x == null)
        setNull(i, Types.REF);
      else
        set(i, x.toString());
    }
    else
    {
      if (x == null)
        setWithTag(i, REF_TAG, NULL_TAG);
      else
        setWithTag(i, REF_TAG, x.toString());
    }
  }

  /**
   * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,
   *      java.util.Calendar)
   */
  public void setDate(int i, java.sql.Date d, java.util.Calendar cal)
      throws SQLException
  {
    if (d == null)
    {
      if (connection.isDriverProcessed())
        setNull(i, Types.DATE);
      else
        setWithTag(i, DATE_TAG, NULL_TAG);
      return;
    }
    else
    {
      if (cal == null)
        setDate(i, d);
      else
      {
        cal.setTime(d);
        setDate(i, new java.sql.Date(cal.getTime().getTime()));
      }
    }
  }

  /**
   * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,
   *      java.util.Calendar)
   */
  public void setTime(int i, Time t, java.util.Calendar cal)
      throws SQLException
  {
    if (t == null)
    {
      if (connection.isDriverProcessed())
        setNull(i, Types.TIME);
      else
        setWithTag(i, TIME_TAG, NULL_TAG);
      return;
    }
    else
    {
      if (cal == null)
        setTime(i, t);
      else
      {
        cal.setTime(t);
        setTime(i, new java.sql.Time(cal.getTime().getTime()));
      }
    }
  }

  /**
   * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,
   *      java.util.Calendar)
   */
  public void setTimestamp(int i, Timestamp t, java.util.Calendar cal)
      throws SQLException
  {
    if (t == null)
    {
      if (connection.isDriverProcessed())
        setNull(i, Types.TIMESTAMP);
      else
        setWithTag(i, TIMESTAMP_TAG, NULL_TAG);
      return;
    }
    else
    {
      if (cal == null)
        setTimestamp(i, t);
      else
      {
        cal.setTime(t);
        setTimestamp(i, new java.sql.Timestamp(cal.getTime().getTime()));
      }
    }
  }

  // ------------------------- JDBC 3.0 -----------------------------------

  /**
   * Sets the designated parameter to the given <code>java.net.URL</code>
   * value. The driver converts this to an SQL <code>DATALINK</code> value
   * when it sends it to the database.
   * 
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the <code>java.net.URL</code> object to be set
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public void setURL(int parameterIndex, java.net.URL x) throws SQLException
  {
    if (connection.isDriverProcessed())
    {
      if (x == null)
        setNull(parameterIndex, Types.OTHER);
      else
        set(parameterIndex, x.toString());
    }
    else
    {
      if (x == null)
        setWithTag(parameterIndex, URL_TAG, NULL_TAG);
      else
        setWithTag(parameterIndex, URL_TAG, x.toString());
    }
  }

  /**
   * Retrieves the number, types and properties of this
   * <code>PreparedStatement</code> object's parameters.
   * 
   * @return a <code>ParameterMetaData</code> object that contains information
   *         about the number, types and properties of this
   *         <code>PreparedStatement</code> object's parameters
   * @exception SQLException if a database access error occurs
   * @see ParameterMetaData
   * @since JDK 1.4
   */
  public ParameterMetaData getParameterMetaData() throws SQLException
  {
    throw new NotImplementedException("getParameterMetaData");
  }

  // **************************************************************
  // END OF PUBLIC INTERFACE
  // **************************************************************

  /**
   * Actually stores the IN parameter into parameters String array. Called by
   * most setXXX() methods.
   * 
   * @param paramIndex the index into the inString
   * @param s a string to be stored
   * @exception SQLException if something goes wrong
   */
  private void set(int paramIndex, String s) throws SQLException
  {
    if (paramIndex < 1 || paramIndex > inStrings.length)
      throw new SQLException("Parameter index out of range.");
    inStrings[paramIndex - 1] = s;
  }

  /**
   * Stores parameter and its type as a <em>quoted</em> String, so the
   * controller can decode them back. Used when driverProcessed is false.
   * <p>
   * When isDriverProcessed() is false, we could avoid inlining the arguments
   * and just tag them and send them apart as an object list. But this would
   * imply a couple of changes elsewhere, among other: macro-handling,
   * recoverylog,...
   * 
   * @param paramIndex the index into the inString
   * @param typeTag type of the parameter
   * @param param the parameter string to be stored
   * @exception SQLException if something goes wrong
   * @see #setPreparedStatement(String, java.sql.PreparedStatement)
   */
  private void setWithTag(int paramIndex, String typeTag, String param)
      throws SQLException
  {
    /**
     * insert TAGS so the controller can parse and "unset" the request using
     * {@link #setPreparedStatement(String, java.sql.PreparedStatement) 
     */
    set(paramIndex, START_PARAM_TAG + typeTag
        + Strings.replace(param, TAG_MARKER, TAG_MARKER_ESCAPE) + END_PARAM_TAG);
  }

  /**
   * Set the auto generated key flag defined in Statement
   * 
   * @param autoGeneratedKeys usually
   *          <code>Statement.RETURN_GENERATED_KEYS</code>
   * @see Connection#prepareStatement(String, int)
   */
  protected void setGeneratedKeysFlag(int autoGeneratedKeys)
  {
    generatedKeysFlag = autoGeneratedKeys;
  }

  /**
   * Static method to initialize a backend PreparedStatement by calling the
   * appropriate setXXX methods on the request skeleton. Has to extract the
   * tagged and inlined parameters from the sql String beforehand. Used by the
   * controller, only when isDriverProcessed() is false.
   * 
   * @param quotedRequest SQL statement with parameters to replace
   * @param backendPS the preparedStatement to set
   * @throws SQLException if an error occurs
   * @see #setWithTag(int, String, String)
   */
  public static void setPreparedStatement(String quotedRequest,
      java.sql.PreparedStatement backendPS) throws SQLException
  {
    int i = 0;
    int paramIdx = 0;

    // Set all parameters
    while ((i = quotedRequest.indexOf(START_PARAM_TAG, i)) > -1)
    {
      paramIdx++;

      int typeStart = i + START_PARAM_TAG.length();

      // Here we assume that all tags have the same length as the boolean tag.
      String paramType = quotedRequest.substring(typeStart, typeStart
          + BOOLEAN_TAG.length());
      String paramValue = quotedRequest.substring(typeStart
          + BOOLEAN_TAG.length(), quotedRequest.indexOf(END_PARAM_TAG, i));
      paramValue = Strings.replace(paramValue, TAG_MARKER_ESCAPE, TAG_MARKER);

      // Test tags in alphabetical order (to make the code easier to read)
      if (paramType.equals(BIG_DECIMAL_TAG))
      {
        if (paramValue.equals(NULL_TAG))
          backendPS.setBigDecimal(paramIdx, null);
        else
        {
          BigDecimal t = new BigDecimal(paramValue);
          backendPS.setBigDecimal(paramIdx, t);
        }
      }
      else if (paramType.equals(BOOLEAN_TAG))
        backendPS.setBoolean(paramIdx, Boolean.valueOf(paramValue)
            .booleanValue());
      else if (paramType.equals(BYTE_TAG))
      {
        byte t = new Integer(paramValue).byteValue();
        backendPS.setByte(paramIdx, t);
      }
      else if (paramType.equals(BYTES_TAG))
      {
        /**
         * encoded by the driver at {@link #setBytes(int, byte[])}in order to
         * inline it in the request (no database encoding here).
         */
        byte[] t = new HexaBlobFilter().decode(paramValue);
        backendPS.setBytes(paramIdx, t);
      }
      else if (paramType.equals(BLOB_TAG))
      {
        if (paramValue.equals(NULL_TAG)){
        	
        }
        else
        {
          /**
           * encoded by the driver at {@link #setBlob(int, java.sql.Blob)}
           */
          Blob b = new Blob(new HexaBlobFilter().decode(paramValue));
          backendPS.setBlob(paramIdx, b);
        }
      }
      else if (paramType.equals(CLOB_TAG))
      {
        if (paramValue.equals(NULL_TAG)){
        	
        }
        else
        {
          Clob c = new Clob(paramValue);
          backendPS.setClob(paramIdx, c);
        }
      }
      else if (paramType.equals(DATE_TAG))
      {
        if (paramValue.equals(NULL_TAG))
          backendPS.setDate(paramIdx, null);
        else
          try
          {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date t = new Date(sdf.parse(paramValue).getTime());
            backendPS.setDate(paramIdx, t);
          }
          catch (ParseException p)
          {
            backendPS.setDate(paramIdx, null);
            throw new SQLException("Couldn't format date!!!");
          }
      }
      else if (paramType.equals(DOUBLE_TAG))
        backendPS.setDouble(paramIdx, Double.valueOf(paramValue).doubleValue());
      else if (paramType.equals(FLOAT_TAG))
        backendPS.setFloat(paramIdx, Float.valueOf(paramValue).floatValue());
      else if (paramType.equals(INTEGER_TAG))
        backendPS.setInt(paramIdx, Integer.valueOf(paramValue).intValue());
      else if (paramType.equals(LONG_TAG))
        backendPS.setLong(paramIdx, Long.valueOf(paramValue).longValue());
      else if (paramType.equals(NULL_TAG))
        backendPS.setNull(paramIdx, Integer.valueOf(paramValue).intValue());
      else if (paramType.equals(OBJECT_TAG))
      {
        if (paramValue.equals(NULL_TAG))
          backendPS.setObject(paramIdx, null);
        else
        {
          try
          {
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(paramValue.getBytes()));
            backendPS.setObject(paramIdx, in.readObject());
            in.close();
          }
          catch (Exception e)
          {
            throw new SQLException("Failed to rebuild object from stream " + e);
          }
        }
      }
      else if (paramType.equals(REF_TAG))
      {
        if (paramValue.equals(NULL_TAG))
          backendPS.setRef(paramIdx, null);
        else
          throw new SQLException("Ref type not supported");
      }
      else if (paramType.equals(SHORT_TAG))
      {
        short t = new Integer(paramValue).shortValue();
        backendPS.setShort(paramIdx, t);
      }
      else if (paramType.equals(STRING_TAG))
      { // Value is not null, null values are handled by NULL_STRING_TAG
        backendPS.setString(paramIdx, paramValue);
      }
      else if (paramType.equals(NULL_STRING_TAG))
      {
        backendPS.setString(paramIdx, null);
      }
      else if (paramType.equals(TIME_TAG))
      {
        if (paramValue.equals(NULL_TAG))
          backendPS.setTime(paramIdx, null);
        else
          try
          {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Time t = new Time(sdf.parse(paramValue).getTime());
            backendPS.setTime(paramIdx, t);
          }
          catch (ParseException p)
          {
            backendPS.setTime(paramIdx, null);
            throw new SQLException("Couldn't format time!!!");
          }
      }
      else if (paramType.equals(TIMESTAMP_TAG))
      {
        if (paramValue.equals(NULL_TAG))
          backendPS.setTimestamp(paramIdx, null);
        else
          try
          {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            Timestamp t = new Timestamp(sdf.parse(paramValue).getTime());
            backendPS.setTimestamp(paramIdx, t);
          }
          catch (ParseException p)
          {
            backendPS.setTimestamp(paramIdx, null);
            throw new SQLException("Couldn't format timestamp!!!");
          }
      }
      else if (paramType.equals(URL_TAG))
      {
        if (paramValue.equals(NULL_TAG))
          backendPS.setURL(paramIdx, null);
        else
          try
          {
            backendPS.setURL(paramIdx, new URL(paramValue));
          }
          catch (MalformedURLException e)
          {
            throw new SQLException("Unable to create URL " + paramValue + " ("
                + e + ")");
          }
      }
      else
      {
        // invalid parameter, we want to be able to store strings like
        // <?xml version="1.0" encoding="ISO-8859-1"?>
        paramIdx--;
      }
      i = typeStart;
    }
  }

  /**
   * This class defines a BatchElement used for the batch update vector of
   * PreparedStatements to execute.
   * 
   * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
   *         </a>
   * @version 1.0
   */
  private class BatchElement
  {
    private String sqlTemplate;
    private String compiledSql;

    /**
     * Creates a new <code>BatchElement</code> object
     * 
     * @param sqlTemplate SQL query template (aka skeleton)
     * @param compiledSql compiled SQL statement
     */
    public BatchElement(String sqlTemplate, String compiledSql)
    {
      this.sqlTemplate = sqlTemplate;
      this.compiledSql = compiledSql;
    }

    /**
     * Returns the compiledSql value.
     * 
     * @return Returns the compiledSql.
     */
    public String getCompiledSql()
    {
      return compiledSql;
    }

    /**
     * Returns the sqlTemplate value.
     * 
     * @return Returns the sqlTemplate.
     */
    public String getSqlTemplate()
    {
      return sqlTemplate;
    }
  }

@Override
public void setAsciiStream(int parameterIndex, InputStream x)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setAsciiStream(int parameterIndex, InputStream x, long length)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setBinaryStream(int parameterIndex, InputStream x)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setBinaryStream(int parameterIndex, InputStream x, long length)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setBlob(int parameterIndex, InputStream inputStream)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setBlob(int parameterIndex, InputStream inputStream, long length)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setCharacterStream(int parameterIndex, Reader reader)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setCharacterStream(int parameterIndex, Reader reader, long length)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setClob(int parameterIndex, Reader reader) throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setClob(int parameterIndex, Reader reader, long length)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setNCharacterStream(int parameterIndex, Reader value)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setNCharacterStream(int parameterIndex, Reader value, long length)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setNClob(int parameterIndex, NClob value) throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setNClob(int parameterIndex, Reader reader) throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setNClob(int parameterIndex, Reader reader, long length)
		throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setNString(int parameterIndex, String value) throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setRowId(int parameterIndex, RowId x) throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
	// TODO Auto-generated method stub
	
}
}