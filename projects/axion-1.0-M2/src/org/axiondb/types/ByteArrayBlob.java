/*
 * $Id: ByteArrayBlob.java,v 1.2 2003/05/14 22:28:40 rwald Exp $
 * =======================================================================
 * Copyright (c) 2003 Axion Development Team.  All rights reserved.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.axiondb.jdbc.AxionBlob;

/**
 * A {@link Blob} based upon a simple byte array.
 * 
 * @version $Revision: 1.2 $ $Date: 2003/05/14 22:28:40 $
 * @author Rodney Waldhoff
 */
public class ByteArrayBlob implements AxionBlob {
    
    public ByteArrayBlob(byte[] value) {
        if(null == value) {
            throw new NullPointerException();
        }
        _value = value;
    }

    public InputStream getBinaryStream() throws SQLException {
        return new ByteArrayInputStream(_value);
    }

    public byte[] getBytes() {
        return _value;
    }

    public byte[] getBytes(long pos, int length) throws SQLException {
        if(pos > length() || pos < 0) {
            throw new SQLException("position " + pos + " is out of bounds.");
        }
        if(length < 0) {
            throw new SQLException("length" + length + " is invalid.");
        }
        if(length + pos > length()) {
            length = (int)(length() - pos);
        }
        byte[] returning = new byte[length];
        System.arraycopy(_value,(int)pos,returning,0,length);
        return returning;
    }

    public long length() throws SQLException {
        return _value.length;
    }

    public long position(Blob pattern, long start) throws SQLException {
        throw new SQLException("Not supported");
    }

    public long position(byte[] pattern, long start) throws SQLException {
        throw new SQLException("Not supported");
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new SQLException("Not supported");
    }

    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        throw new SQLException("Not supported");
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw new SQLException("Not supported");
    }

    public void truncate(long len) throws SQLException {
        throw new SQLException("Not supported");
    }

    private byte[] _value = null;

	public void free() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public InputStream getBinaryStream(long pos, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
