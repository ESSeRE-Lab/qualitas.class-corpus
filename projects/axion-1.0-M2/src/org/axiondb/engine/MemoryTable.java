/*
 * $Id: MemoryTable.java,v 1.23 2003/07/08 06:55:39 rwald Exp $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.axiondb.AxionException;
import org.axiondb.Index;
import org.axiondb.Row;
import org.axiondb.RowIterator;
import org.axiondb.Table;
import org.axiondb.engine.rowiterators.BaseRowIterator;
import org.axiondb.event.RowInsertedEvent;

/**
 * A memory-resident {@link Table}.
 *
 * @version $Revision: 1.23 $ $Date: 2003/07/08 06:55:39 $
 * @author Chuck Burdick
 */
public class MemoryTable extends BaseTable implements Table {
    public MemoryTable(String name) {
        super(name);
    }

    public void populateIndex(Index index) throws AxionException {
        for (int i = 0,I = _rows.size(); i < I; i++) {
            Row row = (Row) (_rows.get(i));
            if (row != null) {
                index.rowInserted(new RowInsertedEvent(this, null, row));
            }
        }
    }

    public synchronized int getNextRowId() {
        int id = -1;
        if (_freeIds.isEmpty()) {
            id = _rows.size();
            _rows.add(null);
        } else {
            id = _freeIds.removeElementAt(0);
            _rows.set(id, null);
        }
        return id;
    }

    public synchronized void freeRowId(int id) {
        _rows.set(id, null);
        _freeIds.add(id);
    }

    public int getRowCount() {
        return _rowCount;
    }

    public Row getRow(int id) throws AxionException {
        return (Row) (_rows.get(id));
    }

    public void applyInserts(Iterator rows) throws AxionException {        
        while(rows.hasNext()) {
            applyInsert((Row)rows.next());
        }
    }

    public void applyDeletes(IntIterator iter) throws AxionException {
        while(iter.hasNext()) {
            applyDelete(iter.next());
        }
    }
    
    public void applyUpdates(Iterator rows) {
        for(Row row; rows.hasNext();) {
            row = (Row)rows.next();
            _rows.set(row.getIdentifier(), row);
        }
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
                if (!hasCurrent()) {
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

            public Row next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No next row");
                } else {
                    do {
                        _currentId = _nextId++;
                        _current = (Row) (getRowList().get(_currentId));
                    } while (null == _current);
                    _currentIndex = _nextIndex;
                    _nextIndex++;
                    return _current;
                }
            }

            public Row previous() {
                if (!hasPrevious()) {
                    throw new NoSuchElementException("No previous row");
                } else {
                    do {
                        _currentId = (--_nextId);
                        _current = (Row) (getRowList().get(_currentId));
                    } while (null == _current);
                    _nextIndex--;
                    _currentIndex = _nextIndex;
                    return _current;
                }
            }

            public void remove() throws AxionException {
                if (-1 == _currentIndex) {
                    throw new IllegalStateException("No current row.");
                } else {
                    deleteRow(_current);
                    _nextIndex--;
                    _currentIndex = -1;
                }
            }

            public void set(Row row) throws AxionException {
                if (-1 == _currentIndex) {
                    throw new IllegalStateException("No current row.");
                } else {
                    updateRow(_current, row);
                }
            }
        };
    }

    private void applyInsert(Row row) {
        _rows.set(row.getIdentifier(), row);
        _rowCount++;
    }

    private void applyDelete(int rowid) {
        _freeIds.add(rowid);
        _rows.set(rowid, null);
        _rowCount--;
    }

    final List getRowList() { return _rows; }

    private List _rows = new ArrayList();
    private ArrayIntList _freeIds = new ArrayIntList();
    private int _rowCount = 0;
}

