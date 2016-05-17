/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks
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
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * This class defines Serializers for SQL Data: per type serialization +
 * deserialization methods and information wrapped in one object. Serializers
 * are implemented as singletons for efficiency.
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @version 1.0
 */
public final class SQLDataSerialization
{

  /**
   * Unsupported types listed in {@link TypeTag}won't be added in the near
   * future, except maybe java.net.URL for JDBC 3.0
   */
  /**
   * CLOB support should be easy to base on BLOB implementation once we
   * figure out the encoding issues.
   */

  private static final Serializer JAVA_STRING     = new StringSerializer();
  private static final Serializer MATH_BIGDECIMAL = new BigDecimalSerializer();
  private static final Serializer JAVA_BOOLEAN    = new BooleanSerializer();
  private static final Serializer JAVA_INTEGER    = new IntegerSerializer();
  private static final Serializer JAVA_LONG       = new LongSerializer();
  private static final Serializer JAVA_FLOAT      = new FloatSerializer();
  private static final Serializer JAVA_DOUBLE     = new DoubleSerializer();
  private static final Serializer JAVA_BYTES      = new BytesSerializer();

  private static final Serializer SQL_DATE        = new DateSerializer();      // UNTESTED
  private static final Serializer SQL_TIME        = new TimeSerializer();      // UNTESTED
  private static final Serializer SQL_TIMESTAMP   = new TimestampSerializer(); // UNTESTED

  // CLOB: TODO
  private static final Serializer SQL_BLOB        = new BlobSerializer();
  private static final int        STREAM_BUF_SIZE = 65536;

  // java.net.URL: TODO

  /** Abstract class hiding type-specific serialization methods and information */
  public abstract static class Serializer
  {
    protected TypeTag typeTag;

    /**
     * @return the corresponding TypeTag
     */
    public TypeTag getTypeTag()
    {
      return typeTag;
    }

    /**
     * Serialize the object to the stream. Warning: the caller must ensure that
     * Serializer subtype and Object subtype are compatible.
     * 
     * @param obj object to send
     * @param output Output stream
     * @throws IOException stream error
     * @throws ClassCastException wrong serializer for this object type
     */

    public abstract void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException, ClassCastException;

    /**
     * De-serialize an object from the stream Warning: the caller must ensure
     * that Serializer subtype and the incoming object are compatible.
     * 
     * @param input Input stream
     * @return the object received from the stream
     * @throws IOException stream error
     */
    public abstract Object receiveFromStream(CJDBCInputStream input)
        throws IOException;

  }

  /**
   * Returns the de/serializer appropriate for the given TypeTag, or for the
   * type of the given SQL object if argument is not a TypeTag (TypeTag already
   * knows how to serialize itself).
   * 
   * @param sqlObjOrTypeTag a typetag or a sample SQL object of the type of
   *          interest
   * @return appropriate serialization + deserialization methods
   * @throws NotImplementedException if we don't know how to serialize objects
   *           such as the given one (including "null").
   * @throws IllegalArgumentException if we gave a wrong TypeTag
   */
  public static Serializer getSerializer(Object sqlObjOrTypeTag)
      throws NotImplementedException, IllegalArgumentException
  {
    if (null == sqlObjOrTypeTag) // not strictly needed but shorter and safer
      throw new NotImplementedException(
          "null has no type, cannot find appropriate serialization");
    /*
     * Default values that never match anything: we just need any reference that
     * is both non-SQL and non-null.
     */
    TypeTag tag = TypeTag.CONTROLLER_READY;
    Object obj = JAVA_STRING;

    /**
     * Now let's get rid of all these nasty type issues for good by casting once
     * for all.
     */
    if (sqlObjOrTypeTag instanceof TypeTag)
      tag = (TypeTag) sqlObjOrTypeTag;
    else
      obj = sqlObjOrTypeTag;

    /**
     * THE big switch on (type). "instanceof" is used on the serialization side,
     * "TypeTag.equals()" is used on the de-serialization side. We could for
     * performance split this method into two different methods (with the
     * added burden of keeping them perfectly synchronized)
     * 
     * @see TypeTag
     */
    // STRING
    if (obj instanceof String || TypeTag.STRING.equals(tag))
      return JAVA_STRING;

    // BIGDECIMAL
    if (obj instanceof BigDecimal || TypeTag.BIGDECIMAL.equals(tag))
      return MATH_BIGDECIMAL;

    // BOOLEAN
    if (obj instanceof Boolean || TypeTag.BOOLEAN.equals(tag))
      return JAVA_BOOLEAN;

    // INTEGER
    if (obj instanceof Integer || TypeTag.INTEGER.equals(tag))
      return JAVA_INTEGER;

    // LONG
    if (obj instanceof Long || TypeTag.LONG.equals(tag))
      return JAVA_LONG;

    // FLOAT
    if (obj instanceof Float || TypeTag.FLOAT.equals(tag))
      return JAVA_FLOAT;

    // DOUBLE
    if (obj instanceof Double || TypeTag.DOUBLE.equals(tag))
      return JAVA_DOUBLE;

    // BYTE ARRAY
    if (obj instanceof byte[] || TypeTag.BYTE_ARRAY.equals(tag))
      return JAVA_BYTES;

    // DATE
    if (obj instanceof java.sql.Date || TypeTag.SQL_DATE.equals(tag))
      return SQL_DATE;

    // TIME
    if (obj instanceof java.sql.Time || TypeTag.SQL_TIME.equals(tag))
      return SQL_TIME;

    // TIMESTAMP
    if (obj instanceof Timestamp || TypeTag.SQL_TIMESTAMP.equals(tag))
      return SQL_TIMESTAMP;
    
    // CLOB: TODO
    if (obj instanceof Clob || TypeTag.CLOB.equals(tag))
      throw new NotImplementedException(
          "Clob serialization not yet implemented");
    
    // BLOB
    if (obj instanceof java.sql.Blob || TypeTag.BLOB.equals(tag))
      return SQL_BLOB;

    // java.net.URL: TODO
    
    if (sqlObjOrTypeTag instanceof TypeTag)
      throw new IllegalArgumentException(
          "Internal error: getSerializer() misused with unknown TypeTag argument:"
              + tag);
    else
      throw new NotImplementedException("Unable to serialize unknown type "
          + sqlObjOrTypeTag.getClass() + " of object " + sqlObjOrTypeTag);
  }

  /*
   * These classes define one serializer per type
   */

  // STRING
  private static final class StringSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.STRING;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeUTF((String) obj);
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return input.readUTF();

    }
  }

  // BIGDECIMAL
  // we serialize this using .toString()
  private static final class BigDecimalSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.BIGDECIMAL;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeUTF(((BigDecimal) obj).toString());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new BigDecimal(input.readUTF());
    }
  }

  // BOOLEAN
  private static final class BooleanSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.BOOLEAN;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeBoolean(((Boolean) obj).booleanValue());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new Boolean(input.readBoolean());
    }
  }

  // INTEGER
  private static final class IntegerSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.INTEGER;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      /**
       * let's also accept Short, see PostgreSQL bug explained here
       * 
       * @see org.objectweb.cjdbc.driver.DriverResultSet#initSerializers()
       */
      output.writeInt(((Number) obj).intValue());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new Integer(input.readInt());
    }
  }

  // LONG
  private static final class LongSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.LONG;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeLong(((Long) obj).longValue());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new Long(input.readLong());
    }
  }

  // FLOAT
  private static final class FloatSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.FLOAT;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeFloat(((Float) obj).floatValue());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new Float(input.readFloat());
    }
  }

  // DOUBLE
  private static final class DoubleSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.DOUBLE;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeDouble(((Double) obj).doubleValue());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new Double(input.readDouble());
    }
  }

  // BYTE ARRAY
  private static final class BytesSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.BYTE_ARRAY;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      byte[] b = (byte[]) obj;
      output.writeInt(b.length);
      output.write(b);
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      int len = input.readInt();
      byte[] b = new byte[len];
      input.readFully(b);
      return b;
    }
  }

  // DATE
  private static final class DateSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.SQL_DATE;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeLong(((java.sql.Date) obj).getTime());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new java.sql.Date(input.readLong());
    }
  }

  // TIME
  private static final class TimeSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.SQL_TIME;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      output.writeInt((int) ((java.sql.Time) obj).getTime());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      return new java.sql.Time(input.readInt());
    }
  }

  // TIMESTAMP
  private static final class TimestampSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.SQL_TIMESTAMP;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      Timestamp ts = (Timestamp) obj;
      // put the milliseconds trick/CPU load on the driver side
      output.writeLong(ts.getTime());
      output.writeInt(ts.getNanos());
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      long tsWithMilli = input.readLong();
      // we don't want the milliseconds twice
      Timestamp ts = new Timestamp((tsWithMilli / 1000) * 1000);
      ts.setNanos(input.readInt());
      return ts;
    }
  }

  // CLOB: TODO

  // BLOB
  private static final class BlobSerializer
      extends Serializer
  {
    {
      typeTag = TypeTag.BLOB;
    }

    public void sendToStream(Object obj, CJDBCOutputStream output)
        throws IOException
    {
      java.sql.Blob blob = (java.sql.Blob) obj;
      try
      {
        // Be very careful to be compatible with JAVA_BYTES.sendToStream(),
        // since we use JAVA_BYTES.receiveFromStream on the other side.
        
        if (blob.length() > Integer.MAX_VALUE)
          // FIXME: this is currently corrupting protocol with driver
          throw new IOException("Blobs bigger than " + Integer.MAX_VALUE
              + " are not supported");

        // send the size of the byte array
        output.writeInt((int) blob.length());

        byte[] tempBuffer = new byte[STREAM_BUF_SIZE];
        java.io.InputStream input = blob.getBinaryStream();
        int nbRead;
        while (true)
        {
          nbRead = input.read(tempBuffer);
          if (-1 == nbRead)
            break;
          output.write(tempBuffer, 0, nbRead);
        }
      }
      catch (SQLException e)
      {
        // Exceptions for Blobs is unfortunately tricky because we can't know in
        // advance if a java array will be big enough (2^31) to hold them.
        throw (IOException) new IOException(e.getLocalizedMessage())
            .initCause(e);
      }
    }

    public Object receiveFromStream(CJDBCInputStream input) throws IOException
    {
      byte[] b = (byte[]) JAVA_BYTES.receiveFromStream(input);
      return new org.objectweb.cjdbc.driver.Blob(b);
    }
  }
}
