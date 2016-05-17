/*
 * $Id: FileOffsetLobSource.java,v 1.2 2002/12/16 23:34:55 rwald Exp $
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.axiondb.AxionException;

/**
 * A {@link LobSource} that's backed by a single file, storing
 * each LOB as an offset and length.
 * 
 * @version $Revision: 1.2 $ $Date: 2002/12/16 23:34:55 $
 * @author Rodney Waldhoff
 */
public class FileOffsetLobSource implements LobSource {
    
    public FileOffsetLobSource(RandomAccessFile file, long offset, int length) {
        _file = file;
        _offset = offset;
        _length = length;
    }

    public long length() throws AxionException {
        return _length;
    }

    public void truncate(long length) throws AxionException {
        throw new AxionException("Can't truncate FileOffsetLobs");
    }

    public InputStream getInputStream() throws AxionException {
        byte[] buffer = new byte[_length];
        try {
            synchronized(_file) {
                _file.seek(_offset);
                _file.read(buffer);
            }
        } catch(IOException e) {
            throw new AxionException(e);
        }
        return new ByteArrayInputStream(buffer);
    }

    public OutputStream setOutputStream(long pos) throws AxionException {
        throw new AxionException("Can't setOutputStream in FileOffsetLobs");
    }

    private RandomAccessFile _file = null;
    private long _offset = -1L;
    private int _length = -1;

}
