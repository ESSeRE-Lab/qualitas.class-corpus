/*
 * $Id: AxionConnection.java,v 1.21 2003/07/09 23:56:18 rwald Exp $
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

import java.io.File;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.Transaction;
import org.axiondb.TransactionConflictException;
import org.axiondb.engine.Databases;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link Connection} implementation.
 * @TODO Support currently unsupported JDBC 3 methods
 *
 * @version $Revision: 1.21 $ $Date: 2003/07/09 23:56:18 $
 * @author Chuck Burdick
 */
public class AxionConnection implements Connection {
    protected AxionConnection(String name, File path, String url) throws AxionException {
        setUrl(url);
        setDatabase(Databases.getOrCreateDatabase(name,path));
    }

    public AxionConnection(Database db, String url) {
        setDatabase(db);
        setUrl(url);
    }

    public AxionConnection(Database db) {
        this(db,null);
    }

    public void clearWarnings() throws SQLException {
    }

    public void close() throws SQLException {
        if(null == _db) {
            throw new SQLException("Already closed.");
        }
        if(!(_db.getTransactionManager().isShutdown())) {
            rollback();
        }
        try {
            _db.checkpoint();
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
        _db = null;
    }

    public void commit() throws SQLException {
        try {
            if(null != _currentTransaction) {
                _db.getTransactionManager().commitTransaction(_currentTransaction);
                _currentTransaction = null;
            }
        } catch(TransactionConflictException e) {
            throw ExceptionConverter.convert("Transaction Conflict",e);
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }           
    }
    
    public void rollback() throws SQLException {
        try {
            if(null != _currentTransaction) {
                _db.getTransactionManager().abortTransaction(_currentTransaction);
                _currentTransaction = null;
            }
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }           
    }

    public Statement createStatement() throws SQLException {
        return new AxionStatement(this);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLException("not supported");
    }

    public boolean getAutoCommit() throws SQLException {
        return _autoCommit;
    }

    public String getCatalog() throws SQLException {
        return "";
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return new AxionDatabaseMetaData(this,_db);
    }

    public int getTransactionIsolation() throws SQLException {
        return _isolationLevel;
    }

    public Map getTypeMap() throws SQLException {
        return Collections.EMPTY_MAP;
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public boolean isClosed() throws SQLException {
        return (_db == null);
    }

    public boolean isReadOnly() throws SQLException {
        return false;
    }

    public String nativeSQL(String sql) throws SQLException {
        return sql;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLException("not supported");
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLException("not supported");
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new AxionPreparedStatement(this,sql);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLException("not supported");
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        _autoCommit = autoCommit;
    }

    public void setCatalog(String catalog) throws SQLException {
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
    }

    public void setTransactionIsolation(int level) throws SQLException {
        if(level == Connection.TRANSACTION_SERIALIZABLE) {
            _isolationLevel = level;
        } else {
            throw new SQLException("Transcation isolation level " + level + " is not supported.");
        }
    }

    public void setTypeMap(Map types) throws SQLException {
    }

    public String getURL() {
        return _url;
    }

    // **** HELPER METHODS ****

    public org.axiondb.Database getDatabase() {
        return _db;
    }

    public Transaction getCurrentTransaction() throws AxionException {
        if(null == _currentTransaction) {            
            _currentTransaction = _db.getTransactionManager().createTransaction();
        }
        return _currentTransaction;
    }
    
    public Transaction forgetCurrentTransaction() {
        Transaction temp = _currentTransaction;
        _currentTransaction = null;
        return temp;
    }
    
    public void commitIfAuto() throws SQLException {
        if(getAutoCommit() && !(_db.getTransactionManager().isShutdown())) {
            commit();
        }
    }
    
    private static Log _log = LogFactory.getLog(AxionConnection.class);
    private Database _db = null;    
    private String _url = null;
    private Transaction _currentTransaction = null;
    private int _isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
    private boolean _autoCommit = true;

    // JDBC 3/JDK 1.4 methods
    /** Currently unsupported. */
    public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
        throw new SQLException("createStatement(int,int,int) is currently not supported");
    }

    /** Currently unsupported. */
    public int getHoldability() throws SQLException {
        throw new SQLException("getHoldability is currently not supported");
    }

    /** Currently unsupported. */
    public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
        throw new SQLException("prepareCall(String,int,int,int) is currently not supported");
    }

    /** Currently unsupported. */
    public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
        throw new SQLException("prepareStatement(String,int,int,int) is currently not supported");
    }

    /** Currently unsupported. */
    public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
        throw new SQLException("prepareStatement(String,int) is currently not supported");
    }

    /** Currently unsupported. */
    public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
        throw new SQLException("prepareStatement(String,int[]) is currently not supported");
    }

    /** Currently unsupported. */
    public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
        throw new SQLException("prepareStatement(String,String[]) is currently not supported");
    }

    /** Currently unsupported. */
    public void releaseSavepoint(Savepoint arg0) throws SQLException {
        throw new SQLException("releaseSavepoint(Savepoint) is currently not supported");
    }

    /** Currently unsupported. */
    public void rollback(Savepoint arg0) throws SQLException {
        throw new SQLException("rollback(Savepoint) is currently not supported");
    }

    /** Currently unsupported. */
    public void setHoldability(int arg0) throws SQLException {
        throw new SQLException("setHoldability(int) is currently not supported");
    }

    /** Currently unsupported. */
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLException("setSavepoint is currently not supported");
    }

    /** Currently unsupported. */
    public Savepoint setSavepoint(String arg0) throws SQLException {
        throw new SQLException("setSavepoint(String) is currently not supported");
    }

    private void setUrl(String url) {
        _url = url;
    }
    
    private void setDatabase(Database db) {
        _db = db;
    }

	public Object unwrap(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValid(int timeout) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	public String getClientInfo(String name) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
