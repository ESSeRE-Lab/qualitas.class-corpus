/*
 * $Id: BlobSource.java,v 1.4 2003/05/14 15:38:12 rwald Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import org.axiondb.AxionException;
import org.axiondb.jdbc.AbstractAxionBlob;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link Blob} wrapping a {@link LobSource}.
 * 
 * @version $Revision: 1.4 $ $Date: 2003/05/14 15:38:12 $
 * @author Rodney Waldhoff
 */
public class BlobSource extends AbstractAxionBlob {

    public BlobSource(LobSource lob) {
        setLobSource(lob);
    }

    public void setLobSource(LobSource lob) {
        _lob = lob;
    }

    public long length() throws SQLException {
        try {
            return _lob.length();
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public byte[] getBytes(long pos, int length) throws SQLException {
        if(pos < 0) {
            throw new SQLException("position " + pos + " is invalid.");
        }
        if(length < 0) {
            throw new SQLException("length" + length + " is invalid.");
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream in = null;
        try {
            in = getBinaryStream();
            // skip pos bytes
            for(long i=0;i<pos;i++) {
                in.read();
            }
            // copy length bytes
            for(int i=0,b=in.read();i<length && b != -1;i++,b=in.read()) {
                buffer.write((byte)b);
            }
            return buffer.toByteArray();
        } catch(IOException e) {
            throw ExceptionConverter.convert(e);
        } finally {
            try { in.close(); } catch(Exception e) { }
        }
    }

    public void truncate(long length) throws SQLException {
        try {
            _lob.truncate(length);
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public InputStream getBinaryStream() throws SQLException {
        try {
            return _lob.getInputStream();
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        try {
            return _lob.setOutputStream(pos);
        } catch(AxionException e) {
            throw ExceptionConverter.convert(e);
        }
    }

    private LobSource _lob = null;
}
