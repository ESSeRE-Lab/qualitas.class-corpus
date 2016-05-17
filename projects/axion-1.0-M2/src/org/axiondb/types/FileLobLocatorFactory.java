/*
 * $Id: FileLobLocatorFactory.java,v 1.3 2003/05/13 23:42:58 rwald Exp $
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.axiondb.util.ExceptionConverter;


/**
 * A {@link LobLocatorFactory} for {@link FileLobLocator}s.
 * 
 * @version $Revision: 1.3 $ $Date: 2003/05/13 23:42:58 $
 * @author Rodney Waldhoff
 */
public class FileLobLocatorFactory implements LobLocatorFactory {
    public void write(LobLocator locator, DataOutput out) throws IOException {
        ((FileLobLocator)locator).write(out);
    }

    public LobLocator read(DataInput in) throws IOException {
        return new FileLobLocator(in.readUTF(),in.readUTF());
    }

    public LobLocator makeLobLocator(File parentdir) {
        try {
            File dir = getNextLobDir(parentdir);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            File file = File.createTempFile(_LOB_PREFIX,_LOB_SUFFIX,dir);
            return new FileLobLocator(file);
        } catch(IOException e) {
            throw ExceptionConverter.convertToRuntimeException(e);
        }
    }

    private File getNextLobDir(File parentdir) {
        return new File(parentdir,String.valueOf(Math.abs(_counter++%1000)));
    }

    private int _counter = (new Random()).nextInt();
    private static final String _LOB_PREFIX = "LOB";
    private static final String _LOB_SUFFIX = ".lob";

}
