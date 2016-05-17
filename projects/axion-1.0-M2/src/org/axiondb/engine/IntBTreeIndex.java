/*
 * $Id: IntBTreeIndex.java,v 1.20 2003/05/20 17:59:10 rwald Exp $
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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntCollections;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;
import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.ComparisonOperator;
import org.axiondb.DataType;
import org.axiondb.IndexLoader;
import org.axiondb.Row;
import org.axiondb.RowIterator;
import org.axiondb.RowSource;
import org.axiondb.Table;
import org.axiondb.engine.rowiterators.EmptyRowIterator;
import org.axiondb.engine.rowiterators.LazyRowRowIterator;
import org.axiondb.event.RowEvent;
import org.axiondb.event.TableModificationListener;
import org.axiondb.util.BTree;

/**
 * A {@link BaseBTreeIndex B-Tree index} over integer keys.
 * 
 * @version $Revision: 1.20 $ $Date: 2003/05/20 17:59:10 $
 * @author Chuck Burdick
 * @author Dave Pekarek Krohn
 */
public class IntBTreeIndex extends BaseBTreeIndex implements TableModificationListener {
    private BTree _tree = null;

    public IntBTreeIndex(String name, Column column, boolean unique, File dataDirectory) throws AxionException {
        super(name, column, unique);
        try {
            _tree = new BTree(dataDirectory, name, 1000);
        } catch (IOException e) {
            String msg = "Unable to create index file for " + getName() + " due to IOException";
            throw new AxionException(msg,e);
        }
    }

    public IntBTreeIndex(String name, Column column, boolean unique) throws AxionException {
        this(name, column, unique, null);
    }

    public RowIterator getRowIterator(RowSource source, ComparisonOperator theOperator, Object value) throws AxionException {        
        IntListIterator resultIds = null;
        
        DataType type = getIndexedColumn().getDataType();

        Object convertedValue = type.convert(value);
        if(null == convertedValue) {
            // null fails all comparisions I support
            return EmptyRowIterator.INSTANCE;
        }
        int iVal;
        try {
            iVal = type.toInt(convertedValue);
        } catch(SQLException e) {
            throw new AxionException(e);
        }        
        try {
            if(ComparisonOperator.EQUAL.equals(theOperator)) {
                if (!isUnique()) {
                    resultIds = _tree.getAll(iVal);
                } else {
                    Integer result = _tree.get(iVal);
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
                resultIds = _tree.getAllTo(iVal);
            } else if (ComparisonOperator.LESS_THAN_OR_EQUAL.equals(theOperator)) {
                int iSuccessor = getSuccessor(type,convertedValue);
                resultIds = _tree.getAllTo(iSuccessor);
            } else if (ComparisonOperator.GREATER_THAN.equals(theOperator)) {

                int iSuccessor = getSuccessor(type,convertedValue);

                // NOTE: _tree.valueIterator returns a continuation rather than enumerating all
                //       elements of the RowIterator first.  This is slightly slower than the
                //       getAllFrom for small tables, but faster and less memory consumptive
                //       memory for large tables, especially when we rarely visit the tail of the
                //       result set.  This also postpones loading the index nodes until the data
                //       is actually read (in constrast, getAllFrom(<some small value>) will load
                //       all or nearly all nodes.
                //       For optimal performance it may be best to determine how large the index 
                //       is and use that to figure out which approach--enumeration or 
                //       continuation--is most appropriate for the given query.

                // resultIds = _tree.getAllFrom(iSuccessor);

                resultIds = _tree.valueIteratorGreaterThanOrEqualTo(iSuccessor); 

            } else if (ComparisonOperator.GREATER_THAN_OR_EQUAL.equals(theOperator)) {
                
                // NOTE: see note above.
                              
                // resultIds = _tree.getAllFrom(iVal);
                
                resultIds =  _tree.valueIteratorGreaterThanOrEqualTo(iVal);        

            } else {
                throw new AxionException("Unsupported operator " + theOperator);
            }
        } catch (IOException e) {
            String msg = "Unable to retrieve values from index " + getName() + " due to IOException";
            throw new AxionException(msg,e);
        }
        return new LazyRowRowIterator(source, resultIds);
    }

    public IndexLoader getIndexLoader() {
        return LOADER;
    }

    protected BTree getBTree() {
        return _tree;
    }
    
    // TABLE MODIFICATION LISTENER
    
    public void rowInserted(RowEvent event) throws AxionException {
        String colName = getIndexedColumn().getName();
        int colIndex = event.getTable().getColumnIndex(colName);
        Integer value = (Integer)event.getNewRow().get(colIndex);
        if (value != null) {
            try {
                _tree.insert(value.intValue(), event.getNewRow().getIdentifier());
            } catch (IOException e) {
                String msg = "Unable to insert into index " + getName() + " due to IOException";
                throw new AxionException(msg,e);
            }
        }
    }

    public void rowDeleted(RowEvent event) throws AxionException {
        String colName = getIndexedColumn().getName();
        int colIndex = event.getTable().getColumnIndex(colName);
        Integer value = (Integer)event.getOldRow().get(colIndex);
        if (value != null) {
            try {
                _tree.delete(value.intValue());
            } catch (IOException e) {
                String msg = "Unable to delete from index " + getName() + " due to IOException";
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
            Integer key = (Integer) row.get(colnum);
            _tree.replaceId(key.intValue(), oldId, newId);
        } catch (IOException e) {
            String msg = "Unable to change row id in index " + getName() + " due to IOException";
            throw new AxionException(msg,e);
        }
    }


    private int getSuccessor(DataType type, Object convertedValue) throws AxionException {
        Object successor = type.successor(convertedValue);
        int iSuccessor;
        try {
            iSuccessor = type.toInt(successor);
        } catch(SQLException e) {
            throw new AxionException(e);
        }        
        return iSuccessor;
    }

    private static final IndexLoader LOADER = new IntBTreeIndexLoader();
}
