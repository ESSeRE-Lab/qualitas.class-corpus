/*
 * $Id: RemountCommand.java,v 1.11 2003/03/27 19:14:08 rwald Exp $
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.Literal;
import org.axiondb.Selectable;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.jdbc.AxionResultSet;

/**
 * A <code>REMOUNT</code> command, which points the database at
 * a new location.
 * 
 * @version $Revision: 1.11 $ $Date: 2003/03/27 19:14:08 $
 * @author Rodney Waldhoff 
 */
public class RemountCommand extends BaseAxionCommand {
    public RemountCommand() {
    }

    public void setDirectory(String dir) {
        _dir = dir;
    }

    public void setDirectory(Literal dir) {
        _dir = dir;
    }

    public Object getDirectory() {
        return _dir;
    }

    public void setTable(TableIdentifier table) {
        _table = table;
    }

    public TableIdentifier getTable() {
        return _table;
    }

    public void setDataFilesOnly(boolean data) {
        _dataFilesOnly = data;
    }

    public boolean getDataFilesOnly() {
        return _dataFilesOnly;
    }

    public boolean execute(Database db) throws AxionException {
        if(null == _table) {
            remountDatabase(db);
        } else {
            remountTable(db);
        }
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
        return "REMOUNT("+_dir+")";
    }

    protected Iterator getBindVariableIterator() {
        List list = new ArrayList();
        if(_dir instanceof Selectable) {
            appendBindVariables((Selectable)_dir,list);
        }
        return list.iterator();
    }    

    private void remountDatabase(Database db) throws AxionException {
        db.remount(new File(getPath()));
    }

    private void remountTable(Database db) throws AxionException {
        Table table = db.getTable(_table);
        if(null == table) {
            throw new AxionException("Table " + _table + " not found.");
        } else {
            table.remount(new File(getPath()),_dataFilesOnly);
        }
    }

    private String getPath() throws AxionException {
        String path = null; 
        if(_dir instanceof Literal) {
            path = (String) ((Literal)_dir).evaluate(null);
        } else {
            path = (String)_dir;
        }
        return path;
    }

    private Object _dir = null; // a literal or String
    private TableIdentifier _table = null;
    private boolean _dataFilesOnly = false;
}
