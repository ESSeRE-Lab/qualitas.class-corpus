/* $Id: ForeignKeyDefinition.java 187 2010-01-13 17:41:03Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tfmorris
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.language.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Transfer object holding all necessary data for a foreign key definition.
 * 
 * @author drahmann
 */
public class ForeignKeyDefinition {
    private TableDefinition table;

    private List<ColumnDefinition> columns;

    private TableDefinition referencesTable;

    private List<ColumnDefinition> referencesColumns;

    private String foreignKeyName;

    private int lower;

    private int upper;

    private int referencesLower;

    private int referencesUpper;

    /**
     * Creates a new ForeignKeyDefinition.
     */
    public ForeignKeyDefinition() {
        columns = new ArrayList<ColumnDefinition>();
        referencesColumns = new ArrayList<ColumnDefinition>();
    }

    /**
     * @return A List of all column names.
     */
    public List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<String>();
        for (Iterator<ColumnDefinition> it = columns.iterator(); it.hasNext();) {
            ColumnDefinition cd = it.next();
            columnNames.add(cd.getName());
        }
        return columnNames;
    }

    /**
     * Add a column definition.
     * 
     * @param colDef
     *            The column definition to add.
     */
    public void addColumnDefinition(ColumnDefinition colDef) {
        columns.add(colDef);
    }

    /**
     * @return A List with all referenced column definitions.
     */
    public List<String> getReferencesColumnNames() {
        List<String> referencesColumnNames = new ArrayList<String>();
        for (ColumnDefinition cd : referencesColumns) {
            referencesColumnNames.add(cd.getName());
        }
        return referencesColumnNames;
    }

    /**
     * Add a referenced column definition.
     * 
     * @param colDef
     *            The column definition to add.
     */
    public void addReferencesColumn(ColumnDefinition colDef) {
        referencesColumns.add(colDef);
    }

    /**
     * @return Returns the columns.
     */
    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    /**
     * @return Returns the foreignKeyName.
     */
    public String getForeignKeyName() {
        return foreignKeyName;
    }

    /**
     * @return Returns the lower.
     */
    public int getLower() {
        return lower;
    }

    /**
     * @return Returns the referencesColumns.
     */
    public List<ColumnDefinition> getReferencesColumns() {
        return referencesColumns;
    }

    /**
     * @return Returns the referencesLower.
     */
    public int getReferencesLower() {
        return referencesLower;
    }

    /**
     * @return Returns the referencesTable.
     */
    public TableDefinition getReferencesTable() {
        return referencesTable;
    }

    /**
     * @return Returns the referencesUpper.
     */
    public int getReferencesUpper() {
        return referencesUpper;
    }

    /**
     * @return Returns the table.
     */
    public TableDefinition getTable() {
        return table;
    }

    /**
     * @return Returns the upper.
     */
    public int getUpper() {
        return upper;
    }

    /**
     * @return The name of the referenced table.
     */
    public String getReferencesTableName() {
        return referencesTable.getName();
    }

    /**
     * @return The name of the table.
     */
    public String getTableName() {
        return table.getName();
    }

    /**
     * @param foreignKeyName
     *            The foreignKeyName to set.
     */
    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    /**
     * @param referencesTable
     *            The referencesTable to set.
     */
    public void setReferencesTable(TableDefinition referencesTable) {
        this.referencesTable = referencesTable;
    }

    /**
     * @param table
     *            The table to set.
     */
    public void setTable(TableDefinition table) {
        this.table = table;
    }

    /**
     * @param lower
     *            The lower to set.
     */
    public void setLower(int lower) {
        this.lower = lower;
    }

    /**
     * @param referencesLower
     *            The referencesLower to set.
     */
    public void setReferencesLower(int referencesLower) {
        this.referencesLower = referencesLower;
    }

    /**
     * @param referencesUpper
     *            The referencesUpper to set.
     */
    public void setReferencesUpper(int referencesUpper) {
        this.referencesUpper = referencesUpper;
    }

    /**
     * @param upper
     *            The upper to set.
     */
    public void setUpper(int upper) {
        this.upper = upper;
    }
}
