/*
 * $Id: ObjectBTreeIndexLoader.java,v 1.2 2002/12/16 22:18:30 rwald Exp $
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

package org.axiondb.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.axiondb.AxionException;
import org.axiondb.Index;
import org.axiondb.IndexLoader;
import org.axiondb.Table;

/** 
 * An {@link IndexLoader} for {@link ObjectBTreeIndex}.
 *
 * @version $Revision: 1.2 $ $Date: 2002/12/16 22:18:30 $
 * @author Dave Pekarek Krohn
 */
public class ObjectBTreeIndexLoader implements IndexLoader {
    public ObjectBTreeIndexLoader() {
    }

    public Index loadIndex(Table table, File dataDirectory) throws AxionException {
        ObjectInputStream in = null;
        try {
            String name = dataDirectory.getName();
            File file = new File(dataDirectory,name + ".data");
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file),8192));
            // read version number
            int ver = in.readInt();
            if(ver != 1) {
                throw new AxionException("Can't parse data file " + file + " for index " + name + ", unrecognized data file version " + ver);
            } else { 
                // read column name
                String col = in.readUTF();
                // read unique flag
                boolean unique = in.readBoolean();
                // create index

                Index index = new ObjectBTreeIndex(name, table.getColumn(col), unique, dataDirectory);
                // return it
                return index;
            }
        } catch(IOException e) {
            throw new AxionException(e);
        } finally {
            try { in.close(); } catch(Exception e) { }
        }
    }

    public void saveIndex(Index ndx, File dataDirectory) throws AxionException {
        ObjectBTreeIndex index = (ObjectBTreeIndex)ndx;
        ObjectOutputStream out = null;
        try {
            String name = index.getName();
            File file = new File(dataDirectory,name + ".data");
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            
            // write version number
            out.writeInt(1);
            
            // write column name
            out.writeUTF(index.getIndexedColumn().getName());
            
            // write unique flag
            out.writeBoolean(index.isUnique());

            // flush
            out.flush();

            index.getBTree().save(dataDirectory);
        
        } catch(IOException e) {
            throw new AxionException(e);
        } catch(ClassNotFoundException e) {
            throw new AxionException(e);
        } finally {
            try { out.close(); } catch(Exception e) { }
        }
    }

}
