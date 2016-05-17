/*
 * $Id: ObjectBTreeIndex.java,v 1.13 2003/07/11 17:04:06 rwald Exp $
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

import java.io.File;
import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntCollections;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.ComparisonOperator;
import org.axiondb.IndexLoader;
import org.axiondb.Row;
import org.axiondb.RowIterator;
import org.axiondb.RowSource;
import org.axiondb.Table;
import org.axiondb.engine.rowiterators.EmptyRowIterator;
import org.axiondb.engine.rowiterators.LazyRowRowIterator;
import org.axiondb.event.RowEvent;
import org.axiondb.event.TableModificationListener;
import org.axiondb.util.ObjectBTree;

/**
 * A {@link BaseBTreeIndex B-Tree index} over <code>Object</code> keys.
 * 
 * @version $Revision: 1.13 $ $Date: 2003/07/11 17:04:06 $
 * @author Dave Pekarek Krohn
 */
public class ObjectBTreeIndex extends BaseBTreeIndex implements TableModificationListener {
    private static final Log _log = LogFactory.getLog(ObjectBTreeIndex.class);
    private ObjectBTree _tree = null;

    public ObjectBTreeIndex(String name, Column column, boolean unique, File dataDirectory) throws AxionException {
        super(name, column, unique);
        try {
            _tree = new ObjectBTree(dataDirectory, name, 1000, column.getDataType().getComparator());
        } catch (IOException e) {
            String msg = "Unable to create index file";
            _log.error(msg, e);
            throw new AxionException(msg,e);
        } catch (ClassNotFoundException e) {
            String msg = "Unable to create index file";
            _log.error(msg, e);
            throw new AxionException(msg,e);
        }
    }

    public ObjectBTreeIndex(String name, Column column, boolean unique) throws AxionException {
        this(name, column, unique, null);
    }

    public RowIterator getRowIterator(RowSource source, ComparisonOperator theOperator, Object value) throws AxionException {        
        IntListIterator resultIds = null;
        Object convertedValue = getIndexedColumn().getDataType().convert(value);
        
        if(null == convertedValue) {
            // null fails all comparisions I support
            return EmptyRowIterator.INSTANCE;
        }

        try {
            if(ComparisonOperator.EQUAL.equals(theOperator)) {
                if (!isUnique()) {
                    resultIds = _tree.getAll(convertedValue);
                } else {
                    Integer result = _tree.get(convertedValue);
                    if (result == null) {
                        resultIds = IntCollections.getEmptyIntListIterator();
                    } else {
                        // TODO: use SingletonIntListIterator instead?
                        IntList list = new ArrayIntList(1);
                        list.add(result.intValue());
                        resultIds = list.listIterator();
                    }
                }
            } else if (ComparisonOperator.LESS_THAN.equals(theOperator)) {
                resultIds = _tree.getAllTo(convertedValue);
            } else if (ComparisonOperator.LESS_THAN_OR_EQUAL.equals(theOperator)) {
                resultIds = _tree.getAllTo(getIndexedColumn().getDataType().successor(convertedValue));
            } else if (ComparisonOperator.GREATER_THAN.equals(theOperator)) {
                resultIds = _tree.getAllFrom(getIndexedColumn().getDataType().successor(convertedValue));
            } else if (ComparisonOperator.GREATER_THAN_OR_EQUAL.equals(theOperator)) {
                resultIds = _tree.getAllFrom(convertedValue);
            } else {
                throw new AxionException("Unsupported operator " + theOperator);
            }
        } catch (IOException e) {
            String msg = "Unable to retrieve values from index" + getName();
            _log.error(msg, e);
            throw new AxionException(msg,e);
        } catch (ClassNotFoundException e) {
            String msg = "Unable to retrieve values from index" + getName();
            _log.error(msg, e);
            throw new AxionException(msg,e);
        }
        return new LazyRowRowIterator(source, resultIds);
    }

    public IndexLoader getIndexLoader() {
        return LOADER;
    }

    public ObjectBTree getBTree() {
        return _tree;
    }

    // TABLE MODIFICATION LISTENER
    
    public void rowInserted(RowEvent event) throws AxionException {
        String colName = getIndexedColumn().getName();
        int colIndex = event.getTable().getColumnIndex(colName);
        Object value = event.getNewRow().get(colIndex);
        if (value != null) {
            try {
                _tree.insert(value, event.getNewRow().getIdentifier());
            } catch (IOException e) {
                String msg = "Unable to insert into index " + getName();
                _log.error(msg, e);
                throw new AxionException(msg,e);
            } catch (ClassNotFoundException e) {
                String msg = "Unable to insert into index " + getName();
                _log.error(msg, e);
                throw new AxionException(msg,e);
            }
        }
    }

    public void rowDeleted(RowEvent event) throws AxionException {
        String colName = getIndexedColumn().getName();
        int colIndex = event.getTable().getColumnIndex(colName);
        Object value = event.getOldRow().get(colIndex);
        if (value != null) {
            try {
                _tree.delete(value);
            } catch (IOException e) {
                String msg = "Unable to delete from index " + getName();
                _log.error(msg, e);
                throw new AxionException(msg,e);
            } catch (ClassNotFoundException e) {
                String msg = "Unable to delete from index " + getName();
                _log.error(msg, e);
                throw new AxionException(msg,e);
            }
        }
    }
    
    public void rowUpdated(RowEvent event) throws AxionException {
        rowDeleted(event);
        rowInserted(event);
    }

    public void changeRowId(Table table, Row row, int oldId, int newId) throws AxionException {
        try {
            int colnum = table.getColumnIndex(getIndexedColumn().getName());
            Object key = row.get(colnum);
            _tree.replaceId(key, oldId, newId);
        } catch (IOException e) {
            String msg = "Unable to change row id";
            _log.error(msg, e);
            throw new AxionException(msg,e);
        } catch (ClassNotFoundException e) {
            String msg = "Unable to change row id";
            _log.error(msg, e);
            throw new AxionException(msg,e);
        }
    }

    private static final IndexLoader LOADER = new ObjectBTreeIndexLoader();
}
