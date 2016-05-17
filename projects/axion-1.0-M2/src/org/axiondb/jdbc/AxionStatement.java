/*
 * $Id: AxionStatement.java,v 1.25 2003/07/10 22:20:37 rwald Exp $
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

import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.axiondb.AxionCommand;
import org.axiondb.AxionException;
import org.axiondb.util.ExceptionConverter;

/** 
 * A {@link Statement} implementation.
 * 
 * @TODO Support currently unsupported JDBC 3 methods
 * 
 * @version $Revision: 1.25 $ $Date: 2003/07/10 22:20:37 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class AxionStatement extends BaseAxionStatement implements Statement {

    protected AxionStatement(AxionConnection conn) throws SQLException {
        super(conn);
    }

    public void addBatch(String sql) throws SQLException {
        addBatchCommand(parseCommand(sql));
    }

    public void cancel() throws SQLException {
        throw new SQLException("cancel is not supported");
    }

    public void clearBatch() throws SQLException {
        clearBatchCommands();
    }

    public void clearWarnings() throws SQLException {
    }

    public boolean execute(String sql) throws SQLException {
        clearCurrentResult();
        AxionCommand cmd  = parseCommand(sql);
        boolean result = false;
        try {
            result = cmd.execute(getDatabase());
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        } catch(RuntimeException e) {
            throw ExceptionConverter.convert(e);
        }
        setCurrentResult(result,cmd);
        getAxionConnection().commitIfAuto();
        return result;
    }

    public int[] executeBatch() throws SQLException {
        SQLException exception = null;
        int[] results = new int[getBatchCommandCount()];
        int i=0;
        for(Iterator iter = getBatchCommands(); iter.hasNext(); i++) {
            AxionCommand cmd = (AxionCommand)iter.next();
            try {
                results[i] = executeUpdate(cmd);
            } catch(SQLException e) {
                exception = e;
                results[i] = EXECUTE_FAILED;
            }
        }
        clearBatch();
        if(null != exception) {
            throw new BatchUpdateException(exception.getMessage(),results);
        } else {
            return results;
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        clearCurrentResult();
        AxionCommand cmd = parseCommand(sql);
        try {
            setCurrentResultSet(cmd.executeQuery(getDatabase()));
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        } catch(RuntimeException e) {
            throw ExceptionConverter.convert(e);
        }
        if(getAxionConnection().getAutoCommit()) {
            getCurrentResultSet().setTransaction(getAxionConnection().getDatabase().getTransactionManager(),getAxionConnection().forgetCurrentTransaction());            
        }
        return getCurrentResultSet();        
    }

    public int executeUpdate(String sql) throws SQLException {
        return executeUpdate(parseCommand(sql));
    }

    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    public int getFetchSize() throws SQLException {
        return 0;
    }

    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    public boolean getMoreResults() throws SQLException {
        closeCurrentResultSet();
        return false;
    }

    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    public ResultSet getResultSet() throws SQLException {
        return getCurrentResultSet();
    }

    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    public int getUpdateCount() throws SQLException {
        return clearCurrentUpdateCount();
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void setCursorName(String name) throws SQLException {
        // "If the database doesn't suport positioned update/delete, this method is a noop."
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        if(!enable) {
            throw new SQLException("Unsupported");
        }
    }

    public void setFetchDirection(int direction) throws SQLException {
        // setFetchDirection is only a hint anyway
        switch (direction) {
            case ResultSet.FETCH_FORWARD:
            case ResultSet.FETCH_UNKNOWN:
            case ResultSet.FETCH_REVERSE:
                break;
            default:
                throw new SQLException("Unrecoginzed fetch direction " + direction + ".");
        }
    }

    public void setFetchSize(int rows) throws SQLException {
        // setFecthSize is only a hint
        if(rows < 0) {
            throw new SQLException("FetchSize should be non-negative");
        }
    }

    public void setMaxFieldSize(int size) throws SQLException {
        if(size < 0) {
            throw new SQLException("MaxFieldSize should be non-negative");
        } else if(size != 0) {
            throw new SQLException("MaxFieldSize  " + size + " is not supported.");
        }
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        if(seconds < 0) {
            throw new SQLException("QueryTimeout should be non-negative");
        } else if(seconds != 0) {
            throw new SQLException("QueryTimeout " + seconds + " is not supported.");
        }
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return AxionResultSet.createEmptyResultSet();
    }

    // JDBC 3/JDK 1.4 methods
    /** Currently unsupported when autoGeneratedKeys is not Statement.NO_GENERATED_KEYS. */
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        if(Statement.NO_GENERATED_KEYS == autoGeneratedKeys) {
            return execute(sql);
        } else {
            throw new SQLException("autoGeneratedKeys are not supported");
        }
    }

    /** Currently unsupported. */
    public boolean execute(String sql, int columnIndexes[]) throws SQLException {
        throw new SQLException("execute(String,int[]) is currently not supported");
    }

    /** Currently unsupported. */
    public boolean execute(String sql, String columnNames[]) throws SQLException {
        throw new SQLException("execute(String,String[]) is currently not supported");
    }

    /** Currently unsupported when auotGeneratedKeys is not Statement.NO_GENERATED_KEYS. */
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        if(Statement.NO_GENERATED_KEYS == autoGeneratedKeys) {
            return executeUpdate(sql);
        } else {
            throw new SQLException("autoGeneratedKeys are not supported");
        }
    }

    /** Currently unsupported. */
    public int executeUpdate(String arg0, int[] arg1) throws SQLException {
        throw new SQLException("executeUpdate(String,int[]) is currently not supported");
    }

    /** Currently unsupported. */
    public int executeUpdate(String arg0, String[] arg1) throws SQLException {
        throw new SQLException("executeUpdate(String,String[]) is currently not supported");
    }

    /** Currently unsupported when current is not Statement.CLOSE_CURRENT_RESULT or Statement.CLOSE_ALL_RESULTS. */
    public boolean getMoreResults(int current) throws SQLException {
        if(Statement.CLOSE_CURRENT_RESULT == current || Statement.CLOSE_ALL_RESULTS == current) {
            return getMoreResults();
        } else {
            throw new SQLException("getMoreResults(" + current + ") is currently not supported");
        }
    }

    /** Currently unsupported. */
    public int getResultSetHoldability() throws SQLException {
        throw new SQLException("getResultSetHoldability is currently not supported");
    }

    protected void addBatchCommand(AxionCommand cmd) {
        _batchCommands.add(cmd);
    }

    protected void clearBatchCommands() {
        _batchCommands.clear();
    }
    
    protected Iterator getBatchCommands() {
        return _batchCommands.iterator();
    }
    
    protected int getBatchCommandCount() {
        return _batchCommands.size();
    }
    
    protected int executeUpdate(AxionCommand cmd) throws SQLException {
        clearCurrentResult();
        try {
            setCurrentUpdateCount(cmd.executeUpdate(getDatabase()));
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        } catch(RuntimeException e) {
            throw ExceptionConverter.convert(e);
        }
        getAxionConnection().commitIfAuto();
        return getCurrentUpdateCount();
    }


    private List _batchCommands = new ArrayList();


	public Object unwrap(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPoolable(boolean poolable) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public boolean isPoolable() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
}
