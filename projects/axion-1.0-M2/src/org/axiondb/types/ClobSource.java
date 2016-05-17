/*
 * $Id: ClobSource.java,v 1.5 2003/05/27 19:08:56 morgand Exp $
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

package org.axiondb.types;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.axiondb.AxionException;
import org.axiondb.jdbc.AbstractAxionClob;
import org.axiondb.util.ExceptionConverter;
import org.axiondb.util.Utf8InputStreamConverter;

/**
 * An {@link Clob} wrapping a {@link LobSource}.
 * @version $Revision: 1.5 $ $Date: 2003/05/27 19:08:56 $
 * @author Rodney Waldhoff
 */
public class ClobSource extends AbstractAxionClob {
    public ClobSource(LobSource lob) {
        setLobSource(lob);
    }

    public void setLobSource(LobSource lob) {
        _lob = lob;
    }

    protected InputStream getUtf8Stream() throws AxionException {
        return _lob.getInputStream();
    }

    /**
     * This method is guaranteed to return an ASCII strream.
     * The stream returned will throw an IOException
     * if it encounters non-ASCII characters
     * in the underlying Clob. 
     */
    public InputStream getAsciiStream() throws SQLException {
        try {
            return new Utf8InputStreamConverter(getUtf8Stream(), "US-ASCII");
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        } catch (UnsupportedEncodingException e) {
            throw new SQLException(e.toString());
        }
    }

    public Reader getCharacterStream() throws SQLException {
        try {
            return new BufferedReader(new InputStreamReader(getUtf8Stream(), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            throw new SQLException(e.toString());
        } catch (AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    /**
     * The behaviour of this method is undefined for non-ASCII
     * input.  Make sure to limit input to ASCII only.
     */
    public OutputStream setAsciiStream(long pos) throws SQLException {
        return setUtf8Stream(pos);
    }
    
    public OutputStream setUtf8Stream(long pos) throws SQLException {
        try {
            return _lob.setOutputStream(pos);
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public long length() throws SQLException {
        try {
            return _lob.length();
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public void truncate(long length) throws SQLException {
        try {
            _lob.truncate(length);
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    private LobSource _lob = null;

	public void free() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
