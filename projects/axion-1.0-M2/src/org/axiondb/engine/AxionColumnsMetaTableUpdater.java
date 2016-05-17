/*
 * $Id: AxionColumnsMetaTableUpdater.java,v 1.4 2003/03/27 19:14:03 rwald Exp $
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

import java.sql.DatabaseMetaData;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionCommand;
import org.axiondb.AxionException;
import org.axiondb.BinaryBranchWhereNode;
import org.axiondb.Column;
import org.axiondb.ColumnIdentifier;
import org.axiondb.ComparisonOperator;
import org.axiondb.Constraint;
import org.axiondb.Database;
import org.axiondb.LeafWhereNode;
import org.axiondb.Literal;
import org.axiondb.Row;
import org.axiondb.SelectableBasedConstraint;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.WhereNode;
import org.axiondb.constraints.NotNullConstraint;
import org.axiondb.constraints.PrimaryKeyConstraint;
import org.axiondb.engine.commands.DeleteCommand;
import org.axiondb.engine.commands.UpdateCommand;
import org.axiondb.event.BaseDatabaseModificationListener;
import org.axiondb.event.ColumnEvent;
import org.axiondb.event.ConstraintEvent;
import org.axiondb.event.DatabaseModificationListener;
import org.axiondb.event.DatabaseModifiedEvent;
import org.axiondb.event.RowEvent;
import org.axiondb.event.TableModificationListener;

/**
 * Updates the <code>AXION_TABLES</code> meta table
 * 
 * @version $Revision: 1.4 $ $Date: 2003/03/27 19:14:03 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class AxionColumnsMetaTableUpdater extends BaseDatabaseModificationListener implements DatabaseModificationListener, TableModificationListener {
    private static Log _log = LogFactory.getLog(AxionColumnsMetaTableUpdater.class);
    private Database _db = null;

    public AxionColumnsMetaTableUpdater(Database db) {
        _db = db;
    }

    public void tableAdded(DatabaseModifiedEvent e) {
        try {
            for(int i = 0; i < e.getTable().getColumnCount(); i++) {
                Column col = e.getTable().getColumn(i);
                Row row = createRowForColumnAdded(e.getTable(), col);
                _db.getTable("AXION_COLUMNS").addRow(row);
            }
        } catch (AxionException ex) {
            _log.error("Unable to mention table in system tables", ex);
        }
    }

    public void tableDropped(DatabaseModifiedEvent e) {
        ColumnIdentifier col = new ColumnIdentifier("TABLE_NAME");
        Literal lit = new Literal(e.getTable().getName());
        ComparisonOperator op = ComparisonOperator.EQUAL;
        WhereNode where = new LeafWhereNode(col, op, lit);
        AxionCommand cmd = new DeleteCommand("AXION_COLUMNS", where);
        try {
            cmd.execute(_db);
        } catch (AxionException ex) {
            _log.error("Unable to remove mention of table in system tables", ex);
        }
    }

    //===================================== TableModificationListener Interface

    public void columnAdded(ColumnEvent e) throws AxionException {
        Row row = createRowForColumnAdded(e.getTable(), e.getColumn());
        _db.getTable("AXION_COLUMNS").addRow(row);
    }

    public void rowInserted(RowEvent event) throws AxionException {
    }

    public void rowDeleted(RowEvent event) throws AxionException {
    }

    public void rowUpdated(RowEvent event) throws AxionException {
    }

    public void constraintAdded(ConstraintEvent event) throws AxionException {
    }

    public void constraintRemoved(ConstraintEvent event) throws AxionException {
    }

    public void updateNullableStatus(ConstraintEvent event, boolean changeNullableTo) {
        Constraint c = event.getConstraint();
        if(c instanceof NotNullConstraint || c instanceof PrimaryKeyConstraint) {
            SelectableBasedConstraint nn = (SelectableBasedConstraint)c;
            for(int i = 0; i < nn.getSelectableCount(); i++) {
                try {
                    AxionCommand cmd = createUpdateNullableCmd(
                        event.getTable(),
                        nn.getSelectable(i).getLabel(),
                        changeNullableTo);
                    cmd.execute(_db);
                } catch (AxionException ex) {
                    _log.error("Unable to mark nullable status in system tables", ex);
                }
            }
        }
    }

    protected Row createRowForColumnAdded(Table t, Column col) throws AxionException {
        boolean isnullable = isNullable(t,col.getName());
        Integer nullableInt = new Integer(isnullable ? 
                                          DatabaseMetaData.columnNullable :
                                          DatabaseMetaData.columnNoNulls);
        String nullableString = isnullable ? "YES" : "NO";
        Short typeVal = new Short((short)col.getDataType().getJdbcType());
        String typeName = col.getDataType().getClass().getName();
        int ord = t.getColumnIndex(col.getName());

        SimpleRow row = new SimpleRow(18);
        row.set(0, "");                    // table_cat
        row.set(1, "");                    // table schem
        row.set(2, t.getName());           // table_name
        row.set(3, col.getName());         // column_name
        row.set(4, typeVal);               // data_type
        row.set(5, typeName);              // type_name
        row.set(6, null);                  // column_size
        row.set(7, null);                  // buffer_length (unused)
        row.set(8, null);                  // decimal_digits
        row.set(9, new Integer(10));       // num_prec_radix
        row.set(10, nullableInt);          // nullable
        row.set(11, null);                 // remarks
        row.set(12, null);                 // column_def
        row.set(13, null);                 // sql_data_type (unused)
        row.set(14, null);                 // sql_datetime_sub (unused)
        row.set(15, null);                 // char_octet_length
        row.set(16, new Integer(ord+1));   // ordinal_position
        row.set(17, nullableString);       // is_nullable
        return row;
    }

    private UpdateCommand createUpdateNullableCmd(Table t, String colName, boolean isnullable) {
        Integer nullableInt = new Integer(isnullable ? 
                                          DatabaseMetaData.columnNullable :
                                          DatabaseMetaData.columnNoNulls);
        String nullableString = isnullable ? "YES" : "NO";

        WhereNode tableMatch = null;
        {
            ColumnIdentifier col = new ColumnIdentifier("TABLE_NAME");
            Literal lit = new Literal(t.getName());
            ComparisonOperator op = ComparisonOperator.EQUAL;
            tableMatch = new LeafWhereNode(col, op, lit);
        }
        
        WhereNode colMatch = null;
        {
            ColumnIdentifier col = new ColumnIdentifier("COLUMN_NAME");
            Literal lit = new Literal(colName.toUpperCase());
            ComparisonOperator op = ComparisonOperator.EQUAL;
            colMatch = new LeafWhereNode(col, op, lit);
        }

        BinaryBranchWhereNode where = new BinaryBranchWhereNode();
        where.setIsAnd(true);
        where.setLeft(tableMatch);
        where.setRight(colMatch);

        UpdateCommand cmd = new UpdateCommand();
        cmd.setTable(new TableIdentifier("AXION_COLUMNS"));
        cmd.addColumn(new ColumnIdentifier("NULLABLE"));
        cmd.addValue(new Literal(nullableInt));
        cmd.addColumn(new ColumnIdentifier("IS_NULLABLE"));
        cmd.addValue(new Literal(nullableString));
        cmd.setWhere(where);
        return cmd;
    }

    private boolean isNullable(Table table, String column) {
        // XXX FIX ME XXX: this is a little hack       
        for(Iterator iter = table.getConstraints(); iter.hasNext(); ) {
            Constraint c = (Constraint)iter.next();
            if(c instanceof NotNullConstraint || c instanceof PrimaryKeyConstraint) {
                SelectableBasedConstraint nn = (SelectableBasedConstraint)c;
                for(int i=0;i<nn.getSelectableCount();i++) {
                    if(nn.getSelectable(i).getLabel().equalsIgnoreCase(column)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }    
}
