/*
 * $Id: FileLobSource.java,v 1.2 2003/03/27 19:14:04 rwald Exp $
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.axiondb.AxionException;
import org.axiondb.util.DataOutputOutputStream;

/**
 * A {@link LobSource} that's backed by a simple {@link File}.
 *
 * @version $Revision: 1.2 $ $Date: 2003/03/27 19:14:04 $
 * @author Rodney Waldhoff
 */
public class FileLobSource implements LobSource {
    
    public FileLobSource(File file) {
        _file = file;
    }

    public long length() throws AxionException {
        return _file.length();
    }

    public void truncate(long length) throws AxionException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(_file,"rw");
            file.setLength(length);
        } catch(IOException e) {
            throw new AxionException(e);
        } finally {
            try { file.close(); } catch(Exception e) { }
        }
    }

    public InputStream getInputStream() throws AxionException {
        try {
            return new BufferedInputStream(new FileInputStream(getFile()));
        } catch(IOException e) {
            throw new AxionException(e);
        }
    }

    public OutputStream setOutputStream(long pos) throws AxionException {
        RandomAccessFile file = null;
        try {
            if(!getFile().exists()) {
                getFile().createNewFile();
            }
            file = new RandomAccessFile(getFile(),"rw");
            file.seek(pos);
            return new DataOutputOutputStream(file);
        } catch(IOException e) {
            throw new AxionException(e);
        }
    }

    public File getFile() {
        return _file;
    }

    private File _file = null;

}
