/*
 * $Id: UpdateCommand.java,v 1.20 2003/05/09 19:48:15 rwald Exp $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.axiondb.AxionException;
import org.axiondb.ColumnIdentifier;
import org.axiondb.DataType;
import org.axiondb.Database;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.RowIterator;
import org.axiondb.Selectable;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.WhereNode;
import org.axiondb.engine.SimpleRow;
import org.axiondb.engine.rowiterators.FilteringRowIterator;
import org.axiondb.jdbc.AxionResultSet;

/**
 * An <tt>UPDATE</tt> command.
 *
 * @version $Revision: 1.20 $ $Date: 2003/05/09 19:48:15 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class UpdateCommand extends BaseAxionCommand {

    //------------------------------------------------------------ Constructors

    
    public UpdateCommand() {
    }

    //---------------------------------------------------------- Public Methods

    public TableIdentifier getTable() {
        return _table;
    }

    public void setTable(TableIdentifier table) {
        _table = table;
    }

    public void addColumn(ColumnIdentifier col) {
        _cols.add(col);
    }

    public void addValue(Selectable val) {
        _vals.add(val);
    }

    public int getColumnCount() {
        return _cols.size();
    }

    public Iterator getColumnIterator() {
        return _cols.iterator();
    }

    public Iterator getValueIterator() {
        return _vals.iterator();
    }

    public int getValueCount() {
        return _vals.size();
    }

    public void setWhere(WhereNode where) {
        _where = where;
    }

    public WhereNode getWhere() {
        return _where;
    }

    public int executeUpdate(org.axiondb.Database db) throws AxionException {
        assertNotReadOnly(db);
        resolve(db);
        // grab the table
        Table table = db.getTable(getTable());
        if(null == table) { 
            throw new AxionException("Table " + getTable() + " not found.");
        }

        // build the indexMap (ColumnIdentifiers --> Integer index in Row)
        Map indexMap = new HashMap();
        {
            int i = 0;
            for(Iterator iter = table.getColumnIdentifiers();iter.hasNext();i++) {
                indexMap.put((ColumnIdentifier)(iter.next()),new Integer(i));
            }
        }
        RowIterator iter = table.getRowIterator(false);

        // if there's a where clause, apply it
        if(null != this.getWhere()) {
            iter = new FilteringRowIterator(iter, new RowDecorator(indexMap), this.getWhere());
        }

        RowDecorator dec = new RowDecorator(indexMap);
        int rowcount = 0;
        while(iter.hasNext()) {
            Row oldrow = iter.next();
            dec.setRow(iter.currentIndex(),oldrow);
            Row row = new SimpleRow(oldrow);
            Iterator colids = this.getColumnIterator();
            Iterator values = this.getValueIterator();
            while(colids.hasNext()) {
                ColumnIdentifier colid = (ColumnIdentifier)(colids.next());
                Selectable sel = (Selectable)(values.next()); 
                Object val = sel.evaluate(dec);
                DataType type = db.getTable(colid.getTableName()).getColumn(colid.getName()).getDataType();
                if(!type.accepts(val)) {
                    throw new AxionException("Invalid value " +
                                           (null == val ? "null" : "\"" + val + "\" (" + val.getClass().getName() + ")") +
                                           " for column " + colid + ", expected " + type + ".");
                } else {
                    val = type.convert(val);
                }
                row.set(table.getColumnIndex(colid.getName()),val);
            }
            iter.set(row);
            rowcount++;
        }
        return rowcount;
    }

    /** 
     * Unsupported, use {@link #executeUpdate} instead.
     * @throws UnsupportedOperationException
     */
    public AxionResultSet executeQuery(Database database) throws AxionException {
        throw new UnsupportedOperationException("Use executeUpdate.");
    }

    public boolean execute(Database database) throws AxionException {
        setEffectedRowCount(executeUpdate(database));
        return false;
    }

    protected Iterator getBindVariableIterator() {
        List list = new ArrayList();
        for(Iterator iter = getValueIterator();iter.hasNext();) {
            appendBindVariables((Selectable)iter.next(),list);
        }
        appendBindVariables(getWhere(),list);
        return list.iterator();
    }    

    private void resolve(Database db) throws AxionException {
        if(!_resolved) {
            TableIdentifier[] tables = new TableIdentifier[] { getTable() };
            for(int i=0;i<_cols.size();i++) {
                _cols.set(i,db.resolveSelectable((ColumnIdentifier)(_cols.get(i)),tables));
            }
            for(int i=0;i<_vals.size();i++) {
                _vals.set(i,db.resolveSelectable((Selectable)(_vals.get(i)),tables));
            }
			/*
            for(Iterator iter = getValueIterator();iter.hasNext();) {
                db.resolveSelectable((Selectable)iter.next(),tables);
            }
			*/
            // resolve WHERE part
            db.resolveWhereNode(getWhere(),tables);
            _resolved = true;
        }
    }

    //-------------------------------------------------------------- Attributes

    private TableIdentifier _table = null;
    private List _cols = new ArrayList();
    private List _vals = new ArrayList();
    private WhereNode _where = null;
    private boolean _resolved = false;
}
