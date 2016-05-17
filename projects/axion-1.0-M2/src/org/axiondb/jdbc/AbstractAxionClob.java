/*
 * $Id: AbstractAxionClob.java,v 1.7 2003/07/10 16:34:34 rwald Exp $
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import org.axiondb.util.ExceptionConverter;

/**
 * Abstract base implementation of {@link AxionClob}.
 * 
 * @version $Revision: 1.7 $ $Date: 2003/07/10 16:34:34 $
 * @author Rodney Waldhoff
 * @author Morgan Delegrange
 */
public abstract class AbstractAxionClob implements AxionClob {
    
    public InputStream getAsciiStream() throws SQLException {
        throw new SQLException("Unsupported");
    }

    public Reader getCharacterStream() throws SQLException {
        throw new SQLException("Unsupported");
    }

    public String getSubString(long pos, int len) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public long length() throws SQLException {
        throw new SQLException("Unsupported");
    }

    public long position(Clob searchstr, long start) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public long position(String searchstr, long start) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public OutputStream setAsciiStream(long pos) throws SQLException {
        throw new SQLException("Unsupported");
    }
    
    /** @see #setCharacterStream */
    public abstract OutputStream setUtf8Stream(long pos) throws SQLException;

    //TODO: since there is byte to character conversion, 
    //the pos argument will not work correctly for non-zero
    //values - fix me
    public Writer setCharacterStream(long pos) throws SQLException {
         try {   
             return new OutputStreamWriter(setUtf8Stream(pos),"UTF8");   
         } catch(UnsupportedEncodingException e) {
             throw ExceptionConverter.convert(e);   
         } 
    }

    /** Invokes {@link #setString(long,String,int,int) setString(pos,str,0,str.length())} */
    public int setString(long pos, String str) throws SQLException {
        return setString(pos,str,0,str.length());
    }

    public int setString(long pos, String str, int offset, int length) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public void truncate(long length) throws SQLException {
        throw new SQLException("Unsupported");
    }
}
