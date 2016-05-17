/*
 * $Id: BaseDiskTable.java,v 1.6 2003/05/20 17:59:10 rwald Exp $
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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.ArrayUnsignedIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.LongIterator;
import org.apache.commons.collections.primitives.LongList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.DataType;
import org.axiondb.Index;
import org.axiondb.IndexLoader;
import org.axiondb.Table;
import org.axiondb.types.LOBType;

/**
 * Abstract base disk-resident implementation of {@link Table}.
 * <code>BaseDiskTable</code> manages the column meta-data
 * for a disk-based table.
 *
 * @version $Revision: 1.6 $ $Date: 2003/05/20 17:59:10 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public abstract class BaseDiskTable extends BaseTable implements Table {

    //------------------------------------------------------------- Constructors

    public BaseDiskTable(String name) throws AxionException {
        super(name);
    }

    //---------------------------------------------------------------- Abstract

    abstract protected void closeFiles();
    abstract protected File getLobDir();
    abstract protected File getRootDir();

    //------------------------------------------------------------------ Public

    public void addColumn(Column col) throws AxionException {
        addColumn(col,true);
    }

    public void addColumn(Column col, boolean metaUpdateNeeded) throws AxionException {
        if(col.getDataType() instanceof LOBType) {
            // XXX FIX ME XXX there ought to be a better way to do this
            LOBType lob = (LOBType)(col.getDataType());
            lob.setLobDir(new File(getLobDir(),col.getName()));
        }
        super.addColumn(col);
        if(metaUpdateNeeded) {
            writeMetaFile(getMetaFile());
        }
    }


    public void drop() throws AxionException {
        closeFiles();
        if(!deleteFile(getRootDir())) {
            throw new AxionException("Unable to delete \"" + getRootDir() + "\" during drop table " + getName());
        }
    }

    public void shutdown() throws AxionException {
        checkpoint();
        closeFiles();
    }

    public void remount(File newdir, boolean datafilesonly) throws AxionException {
        if(!datafilesonly) {
            clearMetaFileReference();
        }
        for(int i=0;i<getColumnCount();i++) {
            Column col = getColumn(i);
            if(col.getDataType() instanceof LOBType) {
                LOBType lob = (LOBType)(col.getDataType());
                lob.setLobDir(new File(getLobDir(),col.getName()));
            }
        }
        super.remount(newdir,datafilesonly);
    }

    //--------------------------------------------------------------- Protected

    protected void createOrLoadMetaFile() throws AxionException {
        if(getMetaFile().exists()) {
            _log.debug("meta file \"" + getMetaFile() + "\" already exists, parsing it.");
            parseMetaFile(getMetaFile());
        } else {
            _log.debug("DiskTable meta file \"" + getMetaFile() + "\" does not exist, creating it.");
            try {
                getMetaFile().createNewFile();
            } catch(IOException e) {
                throw new AxionException("Unable to create meta file \"" + getMetaFile() + "\".",e);
            }
        }
    }

    protected void loadIndices(File parentdir) throws AxionException {
        _log.debug("Loading indices from " + parentdir);
        String[] indices = parentdir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file = new File(dir,name);
                if(file.isDirectory()) {
                    File idx = new File(file,name + ".type");
                    if(idx.exists()) {
                        return true;
                    }
                }
                return false;
            }
        });
        for(int i = 0; i < indices.length; i++) {
            _log.debug("Recreating index " + indices[i]);
            File indexdir = new File(parentdir,indices[i]);            
            File typefile = new File(indexdir,indices[i] + ".type");
            String loadername = null;
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(typefile)));
                loadername = in.readUTF();
            } catch(IOException e) {
                throw new AxionException(e);
            } finally {
                try { in.close(); } catch(Exception e) { }
            }
            IndexLoader loader = null;
            try {
                Class clazz = Class.forName(loadername);                                
                loader = (IndexLoader)(clazz.newInstance());
            } catch(Exception e) {
                throw new AxionException(e);
            }
            Index index = loader.loadIndex(this,indexdir);
            _log.debug("Done loading index " + indices[i] + ", adding it.");
            addIndex(index);
        }
    }

    protected void writeNameToFile(File file, Object obj) throws AxionException {
        ObjectOutputStream out = null;
        try {
            String name = obj.getClass().getName();
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeUTF(name);
            out.flush();
        } catch(IOException e) {
            throw new AxionException(e);
        } finally {
            try { out.close(); } catch(Exception e) { }
        }
    }

    protected boolean deleteFile(File file) {
        if(file.exists()) {
            if(file.isDirectory()) {
                File[] files = file.listFiles();
                for(int i=0;i<files.length;i++) {
                    deleteFile(files[i]);
                }
            }
            if(!file.delete()) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    protected void parseMetaFile(File file) throws AxionException {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            // read version number
            int ver = in.readInt();
            if(ver != 0) {
                throw new AxionException("Can't parse meta file " + file + " for table " + getName() + ", unrecognized meta file version " + ver);
            } else { 
                // read number of columns
                int I = in.readInt();
                for(int i=0;i<I;i++) {
                    // read column name
                    String name = in.readUTF();
                    // read data type class name
                    String dtypename = in.readUTF();
                    // create instance of datatype
                    DataType type = null;
                    try {
                        Class clazz = Class.forName(dtypename);
                        type = (DataType)(clazz.newInstance());
                    } catch(ClassNotFoundException e) {
                        throw new AxionException("Can't load table " + getName() + ", data type " + dtypename + " not found.",e);
                    } catch(ClassCastException e) {
                        throw new AxionException("Can't load table " + getName() + ", data type " + dtypename + " not a DataType.",e);
                    } catch(InstantiationException e) {
                        throw new AxionException("Can't load table " + getName() + ", data type " + dtypename + " can't be instantiated.",e);
                    } catch(IllegalAccessException e) {
                        throw new AxionException("Can't load table " + getName() + ", data type " + dtypename + " can't be instantiated.",e);
                    }
                    addColumn(new Column(name,type),false);
                }
            }
        } catch(IOException e) {
            throw new AxionException("Unable to parse meta file " + file + " for table " + getName(),e);
        } finally {
            try { in.close(); } catch(Exception e) { }
        }
    }
  
    protected void writeMetaFile(File file) throws AxionException {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            // write version number
            out.writeInt(0);
            // write number of columns
            out.writeInt(getColumnCount());
            // for each column
            for(int i=0,I=getColumnCount();i<I;i++) {
                Column col = getColumn(i);                
                // write column name
                out.writeUTF(col.getName());
                // write data type class name
                out.writeUTF(col.getDataType().getClass().getName());
            }
            out.flush();
        } catch(IOException e) {
            throw new AxionException("Unable to write meta file " + file + " for table " + getName(),e);
        } finally {
            try { out.close(); } catch(Exception e) { }
        }
    }

    /**
     * Writes a list of <tt>int</tt> values to a file.
     * 
     * @param file the {@link File} to write to
     */
    protected void writeIntFile(String file, IntList list) throws AxionException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            for(int i=0;i<list.size();i++) {
                out.writeInt(list.get(i));
            }
            out.flush();
        } catch(IOException e) {
            throw new AxionException("Unable to write to " + file,e);
        } finally {
            try { out.close(); } catch(Exception t) { }
        }
    }
    /**
     * Writes a list of <tt>long</tt> values to a file.
     * 
     * @param file the {@link File} to write to
     */
    protected void writeLongFile(String file, LongList list) throws AxionException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            for(int i=0;i<list.size();i++) {
                out.writeLong(list.get(i));
            }
            out.flush();
        } catch(IOException e) {
            throw new AxionException("Unable to write to " + file,e);
        } finally {
            try { out.close(); } catch(Exception t) { }
        }
    }

    /**
     * Appends a long value to a file.
     * 
     * @param file the {@link File} to append to
     * @param value the value to write
     */
    protected void appendLongFile(String file, long value) throws AxionException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
            out.writeLong(value);
        } catch(IOException e) {
            throw new AxionException("Unable to write to " + file,e);
        } finally {
            try { out.close(); } catch(Exception t) { }
        }
    }


    /**
     * Appends several long values to a file.
     * 
     * @param file the {@link File} to append to
     * @param values the values to write
     */
    protected void appendLongFile(String file, LongList value) throws AxionException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
            for(LongIterator iter = value.iterator();iter.hasNext();) {
                out.writeLong(iter.next());
            }
        } catch(IOException e) {
            throw new AxionException("Unable to write to " + file,e);
        } finally {
            try { out.close(); } catch(Exception t) { }
        }
    }

    /**
     * Reads a list of long values from a file.
     * 
     * @param file the {@link File} to read from
     */
    protected LongList parseLongFile(File file) throws AxionException {
        int count = (int)(file.length()/8L);
        LongList list = new ArrayUnsignedIntList(count);
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file),8192));
            for(int i=0;i<count;i++) {
                list.add(in.readLong());
            }
            return list;
        } catch(IOException e) {
            throw new AxionException("Unable to parse " + file,e);
        } finally {
            try {
                in.close();
            } catch(Exception t) {
            }
        }
    }

    /**
     * Reads a list of int values from a file.
     * 
     * @param file the {@link File} to read from
     */
    protected IntList parseIntFile(File file) throws AxionException {
        int count = (int)(file.length()/4L);
        IntList list = new ArrayIntList(count);
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            for(int i=0;i<count;i++) {
                list.add(in.readInt());
            }
            return list;
        } catch(IOException e) {
            throw new AxionException("Unable to parse " + file,e);
        } finally {
            try {
                in.close();
            } catch(Exception t) {
            }
        }
    }

    //----------------------------------------------------------------- Private

    private File getMetaFile() {
        if(null == _metaFile) {
            _metaFile = new File(getRootDir(),getName() + ".meta");
        } 
        return _metaFile;
    }

    private void clearMetaFileReference() {
        _metaFile = null;
    }


    //--------------------------------------------------------------- Attributes

    private File _metaFile = null;
    private static Log _log = LogFactory.getLog(BaseDiskTable.class);
}
