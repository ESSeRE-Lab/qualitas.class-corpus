/*
 * $Id: DiskTable.java,v 1.30 2003/07/08 06:55:39 rwald Exp $
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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.LRUMap;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.ArrayLongList;
import org.apache.commons.collections.primitives.ArrayUnsignedIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.LongList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.Index;
import org.axiondb.IndexLoader;
import org.axiondb.Row;
import org.axiondb.RowIterator;
import org.axiondb.Table;
import org.axiondb.engine.rowiterators.BaseRowIterator;
import org.axiondb.event.RowInsertedEvent;
import org.axiondb.types.FileLobLocator;
import org.axiondb.types.FileOffsetLobLocator;
import org.axiondb.types.FileOffsetLobLocatorFactory;
import org.axiondb.types.LOBType;
import org.axiondb.types.LobLocator;

/**
 * A disk-resident {@link Table}.
 *
 * @version $Revision: 1.30 $ $Date: 2003/07/08 06:55:39 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public final class DiskTable extends BaseDiskTable implements Table {

    //------------------------------------------------------------- Constructors

    public DiskTable(String name, File parentdir) throws AxionException {
        super(name);
        if(_log.isDebugEnabled()) {
            _log.debug("Constructing DiskTable " + name + " in " + parentdir.toString());
        }
        
        _dir = new File(parentdir,name);
        if(!_dir.exists()) {
            _log.debug("Directory \"" + _dir.toString() + "\" doesn't exist, creating it.");
            if(!_dir.mkdirs()) {
                throw new AxionException(
                    "Unable to create directory \"" +
                    _dir.toString() + 
                    "\" for DiskTable \"" + 
                    name +
                    "\".");
            }
        }

        // create the type file if it doesn't already exist
        {
            File typefile = new File(_dir,name + ".type");
            if(!typefile.exists()) {
                writeNameToFile(typefile,new DiskTableFactory());
            }
        }

        _pidx = new ArrayUnsignedIntList();
        _freeIds = new ArrayIntList();

        _lobDir = new File(_dir,"lobs");

        createOrLoadMetaFile();
        createOrLoadPidxFile();
        createOrLoadFreeIdsFile();
        
        initializeRowCount();
        
        // indices - directory containing index files
        {
            _indexDir = new File(_dir,"indices");
            if(_indexDir.exists()) {
                loadIndices(_indexDir);
            } else {
                _indexDir.mkdirs();
            }            
        }
        createOrLoadDataFile();

        createRowCache();
        _log.debug("done loading table");
    }

    //------------------------------------------------------------------ Public

    public synchronized int getNextRowId() {
        _modCount++;
        int id = -1;
        if(_freeIds.isEmpty()) {
            id = _pidx.size();
            _pidx.add(INVALID_OFFSET);
        } else {
            id = _freeIds.removeElementAt(0);
            _pidx.set(id,INVALID_OFFSET);
        }
        return id;
    }

    public synchronized void freeRowId(int id) {
        _modCount++;
        _pidx.set(id,INVALID_OFFSET);
        _freeIds.add(id);
    }

    public int getRowCount() {
        return _rowCount;
    }

    public void applyDeletes(IntIterator iter) throws AxionException {
        if(iter.hasNext()) {
            _modCount++;
            for(int rowid;iter.hasNext();) {
                rowid = iter.next();
                _pidx.set(rowid,INVALID_OFFSET);
                _freeIds.add(rowid);
                uncacheRow(rowid);
                _rowCount--;
            }
            writePidxFile();
            writeFridFile();
        }
    }

    public void applyUpdates(Iterator rows) throws AxionException {
        _modCount++;

        // write all the rows to a buffer, keeping track of the offsets
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        long initoffset = getDataFile().length();        
        LongList offsets = new ArrayLongList();
        {
            DataOutputStream out = new DataOutputStream(buffer);
            while(rows.hasNext()) {
                Row row = (Row)(rows.next());
                long curoffset = initoffset + (long)(buffer.size());
                offsets.add(curoffset);
                _pidx.set(row.getIdentifier(),curoffset);
                for(int i = 0; i < getColumnCount(); i++) {
                    try {
                        getColumn(i).getDataType().write(row.get(i),out);
                    } catch(IOException e) {
                        throw new AxionException("Error buffering column " + i + " data.",e);
                    }
                }
                _pidx.set(row.getIdentifier(),curoffset); // update the slot in the pidx file to point to the new data
                cacheRow(row.getIdentifier(),row); // do we still want this?
            }
            try {
                out.flush();
            } catch(IOException e) {
                throw new AxionException("Error flushing buffer.",e);
            }
        }
        
        // now write out the buffered rows
        RandomAccessFile out = null;
        try {
            out = getWriteFile();
            out.seek(initoffset);
            out.write(buffer.toByteArray());
            appendToPidxFile(offsets);
            writePidxFile();
        } catch(IOException e) {
            throw new AxionException("Error writing buffer.",e);
        } finally {
            try {
                out.getFD().sync();
            } catch(IOException e) {
                // ignored
            } catch(NullPointerException e) {
                // ignored
            }
        }
    }
    
    public void checkpoint() throws AxionException {
        if(_savedAtModCount != _modCount) {
            saveIndices();
            _savedAtModCount = _modCount;
        }
    }

    public void populateIndex(Index index) throws AxionException {
        for(int i=0,I=_pidx.size();i<I;i++) {
            long ptr = _pidx.get(i);
            if(ptr != INVALID_OFFSET) {
                index.rowInserted(
                    new RowInsertedEvent(this,null,getRowByOffset(i,ptr)));
            }
        }
        
        File dataDir = new File(_indexDir,index.getName());
        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }
        File typefile = new File(dataDir,index.getName() + ".type");
        IndexLoader loader = index.getIndexLoader();
        writeNameToFile(typefile,loader);
        index.save(dataDir);
    }

    protected RowIterator getRowIterator() throws AxionException {
        return new BaseRowIterator() {
            Row _current = null;
            int _nextIndex = 0;
            int _currentIndex = -1;
            int _nextId = 0;
            int _currentId = -1;

            public void reset() {
                _current = null;
                _nextIndex = 0;
                _currentIndex = -1;
                _nextId = 0;
            }

            public Row current() {
                if(!hasCurrent()) {
                    throw new NoSuchElementException("No current row.");
                } else {
                    return _current;
                }
            }

            public boolean hasCurrent() {
                return (null != _current);
            }

            public int currentIndex() {
                return _currentIndex;
            }

            public int nextIndex() {
                return _nextIndex;
            }

            public int previousIndex() {
                return _nextIndex - 1;
            }

            public boolean hasNext() {
                return nextIndex() < getRowCount();
            }

            public boolean hasPrevious() {
                return nextIndex() > 0;
            }

            public Row next() throws AxionException {
                if(!hasNext()) {
                    throw new NoSuchElementException("No next row");
                } else {
                    do {
                        _currentId = _nextId++;
                        long offset = _pidx.get(_currentId);
                        if(offset == INVALID_OFFSET) {
                            _current = null;
                        } else {
                            _current = getRowByOffset(_currentId,offset);
                        }
                    } while(null == _current);
                    _currentIndex = _nextIndex;
                    _nextIndex++;
                    return _current;
                }
            }

            public Row previous() throws AxionException {
                if(!hasPrevious()) {
                    throw new NoSuchElementException("No previous row");
                } else {
                    do {
                        _currentId = (--_nextId);
                        long offset = _pidx.get(_currentId);
                        if(offset == INVALID_OFFSET) {
                            _current = null;
                        } else {
                            _current = getRowByOffset(_currentId,offset);
                        }
                    } while(null == _current);
                    _nextIndex--;
                    _currentIndex = _nextIndex;
                    return _current;
                }
            }

            public void remove() throws AxionException {
                if(-1 == _currentIndex) {
                    throw new IllegalStateException("No current row.");
                } else {
                    deleteRow(_current);
                    _nextIndex--;
                    _currentIndex = -1;
                }
            }

            public void set(Row row) throws AxionException {
                if(-1 == _currentIndex) {
                    throw new IllegalStateException("No current row.");
                } else {
                    updateRow(_current,row);
                }
            }
        };
    }

    public Row getRow(int id) throws AxionException {
        Row cached = getCachedRow(id);
        if(null != cached) {
            return cached;
        } else {
            long ptr = _pidx.get(id);
            Row row = getRowByOffset(id,ptr);
            cacheRow(id,row);
            return row;
        }
    }

    public void remount(File newdir, boolean datafilesonly) throws AxionException {
        //TODO: hack - prevent error when remounting while transactions
        //are pending if NEVER_APPLY is true
        if(_savedAtModCount != _modCount && TransactionManagerImpl.NEVER_APPLY == false) {
            throw new AxionException("Can't remount without a checkpoint first.");
        } else {
            closeFiles();
            initFiles(newdir,datafilesonly);
            super.remount(newdir,datafilesonly);
        }
    }

    public void defrag() throws Exception {
        // the new pidx list
        LongList pidx2 = new ArrayUnsignedIntList((_pidx.size()*2)/3);
        // the new .data file
        File df2 = new File(getRootDir(),getName() + ".data.defrag");
        RandomAccessFile data2 = new RandomAccessFile(df2,"rw");
        
        // for each row
        for(int i=0;i<_pidx.size();i++) {
            long offset = _pidx.get(i);
            if(INVALID_OFFSET == offset) {
                // skip the invalid ones
            } else {
                // for valid ones, read the old row
                Row row = getRowByOffset(i,offset);

                // append it to the new pidx list
                long offset2 = data2.length();
                int id2 = pidx2.size();
                pidx2.add(offset2);
                // write it to the new file
                for(int j=0;j<getColumnCount();j++) {
                    getColumn(j).getDataType().write(row.get(j),data2); 
                }
                data2.getFD().sync();
                // and notify the indices that the rowid has changed
                for(Iterator iter = getIndices(); iter.hasNext(); ) {
                    Index index = (Index)(iter.next());
                    index.changeRowId(this,row,i,id2);
                }                              
            }
        }
        // write out the new files        
        data2.close();
        closeFiles();
        saveIndices();
        writeLongFile(_pidxFileName,pidx2);
        _freeIds.clear();
        writeFridFile();
        getDataFile().delete();
        df2.renameTo(getDataFile());        
    }
    
    public void glomLobs() throws Exception {
        FileOffsetLobLocatorFactory factory = new FileOffsetLobLocatorFactory();
        // the glommed .data file
        RandomAccessFile gdata = new RandomAccessFile(new File(getRootDir(),getName() + ".data.glom"),"rw");
        // the glommed .pidx file
        String gpidxfilename = _pidxFileName + ".glom";

        RandomAccessFile[] glom = new RandomAccessFile[getColumnCount()];
        for(int i=0;i<glom.length;i++) {
            Column col = getColumn(i);
            if(col.getDataType() instanceof LOBType) {
                glom[i] = new RandomAccessFile(new File(getLobDir(),col.getName()+".glom"),"rw");
            } else {
                glom[i] = null;
            }
        }

        // FOR EACH ROW
        for(int i=0;i<_pidx.size();i++) {
            long oldoffset = _pidx.get(i);
            if(oldoffset == INVALID_OFFSET) {
                appendLongFile(gpidxfilename,INVALID_OFFSET);
            } else {
                Row row = getRowByOffset(i,oldoffset);
                SimpleRow grow = new SimpleRow(row);
                // FOR EACH COLUMN
                for(int k=0;k<glom.length;k++) {
                    // FOR EACH LOB COLUMN
                    if(null != glom[k]) {
                        // GET THE OLD FILE
                        Column col = getColumn(k);
                        FileLobLocator loc = (FileLobLocator)(col.getDataType().convert(row.get(k)));
                        if(loc != null) {
                            File oldfile = loc.getFile(((LOBType)(col.getDataType())).getLobDir());
                            // WRITE IT TO THE NEW FILE
                            long offset = glom[k].length();
                            int length = 0;
                            InputStream in = new BufferedInputStream(new FileInputStream(oldfile));
                            for(int b=in.read();b!=-1;b=in.read()) {
                                glom[k].write(b);
                                length++;
                            }
                            in.close();
                            glom[k].getFD().sync();

                            // SET THE LOB LOCATOR FOR THAT
                            FileOffsetLobLocator gloc = new FileOffsetLobLocator(offset,length);
                            grow.set(k,gloc);
                        } else {
                            grow.set(k,null);
                        }
                    }
                }

                // SET THE NEW PIDX ENTRY
                appendLongFile(gpidxfilename,gdata.length());

                // WRITE THE ROW TO THE NEW DATA FILE
                for(int j=0;j<getColumnCount();j++) {
                    if(glom[j] != null) {
                        gdata.writeBoolean(true);
                        factory.write((LobLocator)(grow.get(j)),gdata);
                    } else {
                        getColumn(j).getDataType().write(grow.get(j),gdata); 
                    }
                }
            }
        }

        // CLOSE OUT ALL THE GLOMMED FILES
        for(int i=0;i<glom.length;i++) {
            if(glom[i] != null) {
                glom[i].close();
            }
        }

        // CLOSE OUT THE GLOMMED .DATA FILE
        gdata.close();
    }

    //--------------------------------------------------------------- Protected

    public void applyInserts(Iterator rows) throws AxionException {
        _modCount++;

        // write all the rows to a buffer, keeping track of the offsets
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        long initoffset = getDataFile().length();        
        LongList offsets = new ArrayLongList();
        {
            DataOutputStream out = new DataOutputStream(buffer);
            while(rows.hasNext()) {
                Row row = (Row)(rows.next());
                _rowCount++;
                long curoffset = initoffset + (long)(buffer.size());
                offsets.add(curoffset);
                _pidx.set(row.getIdentifier(),curoffset);
                for(int i = 0; i < getColumnCount(); i++) {
                    try {
                        getColumn(i).getDataType().write(row.get(i),out);
                    } catch(IOException e) {
                        throw new AxionException("Error buffering column " + i + " data.",e);
                    }
                }
                cacheRow(row.getIdentifier(),row); // do we still want this?
            }
            try {
                out.flush();
            } catch(IOException e) {
                throw new AxionException("Error flushing buffer.",e);
            }
        }
        
        // now write out the buffered rows
        RandomAccessFile out = null;
        try {
            out = getWriteFile();
            out.seek(initoffset);
            out.write(buffer.toByteArray());
            appendToPidxFile(offsets);
            writeFridFile();
        } catch(IOException e) {
            throw new AxionException("Error writing buffer.",e);
        } finally {
            try {
                out.getFD().sync();
            } catch(IOException e) {
                // ignored
            } catch(NullPointerException e) {
                // ignored
            }
        }
                
    }
    
    protected File getLobDir() {
        return _lobDir;
    }

    protected File getRootDir() {
        return _dir;
    }

    protected void closeFiles() {
        if(null != _readFile) {
            try { 
                _readFile.close(); 
            } catch(IOException e) {
                // ignored
            }
            _readFile = null;
        }
        if(null != _writeFile) {
            try { 
                _writeFile.close(); 
            } catch(IOException e) {
                // ignored
            }
            _writeFile = null;
        }
    }

    //----------------------------------------------------------------- Private

    private void saveIndices() throws AxionException {
        for(Iterator iter = getIndices(); iter.hasNext(); ) {
            Index index = (Index)(iter.next());
            File dataDir = new File(_indexDir,index.getName());
            index.save(dataDir);
        }
    }

    private void createOrLoadPidxFile() throws AxionException {
        File pidxFile = new File(getRootDir(),getName()+ ".pidx");
        try {
            _pidxFileName = pidxFile.getCanonicalPath();
        } catch(IOException e) {
            throw new AxionException(e);
        }
        if(pidxFile.exists()) {
            _log.debug("pidx file \"" + _pidxFileName + "\" already exists, parsing it.");
            _pidx = parseLongFile(pidxFile);
        } else {
            try {
                pidxFile.createNewFile();
            } catch(IOException e) {
                throw new AxionException("Unable to create pidxFile file \"" + _pidxFileName + "\".",e);
            }
        }
    }

    private void createOrLoadFreeIdsFile() throws AxionException {
        _log.debug("createOrLoadFreeIdsFile");
        File freeIdsFile = new File(getRootDir(), getName() + ".frid");
        try {
            _freeIdsFileName = freeIdsFile.getCanonicalPath();
        } catch(IOException e) {
            throw new AxionException(e);
        }
        if(freeIdsFile.exists()) {
            _log.debug("free row ids file \"" + _freeIdsFileName + "\" already exists, parsing it.");
            _freeIds = parseIntFile(freeIdsFile);
        } else {
            try {
                freeIdsFile.createNewFile();
            } catch(IOException e) {
                throw new AxionException("Unable to create freeIdsFile file \"" + _freeIdsFileName + "\".",e);
            }
        }
    }

    private void createOrLoadDataFile() throws AxionException {
        _log.debug("createOrLoadDataFile");
        if(!getDataFile().exists()) {
            _log.debug("data file \"" + getDataFile() + "\" does not exist, creating it");
            try {
                getDataFile().createNewFile();
            } catch(IOException e) {
                throw new AxionException("Unable to create data file \"" + getDataFile() + "\".",e);
            }
        }
    }

    Row getRowByOffset(int idToAssign, long ptr) throws AxionException {
        RandomAccessFile file = getReadFile();
        return getRowByOffset(idToAssign,ptr,file);
    }

    private Row getRowByOffset(int idToAssign, long ptr, RandomAccessFile data) throws AxionException {
        try {
            Row row = new SimpleRow(idToAssign,getColumnCount());
            synchronized(data) {
                data.seek(ptr);
                for(int i=0,I=getColumnCount();i<I;i++) {
                    row.set(i, getColumn(i).getDataType().read(data));
                }
            }
            return row;
        } catch(IOException e) {
            _log.error("IOException in getRowByOffset",e);
            throw new AxionException(e);
        }
    }

    private RandomAccessFile getReadFile() throws AxionException {
        if(null == _readFile) {
            try {
                //_readFile = new BufferedRandomAccessFile(getDataFile(),"r",32);
                _readFile = new RandomAccessFile(getDataFile(),"r");
            } catch(IOException e) {
                throw new AxionException("Exception while opening read file",e);
            }
        }
        return _readFile;
    }

    private RandomAccessFile getWriteFile() throws AxionException {
        if(null == _writeFile) {
            try {
                _writeFile = new RandomAccessFile(getDataFile(),"rw");
            } catch(IOException e) {
                throw new AxionException("Exception while opening write file",e);
            }
        }
        return _writeFile;
    }

    private void initFiles(File basedir, boolean datafilesonly) throws AxionException {
        try {
            if(!datafilesonly) {
                _dir = basedir;
                _indexDir = new File(_dir,"indices");
                _pidxFileName = (new File(_dir,getName() + ".pidx")).getCanonicalPath();
                _freeIdsFileName = (new File(_dir,getName() + ".frid")).getCanonicalPath();
            }
            _lobDir = new File(_dir,"lobs");
            notifyColumnsOfNewLobDir(_lobDir);
            clearDataFileReference(); getDataFile();
            _readFile = null;
            _writeFile = null;
        } catch(IOException e) {
            if(_log.isDebugEnabled()) {
                _log.debug("initFiles(" + basedir + ", " + datafilesonly + ")",e);
            }
            throw new AxionException(e);
        }
    }

    private File getDataFile() {
        if(null == _dataFile) {
            _dataFile = new File(getRootDir(), getName() + ".data");
        }
        return _dataFile;
    }

    private void clearDataFileReference() {
        _dataFile = null;
    }

    private void createRowCache() {
        _rowCache = new LRUMap(100);
    }

    private void cacheRow(int rowid, Row row) {
        cacheRow(new Integer(rowid),row);
    }

    private void cacheRow(Integer rowid, Row row) {
        if(null != _rowCache) {
            _rowCache.put(rowid,row);
        }
    }

    private void uncacheRow(int rowid) {
        if(null != _rowCache) {
            _rowCache.remove(new Integer(rowid));
        }
    }

    private Row getCachedRow(int rowid) {
        if(null != _rowCache) {
            return (Row)(_rowCache.get(new Integer(rowid)));
        } else {
            return null;
        }
    }

    private void initializeRowCount() {
        _rowCount = 0;
        for(int i=0,I=_pidx.size();i<I;i++) {
            long ptr = _pidx.get(i);
            if(ptr != INVALID_OFFSET) {
                _rowCount++;
            }
        }
    }

    final private void writePidxFile() throws AxionException {
        writeLongFile(_pidxFileName,_pidx);
    }
    
    final private void writeFridFile() throws AxionException {
        writeIntFile(_freeIdsFileName,_freeIds);
    }
    
    final private void appendToPidxFile(LongList values) throws AxionException {
        appendLongFile(_pidxFileName, values);
    }
    
    //--------------------------------------------------------------- Attributes

    /** The directory in which my data are stored. */
    private File _dir = null;
    /** The name of my ".pidx" file. */
    private String _pidxFileName = null;
    /** The name of my ".frid" file. */
    private String _freeIdsFileName = null;
    /** The name of my ".data" file. */
    private File _dataFile = null;
    /** The directory in which my LOB data are stored. */
    private File _lobDir = null;
    /** The directory in which my indices are stored. */
    private File _indexDir = null;
    /** List of offsets into the .data file, by row id. */
    LongList _pidx = null;
    /** List of free ids. */
    private IntList _freeIds = null;
    private RandomAccessFile _readFile = null;
    private RandomAccessFile _writeFile = null;
    private int _modCount = 0;
    private int _savedAtModCount = 0;
    private LRUMap _rowCache = null;
    private int _rowCount = 0;
    private static final long INVALID_OFFSET = ArrayUnsignedIntList.MAX_VALUE;
    private static final Log _log = LogFactory.getLog(DiskTable.class);
}
