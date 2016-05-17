/*
 * $Id: LOBType.java,v 1.7 2003/05/14 19:07:30 rwald Exp $
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link DataType} representing a Large Object (LOB), for 
 * example a {@link Clob} or {@link Blob}.
 * 
 * @version $Revision: 1.7 $ $Date: 2003/05/14 19:07:30 $
 * @author James Burke
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class LOBType extends BaseDataType {
    
    public LOBType() {
    }

    public int getJdbcType() {
        return java.sql.Types.BLOB;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public boolean accepts(Object value) {
        return (
            null == value || 
            value instanceof String || 
            value instanceof byte[] || 
            value instanceof Clob || 
            value instanceof Blob || 
            value instanceof LobLocator);
    }

    public Object convert(Object value) throws IllegalArgumentException {
        if("newlob()".equals(value)) {
            LobLocator loc = _locatorFactory.makeLobLocator(getLobDir());
            return loc;
        } else if(null == value) {
            return null;
        } else if(value instanceof LobLocator) {
            return value;
        } else if(value instanceof String) {
            return new StringClob((String)value);
        } else if(value instanceof byte[]) {
            return new ByteArrayBlob((byte[])value);
        } else if(value instanceof Clob) {
            return value;
        } else if(value instanceof Blob) {
            return value;
        } else {
            throw new IllegalArgumentException("Can't convert " + value.getClass().getName() + " " + value + ".");
        }
    }

    public Blob toBlob(Object value) throws SQLException {
        Object obj = convert(value);
        if(obj instanceof Blob) {
            return (Blob)obj;
        } else if(obj instanceof LobLocator) {
            try {
                return makeBlobSource((LobLocator)obj);
            } catch(AxionException e) {
                throw ExceptionConverter.convert(e);
            }
        } else if(null == obj) {
            return null;
        } else {
            throw new SQLException("Expected Blob or LobLocator, found " + obj);
        }
    }

    public Clob toClob(Object value) throws SQLException {       
        Object obj = convert(value);
        if(obj instanceof Clob) {
            return (Clob)obj;
        } else if(obj instanceof LobLocator) {
            try {
                return makeClobSource((LobLocator)obj);
            } catch(AxionException e) {
                throw ExceptionConverter.convert(e);
            }
        } else if(null == obj) {
            return null;
        } else {
            throw new SQLException("Expected Clob or LobLocator, found " + obj);
        }
    }

    public String toString(Object value) throws SQLException {
        Clob clob = toClob(value);
        if(null == clob) {
            return null;
        } else {
            StringBuffer buf = new StringBuffer();
            Reader in = clob.getCharacterStream();
            try {
                for(int c = in.read(); c != -1; c = in.read()) {
                    buf.append((char)c);
                }
                return buf.toString(); 
            } catch(IOException e) {
                throw ExceptionConverter.convert(e);
            }
        }
    }

    public String toString() {
        return "lob";
    }

    public DataType makeNewInstance() {
        return new LOBType();
    }

    public Object read(DataInput in) throws IOException {
        if(in.readBoolean()) {
            return _locatorFactory.read(in);
        } else {
            return null;
        }
    }

    // @TODO: clean up the exception handling/conversion here
    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeBoolean(false);
        } else if(value instanceof LobLocator) {
            out.writeBoolean(true);
            _locatorFactory.write(((LobLocator)value),out);
        } else if(value instanceof StringClob) {
            LobLocator loc = writeStringClob((StringClob)value);
            out.writeBoolean(true);
            _locatorFactory.write(((LobLocator)loc),out);
        } else if(value instanceof ByteArrayBlob) {
            LobLocator loc = writeByteArrayBlob((ByteArrayBlob)value);
            out.writeBoolean(true);
            _locatorFactory.write(((LobLocator)loc),out);
        } else if(value instanceof StringClob) {
            LobLocator loc = writeStringClob((StringClob)value);
            out.writeBoolean(true);
            _locatorFactory.write(((LobLocator)loc),out);
        } else {
            throw new IllegalArgumentException(value.getClass().getName() + ":" + value);
        }
    }

    public File getLobDir() {
        return _lobDir;
    }

    public void setLobDir(File lobDir) {
        closeLobFile();
        _lobDir = lobDir;
        if(!_lobDir.exists() || _lobDir.isDirectory()) {
            _locatorFactory = new FileLobLocatorFactory();
        } else {            
            openLobFile();
            _locatorFactory = new FileOffsetLobLocatorFactory();
        }
    }

    public short getSearchableCode() {
        return DatabaseMetaData.typePredNone;
    }

    protected RandomAccessFile getLobFile() {
        return _raFile;
    }

    protected LobLocator writeStringClob(StringClob value) throws IOException {
        LobLocator loc = _locatorFactory.makeLobLocator(getLobDir());
        Writer clobout = null;
        try {
            ClobSource clob = makeClobSource(loc);
            clobout = clob.setCharacterStream(0);
            clobout.write(value.getString());
        } catch(SQLException e) {
            throw ExceptionConverter.convertToIOException(e);                
        } catch(AxionException e) {
            throw ExceptionConverter.convertToIOException(e);                
        } finally {
            try { clobout.close(); } catch(Exception e) { }
        }
        return loc;
    }

    protected LobLocator writeByteArrayBlob(ByteArrayBlob value) throws IOException {
        LobLocator loc = _locatorFactory.makeLobLocator(getLobDir());
        OutputStream blobout = null;
        try {
            BlobSource blob = makeBlobSource(loc);
            blobout = blob.setBinaryStream(0);
            blobout.write(value.getBytes());
        } catch(SQLException e) {
            throw ExceptionConverter.convertToIOException(e);                
        } catch(AxionException e) {
            throw ExceptionConverter.convertToIOException(e);                
        } finally {
            try { blobout.close(); } catch(Exception e) { }
        }
        return loc;
    }
/*
    // TODO: this may support transactional updates of the "select for update" style of clob writing
    private StringClob readStringClob(LobLocator loc) throws IOException, AxionException, SQLException {
        ClobSource src = makeClobSource(loc);
        StringBuffer buf = new StringBuffer();
        Reader in = src.getCharacterStream();
        for(int c = in.read(); c != -1; c = in.read()) {
            buf.append((char)c);
        }
        in.close();
        return new StringClob(buf.toString());
    }
*/
    protected BlobSource makeBlobSource(LobLocator loc) throws AxionException {
        return new BlobSource(loc.getLobSource(getLobDir(),getLobFile()));
    }

    protected ClobSource makeClobSource(LobLocator loc) throws AxionException {
        return new ClobSource(loc.getLobSource(getLobDir(),getLobFile()));
    }

    private void closeLobFile() {
        if(null != _raFile) {
            try {
                _raFile.close();
            } catch(IOException e) {
                // ignored
            }
            _raFile = null;
        }
    }

    private void openLobFile() {
        try {
            //_raFile = new BufferedRandomAccessFile(_lobDir,"r");
            _raFile = new RandomAccessFile(_lobDir,"r");
        } catch(FileNotFoundException e) {
            _raFile = null;
        }
    }

    private File _lobDir = null;
    private RandomAccessFile _raFile = null;
    private LobLocatorFactory _locatorFactory = new FileLobLocatorFactory();
}

