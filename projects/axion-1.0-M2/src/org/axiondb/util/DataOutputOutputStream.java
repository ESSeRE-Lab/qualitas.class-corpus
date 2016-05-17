/*
 * $Id: DataOutputOutputStream.java,v 1.3 2002/12/16 23:34:55 rwald Exp $
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

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * An {@link OutputStream} wrapping a {@link DataOutput} stream.
 * 
 * @version $Revision: 1.3 $ $Date: 2002/12/16 23:34:55 $
 * @author Rodney Waldhoff
 */
public class DataOutputOutputStream extends OutputStream implements DataOutput {
    /**
     * Create an {@link OutputStream} wrapping the given {@link DataOutput}.
     */
    public DataOutputOutputStream(DataOutput out) {
        _out = out;
    }

    /**
     * Close me and free any associated resources.
     */
    public void close() throws IOException {
        if(_out instanceof RandomAccessFile) {
            ((RandomAccessFile)_out).close();
        }
    }

    /**
     * Write the given byte.
     * @see DataOutput#write(int)
     */
    public void write(int b) throws IOException {
        _out.write(b);
    }

    /**
     * Write the given byte array.
     * @see DataOutput#write(byte[])
     */
    public void write(byte b[]) throws IOException {
        _out.write(b);
    }

    /**
     * Write the specified sub-array of the given byte array.
     * @see DataOutput#write(byte[],int,int)
     */
    public void write(byte b[], int off, int len) throws IOException {
        _out.write(b,off,len);
    }

    /**
     * Flush any buffered bytes.
     */
    public void flush() throws IOException {
    }
    
    /**
     * Write the given value.
     * @see DataOutput#writeBoolean
     */
    public void writeBoolean(boolean v)  throws IOException {
        _out.writeBoolean(v);
    }

    /**
     * Write the given value.
     * @see DataOutput#writeByte
     */
    public void writeByte(int v) throws IOException {
        _out.writeByte(v);
    }
    
    /**
     * Write the given value.
     * @see DataOutput#writeBytes
     */
    public void writeBytes(String s) throws IOException {
        _out.writeBytes(s);
    }
 
    /**
     * Write the given value.
     * @see DataOutput#writeChar
     */
    public void writeChar(int v) throws IOException {
        _out.writeChar(v);
    }
 
    /**
     * Write the given value.
     * @see DataOutput#writeChars
     */
    public void writeChars(String s) throws IOException {
        _out.writeChars(s);
    }

    /**
     * Write the given value.
     * @see DataOutput#writeDouble
     */
    public void writeDouble(double v) throws IOException {
        _out.writeDouble(v);
    }
    
    /**
     * Write the given value.
     * @see DataOutput#writeFloat
     */
    public void writeFloat(float v) throws IOException {
        _out.writeFloat(v);
    }

    /**
     * Write the given value.
     * @see DataOutput#writeInt
     */
    public void writeInt(int v) throws IOException {
        _out.writeInt(v);
    }
 
    /**
     * Write the given value.
     * @see DataOutput#writeLong
     */
    public void writeLong(long v) throws IOException {
        _out.writeLong(v);
    }
 
    /**
     * Write the given value.
     * @see DataOutput#writeShort
     */
    public void writeShort(int v) throws IOException {
        _out.writeShort(v);
    }

    /**
     * Write the given value.
     * @see DataOutput#writeUTF
     */
    public void writeUTF(String str) throws IOException {
        _out.writeUTF(str);
    }

    /**
     * My {@link DataOutput} to delegate to.
     */
    private DataOutput _out = null;
}
