/*
 * $Id: BaseAxionStatement.java,v 1.7 2003/07/10 21:11:56 rwald Exp $
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

import java.sql.Connection;
import java.sql.SQLException;

import org.axiondb.AxionCommand;
import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.parser.AxionSqlParser;
import org.axiondb.util.ExceptionConverter;

/** 
 * Abstract base {@link Statement} implementation.
 * @version $Revision: 1.7 $ $Date: 2003/07/10 21:11:56 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public abstract class BaseAxionStatement /* implements Statement */{
    protected BaseAxionStatement(AxionConnection conn) throws SQLException {
        _conn = conn;
        _parser = new AxionSqlParser();
    }

    // ------------------------------------------------------------------------

    public Connection getConnection() throws SQLException {
        return _conn;
    }

    public void close() throws SQLException {
        assertOpen();
        closeCurrentResultSet();
        clearConnection();
        _closed = true;
    }

    public void setMaxRows(int max) throws SQLException {
        if(max < 0) {
            throw new SQLException("MaxRows should be non-negative");
        }
        _maxRows = max;
    }

    public int getMaxRows() throws SQLException {
        return _maxRows;
    }

    // ------------------------------------------------------------------------

    protected void assertOpen() throws SQLException {
        if(_closed) {
            throw new SQLException("Already closed.");
        }
    }
    
    protected AxionConnection getAxionConnection() throws SQLException {
        return (AxionConnection)(getConnection());
    }
    
    protected void clearConnection() {
        _conn = null;
    }

    protected void clearCurrentResult() throws SQLException {
        clearCurrentUpdateCount();
        closeCurrentResultSet();
    }
    
    protected void setCurrentResult(boolean isrset, AxionCommand cmd) {
        if(isrset) {
            setCurrentResultSet((AxionResultSet)cmd.getResultSet());
        } else {
            setCurrentUpdateCount(cmd.getEffectedRowCount());
        }
    }    

    /**
     * @param rset the non-<code>null</code> instance to set current {@link ResultSet} to
     * @see #clearCurrentResult
     */    
    protected void setCurrentResultSet(AxionResultSet rset) {
        rset.setMaxRows(_maxRows);
        _rset = rset;
    }
        
    protected AxionResultSet getCurrentResultSet() {
        return _rset;
    }
        
    protected boolean hasCurrentResultSet() {
        return (null != _rset);
    }
    
    protected void closeCurrentResultSet() throws SQLException {
        try {
            if(null != _rset) { _rset.close(); }
        } finally {
            _rset = null; 
        }
    }
        
    protected int getCurrentUpdateCount() {
        return _updateCount;
    }
    
    protected void setCurrentUpdateCount(int count) {
        _updateCount = count;
    }
    
    protected int clearCurrentUpdateCount() {
        int count = getCurrentUpdateCount();
        setCurrentUpdateCount(-1);
        return count;
    }

    protected Database getDatabase() throws AxionException {
        return _conn.getCurrentTransaction();
    }
    
    protected AxionCommand parseCommand(String sql) throws SQLException {
        try {
            return _parser.parse(sql);
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        } catch(RuntimeException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    // ------------------------------------------------------------------------

    private int _maxRows = 0;
    private AxionResultSet _rset = null;
    private int _updateCount = -1;
    private boolean _closed = false;
    private AxionConnection _conn = null;
    private AxionSqlParser _parser = null;
}
