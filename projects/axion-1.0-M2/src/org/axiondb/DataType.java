/*
 * $Id: DataType.java,v 1.8 2003/05/14 19:07:31 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above 
 *    copyright notice, this list of conditions and the following 
 *    disclaimer. 
 *   
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *   
 * 3. The names "Tigris", "Axion", nor the names of its contributors may 
 *    not be used to endorse or promote products derived from this 
 *    software without specific prior written permission. 
 *  
 * 4. Products derived from this software may not be called "Axion", nor 
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Comparator;

/**
 * The type of a field (column) that can be stored in a {@link Table}.
 * <p>
 * Responsible for {@link #accepts testing} that a value is assignable 
 * to fields of this type, for {@link #convert converting} 
 * {@link Object Objects} to this type, and for 
 * {@link #read reading} values from and {@link #write writing} values
 * to a stream.
 *
 * @version $Revision: 1.8 $ $Date: 2003/05/14 19:07:31 $
 * @author Rodney Waldhoff
 * @author Rob Oxspring
 * @author Chuck Burdick
 */
public interface DataType extends Serializable {
    /**
     * Returns the "normal" type returned by {@link #convert}.
     * Returns <tt>java.lang.Object</tt> if unknown.
     * @see java.sql.ResultSetMetaData#getColumnClassName 
     */
    String getPreferredValueClassName();

    /**
     * Returns the JDBC {@link java.sql.Types type code} most closely 
     * matching this type.
     */
    int getJdbcType();

    /**
     * Returns a comparator used for comparing and ordering values of this type.
     */
    Comparator getComparator();
    
    /** 
     * Value returned by {@link ResultSetMetaData#getColumnDisplaySize}
     * for this data type.
     * @see java.sql.ResultSetMetaData#getColumnDisplaySize
     */
    int getColumnDisplaySize();

    /** 
     * Value returned by {@link ResultSetMetaData#getScale}
     * for this data type.
     * @see java.sql.ResultSetMetaData#getScale
     */
    int getScale();

    /** 
     * Value returned by {@link ResultSetMetaData#getPrecision}
     * for this data type.
     * @see java.sql.ResultSetMetaData#getPrecision
     */
    int getPrecision();

    /**
     * For character and string-related types, indicates whether type
     * acknowledges case when storing and retrieving values
     * @see java.sql.DatabaseMetaData#getTypeInfo
     * @see java.sql.ResultSetMetaData#isCaseSensitive
     */
    boolean isCaseSensitive();

    /**
     * @see java.sql.ResultSetMetaData#isCurrency
     */
    boolean isCurrency();

    /**
     * Prefix used to quote a literal to delimit value for this type
     * when in SQL syntax or result display
     * @see java.sql.DatabaseMetaData#getTypeInfo
     */
    String getLiteralPrefix();

    /**
     * Suffix used to quote a literal to delimit value for this type
     * when in SQL syntax or result display
     * @see java.sql.DatabaseMetaData#getTypeInfo
     */
    String getLiteralSuffix();

    /**
     * Code indicating that type does not accept, does accept, or does
     * not disclose acceptance of <code>null</code> values
     * @see java.sql.DatabaseMetaData#getTypeInfo
     */
    int getNullableCode();

    /**
     * Code indicating how much <code>WHERE ... LIKE</code> support is
     * available across a column of this type
     * @see java.sql.DatabaseMetaData#getTypeInfo
     */
    short getSearchableCode();

    /**
     * For numeric types, indicates whether type stores only non-negative
     * (&gt;= 0) values
     * @see java.sql.DatabaseMetaData#getTypeInfo
     */
    boolean isUnsigned();

    /**
     * Return <code>true</code> if a field of my type can
     * be assigned the given non-<code>null</code> <i>value</i>, 
     * <code>false</code> otherwise.
     * @param value non-<code>null</code> value
     */
    boolean accepts(Object value);

    /** 
     * Converts an {@link #accepts acceptable} value
     * to one of the appropriate type.
     */
    Object convert(Object value);

    /**
     * Instantiate an object of my type from the given {@link DataInput}.
     * The next sequence of bytes to be read from the <code>DataInput</code>
     * will have been written by {@link #write}.
     */
    Object read(DataInput in) throws IOException;

    /**
     * Write an object of my type to the given {@link DataOutput}.
     * @param value the value to write, which must be 
     *              {@link #accepts acceptable} to this <code>DataType</code>
     */
    void write(Object value, DataOutput out) throws IOException;

    /**
     * Returns <code>true</code> if the 
     * {@link #successor} method is supported,
     * false otherwise.
     */
    boolean supportsSuccessor();

    /**
     * Returns the successor for the given value.
     * For example, the successor of the integer 1 is 2.
     */
    Object successor(Object value) throws UnsupportedOperationException;

    /**
     * Convert the given non-<code>null</code> <i>value</i> 
     * to a <code>boolean</code>,
     * or throw a {@link SQLException}.
     * @see java.sql.ResultSet#getBoolean 
     */
    boolean toBoolean(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>byte</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getByte
     */
    byte toByte(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i> 
     * to a {@link java.sql.Date},
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getDate
     */
    Date toDate(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>double</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getDouble
     */
    double toDouble(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>float</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getFloat
     */
    float toFloat(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>int</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getInt
     */
    int toInt(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>long</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getLong
     */
    long toLong(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>short</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getShort
     */
    short toShort(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>BigDecimal</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getBigDecimal
     */
    BigDecimal toBigDecimal(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a <code>BigInteger</code>,
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getBigInteger
     */
    BigInteger toBigInteger(Object value) throws SQLException;
    
    /**
     * Convert the given non-<code>null</code> <i>value</i> 
     * to a {@link String},
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getString
     */
    String toString(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i> 
     * to a {@link Time},
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getTime
     */
    Time toTime(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a {@link Timestamp},
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getTimestamp
     */
    Timestamp toTimestamp(Object value) throws SQLException;


    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a {@link Clob},
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getClob
     */
    Clob toClob(Object value) throws SQLException;

    /**
     * Convert the given non-<code>null</code> <i>value</i>
     * to a {@link Blob},
     * or throw a {@link SQLException}. 
     * @see java.sql.ResultSet#getBlob
     */
    Blob toBlob(Object value) throws SQLException;
}
