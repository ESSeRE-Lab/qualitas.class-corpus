/* $Id: TableModelCodeCreators.java 188 2010-01-13 17:41:24Z linus $
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

// Copyright (c) 2007-2008 The Regents of the University of California. All
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

package org.argouml.ui;

import javax.swing.table.AbstractTableModel;

import org.argouml.i18n.Translator;
import org.argouml.language.sql.GeneratorSql;
import org.argouml.language.sql.SqlCodeCreator;

/**
 * A table model for the SQL code creators with a row per SQL dialect.
 */
class TableModelCodeCreators extends AbstractTableModel {
    private String[] columnNames = {
            Translator.localize("argouml-sql.settings.code-creator-name"),
            Translator.localize("argouml-sql.settings.code-creator-classname") };

    @Override
    public Class<String> getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        return GeneratorSql.getInstance().getSqlCodeCreators().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if (rowIndex >= 0 && rowIndex < getRowCount()) {
			SqlCodeCreator scc = GeneratorSql.getInstance()
					.getSqlCodeCreators().get(rowIndex);
			if (columnIndex == 0) {
				result = scc.getName();
			} else if (columnIndex == 1) {
				result = scc.getClass().getName();
			} else if (columnIndex == -1) {
				result = scc;
			}
		}
        return result;
    }
}
