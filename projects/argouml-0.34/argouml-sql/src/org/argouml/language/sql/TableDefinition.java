/* $Id: TableDefinition.java 187 2010-01-13 17:41:03Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    drahmann
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data transfer object containing all necessary data for creating a table.
 * 
 * @author drahmann
 */
public class TableDefinition {
    private String name;

    private List columnDefinitions;

    private List primaryKeyFields;

    private Map columnDefNames;

    /**
     * Creates a new TableDefinition.
     */
    public TableDefinition() {
        columnDefinitions = new ArrayList();
        primaryKeyFields = new ArrayList();
        columnDefNames = new HashMap();
    }

    /**
     * @return Returns the columnDefinitions.
     */
    public List getColumnDefinitions() {
        return columnDefinitions;
    }

    /**
     * Adds a {@link ColumnDefinition} to this table's column definitions.
     * 
     * @param cd
     *            The {@link ColumnDefinition} to add.
     */
    public void addColumnDefinition(ColumnDefinition cd) {
        columnDefinitions.add(cd);
        columnDefNames.put(cd.getName(), cd);
    }

    /**
     * Adds a field to the primary key of this table.
     * 
     * @param name
     *            The fieldname.
     */
    public void addPrimaryKeyField(String name) {
        primaryKeyFields.add(name);
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the primaryKeyFields.
     */
    public List getPrimaryKeyFields() {
        return primaryKeyFields;
    }

    /**
     * Returns the {@link ColumnDefinition} with the given name.
     * 
     * @param name
     *            The name of the {@link ColumnDefinition} to return.
     * @return The {@link ColumnDefinition} with the given name. If no such
     *         {@link ColumnDefinition} exists, this method return
     *         <code>null</code>.
     */
    public ColumnDefinition getColumnDefinition(String name) {
        return (ColumnDefinition) columnDefNames.get(name);
    }
}
