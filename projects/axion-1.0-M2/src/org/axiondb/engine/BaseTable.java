/*
 * $Id: BaseTable.java,v 1.35 2003/07/08 06:55:39 rwald Exp $
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.BinaryBranchWhereNode;
import org.axiondb.BindVariable;
import org.axiondb.Column;
import org.axiondb.ColumnIdentifier;
import org.axiondb.ComparisonOperator;
import org.axiondb.Constraint;
import org.axiondb.ConstraintViolationException;
import org.axiondb.Index;
import org.axiondb.LeafWhereNode;
import org.axiondb.Literal;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.RowIterator;
import org.axiondb.Selectable;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.TransactableTable;
import org.axiondb.WhereNode;
import org.axiondb.constraints.PrimaryKeyConstraint;
import org.axiondb.engine.rowiterators.FilteringRowIterator;
import org.axiondb.engine.rowiterators.RebindableIndexedRowIterator;
import org.axiondb.engine.rowiterators.UnmodifiableRowIterator;
import org.axiondb.event.BaseTableModificationPublisher;
import org.axiondb.event.ColumnEvent;
import org.axiondb.event.ConstraintEvent;
import org.axiondb.event.RowDeletedEvent;
import org.axiondb.event.RowEvent;
import org.axiondb.event.RowInsertedEvent;
import org.axiondb.event.RowUpdatedEvent;
import org.axiondb.event.TableModificationListener;
import org.axiondb.types.LOBType;

/**
 * An abstract base implementation of {@link Table}.
 *
 * @version $Revision: 1.35 $ $Date: 2003/07/08 06:55:39 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public abstract class BaseTable extends BaseTableModificationPublisher implements Table {
    protected abstract RowIterator getRowIterator() throws AxionException;
    
    public BaseTable(String name) {
        _name = name == null ? null : name.toUpperCase();
        setType(REGULAR_TABLE_TYPE);
    }

    public RowIterator getRowIterator(boolean readOnly) throws AxionException {
        if(readOnly) {
            return UnmodifiableRowIterator.wrap(getRowIterator());
        } else {
            return getRowIterator();
        }
    }

    public void addRow(Row row) throws AxionException {
        int rowid = getNextRowId();
        row.setIdentifier(rowid);
        RowInsertedEvent event = new RowInsertedEvent(this,null,row);
        try {
            checkConstraints(event);
        } catch(AxionException e) {
            freeRowId(rowid);
            throw e;
        }        
        applyInserts(IteratorUtils.singletonIterator(row));
        publishEvent(event);
    }

    protected void deleteRow(Row row) throws AxionException {
        RowDeletedEvent event = new RowDeletedEvent(this,row,null);
        checkConstraints(event);
        // TODO: create singletonIntIterator(int) method in commons-collections
        IntList list = new ArrayIntList(1);
        list.add(row.getIdentifier());        
        applyDeletes(list.iterator());
        publishEvent(event);
    }

    protected void updateRow(Row oldrow, Row newrow) throws AxionException {
        newrow.setIdentifier(oldrow.getIdentifier());
        RowUpdatedEvent event = new RowUpdatedEvent(this,oldrow,newrow);
        checkConstraints(event);
        applyUpdates(IteratorUtils.singletonIterator(newrow));
        publishEvent(event);
    }

    public String toString() {
        return getName();
    }

    public String getName() {
        return _name;
    }

    public String getType() {
        return _type;
    }

    protected void setType(String type) {
        _type = type;
    }

    public void addConstraint(Constraint constraint) throws AxionException {
        if(constraint instanceof PrimaryKeyConstraint && null != getPrimaryKey()) {
            throw new AxionException("This table already has a primary key");
        } else if(_constraints.containsKey(constraint.getName())) {
            throw new AxionException("A constraint named " + constraint.getName() + " already exists.");
        } else {
            _constraints.put(constraint.getName(),constraint);
            Iterator iter = getTableModificationListeners();
            while(iter.hasNext()) {
                TableModificationListener listener = (TableModificationListener)(iter.next());
                listener.constraintAdded(new ConstraintEvent(this, constraint));
            }
        }        
    }
    
    public void removeConstraint(String name) {
        if(name != null) {
            name = name.toUpperCase();
        }
        if (_constraints.containsKey(name)) {
            Constraint constraint = (Constraint)_constraints.get(name);
            Iterator iter = getTableModificationListeners();
            while(iter.hasNext()) {
                TableModificationListener listener = (TableModificationListener)(iter.next());
                try {
                    listener.constraintRemoved(new ConstraintEvent(this, constraint));
                } catch (AxionException e) {
                    _log.error("Unable to publish constraint removed event", e);
                }
            }
            _constraints.remove(name);
        }
    }
    
    private PrimaryKeyConstraint getPrimaryKey() {
        for(Iterator iter = _constraints.values().iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint)(iter.next());
            if(constraint instanceof PrimaryKeyConstraint) {
                return (PrimaryKeyConstraint)(constraint);
            }
        }
        return null;
    }
    
    public Iterator getConstraints() {
        return _constraints.values().iterator();
    }

    public void addIndex(Index index) throws AxionException {
        if(_log.isDebugEnabled()) { _log.debug("Adding index " + index.getName()); }
        _indices.add(index);
        addTableModificationListener(index);
        if(_log.isDebugEnabled()) { _log.debug("Done adding index"); }
    }

    public void removeIndex(Index index) throws AxionException {
        if(_log.isDebugEnabled()) { _log.debug("Removing index " + index.getName()); }
        _indices.remove(index);
        this.removeTableModificationListener(index);
        if(_log.isDebugEnabled()) { _log.debug("Done removing index"); }
    }

    public Index getIndexForColumn(Column column) {
        Iterator indices = _indices.iterator();

        while(indices.hasNext()) {
            Index index = (Index)indices.next();

            if(column.equals(index.getIndexedColumn())) {
                return index;
            }
        }

        return null;
    }

    public boolean isColumnIndexed(Column column) {
        Iterator indices = _indices.iterator();

        while(indices.hasNext()) {
            Index index = (Index)indices.next();

            if(column.equals(index.getIndexedColumn())) {
                return true;
            }
        }

        return false;
    }

    public RowIterator getMatchingRows(List selectables, List values) throws AxionException {
        if(null == selectables || selectables.isEmpty()) {
            return getRowIterator(true);
        } else {
            // TODO: may be able to optimize this by first checking for for indices or
            //       with a special subclass of AbstractAcceptingRowIterator rather
            //       than creating a WhereNode
            WhereNode root = null;
            Iterator left = selectables.iterator();
            Iterator right = values.iterator();
            while(left.hasNext()) {
                LeafWhereNode leaf = null;
                Object value = right.next();               
                leaf = new LeafWhereNode(
                    (Selectable)left.next(),
                    ComparisonOperator.EQUAL,
                    new Literal(value));
                if(null == root) {
                    root = leaf;
                } else {
                    BinaryBranchWhereNode branch = new BinaryBranchWhereNode();
                    branch.setIsAnd(true);
                    branch.setLeft(root);
                    branch.setRight(leaf);
                    root = branch;
                }
            }
            
            return new FilteringRowIterator(getRowIterator(true),makeRowDecorator(),root);
        }
    }

    public RowIterator getIndexedRows(WhereNode node, boolean readOnly) throws AxionException {
        if(readOnly) {
            return UnmodifiableRowIterator.wrap(getIndexedRows(node));
        } else {
            return getIndexedRows(node);
        }
    }
    
    /**
     * Add the given {@link Column} to this table.
     * This implementation throws an {@link AxionException}
     * if rows have already been added to the table.
     */
    public void addColumn(Column col) throws AxionException {
        if(getRowCount() > 0) {
            throw new AxionException("Cannot add column because table already contains rows.");
        } else {
            // XXX FIX ME XXX
            if(col.getDataType() instanceof LOBType) {
                LOBType lob = (LOBType)(col.getDataType());
                if(null == lob.getLobDir()) {
                    lob.setLobDir(new File(System.getProperty("axiondb.lobdir", "."), col.getName()));
                }
            }
            _cols.add(col);
            _colIndexToColIdMap = null;
            publishEvent(new ColumnEvent(this, col));
        }
    }

    public boolean hasColumn(ColumnIdentifier id) {
        boolean result = false;
        String tableName = id.getTableName();
        if(tableName == null || tableName.equals(getName())) {
            result = (getColumn(id.getName()) != null);
        }
        return result;
    }

    public Column getColumn(int index) {
        return (Column)(_cols.get(index));
    }

    public Column getColumn(String name) {
        for(int i = 0; i < _cols.size(); i++) {
            Column col = (Column)(_cols.get(i));
            if(col.getName().equalsIgnoreCase(name)) {
                return col;
            }
        }
        return null;
    }

    public int getColumnIndex(String name) throws AxionException {
        for(int i = 0; i < _cols.size(); i++) {
            Column col = (Column)(_cols.get(i));
            if(col.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        throw new AxionException("Column " + name + " not found.");
    }

    public Iterator getColumnIdentifiers() {
        List result = new ArrayList();
        for(int i=0;i<_cols.size();i++) {
            Column col = (Column)(_cols.get(i));
            result.add(new ColumnIdentifier(new TableIdentifier(getName()),col.getName(),null,col.getDataType()));
        }
        return result.iterator();
    }

    public int getColumnCount() {
        return _cols.size();
    }

    public void drop() throws AxionException {
    }

    public void remount(File dir, boolean datafilesonly) throws AxionException {
    }

    public void checkpoint() throws AxionException {
    }

    public void shutdown() throws AxionException {
    }

    public RowDecorator makeRowDecorator() {
        if(null == _colIndexToColIdMap) {
            Map map = new HashMap();
            int i=0;
            for(Iterator iter = getColumnIdentifiers();iter.hasNext();) {
                map.put(iter.next(),new Integer(i++));
            }
            _colIndexToColIdMap = map;
        }
        return new RowDecorator(_colIndexToColIdMap);
    }
    
    public TransactableTable makeTransactableTable() {
        return new TransactableTableImpl(this);
    }

    public Iterator getIndices() {
        return _indices.iterator();
    }
    
    protected void checkConstraints(RowEvent event) throws AxionException {
        checkConstraints(event,false);
    }
    
    protected void checkConstraints(RowEvent event, boolean deferred) throws AxionException {
        for(Iterator iter = getConstraints();iter.hasNext();) {
            Constraint c = (Constraint)iter.next();
            if(c.isDeferred() == deferred) {
                if(!c.evaluate(event)) {
                    throw new ConstraintViolationException(c);
                }
            }
        }
    }

    protected boolean hasDeferredConstraint() {
        for(Iterator iter = getConstraints();iter.hasNext();) {
            Constraint c = (Constraint)iter.next();
            if(c.isDeferred()) {
                return true;
            }
        }
        return false;
    }

    protected void notifyColumnsOfNewLobDir(File directory) {
        Iterator iter = _cols.iterator();
        while(iter.hasNext()) {
            Column col = (Column)(iter.next());
            if(col.getDataType() instanceof LOBType) {
                LOBType lob = (LOBType)(col.getDataType());
                if(null == lob.getLobDir()) {
                    lob.setLobDir(new File(directory,col.getName()));
                }
            }
        }
    }

    private RowIterator getIndexedRows(WhereNode node) throws AxionException {
        if(node instanceof LeafWhereNode) {
            LeafWhereNode leaf = (LeafWhereNode)node;
            Column column = null;
            Literal literal = null;
            ComparisonOperator op = leaf.getOperator();
            if(leaf.getLeft() instanceof ColumnIdentifier && leaf.getRight() instanceof Literal) {
                column = getColumn(((ColumnIdentifier)leaf.getLeft()).getName());
                literal = (Literal)(leaf.getRight());
            } else if(leaf.getLeft() instanceof Literal && leaf.getRight() instanceof ColumnIdentifier) {
                column = getColumn(((ColumnIdentifier)leaf.getRight()).getName());
                literal = (Literal)(leaf.getLeft());
                op = op.flip();
            } else {
                return null;
            }
            if(!isColumnIndexed(column)) {
                return null;
            } else {
                Index index = getIndexForColumn(column);
                if(!index.supportsOperator(op)) {
                    return null;
                } else if(literal instanceof BindVariable) {
                    return new RebindableIndexedRowIterator(index,this,op,(BindVariable)literal);
                } else {
                    return index.getRowIterator(this,op,literal.evaluate(null));
                }
            }
        } else {
            return null;
        }
    }
    
    private String _name = null;
    private String _type = null;
    private List _cols = new ArrayList();
    private List _indices = new ArrayList();
    private Map _constraints = new HashMap();
    private Map _colIndexToColIdMap = null;
    private static Log _log = LogFactory.getLog(BaseTable.class);   
}
