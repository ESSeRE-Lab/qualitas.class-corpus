/*
 * $Id: InsertCommand.java,v 1.20 2003/07/11 15:05:44 rwald Exp $
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
import org.axiondb.Selectable;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.engine.SimpleRow;
import org.axiondb.jdbc.AxionResultSet;

/**
 * An <tt>INSERT</tt> statement.
 *
 * @version $Revision: 1.20 $ $Date: 2003/07/11 15:05:44 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class InsertCommand extends BaseAxionCommand {

    //------------------------------------------------------------ Constructors
    
    /**
     * @param table The table in which to insert
     */
    public InsertCommand(TableIdentifier table) {
        this(table,(List)null,(List)null);
    }

    /**
     * @param table The table in which to insert
     * @param column a column to insert
     * @param values a value to insert in the corresponding column
     */
    public InsertCommand(TableIdentifier table, ColumnIdentifier column, Selectable value) {
        this(table);
        addColumn(column);
        addValue(value);
    }

    /**
     * @param table The table in which to insert
     * @param columns List of {@link ColumnIdentifier ColumnIdentifiers}, which may be <code>null</code>
     * @param values List of {@link Object Objects}, which may be <code>null</code>
     * @throws InvalidArgumentException if <code>columns.size() &gt; 0 &amp;&amp; columns.size() != values.size()</code>
     */
    public InsertCommand(TableIdentifier table, List columns, List values) {
        _table = table;
        _cols = (null == columns ? new ArrayList() : columns);
        _vals = (null == values ? new ArrayList() : values);
        if(_cols.size() > 0 && _cols.size() != _vals.size()) {
            throw new IllegalArgumentException("When any columns are specified, the number of columns and values must match.");
        }
    }

    //---------------------------------------------------------- Public Methods
    
    /** 
     * @throws IllegalStateException if I've already been resolved
     */
    public void addColumn(ColumnIdentifier col) {
        if(_resolved) { throw new IllegalStateException("Already resolved."); }
        _cols.add(col);
    }

    /** 
     * @throws IllegalStateException if I've already been resolved
     */
    public void addValue(Selectable val) {
        if(_resolved) { throw new IllegalArgumentException("Already resolved."); }
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

    public TableIdentifier getTable() {
        return _table;
    }

    public int executeUpdate(org.axiondb.Database db) throws AxionException {
        assertNotReadOnly(db);
        resolve(db);
        Table table = db.getTable(this.getTable());
        if(null == table) { 
            throw new AxionException("Table " + getTable() + " not found.");
        }
        Row row = new SimpleRow(table.getColumnCount());

        RowDecorator dec = buildRowDecorator(table);
        
        Iterator cols = getSelectableIterator(table);
        Iterator vals = this.getValueIterator();        
        while(vals.hasNext()) {
            ColumnIdentifier colid = (ColumnIdentifier)(cols.next());
            Object val = vals.next();
            if(val instanceof Selectable) {
                dec.setRow(row);
                val = ((Selectable)val).evaluate(dec);
            }
            DataType type = table.getColumn(colid.getName()).getDataType();
            if(!type.accepts(val)) {
                throw new AxionException("Invalid value " +
                                       (null == val ? "null" : "\"" + val + "\" (" + val.getClass().getName() + ")") +
                                       " for column " + colid + ", expected " + type + ".");
            } else {
                val = type.convert(val);
            }
            row.set(table.getColumnIndex(colid.getName()),val);
        }
        table.addRow(row);
        return 1;
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
        return list.iterator();
    }    

    private void resolve(Database db) throws AxionException {
        if(!_resolved) {
            TableIdentifier[] tables = new TableIdentifier[] { getTable() };
            for(int i=0;i<_cols.size();i++) {
                _cols.set(i,db.resolveSelectable((Selectable)_cols.get(i),tables));
            }
            for(int i=0;i<_vals.size();i++) {
                _vals.set(i,db.resolveSelectable((Selectable)_vals.get(i),tables));
            }
            _resolved = true;
        }
    }

    private RowDecorator buildRowDecorator(Table table) {
        RowDecorator dec = null;
        {
            Map map = new HashMap();
            int i=0;
            for(Iterator iter = getSelectableIterator(table);iter.hasNext();i++) {
                map.put(iter.next(),new Integer(i));               
            }
            dec = new RowDecorator(map);
        }
        return dec;
    }

    private Iterator getSelectableIterator(Table table) {
        Iterator cols;
        if(_cols.size() == 0) {
            cols = table.getColumnIdentifiers();
        } else {
            cols = getColumnIterator();
        }
        return cols;
    }

    //-------------------------------------------------------------- Attributes
    
    private boolean _resolved = false;
    private TableIdentifier _table = null;
    private List _cols = null;
    private List _vals = null;

}
