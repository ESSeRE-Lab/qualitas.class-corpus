/*
 
   Derby - Class org.apache.derby.impl.jdbc.EmbedResultSet40
 
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
 */

package org.apache.derby.impl.jdbc;

import org.apache.derby.iapi.sql.ResultSet;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.derby.iapi.reference.SQLState;

/**
 * JDBC 4 specific methods that cannot be implemented in superclasses and
 * unimplemented JDBC 4 methods.
 * In general, the implementations should be pushed to the superclasses. This
 * is not possible if the methods use objects or features not available in the
 * Java version associated with the earlier JDBC version, since Derby classes
 * are compiled with the lowest possible Java version.
 */
public class EmbedResultSet40 extends org.apache.derby.impl.jdbc.EmbedResultSet20{
    
    /** Creates a new instance of EmbedResultSet40 */
    public EmbedResultSet40(org.apache.derby.impl.jdbc.EmbedConnection conn,
        ResultSet resultsToWrap,
        boolean forMetaData,
        org.apache.derby.impl.jdbc.EmbedStatement stmt,
        boolean isAtomic)
        throws SQLException {
        
        super(conn, resultsToWrap, forMetaData, stmt, isAtomic);
    }
    
    public RowId getRowId(int columnIndex) throws SQLException {
        throw Util.notImplemented();
    }
    
    
    public RowId getRowId(String columnName) throws SQLException {
        throw Util.notImplemented();
    }

    public void updateNCharacterStream(int columnIndex, Reader x)
            throws SQLException {
        throw Util.notImplemented();
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length)
        throws SQLException {
        throw Util.notImplemented();
    }

    public void updateNCharacterStream(String columnName, Reader x)
            throws SQLException {
        throw Util.notImplemented();
    }

    public void updateNCharacterStream(String columnName, Reader x, long length)
        throws SQLException {
        throw Util.notImplemented();
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw Util.notImplemented();
    }
    
    public void updateNString(String columnName, String nString) throws SQLException {
        throw Util.notImplemented();
    }
    
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw Util.notImplemented();
    }

    public void updateNClob(int columnIndex, Reader reader)
            throws SQLException {
        throw Util.notImplemented();
    }
    
    public void updateNClob(String columnName, NClob nClob) throws SQLException {
        throw Util.notImplemented();
    }

    public void updateNClob(String columnName, Reader reader)
            throws SQLException {
        throw Util.notImplemented();
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw Util.notImplemented();
    }
    
    public Reader getNCharacterStream(String columnName) throws SQLException {
        throw Util.notImplemented();
    }

    public NClob getNClob(int i) throws SQLException {
        throw Util.notImplemented();
    }
    
    public NClob getNClob(String colName) throws SQLException {
        throw Util.notImplemented();
    }
    
    public String getNString(int columnIndex) throws SQLException {
        throw Util.notImplemented();
    }
    
    public String getNString(String columnName) throws SQLException {
        throw Util.notImplemented();
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw Util.notImplemented();
    }

    public void updateRowId(String columnName, RowId x) throws SQLException {
        throw Util.notImplemented();
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw Util.notImplemented();
    }
    
    public SQLXML getSQLXML(String colName) throws SQLException {
        throw Util.notImplemented();
    }
    
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw Util.notImplemented();
    }
    
    public void updateSQLXML(String columnName, SQLXML xmlObject) throws SQLException {
        throw Util.notImplemented();
    }
    
    /**
     * Returns false unless <code>interfaces</code> is implemented 
     * 
     * @param  interfaces             a Class defining an interface.
     * @return true                   if this implements the interface or 
     *                                directly or indirectly wraps an object 
     *                                that does.
     * @throws java.sql.SQLException  if an error occurs while determining 
     *                                whether this is a wrapper for an object 
     *                                with the given interface.
     */
    public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
        checkIfClosed("isWrapperFor");
        return interfaces.isInstance(this);
    }
    
    /**
     * Returns <code>this</code> if this class implements the interface
     *
     * @param  interfaces a Class defining an interface
     * @return an object that implements the interface
     * @throws java.sql.SQLExption if no object if found that implements the 
     * interface
     */
    public <T> T unwrap(java.lang.Class<T> interfaces) 
                            throws SQLException{
        checkIfClosed("unwrap");
        //Derby does not implement non-standard methods on 
        //JDBC objects
        //hence return this if this class implements the interface 
        //or throw an SQLException
        try {
            return interfaces.cast(this);
        } catch (ClassCastException cce) {
            throw newSQLException(SQLState.UNABLE_TO_UNWRAP,interfaces);
        }
    }
    
    /**
     *
     * Updates the designated column using the given Reader  object,
     * which is the given number of characters long.
     *
     * @param columnIndex -
     *        the first column is 1, the second is 2
     * @param x -
     *        the new column value
     * @param length -
     *        the length of the stream
     *
     * @exception SQLException
     *                Feature not implemented for now.
     */
    public void updateNClob(int columnIndex, Reader x, long length)
    throws SQLException {
        throw Util.notImplemented();
    }

    /**
     * Updates the designated column using the given Reader  object,
     * which is the given number of characters long.
     *
     * @param columnName -
     *            the Name of the column to be updated
     * @param x -
     *            the new column value
     * @param length -
     *        the length of the stream
     *
     * @exception SQLException
     *                Feature not implemented for now.
     *
     */
    public void updateNClob(String columnName, Reader x, long length)
    throws SQLException{
        throw Util.notImplemented();
    }

    ////////////////////////////////////////////////////////////////////
    //
    // INTRODUCED BY JDBC 4.1 IN JAVA 7
    //
    ////////////////////////////////////////////////////////////////////
    
    /**
     * Retrieve the column as an object of the desired type.
     */
    public  <T> T getObject( int columnIndex, Class<T> type )
            throws SQLException
    {
        checkIfClosed("getObject");

        if ( type == null )
        {
            throw mismatchException( "NULL", columnIndex );
        }

        Object   retval;
            
        if ( String.class.equals( type ) ) { retval = getString( columnIndex ); }
        else if ( BigDecimal.class.equals( type ) ) { retval = getBigDecimal( columnIndex ); }
        else if ( Boolean.class.equals( type ) ) { retval = Boolean.valueOf( getBoolean(columnIndex ) ); }
        else if ( Byte.class.equals( type ) ) { retval = Byte.valueOf( getByte( columnIndex ) ); }
        else if ( Short.class.equals( type ) ) { retval = Short.valueOf( getShort( columnIndex ) ); }
        else if ( Integer.class.equals( type ) ) { retval = Integer.valueOf( getInt( columnIndex ) ); }
        else if ( Long.class.equals( type ) ) { retval = Long.valueOf( getLong( columnIndex ) ); }
        else if ( Float.class.equals( type ) ) { retval = Float.valueOf( getFloat( columnIndex ) ); }
        else if ( Double.class.equals( type ) ) { retval = Double.valueOf( getDouble( columnIndex ) ); }
        else if ( Date.class.equals( type ) ) { retval = getDate( columnIndex ); }
        else if ( Time.class.equals( type ) ) { retval = getTime( columnIndex ); }
        else if ( Timestamp.class.equals( type ) ) { retval = getTimestamp( columnIndex ); }
        else if ( Blob.class.equals( type ) ) { retval = getBlob( columnIndex ); }
        else if ( Clob.class.equals( type ) ) { retval = getClob( columnIndex ); }
        else if ( type.isArray() && type.getComponentType().equals( byte.class ) ) { retval = getBytes( columnIndex ); }
        else { retval = getObject( columnIndex ); }

        if ( wasNull() ) { retval = null; }

        if ( (retval == null) || (type.isInstance( retval )) ) { return type.cast( retval ); }
        
        throw mismatchException( type.getName(), columnIndex );
    }
    private SQLException    mismatchException( String targetTypeName, int columnIndex )
        throws SQLException
    {
        String sourceTypeName = getMetaData().getColumnTypeName( columnIndex );
        SQLException se = newSQLException( SQLState.LANG_DATA_TYPE_GET_MISMATCH, targetTypeName, sourceTypeName );

        return se;
    }

    public  <T> T getObject( String columnName, Class<T> type )
            throws SQLException
    {
        checkIfClosed("getObject");

        return getObject( findColumn( columnName ), type );
    }


}
