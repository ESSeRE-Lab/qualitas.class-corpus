/*
 * $Id: CreateIndexCommand.java,v 1.10 2002/12/16 22:18:30 rwald Exp $
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

package org.axiondb.engine.commands;

import java.util.ArrayList;
import java.util.List;

import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.ColumnIdentifier;
import org.axiondb.Database;
import org.axiondb.Index;
import org.axiondb.IndexFactory;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.jdbc.AxionResultSet;

/**
 * A <code>CREATE [UNIQUE] [<i>TYPE</i>] INDEX</code> command.
 * @version $Revision: 1.10 $ $Date: 2002/12/16 22:18:30 $
 * @author Rodney Waldhoff 
 */
public class CreateIndexCommand extends BaseAxionCommand {
    public CreateIndexCommand() {
    }
   
    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public TableIdentifier getTable() {
        return _table;
    }

    public void setTable(TableIdentifier table) {
        _table = table;
    }

    public void setTable(String tableName) {
        _table = new TableIdentifier(tableName);
    }

    public void addColumn(String name) {
        _columns.add(new ColumnIdentifier(name));
    }

    public void addColumn(ColumnIdentifier col) {
        _columns.add(col);
    }

    public ColumnIdentifier getColumn(int i) {
        return (ColumnIdentifier)(_columns.get(i));
    }

    public int getColumnCount() {
        return _columns.size();
    }

    public void setUnique(boolean unique) {
        _unique = unique;
    }

    public boolean isUnique() {
        return _unique;
    }

    public void setType(String type) {
        _type = type;
    }

    public String getType() {
        return _type;
    }

    public boolean execute(Database db) throws AxionException {
        assertNotReadOnly(db);
        if(getColumnCount() > 1) {
            throw new AxionException("Multi-column indices are not supported yet."); 
        }
        Table table = db.getTable(getTable());
        if(null == table) { 
            throw new AxionException("Table " + getTable() + " not found.");
        }
        String columnName = getColumn(0).getName();
        Column column = table.getColumn(columnName);

        Index index = null;
        IndexFactory factory = db.getIndexFactory(null == _type ? "default" : _type);
        if(null == factory) {
            throw new AxionException("Index type \"" + _type + "\" not recognized.");
        }
        index = factory.makeNewInstance(_name,column,_unique);
        table.populateIndex(index);
        table.addIndex(index);
        return false;
    }

    /** Unsupported */
    public AxionResultSet executeQuery(Database database) throws AxionException {
        throw new UnsupportedOperationException("Use execute.");
    }

    public int executeUpdate(Database database) throws AxionException {
        execute(database);
        return 0;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("CREATE ");
        if(_unique) {
            buf.append("unique ");
        }
        if(null != _type) {
            buf.append(_type).append(" ");
        }
        buf.append("INDEX ON ");
        buf.append(_table);
        buf.append(" (");
        for(int i=0;i<_columns.size();i++) {
            if(i != 0) {
                buf.append(", ");
            }
            buf.append(_columns.get(i));
        }
        buf.append(")");
        return buf.toString();
    }

    private boolean _unique = false;
    private String _type = null;
    private String _name = null;
    private TableIdentifier _table = null;
    private List _columns = new ArrayList();
}
