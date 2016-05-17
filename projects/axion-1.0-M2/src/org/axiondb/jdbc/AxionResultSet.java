/*
 * $Id: AxionResultSet.java,v 1.24 2003/07/09 23:56:17 rwald Exp $
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

package org.axiondb.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.ColumnIdentifier;
import org.axiondb.DataType;
import org.axiondb.RowDecorator;
import org.axiondb.RowDecoratorIterator;
import org.axiondb.Selectable;
import org.axiondb.Transaction;
import org.axiondb.TransactionManager;
import org.axiondb.engine.rowiterators.EmptyRowIterator;
import org.axiondb.engine.rowiterators.RowIteratorRowDecoratorIterator;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link java.sql.ResultSet} implementation.
 *
 * @TODO Support currently unsupported JDBC 3 methods.
 * @version $Revision: 1.24 $ $Date: 2003/07/09 23:56:17 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class AxionResultSet implements ResultSet {

    public AxionResultSet(RowDecoratorIterator rows, Selectable[] selected) {
        _rows = rows;
        _selected = selected;
        _meta = new AxionResultSetMetaData(selected);
    }    

    public void setTransaction(TransactionManager manager, Transaction transaction) {
        _transactionManager = manager;
        _transaction = transaction;
    }


    public static ResultSet createEmptyResultSet() {
        return new AxionResultSet(new RowIteratorRowDecoratorIterator(EmptyRowIterator.INSTANCE,new RowDecorator(Collections.EMPTY_MAP)),new Selectable[0]);
    }    
    
    //------------------------------------------------------- ResultSet Methods

    public boolean absolute(int row) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void afterLast() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void beforeFirst() throws SQLException {
        try {
            _rows.reset();
            _currentRowIndex = 0; //TODO: this should probably be an attribute of the RowDecoratorIterator
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        } catch(RuntimeException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void clearWarnings() throws SQLException {
    }

    public void close() throws SQLException {
        if(null != _transactionManager && null != _transaction) {
            try {
                _transactionManager.commitTransaction(_transaction);
            } catch(AxionException e) {
                throw ExceptionConverter.convert(e);
            }
            _transactionManager = null;
            _transaction = null;
        }
        _closed = true;
        _selected = null;
        _currentRow = null;
    }

    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int findColumn(String colName) throws SQLException {
        assertOpen();
        return getResultSetIndexForColumnName(colName);
    }

    public boolean first() throws SQLException {
        beforeFirst();
        return next();
    }

    public Array getArray(int i) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Array getArray(String colName) throws SQLException {
        return getArray(getResultSetIndexForColumnName(colName));
    }

    public InputStream getAsciiStream(int i) throws SQLException {
        Clob clob = getClob(i);
        if(null == clob) {
            return NULL_STREAM;
        } else {
            return clob.getAsciiStream();
        }
    }

    public InputStream getAsciiStream(String colName) throws SQLException {
        return getAsciiStream(getResultSetIndexForColumnName(colName));
    }

    public BigDecimal getBigDecimal(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_BIGDECIMAL;
        } else {
            return getDataType(i).toBigDecimal(value);
        }
    }

    public BigDecimal getBigDecimal(String colName) throws SQLException {
        return getBigDecimal(getResultSetIndexForColumnName(colName));
    }

    /** @deprecated See {@link java.sql.ResultSet#getBigDecimal(int,int)} */
    public BigDecimal getBigDecimal(int i, int scale) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_BIGDECIMAL;
        } else {
            BigInteger bigint = getDataType(i).toBigInteger(value);
            if(null == bigint) {
                return NULL_BIGDECIMAL;
            } else {
                return new BigDecimal(bigint,scale);
            }
        }
    }

    /** @deprecated See {@link java.sql.ResultSet#getBigDecimal(java.lang.String,int)} */
    public BigDecimal getBigDecimal(String colName, int scale) throws SQLException {
        return getBigDecimal(getResultSetIndexForColumnName(colName),scale);
    }

    public InputStream getBinaryStream(int i) throws SQLException {
        Blob blob = getBlob(i);
        if(null == blob) {
            return NULL_STREAM;
        } else {
            return blob.getBinaryStream();
        }
    }

    public InputStream getBinaryStream(String colName) throws SQLException {
        return getBinaryStream(getResultSetIndexForColumnName(colName));
    }

    public Blob getBlob(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_BLOB;
        } else {
            return getDataType(i).toBlob(value);
        }
    }

    public Blob getBlob(String colName) throws SQLException {
        return getBlob(getResultSetIndexForColumnName(colName));
    }

    public boolean getBoolean(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_BOOLEAN;
        } else {
            return getDataType(i).toBoolean(value);
        }
    }

    public boolean getBoolean(String colName) throws SQLException {
        return getBoolean(getResultSetIndexForColumnName(colName));
    }

    public byte getByte(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_BYTE;
        } else {
            return getDataType(i).toByte(value);
        }
    }

    public byte getByte(String colName) throws SQLException {
        return getByte(getResultSetIndexForColumnName(colName));
    }

    public byte[] getBytes(int i) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public byte[] getBytes(String colName) throws SQLException {
        return getBytes(getResultSetIndexForColumnName(colName));
    }

    public Reader getCharacterStream(int i) throws SQLException {
        Clob clob = getClob(i);
        if(null == clob) {
            return NULL_READER;
        } else {
            return clob.getCharacterStream();
        }
    }

    public Reader getCharacterStream(String colName) throws SQLException {
        return getCharacterStream(getResultSetIndexForColumnName(colName));
    }

    public Clob getClob(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_CLOB;
        } else {
            return getDataType(i).toClob(value);
        }
    }

    public Clob getClob(String colName) throws SQLException {
        return getClob(getResultSetIndexForColumnName(colName));
    }

    public int getConcurrency() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getCursorName() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Date getDate(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_DATE;
        } else {
            return getDataType(i).toDate(value);
        }
    }

    public Date getDate(String colName) throws SQLException {
        return getDate(getResultSetIndexForColumnName(colName));
    }

    public Date getDate(int i, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Date getDate(String colName, Calendar cal) throws SQLException {
        return getDate(getResultSetIndexForColumnName(colName),cal);
    }

    public double getDouble(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_DOUBLE;
        } else {
            return getDataType(i).toDouble(value);
        }
    }

    public double getDouble(String colName) throws SQLException {
        return getDouble(getResultSetIndexForColumnName(colName));
    }

    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public float getFloat(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_FLOAT;
        } else {
            return getDataType(i).toFloat(value);
        }
    }

    public float getFloat(String colName) throws SQLException {
        return getFloat(getResultSetIndexForColumnName(colName));
    }

    public int getInt(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_INT;
        } else {
            return getDataType(i).toInt(value);
        }
    }

    public int getInt(String colName) throws SQLException {
        return getInt(getResultSetIndexForColumnName(colName));
    }

    public long getLong(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_LONG;
        } else {
            return getDataType(i).toLong(value);
        }
    }

    public long getLong(String colName) throws SQLException {
        return getLong(getResultSetIndexForColumnName(colName));
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return _meta;
    }

    public Object getObject(int i) throws SQLException {
        return getValue(i);
    }

    public Object getObject(String colName) throws SQLException {
        return getObject(getResultSetIndexForColumnName(colName));
    }

    public Object getObject(int i, Map map) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(String colName, Map map) throws SQLException {
        return getObject(getResultSetIndexForColumnName(colName),map);
    }

    public Ref getRef(int i) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Ref getRef(String colName) throws SQLException {
        return getRef(getResultSetIndexForColumnName(colName));
    }

    public int getRow() throws SQLException {
        try {
            return _currentRow.getRowIndex() + 1;
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public short getShort(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_SHORT;
        } else {
            return getDataType(i).toShort(value);
        }
    }

    public short getShort(String colName) throws SQLException {
        return getShort(getResultSetIndexForColumnName(colName));
    }

    public Statement getStatement() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getString(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_STRING;
        } else {
            return getDataType(i).toString(value);
        }
    }

    public String getString(String colName) throws SQLException {
        return getString(getResultSetIndexForColumnName(colName));
    }

    public Time getTime(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_TIME;
        } else {
            return getDataType(i).toTime(value);
        }
    }

    public Time getTime(String colName) throws SQLException {
        return getTime(getResultSetIndexForColumnName(colName));
    }

    public Time getTime(int i, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Time getTime(String colName, Calendar cal) throws SQLException {
        return getTime(getResultSetIndexForColumnName(colName),cal);
    }

    public Timestamp getTimestamp(int i) throws SQLException {
        Object value = getValue(i);
        if(null == value) {
            return NULL_TIMESTAMP;
        } else {
            return getDataType(i).toTimestamp(value);
        }
    }

    public Timestamp getTimestamp(String colName) throws SQLException {
        return getTimestamp(getResultSetIndexForColumnName(colName));
    }

    public Timestamp getTimestamp(int i, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Timestamp getTimestamp(String colName, Calendar cal) throws SQLException {
        return getTimestamp(getResultSetIndexForColumnName(colName),cal);
    }

    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    /** @deprecated See {@link java.sql.ResultSet#getUnicodeStream} */
    public InputStream getUnicodeStream(int i) throws SQLException {
        String val = getString(i);
        if(null == val) {
            return NULL_STREAM;
        } else {
            try {
                return new ByteArrayInputStream(val.getBytes("UnicodeBig"));
            } catch(UnsupportedEncodingException e) {
                throw ExceptionConverter.convert(e);
            }
        }
    }

    /** @deprecated See {@link java.sql.ResultSet#getUnicodeStream} */
    public InputStream getUnicodeStream(String colName) throws SQLException {
        return getUnicodeStream(getResultSetIndexForColumnName(colName));
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isAfterLast() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isBeforeFirst() throws SQLException {
        return 0 == _currentRowIndex && _rows.hasNext();
    }

    public boolean isFirst() throws SQLException {
        return 1 == _currentRowIndex;
    }

    public boolean isLast() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean last() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean next() throws SQLException {       
        assertOpen();
        if(_maxRows > 0 && _currentRowIndex >= _maxRows) {
            return false;
        }            
        boolean result = _rows.hasNext();
        if(result) {
            try {
                _currentRow = _rows.next();
            } catch(AxionException e) {
                throw ExceptionConverter.convert(e);
            }
            _currentRowIndex++;
        }
        return result;
    }

    public boolean previous() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean relative(int rows) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowDeleted() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowInserted() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowUpdated() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setFetchDirection(int direction) throws SQLException {
        // fetchDirection is just a hint
    }

    public void setFetchSize(int size) throws SQLException {
        // fetch size is just a hint
    }

    public void updateAsciiStream(int i, InputStream in, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(String colName, InputStream in, int length) throws SQLException {
        updateAsciiStream(getResultSetIndexForColumnName(colName),in,length);
    }

    public void updateBigDecimal(int i, BigDecimal value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBigDecimal(String colName, BigDecimal value) throws SQLException {
        updateBigDecimal(getResultSetIndexForColumnName(colName),value);
    }

    public void updateBinaryStream(int i, InputStream value, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(String colName, InputStream value, int length) throws SQLException {
        updateBinaryStream(getResultSetIndexForColumnName(colName),value,length);
    }

    public void updateBoolean(int i, boolean value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBoolean(String colName, boolean value) throws SQLException {
        updateBoolean(getResultSetIndexForColumnName(colName),value);
    }

    public void updateByte(int i, byte value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateByte(String colName, byte value) throws SQLException {
        updateByte(getResultSetIndexForColumnName(colName),value);
    }

    public void updateBytes(int i, byte[] value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBytes(String colName, byte[] value) throws SQLException {
        updateBytes(getResultSetIndexForColumnName(colName),value);
    }

    public void updateCharacterStream(int i, Reader value, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(String colName, Reader value, int length) throws SQLException {
        updateCharacterStream(getResultSetIndexForColumnName(colName),value,length);
    }

    public void updateDate(int i, Date value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDate(String colName, Date value) throws SQLException {
        updateDate(getResultSetIndexForColumnName(colName),value);
    }

    public void updateDouble(int i, double value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDouble(String colName, double value) throws SQLException {
        updateDouble(getResultSetIndexForColumnName(colName),value);
    }

    public void updateFloat(int i, float value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateFloat(String colName, float value) throws SQLException {
        updateFloat(getResultSetIndexForColumnName(colName),value);
    }

    public void updateInt(int i, int value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateInt(String colName, int value) throws SQLException {
        updateInt(getResultSetIndexForColumnName(colName),value);
    }

    public void updateLong(int i, long value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateLong(String colName, long value) throws SQLException {
        updateLong(getResultSetIndexForColumnName(colName),value);
    }

    public void updateNull(int i) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNull(String colName) throws SQLException {
        updateNull(getResultSetIndexForColumnName(colName));
    }

    public void updateObject(int i, Object value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateObject(String colName, Object value) throws SQLException {
        updateObject(getResultSetIndexForColumnName(colName),value);
    }

    public void updateObject(int i, Object value, int scale) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateObject(String colName, Object value, int scale) throws SQLException {
        updateObject(getResultSetIndexForColumnName(colName),value,scale);
    }

    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateShort(int i, short value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateShort(String colName, short value) throws SQLException {
        updateShort(getResultSetIndexForColumnName(colName),value);
    }

    public void updateString(int i, String value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateString(String colName, String value) throws SQLException {
        updateString(getResultSetIndexForColumnName(colName),value);
    }

    public void updateTime(int i, Time value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTime(String colName, Time value) throws SQLException {
        updateTime(getResultSetIndexForColumnName(colName),value);
    }

    public void updateTimestamp(int i, Timestamp value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTimestamp(String colName, Timestamp value) throws SQLException {
        updateTimestamp(getResultSetIndexForColumnName(colName),value);
    }

    public boolean wasNull() throws SQLException {
        return _wasNull;
    }

    public void setMaxRows(int max) {
        _maxRows = max;
    }

    //------------------------------------------------------------ Private Util

    /**
     * Get the 1-based ResultSet index for the specified column name.
     */
    private int getResultSetIndexForColumnName(String columnname) throws SQLException {
        ColumnIdentifier id = null;
        for(int i=0;i<_selected.length;i++) {
            if(_selected[i] instanceof ColumnIdentifier) {
                id = (ColumnIdentifier)(_selected[i]);
                if(columnname.equalsIgnoreCase(id.getName())) {
                    return (i+1);
                }
            }
        }
        throw new SQLException("No column named " + columnname + " found.");
    }

    /** Throw a {@link SQLException} if there is no {@link #_currentRow}. */
    private final void assertCurrentRow() throws SQLException {
        if(null == _currentRow) {
            throw new SQLException("No current row");
        }
    }

    private final void assertOpen() throws SQLException {
        if(_closed) {
            throw new SQLException("Already closed");
        }
    }

    /**
     * Obtain the value from the current row for the
     * given 1-based (ResultSet) index, and convert it
     * according to the corresponding {@link DataType}
     */
    private Object getValue(int num) throws SQLException {
        assertCurrentRow();
        assertOpen();
        Selectable sel = _selected[num-1];
        try {
            Object val = sel.evaluate(_currentRow);
            _wasNull = (null == val);
            return val;
        } catch(AxionException e) {
            if(_log.isDebugEnabled()) {
                _log.debug("AxionException in getValue",e);
            }
            throw ExceptionConverter.convert(e);
        }
    }

    /**
     * Obtain the DataType for the
     * given 1-based (ResultSet) index
     */
    private DataType getDataType(int num) throws SQLException {
        Selectable sel = _selected[num-1];
        return sel.getDataType();
    }

    //-------------------------------------------------------------- Attributes

    protected RowDecoratorIterator _rows = null;
    private RowDecorator _currentRow = null;
    private Selectable[] _selected = null;
    private boolean _closed = false;
    private int _maxRows = 0;
    private int _currentRowIndex = 0;
    private ResultSetMetaData _meta = null;

    /** Whether the last value returned was NULL. */
    private boolean _wasNull = false;

    /** What {@link #getInt} returns when the corresponding value is NULL. */
    private static final int NULL_INT = 0;

    /** What {@link #getFloat} returns when the corresponding value is NULL. */
    private static final float NULL_FLOAT = (float)0;

    /** What {@link #getShort} returns when the corresponding value is NULL. */
    private static final short NULL_SHORT = (short)0;

    /** What {@link #getLong} returns when the corresponding value is NULL. */
    private static final long NULL_LONG = 0L;

    /** What {@link #getByte} returns when the corresponding value is NULL. */
    private static final byte NULL_BYTE = (byte)0;

    /** What {@link #getBoolean} returns when the corresponding value is NULL. */
    private static final boolean NULL_BOOLEAN = false;

    /** What {@link #getDouble} returns when the corresponding value is NULL. */
    private static final double NULL_DOUBLE = 0d;

    /** What {@link #getDate} returns when the corresponding value is NULL. */
    private static final Date NULL_DATE = null;

    /** What {@link #getTime} returns when the corresponding value is NULL. */
    private static final Time NULL_TIME = null;

    /** What {@link #getTimestamp} returns when the corresponding value is NULL. */
    private static final Timestamp NULL_TIMESTAMP = null;

    /** What {@link #getString} returns when the corresponding value is NULL. */
    private static final String NULL_STRING = null;

    /** What {@link #getClob} returns when the corresponding value is NULL. */
    private static final Clob NULL_CLOB = null;

    /** What {@link #getBlob} returns when the corresponding value is NULL. */
    private static final Blob NULL_BLOB = null;

    /** What {@link #getBinaryStream} returns when the corresponding value is NULL. */
    private static final InputStream NULL_STREAM = null;

    /** What {@link #getCharacterStream} returns when the corresponding value is NULL. */
    private static final Reader NULL_READER = null;

    /** What {@link #getBigDecimal} returns when the corresponding value is NULL. */
    private static final BigDecimal NULL_BIGDECIMAL = null;

    private static Log _log = LogFactory.getLog(AxionResultSet.class);
    private Transaction _transaction = null;
    private TransactionManager _transactionManager = null;

    // JDBC 3/JDK 1.4 methods
    /** Currently unsupported. */
    public URL getURL(int arg0) throws SQLException {
        throw new SQLException("getURL is currently not supported");
    }

    /** Currently unsupported. */
    public URL getURL(String arg0) throws SQLException {
        throw new SQLException("getURL is currently not supported");
    }

    /** Currently unsupported. */
    public void updateArray(int arg0, Array arg1) throws SQLException {
        throw new SQLException("updateArray is currently not supported");
    }

    /** Currently unsupported. */
    public void updateArray(String arg0, Array arg1) throws SQLException {
        throw new SQLException("updateArray is currently not supported");
    }

    /** Currently unsupported. */
    public void updateBlob(int arg0, Blob arg1) throws SQLException {
        throw new SQLException("updateBlob is currently not supported");
    }

    /** Currently unsupported. */
    public void updateBlob(String arg0, Blob arg1) throws SQLException {
        throw new SQLException("updateBlob is currently not supported");
    }

    /** Currently unsupported. */
    public void updateClob(int arg0, Clob arg1) throws SQLException {
        throw new SQLException("updateClob is currently not supported");
    }

    /** Currently unsupported. */
    public void updateClob(String arg0, Clob arg1) throws SQLException {
        throw new SQLException("updateClob is currently not supported");
    }

    /** Currently unsupported. */
    public void updateRef(int arg0, Ref arg1) throws SQLException {
        throw new SQLException("updateRef is currently not supported");
    }

    /** Currently unsupported. */
    public void updateRef(String arg0, Ref arg1) throws SQLException {
        throw new SQLException("updateRef is currently not supported");
    }

	public Object unwrap(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateNString(int columnIndex, String nString)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNString(String columnLabel, String nString)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public NClob getNClob(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public String getNString(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNString(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
