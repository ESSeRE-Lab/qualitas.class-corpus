/*
 * $Id: StringClob.java,v 1.5 2003/05/14 17:00:30 rwald Exp $
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

package org.axiondb.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import org.axiondb.jdbc.AxionClob;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link Clob} based upon a simple {@link String}.
 * This type allows any string value to be treated as 
 * a {@link AxionClob}.
 * 
 * @version $Revision: 1.5 $ $Date: 2003/05/14 17:00:30 $
 * @author Rodney Waldhoff
 */
public class StringClob implements AxionClob {
    
    public StringClob(String str) {
        if(null == str) {
            throw new NullPointerException();
        }
        _value = str;
    }

    public InputStream getAsciiStream() throws SQLException {
        try {
            return new ByteArrayInputStream(_value.getBytes("ASCII"));
        } catch(UnsupportedEncodingException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public Reader getCharacterStream() throws SQLException {
        return new StringReader(_value);
    }

    public String getSubString(long pos, int len) throws SQLException {
        return _value.substring((int)pos,((int)pos+len));
    }

    public long length() throws SQLException {
        return _value.length();
    }

    public long position(Clob searchstr, long start) throws SQLException {
        if(null == searchstr) {
            throw new SQLException("search string was null");
        }
        StringBuffer buf = new StringBuffer();
        Reader in = searchstr.getCharacterStream();
        try {
            for(int c = in.read(); c != -1; c = in.read()) {
                buf.append((char)c);
            }
        } catch(IOException e) {
            throw ExceptionConverter.convert(e);
        }
        return position(buf.toString(),start);
    }

    public long position(String searchstr, long start) throws SQLException {
        if(null == searchstr) {
            throw new SQLException("search string was null");
        }
        return (long)(_value.indexOf(searchstr,(int)start));
    }

    public OutputStream setAsciiStream(long pos) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public Writer setCharacterStream(long pos) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public int setString(long pos, String str) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public int setString(long pos, String str, int offset, int length) throws SQLException {
        throw new SQLException("Unsupported");
    }

    public String getString() {
        return _value;
    }

    public void truncate(long length) throws SQLException {
        throw new SQLException("Unsupported.");
    }

    private String _value = null;

	public void free() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
