/*
 * $Id: AxionDataSource.java,v 1.3 2003/07/09 23:56:18 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002 Axion Development Team.  All rights reserved.
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.axiondb.AxionException;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link DataSource} implementation.
 *
 * @version $Revision: 1.3 $ $Date: 2003/07/09 23:56:18 $
 * @author Rodney Waldhoff
 */
public class AxionDataSource extends ConnectionFactory implements DataSource {
    public AxionDataSource(String connectString) {
        _connectString = connectString;
    }

    public Connection getConnection() throws SQLException {
        if(isValidConnectString(_connectString)) {
            try {
                return createConnection(_connectString);
            } catch(AxionException e) {
                throw ExceptionConverter.convert(e);
            }
        } else {
            throw new SQLException("Can't create a Connection for " + _connectString);
        }
    }

    public Connection getConnection(String uname, String passwd) throws SQLException {
        return getConnection();
    }

    public int getLoginTimeout() {
        return _loginTimeout;
    }

    public void setLoginTimeout(int seconds) {
        _loginTimeout = seconds;
    }

    public PrintWriter getLogWriter() {
        return _logWriter;
    }

    public void setLogWriter(PrintWriter log) {
        _logWriter = log;
    }

    private int _loginTimeout = 0;
    private PrintWriter _logWriter = null;
    private String _connectString = null;
}
