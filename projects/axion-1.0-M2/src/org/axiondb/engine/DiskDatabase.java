/*
 * $Id: DiskDatabase.java,v 1.10 2003/05/09 21:49:41 rwald Exp $
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

package org.axiondb.engine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.Sequence;
import org.axiondb.Table;
import org.axiondb.TableFactory;

/**
 * A disk-resident {@link org.axiondb.Database}.
 * 
 * @version $Revision: 1.10 $ $Date: 2003/05/09 21:49:41 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 * @author Morgan Delagrange
 */
public class DiskDatabase extends BaseDatabase implements Database {

    //------------------------------------------------------------- Constructors

    public DiskDatabase(File dbDir) throws AxionException {
        this(dbDir.getName(), dbDir);
    }

    public DiskDatabase(String name, File dbDir) throws AxionException {
        this(name, dbDir, null);
    }

    public DiskDatabase(String name, File dbDir, Properties props) throws AxionException {
        super(name);
        if(null == dbDir) {
            throw new AxionException("Database directory required.");
        } else {
            _tableDirectory = dbDir;
            _log.debug("Constructing disk-based database in " + dbDir);
            if(!dbDir.exists()) { dbDir.mkdirs(); }
            if(!dbDir.exists() || !dbDir.isDirectory()) {
                throw new AxionException("Database directory \"" + dbDir + "\" could not be created or is not a directory.");
            } else {
                createMetaDataTables();
                if(null == props) {
                    if(null != getBaseProperties()) {
                        props = new Properties(getBaseProperties());
                    } else {
                        props = new Properties();
                    }
                    File propfile = new File(dbDir,"axiondb.properties");
                    if(propfile.exists()) {
                        _log.debug("Loading properties from \"" + propfile + "\".");
                        InputStream in = null;
                        try {
                            in = new FileInputStream(propfile);
                            props.load(in);
                        } catch(Exception e) {
                            _log.error("Exception while loading properties from \"" + propfile + "\".",e); // PROPOGATE UP!?!
                        } finally {
                            try { in.close(); } catch(Exception e) { }
                        }
                    }
                }
                loadProperties(props);
            
                loadTables(_tableDirectory);
                loadSequences();
                _log.debug("Disk-based database construction successful");
            }
        }
    }

    public File getTableDirectory() {
        return _tableDirectory;
    }

    public void remount(File newdir) throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug("Remounting from " + _tableDirectory + " to " + newdir);
        }
        _tableDirectory = newdir;
        super.remount(newdir);
    }

    public TableFactory getTableFactory(String name) {
        if(null == name || "default".equals(name)) {
            return DEFAULT_TABLE_FACTORY;
        } else {
            return super.getTableFactory(name);
        }
    }

    public void defrag() throws Exception {
        checkpoint();
        for(Iterator tables = getTables(); tables.hasNext();) {
            Table table = (Table)(tables.next());
            if(table instanceof DiskTable) {
                ((DiskTable)table).defrag();
            }
        }
    }

    protected Table createSystemTable(String name) {
        MemoryTable t = new MemoryTable(name);
        t.setType(Table.SYSTEM_TABLE_TYPE);
        return t;
    }

    private void loadTables(File parentdir) throws AxionException {
        String[] tables = parentdir.list(new FilenameFilter() {
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
        //TableLoader[] loaders = new TableLoader[tables.length];
        for(int i=0;i<tables.length;i++) {
            _log.debug("Recreating table " + tables[i]);
            File tabledir = new File(parentdir,tables[i]);            
            File typefile = new File(tabledir,tables[i] + ".type");
            String factoryname = null;
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(typefile)));
                factoryname = in.readUTF();
            } catch(IOException e) {
                throw new AxionException(e);
            } finally {
                try { in.close(); } catch(Exception e) { }
            }
            TableFactory factory = null;
            try {
                Class clazz = Class.forName(factoryname);
                factory = (TableFactory)(clazz.newInstance());
            } catch(Exception e) {
                throw new AxionException(e);
            }
            Table table = factory.createTable(this,tabledir.getName());
            addTable(table);
        }
    }

    private void loadSequences() throws AxionException {
        File seqFile = new File(_tableDirectory, getName() + ".seq");
        if (seqFile.exists()) {
            FileInputStream fin = null;
            DataInputStream in = null;
            try {
                fin = new FileInputStream(seqFile);
                in = new DataInputStream(fin);
                
                int size = in.readInt();
                for (int i = 0; i < size; i++) {
                    String name = in.readUTF();
                    int value = in.readInt();
                    Sequence seq = new Sequence(name, value);
                    super.createSequence(seq);
                }
            } catch (IOException e) {
                String msg = "Unable to read sequence file";
                _log.error(msg, e);
                throw new AxionException(msg);
            } finally {
                if (in!=null) { try { in.close(); } catch (Exception e) {}}
                if (fin!=null) { try { fin.close(); } catch (Exception e) {}}
            }
        }
    }

    public void checkpoint() throws AxionException {
        super.checkpoint();
        if (getSequenceCount() != 0) {
            File seqFile = new File(_tableDirectory, getName() + ".seq");
            FileOutputStream fout = null;
            DataOutputStream out = null;
            try {
                fout = new FileOutputStream(seqFile);
                out = new DataOutputStream(fout);
                out.writeInt(getSequenceCount());

                for(Iterator i = getSequences(); i.hasNext(); ) {
                    Sequence cur = (Sequence)(i.next());
                    out.writeUTF(cur.getName());
                    out.writeInt(cur.getDataType().toInt(cur.getValue()));
                }
                out.flush();
                fout.flush();
            } catch (IOException e) {
                String msg = "Unable to persist sequence file";
                _log.error(msg, e);
                throw new AxionException(msg);
            } catch (SQLException e) {
                String msg = "Unable to convert type";
                _log.error(msg, e);
                throw new AxionException(msg);
            } finally {
                if (out!=null) { try { out.close(); } catch (Exception e) {}}
                if (fout!=null) { try { fout.close(); } catch (Exception e) {}}
            }
        }
    }

    public void createSequence(Sequence seq) throws AxionException {
        super.createSequence(seq);
        checkpoint();
    }

    //-------------------------------------------------------------- Attributes

    private static final TableFactory DEFAULT_TABLE_FACTORY = new DiskTableFactory();
    private static Log _log = LogFactory.getLog(DiskDatabase.class);
    private File _tableDirectory = null;

    //----------------------------------------------------------- Inner Classes
}
