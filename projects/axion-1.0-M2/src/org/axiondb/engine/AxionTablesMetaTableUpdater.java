/*
 * $Id: AxionTablesMetaTableUpdater.java,v 1.3 2003/03/27 19:14:03 rwald Exp $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionCommand;
import org.axiondb.AxionException;
import org.axiondb.ColumnIdentifier;
import org.axiondb.ComparisonOperator;
import org.axiondb.Database;
import org.axiondb.LeafWhereNode;
import org.axiondb.Literal;
import org.axiondb.Row;
import org.axiondb.Table;
import org.axiondb.WhereNode;
import org.axiondb.engine.commands.DeleteCommand;
import org.axiondb.event.BaseDatabaseModificationListener;
import org.axiondb.event.DatabaseModificationListener;
import org.axiondb.event.DatabaseModifiedEvent;

/**
 * Updates the <code>AXION_TABLES</code> meta table
 * 
 * @version $Revision: 1.3 $ $Date: 2003/03/27 19:14:03 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class AxionTablesMetaTableUpdater extends BaseDatabaseModificationListener implements DatabaseModificationListener {
    private static Log _log = LogFactory.getLog(AxionTablesMetaTableUpdater.class);
    private Database _db = null;

    public AxionTablesMetaTableUpdater(Database db) {
        _db = db;
    }

    public void tableAdded(DatabaseModifiedEvent e) {
        Row row = createRowForAddedTable(e.getTable());
        try {
            _db.getTable("AXION_TABLES").addRow(row);
        } catch (AxionException ex) {
            _log.error("Unable to mention table in system tables", ex);
        }
    }

    public void tableDropped(DatabaseModifiedEvent e) {
        ColumnIdentifier col = new ColumnIdentifier("TABLE_NAME");
        Literal lit = new Literal(e.getTable().getName());
        ComparisonOperator op = ComparisonOperator.EQUAL;
        WhereNode where = new LeafWhereNode(col, op, lit);
        AxionCommand cmd = new DeleteCommand("AXION_TABLES", where);
        try {
            cmd.execute(_db);
        } catch (AxionException ex) {
            _log.error("Unable to remove mention of table in system tables", ex);
        }
    }

    protected Row createRowForAddedTable(Table table) {
        String tableName = table.getName();
        String tableType = table.getType();
        SimpleRow row = new SimpleRow(5);
        row.set(0,"");                          // table_cat
        row.set(1,"");                          // table_schem
        row.set(2, tableName);                  // table_name
        row.set(3, tableType);                  // table_type
        row.set(4,null);                        // remarks
        return row;
    }
}
