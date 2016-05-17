/*
 * $Id: DeleteCommand.java,v 1.15 2003/05/09 19:48:15 rwald Exp $
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

package org.axiondb.engine.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.RowIterator;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.WhereNode;
import org.axiondb.engine.rowiterators.FilteringRowIterator;
import org.axiondb.jdbc.AxionResultSet;

/**
 * A <tt>DELETE</tt> command.
 *
 * @version $Revision: 1.15 $ $Date: 2003/05/09 19:48:15 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class DeleteCommand extends BaseAxionCommand {
    private TableIdentifier _table = null;
    private WhereNode _where = null;
    private boolean _resolved = false;

    public DeleteCommand() {
    }

    public DeleteCommand(String tableName, WhereNode where) {
        setTable(new TableIdentifier(tableName));
        setWhere(where);
    }

    public DeleteCommand(TableIdentifier table, WhereNode where) {
        setTable(table);
        setWhere(where);
    }

    public TableIdentifier getTable() {
        return _table;
    }

    public void setTable(TableIdentifier table) {
        if(_resolved) { throw new IllegalStateException("Already resolved."); }                
        _table = table;
    }

    public void setWhere(WhereNode where) {
        if(_resolved) { throw new IllegalStateException("Already resolved."); }                
        _where = where;
    }

    public WhereNode getWhere() {
        return _where;
    }

    public int executeUpdate(org.axiondb.Database db) throws AxionException {
        assertNotReadOnly(db);
        resolve(db);
        Table table = db.getTable(getTable());
        if(null == table) {
            throw new AxionException("Table " + getTable() + " not found.");
        }
        RowIterator rows = null;
        if(null != getWhere()) {
            rows = new FilteringRowIterator(
                table.getRowIterator(false), 
                table.makeRowDecorator(), 
                getWhere());
        } else {
            rows = table.getRowIterator(false);
        }
                                    
        int rowcount = 0;
        while(rows.hasNext()) {
            rows.next();
            rows.remove();
            rowcount++;
        }
        return rowcount;
    }

    /** Unsupported */
    public AxionResultSet executeQuery(Database database) throws AxionException {
        throw new UnsupportedOperationException("Use executeUpdate.");
    }

    public boolean execute(Database database) throws AxionException {
        setEffectedRowCount(executeUpdate(database));
        return false;
    }

    protected Iterator getBindVariableIterator() {
        List list = new ArrayList();
        appendBindVariables(getWhere(),list);
        return list.iterator();
    }

    private void resolve(Database db) throws AxionException {
        if(!_resolved) {
            TableIdentifier[] tables = new TableIdentifier[] { getTable() };
            db.resolveWhereNode(getWhere(),tables);
            _resolved = true;
        }
    }

}

