/*
 * $Id: SnapshotIsolationTransaction.java,v 1.19 2003/07/07 23:36:12 rwald Exp $
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.Database;
import org.axiondb.FromNode;
import org.axiondb.IndexFactory;
import org.axiondb.Selectable;
import org.axiondb.Sequence;
import org.axiondb.Table;
import org.axiondb.TableFactory;
import org.axiondb.TableIdentifier;
import org.axiondb.TransactableTable;
import org.axiondb.Transaction;
import org.axiondb.TransactionManager;
import org.axiondb.WhereNode;
import org.axiondb.event.ColumnEvent;
import org.axiondb.event.ConstraintEvent;
import org.axiondb.event.DatabaseModificationListener;
import org.axiondb.event.RowEvent;
import org.axiondb.event.TableModificationListener;

/**
 * A {@link Transaction} implementation that provides
 * "snapshot isolation", which supports TRANSACTION_SERIALIZABLE
 * isolation without locking.
 * 
 * @version $Revision: 1.19 $ $Date: 2003/07/07 23:36:12 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 * @author Amrish Lal
 */
public class SnapshotIsolationTransaction implements Transaction, TableModificationListener {
    public SnapshotIsolationTransaction(Database db) {
        _openOnTransaction = db;
    }

    public int getState() {
        return _state;
    }

    public String getName() {
        return _openOnTransaction.getName();
    }

    public Table getTable(String name) throws AxionException {
        return getWrappedTable(new TableIdentifier(name));
    }

    public Table getTable(TableIdentifier table) throws AxionException {
        return getWrappedTable(table);
    }

    public void dropTable(String name) throws AxionException {
        _openOnTransaction.dropTable(name);
    }

    public void addTable(Table table) throws AxionException {
        _openOnTransaction.addTable(table);
    }

    public void tableAltered(Table table) throws AxionException {
        _openOnTransaction.tableAltered(table);
    }

    public DataType getDataType(String name) {
        return _openOnTransaction.getDataType(name);
    }

    public IndexFactory getIndexFactory(String name) {
        return _openOnTransaction.getIndexFactory(name);
    }

    public TableFactory getTableFactory(String name) {
        return _openOnTransaction.getTableFactory(name);
    }

    public File getTableDirectory() {
        return _openOnTransaction.getTableDirectory();
    }

    public Selectable resolveSelectable(Selectable selectable, TableIdentifier[] tables) throws AxionException {
        return _openOnTransaction.resolveSelectable(selectable,tables);
    }

    public void resolveFromNode(FromNode from, TableIdentifier[] tables) throws AxionException {
        _openOnTransaction.resolveFromNode(from, tables);
    }
    
    public void resolveWhereNode(WhereNode where, TableIdentifier[] tables) throws AxionException {
        _openOnTransaction.resolveWhereNode(where,tables);
    }

    public void checkpoint() throws AxionException {
        _openOnTransaction.checkpoint();
    }

    public void shutdown() throws AxionException {
        _openOnTransaction.shutdown();
    }

    public void remount(File newdir) throws AxionException {
        _openOnTransaction.remount(newdir);
    }

    public boolean isReadOnly() {
        return _openOnTransaction.isReadOnly();
    }

    public void createSequence(Sequence seq) throws AxionException {
        _openOnTransaction.createSequence(seq);
    }

    public Sequence getSequence(String name) {
        return _openOnTransaction.getSequence(name);
    }

    public void dropSequence(String name) throws AxionException {
        _openOnTransaction.dropSequence(name);
    }

    public void commit() throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug(this + " commit()");
        }
        assertOpen();
        for(Iterator iter = _wrappedTables.values().iterator(); iter.hasNext();) {
            TransactableTable ttable = (TransactableTable)(iter.next());
            ttable.commit();
        }
        _state = STATE_COMMITTED;
    }

    public void rollback() throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug(this + " rollback()");
        }
        assertOpen();
        for(Iterator iter = _wrappedTables.values().iterator(); iter.hasNext();) {
            TransactableTable ttable = (TransactableTable)(iter.next());
            ttable.rollback();
        }
        _state = STATE_ABORTED;
    }

    public void apply() throws AxionException {
        if(_log.isDebugEnabled()) {
            _log.debug(this + " apply()");
        }
        if(STATE_COMMITTED != _state) {
            throw new AxionException("Not committed, can't apply.");
        }
        for(Iterator iter = _wrappedTables.values().iterator(); iter.hasNext();) {
            TransactableTable ttable = (TransactableTable)(iter.next());
            ttable.apply();
        }
        _state = STATE_APPLIED;
    }

    /**
     * FIXME - Needs Impl! (CB, 12/23/2002
     */
    public void addDatabaseModificationListener(DatabaseModificationListener l) {
    }

    /**
     * FIXME - Needs Impl! (CB, 12/23/2002
     */
    public List getDatabaseModificationListeners() {
        return null;
    }

    /**
     * FIXME - Needs Impl! (CB, 12/23/2002
     */
    public void columnAdded(ColumnEvent event) throws AxionException {
    }

    public void rowInserted(RowEvent event) throws AxionException {
        _modifiedTables.add(event.getTable().getName());
    }

    public void rowDeleted(RowEvent event) throws AxionException {
        _modifiedTables.add(event.getTable().getName());
    }

    public void rowUpdated(RowEvent event) throws AxionException {
        _modifiedTables.add(event.getTable().getName());
    }

    public void constraintAdded(ConstraintEvent event) throws AxionException {
    }

    public void constraintRemoved(ConstraintEvent event) throws AxionException {
    }

    public Database getOpenOnTransaction() {
        return _openOnTransaction;
    }

    public Set getModifiedTables() {
        return _modifiedTables;
    }
    
    public Set getReadTables() {
        return _readTables;
    }

    public TransactionManager getTransactionManager() {
        return _openOnTransaction.getTransactionManager();
    }
    
    private TransactableTable getWrappedTable(TableIdentifier id) throws AxionException {
        TransactableTable ttable = (TransactableTable)(_wrappedTables.get(id));
        if(null == ttable) {
            Table table = _openOnTransaction.getTable(id);
            if(null == table) {
                return null;
            } else {
                ttable = table.makeTransactableTable();
                if(_log.isDebugEnabled()) {
                    _log.debug(this + " created TransactableTable " + ttable);
                }
                ttable.addTableModificationListener(this);
                _wrappedTables.put(id,ttable);
                _readTables.add(ttable.getName());
            }
        }
        return ttable;
    }

    private void assertOpen() throws AxionException {
        if(STATE_OPEN != _state) {
            throw new AxionException("Already committed or rolled back.");
        }
    }

    private Set _modifiedTables = new HashSet();
    private Set _readTables = new HashSet();
    private Map _wrappedTables = new HashMap();
    private Database _openOnTransaction = null;
    private int _state = STATE_OPEN;
    private static Log _log = LogFactory.getLog(SnapshotIsolationTransaction.class);
}
