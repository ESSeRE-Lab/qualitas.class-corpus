/*
 * $Id: BufferedRandomAccessFile.java,v 1.3 2002/12/16 23:34:55 rwald Exp $
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

package org.axiondb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A buffered extension of {@link RandomAccessFile}.
 * 
 * (A little buggy, so currently unused.)
 * 
 * @version $Revision: 1.3 $ $Date: 2002/12/16 23:34:55 $
 * @author Rodney Waldhoff
 */
public class BufferedRandomAccessFile extends RandomAccessFile {
    /**
     * @see RandomAccessFile#RandomAccessFile(java.io.File,java.lang.String)
     */
    public BufferedRandomAccessFile(File file, String mode) throws FileNotFoundException {
        this(file,mode,2048);
    }

    /**
     * @param size buffer size in bytes
     * @see RandomAccessFile#RandomAccessFile(java.io.File,java.lang.String)
     */
    public BufferedRandomAccessFile(File file, String mode, int size) throws FileNotFoundException {
        super(file,mode);
        _buffer = new byte[size];
    }

    /**
     * @see RandomAccessFile#RandomAccessFile(java.lang.String,java.lang.String)
     */
    public BufferedRandomAccessFile(String filename, String mode) throws FileNotFoundException {
        this(filename,mode,2048);
    }

    /**
     * @param size buffer size in bytes
     * @see RandomAccessFile#RandomAccessFile(java.lang.String,java.lang.String)
     */
    public BufferedRandomAccessFile(String filename, String mode, int size) throws FileNotFoundException {
        super(filename,mode);
        _buffer = new byte[size];
    }

    /**
     * @see RandomAccessFile#read
     */
    public int read() throws IOException {
        if(_nextByte >= _maxBufferedByte) {
            fillBuffer();
        }
        if(_nextByte >= _maxBufferedByte) {
            return -1;
        } else {
            return (((int)_buffer[_nextByte++])&0xFF);
        }
    }

    /**
     * @see RandomAccessFile#read(byte[],int,int)
     */
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int bytesread = 0;
        if(_maxBufferedByte == -1) {
            return -1;
        } else {
            if(length >= _buffer.length) {
                clearBuffer();
                return super.read(bytes,offset,length);
            } else {            
                if(length == _maxBufferedByte - _nextByte) {
                    System.arraycopy(_buffer,_nextByte,bytes,offset,(_maxBufferedByte - _nextByte));
                    bytesread = _maxBufferedByte - _nextByte;
                    clearBuffer();
                    return bytesread;
                } else if(length < _maxBufferedByte - _nextByte) {
                    System.arraycopy(_buffer,_nextByte,bytes,offset,length);
                    _nextByte += length;
                    return length;
                } else { // if(length > _maxBufferedByte - _nextByte)
                    while(length > _maxBufferedByte - _nextByte) {
                        System.arraycopy(_buffer,_nextByte,bytes,offset,(_maxBufferedByte - _nextByte));
                        length -= (_maxBufferedByte - _nextByte);
                        bytesread += (_maxBufferedByte - _nextByte);
                        fillBuffer();
                        if(_nextByte >= _maxBufferedByte) {
                            return bytesread;
                        }
                    }
                    return bytesread;
                }
            }
        }
    }

    /**
     * @see RandomAccessFile#seek(long)
     */
    public void seek(long pos) throws IOException {
        /*
        if(pos < (_firstBufferPosition + (long)_maxBufferedByte)) {
            _nextByte = (int)(pos - _firstBufferPosition); 
            if(_nextByte < 0) { clearBuffer(); }
        } else {
            clearBuffer();
        }
        */
        clearBuffer();
        super.seek(pos);
    }
    
    /**
     * Fill my buffer with bytes from the underlying file.
     */
    private void fillBuffer() throws IOException {
        _firstBufferPosition = super.getFilePointer();
        _maxBufferedByte = super.read(_buffer);
        _nextByte = 0;
    }

    /**
     * Clear my buffer.
     */
    private void clearBuffer() {
        _maxBufferedByte = 0;
        _nextByte = 0;
        _firstBufferPosition = Long.MIN_VALUE;
    }

    private byte[] _buffer = null;  
    private int _nextByte = 0;
    private int _maxBufferedByte = 0;
    private long _firstBufferPosition = Long.MIN_VALUE;
}

