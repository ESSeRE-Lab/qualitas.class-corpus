/*
 * $Id: TransactableTableImpl.java,v 1.44 2003/07/08 06:55:39 rwald Exp $
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
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.BinaryBranchWhereNode;
import org.axiondb.Column;
import org.axiondb.ColumnIdentifier;
import org.axiondb.ComparisonOperator;
import org.axiondb.Constraint;
import org.axiondb.ConstraintViolationException;
import org.axiondb.Index;
import org.axiondb.LeafWhereNode;
import org.axiondb.Literal;
import org.axiondb.Row;
import org.axiondb.RowComparator;
import org.axiondb.RowDecorator;
import org.axiondb.RowIterator;
import org.axiondb.Selectable;
import org.axiondb.Table;
import org.axiondb.TransactableTable;
import org.axiondb.Transaction;
import org.axiondb.WhereNode;
import org.axiondb.engine.rowiterators.AbstractAcceptingRowIterator;
import org.axiondb.engine.rowiterators.ChainedRowIterator;
import org.axiondb.engine.rowiterators.DelegatingRowIterator;
import org.axiondb.engine.rowiterators.FilteringRowIterator;
import org.axiondb.engine.rowiterators.ListIteratorRowIterator;
import org.axiondb.engine.rowiterators.TransformingRowIterator;
import org.axiondb.engine.rowiterators.UnmodifiableRowIterator;
import org.axiondb.event.BaseTableModificationPublisher;
import org.axiondb.event.RowDeletedEvent;
import org.axiondb.event.RowEvent;
import org.axiondb.event.RowInsertedEvent;
import org.axiondb.event.RowUpdatedEvent;

/**
 * An implemenation of {@link TransactableTable}.
 * 
 * @version $Revision: 1.44 $ $Date: 2003/07/08 06:55:39 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public final class TransactableTableImpl extends BaseTableModificationPublisher implements TransactableTable {

// XXX TO DO: XXX
// some of this code is cut-and-paste from BaseTable it would be
// helpful to refactor so that BaseTable and TransactableTableImpl
// share more code directly

    public TransactableTableImpl(Table table) {
        _table = table;
    }

    public String getName() {
        return _table.getName();
    }

    public String getType() {
        return _table.getType();
    }

    public RowDecorator makeRowDecorator() {
        return _table.makeRowDecorator();
    }
    
    public void addConstraint(Constraint constraint) throws AxionException {
        _table.addConstraint(constraint);
    }
    
    public void removeConstraint(String name) {
        _table.removeConstraint(name);
    }
    
    public Iterator getConstraints() {
        return _table.getConstraints();
    }
    
    public void addIndex(Index index) throws AxionException {
        _table.addIndex(index);
    }

    public void removeIndex(Index index) throws AxionException {
        _table.removeIndex(index);
    }

    public void populateIndex(Index index) throws AxionException {
        _table.populateIndex(index);
    }

    public Index getIndexForColumn(Column column) {
        return _table.getIndexForColumn(column);
    }

    public boolean isColumnIndexed(Column column) {
        return _table.isColumnIndexed(column);
    }

    public void addColumn(Column col) throws AxionException {
        _table.addColumn(col);
    }

    public Column getColumn(int index) {
        return _table.getColumn(index);
    }

    public Column getColumn(String name) {
        return _table.getColumn(name);
    }

    public boolean hasColumn(ColumnIdentifier id) {
        return _table.hasColumn(id);
    }

    public int getColumnIndex(String name) throws AxionException {
        return _table.getColumnIndex(name);
    }

    public Iterator getColumnIdentifiers() {
        return _table.getColumnIdentifiers();
    }

    public int getColumnCount() {
        return _table.getColumnCount();
    }
    
    public Iterator getIndices() {
        return _table.getIndices();
    }

    public void addRow(Row row) throws AxionException {
        int rowid = _table.getNextRowId();
        row.setIdentifier(rowid);
        RowEvent event = new RowInsertedEvent(this,null,row);
        try {
            checkConstraints(event);
        } catch(AxionException e) {
            _table.freeRowId(rowid);
            throw e;
        }                
        _reservedRowIds.add(row.getIdentifier());
        _insertedRows.add(row);
        publishEvent(event);
    }

    public RowIterator getRowIterator(boolean readOnly) throws AxionException {
        if((!readOnly) || hasUpdates() || hasDeletes() || hasInserts()) {            
            ChainedRowIterator chain = new ChainedRowIterator();
            chain.addRowIterator(excludeDeletedTransformUpdated(_table.getRowIterator(readOnly)));
            chain.addRowIterator(new InsertedRowIterator(_insertedRows.listIterator()));
            return chain;
        } else {
            return UnmodifiableRowIterator.wrap(_table.getRowIterator(readOnly));
        }
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
        if(!readOnly) {
            // NOTE: TransactableTableImpl can't currently support modifiable indexed-derived RowIterators.
            //       (Need a good way to have a "live" iterator across the _updatedRows.values() set)
            return null;
        } else {
            RowComparator comparator = getComparatorForWhereNode(node);
            if(null == comparator) {
                return null;
            } else {
                RowIterator baseIter = _table.getIndexedRows(node,readOnly);
                if(null == baseIter) {
                    return null;
                } else {
                    // create a chain of...
                    ChainedRowIterator chained = new ChainedRowIterator();
                
                    if(!hasUpdates() && !hasDeletes()) {
                        // if no updates or deletes have been
                        // applied, only the underlying iterator is needed
                        chained.addRowIterator(baseIter);
                    } else {
                        // else filter out the updated or deleted rows 
                        // (matching updates will be added back in below)
                        chained.addRowIterator(excludeDeletedAndUpdated(baseIter));
                    }

                    if(hasInserts()) {
                        // if any rows have been inserted,
                        // add the matching inserted rows
                        chained.addRowIterator(new FilteringRowIterator(
                            new InsertedRowIterator(_insertedRows.listIterator()),
                            _table.makeRowDecorator(),
                            node));

                    }
                
                    if(hasUpdates()) {
                        // if not read only or any rows have been updated,
                        // add the matching updated rows
                        // NOTE: this is the part that doesn't handle later updates correctly,
                        //       since our matching.listIterator is disassociated from the
                        //       underlying _updateRows.values()
                        RowDecorator dec = _table.makeRowDecorator();                
                        List matching = new ArrayList();
                        for(Iterator iter = _updatedRows.values().iterator();iter.hasNext();) {
                            Row row = (Row)(iter.next());
                            dec.setRow(row);
                            if(((Boolean)node.evaluate(dec)).booleanValue()) {
                                matching.add(row);
                            }    
                        }    
                        if(!matching.isEmpty()) {                
                            chained.addRowIterator(
                                new TransactableTableRowIterator(
                                    new ListIteratorRowIterator(
                                        matching.listIterator())));
                        }
                
                    }
                
                    return UnmodifiableRowIterator.wrap(chained);
                }
            }
        }
    }
    
    private RowComparator getComparatorForWhereNode(WhereNode node) {
        if(node instanceof LeafWhereNode) {
            LeafWhereNode leaf = (LeafWhereNode)node;
            ColumnIdentifier columnId = null;
            if(leaf.getLeft() instanceof ColumnIdentifier && leaf.getRight() instanceof Literal) {
                columnId = (ColumnIdentifier)leaf.getLeft();
            } else if(leaf.getLeft() instanceof Literal && leaf.getRight() instanceof ColumnIdentifier) {
                columnId = (ColumnIdentifier)leaf.getRight();
            }
            if(columnId != null) {
                return new RowComparator(columnId,makeRowDecorator());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public int getRowCount() {
        return _table.getRowCount() + _insertedRows.size() - _deletedRows.size();
    }

    public int getNextRowId() {
        return _table.getNextRowId();
    }

    public void freeRowId(int id) {
        _table.freeRowId(id);
    }

    public void drop() throws AxionException {
        _table.drop();
    }

    public void checkpoint() throws AxionException {
        _table.checkpoint();
    }

    public void shutdown() throws AxionException {
        _table.shutdown();
    }

    public void remount(File dir, boolean dataOnly) throws AxionException {
        _table.remount(dir, dataOnly);
    }

    public Row getRow(int id) throws AxionException {
        Integer Id = new Integer(id);
        ;
        if(_deletedRows.contains(id)) {
            return null;
        } else {
            Row row = (Row)_updatedRows.get(Id);
            if(null != row) {
                return row;
            }
            row = getInsertedRow(id);
            if(null != row) {
                return row;
            }
            return _table.getRow(id);
        }
    }

    public void applyInserts(Iterator rows) throws AxionException {
        _table.applyInserts(rows);
    }

    public void applyDeletes(IntIterator rowids) throws AxionException {
        _table.applyDeletes(rowids);
    }

    public void applyUpdates(Iterator rows) throws AxionException {
        _table.applyUpdates(rows);
    }

    public void commit() throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug("commit() " + this);
        }
        assertOpen();
        if(hasDeferredConstraint()) {
            for(Iterator iter = _insertedRows.iterator();iter.hasNext(); ) {
                Row row = (Row)iter.next();
                RowEvent event = new RowInsertedEvent(this,null,row);
                checkConstraints(event,true);
            }
            for(int i=0;i<_deletedRows.size();i++) {
                int id = _deletedRows.get(i);
                Row row = _table.getRow(id);
                RowEvent event = new RowDeletedEvent(this,row,null);
                checkConstraints(event,true);
            }
            for(Iterator iter = _updatedRows.values().iterator();iter.hasNext(); ) {
                Row row = (Row)iter.next();
                Row oldrow = _table.getRow(row.getIdentifier());
                RowEvent event = new RowUpdatedEvent(this,oldrow,row);
                checkConstraints(event,true);
            }
        }
        _state = Transaction.STATE_COMMITTED;
    }

    public void rollback() throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug("rollback() " + this);
        }
        assertOpen();
        for(int i=0;i<_reservedRowIds.size();i++) {
            freeRowId(_reservedRowIds.get(i));
        }
        _reservedRowIds = null;
        _table = null;
        _insertedRows = null;
        _updatedRows = null;
        _deletedRows = null;
        _state = Transaction.STATE_ABORTED;
    }

    public void apply() throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug("apply() " + this);
        }
        assertCommitted();
        
        // apply deletes
        if(!_deletedRows.isEmpty()) {
            for(int i=0;i<_deletedRows.size();i++) {
                Row deleted = _table.getRow(_deletedRows.get(i));
                RowEvent event = new RowDeletedEvent(_table,deleted,null);
                for(Iterator iter = _table.getIndices();iter.hasNext();) {
                    Index index = (Index)(iter.next());
                    index.rowDeleted(event);                
                }
            }
            _table.applyDeletes(_deletedRows.iterator());
        }

        // apply updates       
        if(!_updatedRows.isEmpty()) {
            for(Iterator iter = _updatedRows.values().iterator(); iter.hasNext();) {
                Row newrow = (Row)(iter.next());
                Row oldrow = _table.getRow(newrow.getIdentifier());
                RowEvent event = new RowUpdatedEvent(_table,oldrow,newrow);
                for(Iterator indexIter = _table.getIndices();indexIter.hasNext();) {
                    Index index = (Index)(indexIter.next());
                    index.rowUpdated(event);                
                }
            }
            _table.applyUpdates(_updatedRows.values().iterator());
        }

        // apply inserts        
        if(!_insertedRows.isEmpty()) {
            for(Iterator iter = _insertedRows.iterator(); iter.hasNext();) {
                Row row= (Row)(iter.next());
                RowEvent event = new RowInsertedEvent(_table,null,row);
                for(Iterator indexIter = _table.getIndices();indexIter.hasNext();) {
                    Index index = (Index)(indexIter.next());
                    index.rowInserted(event);                
                }
            }
            _table.applyInserts(_insertedRows.iterator());
        }

        _state = Transaction.STATE_APPLIED;
    }

    public TransactableTable makeTransactableTable() {
        return new TransactableTableImpl(this);
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

    void deleteRow(Row row) throws AxionException {
        // by construction, this method should never be
        // called for a row that only exists in _insertedRows,
        // so we'll ignore that case

        RowEvent event = new RowDeletedEvent(this,row,null);
        checkConstraints(event);
        
        // add the row to our list of deleted rows
        if(!_deletedRows.contains(row.getIdentifier())) {
            _deletedRows.add(row.getIdentifier());
        }
        
        // delete from _updatedRows, if it's in there
        _updatedRows.remove(new Integer(row.getIdentifier()));
        
        publishEvent(event);
    }

    void updateRow(Row oldrow, Row newrow) throws AxionException {
        newrow.setIdentifier(oldrow.getIdentifier());
        RowEvent event = new RowUpdatedEvent(this,oldrow,newrow);
        checkConstraints(event);
        _updatedRows.put(new Integer(oldrow.getIdentifier()),newrow);
        publishEvent(event);
    }

    private Row getInsertedRow(int id) {
        for(int i=0;i<_insertedRows.size();i++) {
            Row row = (Row)_insertedRows.get(i);
            if(row.getIdentifier() == id) {
                return row;
            }
        }
        return null;
    }

    private void assertOpen() throws AxionException {
        if(Transaction.STATE_OPEN != _state) {
            throw new AxionException("Already committed or rolled back [" + _state + "].");
        }
    }

    private void assertCommitted() throws AxionException {
        if(Transaction.STATE_COMMITTED != _state) {
            throw new AxionException("Not committed [" + _state + "].");
        }
    }

    /**
     * Overrides {@link #remove} and {@link #set} to apply them to the 
     * current transaction.
     */
    private class TransactableTableRowIterator extends DelegatingRowIterator {
        public TransactableTableRowIterator(RowIterator iter) {
            super(iter);
        }

        public void remove() throws AxionException {
            deleteRow(current());
        }

        public void set(Row row) throws AxionException {
            updateRow(current(),row);
        }
    }

    /**
     * Filters out rows that have been deleted in the current transaction.
     */
    private class ExcludeDeleted extends AbstractAcceptingRowIterator {
        public ExcludeDeleted(RowIterator iter) {
            super(iter);
        }

        protected boolean acceptable(int rowindex, Row row) throws AxionException {
            return !(_deletedRows.contains(row.getIdentifier()));
        }
    }

    /**
     * Filters out rows that have been updated in the current transaction.
     */
    private class ExcludeUpdated extends AbstractAcceptingRowIterator {
        public ExcludeUpdated(RowIterator iter) {
            super(iter);
        }

        protected boolean acceptable(int rowindex, Row row) throws AxionException {
            return !(_updatedRows.containsKey(new Integer(row.getIdentifier())));
        }
    }

    /**
     * Transforms rows that have been updated within the current transaction.
     */
    private class TransformUpdated extends TransformingRowIterator {
        public TransformUpdated(RowIterator iter) {
            super(iter);
        }

        public void remove() throws AxionException {
            deleteRow(current());
        }

        public void set(Row row) throws AxionException {
            updateRow(current(),row);
        }

        protected Row transform(Row row) {
            Row updated = (Row)_updatedRows.get(new Integer(row.getIdentifier()));
            if(null != updated) {
                return updated;
            } else {
                return row;
            }
        }
    }
    
    private RowIterator excludeDeletedTransformUpdated(RowIterator base) {
        if(null == base) {
            return null;
        } else {
            return new TransactableTableRowIterator(
                new ExcludeDeleted(
                    new TransformUpdated(base)));                    
        }
    }

    private RowIterator excludeDeletedAndUpdated(RowIterator base) {
        if(null == base) {
            return null;
        } else {
            return new TransactableTableRowIterator(
                new ExcludeDeleted(
                    new ExcludeUpdated(base)));                    
        }
    }

    private class InsertedRowIterator extends ListIteratorRowIterator {
        public InsertedRowIterator(ListIterator iter) {
            super(iter);
        }

        public void remove() {
            if(_reservedRowIds.removeElement(current().getIdentifier())) {
                _table.freeRowId(current().getIdentifier());
            }
            super.remove();
        }
    }

    private boolean hasUpdates() {
        return !(_updatedRows.isEmpty());
    }
    
    private boolean hasDeletes() {
        return !(_deletedRows.isEmpty());
    }
    
    private boolean hasInserts() {
        return !(_insertedRows.isEmpty());
    }
    
    Table _table = null;
    /** {@link IntArrayList} of row identifiers that have been reserved from my underlying table. */
    IntList _reservedRowIds = new ArrayIntList();
    /** {@link List} of {@link Row}s that have been inserted. */
    private List _insertedRows = new ArrayList();
    /** {@link Map} of {@link Row}s that have been updated, keyed by row identifier. */
    Map _updatedRows = new HashMap();
    /** {@link IntArrayList} of row identifiers that have been deleted. */
    IntList _deletedRows = new ArrayIntList();
    /** My current state. */
    private int _state = Transaction.STATE_OPEN;

    private static Log _log = LogFactory.getLog(TransactableTableImpl.class);
}
