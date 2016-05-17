/*
 * $Id: SortedMapIndex.java,v 1.14 2003/05/02 15:11:18 rwald Exp $
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.adapters.IntListList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.ComparisonOperator;
import org.axiondb.Index;
import org.axiondb.IndexLoader;
import org.axiondb.Row;
import org.axiondb.RowIterator;
import org.axiondb.RowSource;
import org.axiondb.Table;
import org.axiondb.engine.rowiterators.EmptyRowIterator;
import org.axiondb.engine.rowiterators.LazyRowRowIterator;
import org.axiondb.engine.rowiterators.SingleRowIterator;
import org.axiondb.event.ColumnEvent;
import org.axiondb.event.ConstraintEvent;
import org.axiondb.event.RowEvent;

/**
 * An {@link Index} based upon a {@link SortedMap}.
 * 
 * @version $Revision: 1.14 $ $Date: 2003/05/02 15:11:18 $
 * @author Morgan Delagrange
 * @author Rodney Waldhoff
 */
public class SortedMapIndex implements Index {

    public SortedMapIndex(String name, Column col) {
        this(name,col,null,false);
    }

    public SortedMapIndex(String name, Column col, boolean unique) {
        this(name,col,null,unique);
    }

    public SortedMapIndex(String name, Column col, SortedMap map) {        
        this(name,col,map,false);
    }

    public SortedMapIndex(String name, Column col, SortedMap map, boolean unique) {
        _unique = unique;
        _name = name;
        _column = col;
        _map = (null == map ? new TreeMap() : map);
        if(!(_map instanceof Serializable)) {
            throw new IllegalArgumentException("Map must be Serializable, found " + map.getClass().getName());
        }
        if(_log.isDebugEnabled()) {
            _log.debug("SortedMapIndex \"" + name + "\" created on column \"" + col + "\".");
        }
    }
    
    public String getName() {
        return _name;
    }

    public Column getIndexedColumn() {
        return _column;
    }

    public boolean isUnique() {
        return _unique;
    }

    public RowIterator getRowIterator(RowSource source, ComparisonOperator op, Object value) throws AxionException {
        _log.debug("SortedMapIndex.getRowIterator(RowSource,ComparisonOperator,Object)");
        Object convertedValue = _column.getDataType().convert(value);
        if(ComparisonOperator.EQUAL.equals(op)) {
            Object matching = _map.get(convertedValue);            
            if(null == matching) {
                return EmptyRowIterator.INSTANCE;
            }
            if(matching instanceof IntList) {
                return new LazyRowRowIterator(source,((IntList)matching).listIterator());
            } else {
                return new SingleRowIterator(new LazyRow(source,((Integer)matching).intValue(),source.getColumnIndex(_column.getName()),value));
            }
        } else if(ComparisonOperator.GREATER_THAN.equals(op)) {
            convertedValue = _column.getDataType().successor(convertedValue);
            SortedMap submap = _map.tailMap(convertedValue);
            List[] lists = flattenLists(submap.entrySet());
            return new LazyRowRowIterator(source,lists[0].listIterator(),source.getColumnIndex(_column.getName()),lists[1].listIterator());
        } else if(ComparisonOperator.GREATER_THAN_OR_EQUAL.equals(op)) {
            SortedMap submap = _map.tailMap(convertedValue);
            List[] lists = flattenLists(submap.entrySet());
            return new LazyRowRowIterator(source,lists[0].listIterator(),source.getColumnIndex(_column.getName()),lists[1].listIterator());
        } else if(ComparisonOperator.LESS_THAN.equals(op)) {
            SortedMap submap = _map.headMap(convertedValue);
            List[] lists = flattenLists(submap.entrySet());
            return new LazyRowRowIterator(source,lists[0].listIterator(),source.getColumnIndex(_column.getName()),lists[1].listIterator());
        } else if(ComparisonOperator.LESS_THAN_OR_EQUAL.equals(op)) {
            convertedValue = _column.getDataType().successor(convertedValue);
            SortedMap submap = _map.headMap(convertedValue);
            List[] lists = flattenLists(submap.entrySet());
            return new LazyRowRowIterator(source,lists[0].listIterator(),source.getColumnIndex(_column.getName()),lists[1].listIterator());
        } else {
            throw new AxionException("Unsupported operator " + op);
        }
    }

    public boolean supportsOperator(ComparisonOperator op) {
        _log.debug("SortedMapIndex.supportsOperator(ComparisonOperator)");
        if(ComparisonOperator.EQUAL.equals(op)) {
            return true;
        } else if(ComparisonOperator.GREATER_THAN.equals(op)) {
            return _column.getDataType().supportsSuccessor();
        } else if(ComparisonOperator.GREATER_THAN_OR_EQUAL.equals(op)) {
            return true;
        } else if(ComparisonOperator.LESS_THAN.equals(op)) {
            return true;
        } else if(ComparisonOperator.LESS_THAN_OR_EQUAL.equals(op)) {
            return _column.getDataType().supportsSuccessor();
        } else {
            return false;
        }
    }

    public void columnAdded(ColumnEvent event) throws AxionException {
    }

    public void rowInserted(RowEvent event) throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug("SortedMapIndex.rowInserted(TableModifiedEvent): " + event);
        }
        int colnum = event.getTable().getColumnIndex(_column.getName());
        Object key = event.getNewRow().get(colnum);
        Object val = _map.get(key);
        if(null == val) {
            //_log.debug("SortedMapIndex.rowInserted(): No value found for key, inserting.");
            _map.put(key,new Integer(event.getNewRow().getIdentifier()));
        } else if(val instanceof IntList) {
            //_log.debug("SortedMapIndex.rowInserted(): List found for key, appending.");
            if(_unique) {
                throw new AxionException("Expected " + _column.getName() + " to be unique.");
            } else {
                IntList list = (IntList)val;
                list.add(event.getNewRow().getIdentifier());
            }
        } else {
            //_log.debug("SortedMapIndex.rowInserted(): Non-list value found for key, creating new list.");
            if(_unique) {
                throw new AxionException("Expected " + _column.getName() + " to be unique.");
            } else {
                IntList list = new ArrayIntList();
                list.add(((Number)val).intValue());
                list.add(event.getNewRow().getIdentifier());
                _map.put(key,list);
            }
        }
    }
    
    public void rowDeleted(RowEvent event) throws AxionException {
        _log.debug("SortedMapIndex.rowDeleted(TableModifiedEvent)");
        int colnum = event.getTable().getColumnIndex(_column.getName());
        Object key = event.getOldRow().get(colnum);
        Object val = _map.get(key);
        if(null == val) {
            return;
        } else if(val instanceof IntList) {
            IntList list = (IntList)val;
            list.removeElement(event.getOldRow().getIdentifier());
        } else if(event.getOldRow().getIdentifier() == ((Integer)val).intValue()) {
            _map.remove(key);
        }
    }

    public void rowUpdated(RowEvent event) throws AxionException {
        _log.debug("SortedMapIndex.rowUpdated(TableModifiedEvent)");
        int colnum = event.getTable().getColumnIndex(_column.getName());
        Object newkey = event.getNewRow().get(colnum);
        Object oldkey = event.getOldRow().get(colnum);
        if(null == newkey ? null == oldkey : newkey.equals(oldkey)) {
            return;
        } else {
            rowDeleted(event);
            rowInserted(event);
        }
    }
    
    public void constraintAdded(ConstraintEvent event) throws AxionException {
    }

    public void constraintRemoved(ConstraintEvent event) throws AxionException {
    }

    public void save(File dataDirectory) throws AxionException {
        getIndexLoader().saveIndex(this,dataDirectory);
    }

    public IndexLoader getIndexLoader() {
        return LOADER;
    }

    public void changeRowId(Table table, Row row, int oldId, int newId) throws AxionException {
        int colnum = table.getColumnIndex(_column.getName());
        Object key = row.get(colnum);
        Object val = _map.get(key);
        if(null == val) {
            return;
        } else if(val instanceof IntList) {
            IntList list = (IntList)val;
            for(int i=0;i<list.size();i++) {
                int id = list.get(i);
                if(id == oldId) {
                    list.set(i,newId);
                    break;
                }
            }
        } else if(oldId == ((Integer)val).intValue()) {
            _map.put(key,new Integer(newId));
        }
    }
    
    private List[] flattenLists(Collection values) {
        if(_log.isDebugEnabled()) {
            _log.debug("SortedMapIndex.flattenList(Collection): " + values);
        }

        IntList rowidlist = new ArrayIntList();
        List valuelist = new ArrayList();

        Iterator iter = values.iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)(iter.next());
            if(entry.getValue() instanceof IntList) {
                IntList intlist = (IntList)(entry.getValue());
                for(int i=0;i<intlist.size();i++) {
                    rowidlist.add(intlist.get(i));
                    valuelist.add(entry.getKey());
                }
            } else {
                rowidlist.add(((Number)entry.getValue()).intValue());
                valuelist.add(entry.getKey());
            }
        }

        return new List[] { IntListList.wrap(rowidlist), valuelist };
    }

    protected SortedMap getMap() {
        return _map;
    }

    private SortedMap _map = null;
    private String _name = null;
    private Column _column = null;
    private boolean _unique = false;
    private static Log _log = LogFactory.getLog(SortedMapIndex.class);
    private static final IndexLoader LOADER = new SortedMapIndexLoader();

    class IntMapEntry implements Map.Entry {
        IntMapEntry(Object key, int value) {
            _key = key;
            _value = value;
        }

        public Object getKey() {
            return _key;
        }

        public Object getValue() {
            return new Integer(_value);
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        private Object _key;
        private int _value;
    }
}
