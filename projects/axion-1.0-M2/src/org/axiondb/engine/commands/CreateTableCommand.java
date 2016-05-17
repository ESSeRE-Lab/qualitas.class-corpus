/*
 * $Id: CreateTableCommand.java,v 1.13 2002/12/16 22:18:30 rwald Exp $
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
import java.util.Iterator;
import java.util.List;

import org.axiondb.AxionCommand;
import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.DataType;
import org.axiondb.Database;
import org.axiondb.Table;
import org.axiondb.jdbc.AxionResultSet;

/**
 * A <code>CREATE[<i>TYPE</i>] TABLE</code> command.
 * @version $Revision: 1.13 $ $Date: 2002/12/16 22:18:30 $
 * @author Chuck Burdick 
 * @author James Strachan
 * @author Rodney Waldhoff
 */
public class CreateTableCommand extends BaseAxionCommand {
    String _tableName = null;
    String _type = null;
    List _columnNames = new ArrayList();
    List _dataTypes = new ArrayList();
    List _childCommands = new ArrayList();

    public CreateTableCommand() {
    }

    public CreateTableCommand(String tableName) {
        _tableName = tableName;
    }

    public List getColumnNames() {
        return _columnNames;
    }
    
    public List getDataTypes() {
        return _dataTypes;
    }
    
    public void addColumn(String name, String datatypename) {
        _columnNames.add(name);
        _dataTypes.add(datatypename);
    }

    public void addChildCommand(AxionCommand cmd) {
        _childCommands.add(cmd);
    }
    
    public int getChildCommandCount() {
        return _childCommands.size();
    }
    
    public AxionCommand getChildCommand(int i) {
        return (AxionCommand)(_childCommands.get(i));
    }

    public void setType(String type) {
        _type = type;
    }

    public String getType() {
        return _type;
    }

    public void setName(String name) {
        _tableName = name;
    }

    public String getName() {
        return _tableName;
    }

    public boolean execute(Database db) throws AxionException {
        assertNotReadOnly(db);
        Column[] columns = new Column[_columnNames.size()];                
        for(int i=0;i<_columnNames.size();i++) {
            DataType type = db.getDataType((String)_dataTypes.get(i));
            if(null == type) {
                try {
                    type = (DataType)(Class.forName((String)(_dataTypes.get(i))).newInstance());
                } catch(Exception e) {
                    type = null;
                }
            }
            if(null == type) {
                throw new AxionException("Type " + _dataTypes.get(i) + " not recognized.");
            }
            Column col = new Column((String)_columnNames.get(i),type);
            columns[i] = col;
        }
        Table table = db.getTableFactory(_type).createTable(db,_tableName);
        for(int i=0;i<columns.length;i++) {
            table.addColumn(columns[i]);
        }
        db.addTable(table);                
        for(Iterator iter = _childCommands.iterator(); iter.hasNext(); ) {
            AxionCommand cmd = (AxionCommand)(iter.next());
            cmd.execute(db);
        }
        return false;
    }

    /** Unsupported */
    public AxionResultSet executeQuery(Database database) throws AxionException  {
        throw new UnsupportedOperationException("Use execute.");
    }

    public int executeUpdate(Database database) throws AxionException  {
        execute(database);
        return 0;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("CREATE TABLE ");
        buf.append(_tableName);
        buf.append(" (");
        for(int i=0;i<_columnNames.size();i++) {
            if(i != 0) {
                buf.append(", ");
            }
            buf.append(_columnNames.get(i));
            buf.append(" ");
            buf.append(_dataTypes.get(i));
        }
        for(Iterator iter = _childCommands.iterator(); iter.hasNext(); ) {
            buf.append(", ");
            buf.append(iter.next().toString());
        }
        buf.append(")");
        return buf.toString();
    }

}
