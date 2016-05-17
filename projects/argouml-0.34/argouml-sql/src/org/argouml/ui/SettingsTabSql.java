/* $Id: SettingsTabSql.java 188 2010-01-13 17:41:24Z linus $
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

package org.argouml.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.argouml.application.api.GUISettingsTabInterface;
import org.argouml.i18n.Translator;
import org.argouml.language.sql.DomainMapper;
import org.argouml.language.sql.GeneratorSql;
import org.argouml.language.sql.SqlCodeCreator;

/**
 * Settings tab for this module. Allows configuration of domain mappings.
 *
 * @author drahmann
 */
public class SettingsTabSql extends JPanel implements GUISettingsTabInterface,
        ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
        updateMappings();
        int index = tblFound.getSelectedRow();
        SqlCodeCreator scc = (SqlCodeCreator) tblFound.getModel().getValueAt(
                index, -1);
        TableModel tm = new TableModelDomainMappings(scc.getClass());
        tblDomainMappings.setModel(tm);

        previousSelected = scc;
    }

    private Object[] getElementForRow(int index) {
        return (Object[]) elements.get(index);
    }

    private class TableModelDomainMappings extends AbstractTableModel {
        public TableModelDomainMappings(Class codeCreatorClass) {
            DomainMapper m = GeneratorSql.getInstance().getDomainMapper();
            Map mappings = m.getMappingsFor(codeCreatorClass);

            elements = new ArrayList();
            if (mappings != null) {
                Set entries = mappings.entrySet();
                for (Iterator it = entries.iterator(); it.hasNext();) {
                    Entry entry = (Entry) it.next();
                    elements.add(newElement(entry.getKey(), entry.getValue()));
                }
            }
        }

        private Object[] newElement(Object key, Object value) {
            Object[] element = new Object[2];
            element[0] = key;
            element[1] = value;
            return element;
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex >= 0 && columnIndex <= 1) {
                return String.class;
            }
            return null;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getRowCount() {
            return elements.size() + 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Object result = null;
            if (rowIndex == elements.size()) {
                result = "";
            } else {
                Object[] element = getElementForRow(rowIndex);
                result = element[columnIndex];
            }
            return result;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return rowIndex < getRowCount() && columnIndex >= 0
                    && columnIndex <= 1;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex == elements.size()) {
                if (columnIndex == 0) {
                    elements.add(newElement(aValue, ""));
                } else if (columnIndex == 1) {
                    elements.add(newElement("", aValue));
                }
            } else {
                getElementForRow(rowIndex)[columnIndex] = aValue;
            }
        }
    }

    private String[] columnNames = {
            Translator.localize("argouml-sql.settings.domain"),
            Translator.localize("argouml-sql.settings.datatype") };

    private List elements;

    // private Object[][] elements;

    private boolean initialized;

    private JLabel lblDomainMappings;

    private JLabel lblFound;

    private JTable tblFound;

    private SqlCodeCreator previousSelected;

    private JTable tblDomainMappings;

    /**
     * Create a new instance of this settings tab.
     */
    public SettingsTabSql() {
        super();
        initialized = false;
    }

    public String getTabKey() {
        return "argouml-sql.settings.title";
    }

    public JPanel getTabPanel() {
        if (!initialized) {
            GridBagLayout l = new GridBagLayout();
            l.rowWeights = new double[] { 0, 1, 0, 1 };
            l.columnWeights = new double[] { 0, 1 };

            setLayout(l);

            lblFound = new JLabel(Translator
                    .localize("argouml-sql.settings.found-creators")
                    + ":");
            tblFound = new JTable(new TableModelCodeCreators());
            tblFound.getSelectionModel().addListSelectionListener(this);
            lblDomainMappings = new JLabel(Translator
                    .localize("argouml-sql.settings.mapped-domains")
                    + ":");
            tblDomainMappings = new JTable();

            add(lblFound, GridBagUtils.captionConstraints(0, 0));
            JScrollPane spFound = new JScrollPane(tblFound);
            spFound.setPreferredSize(new Dimension(300, 200));
            add(spFound, GridBagUtils.clientAlignConstraints(0, 1, 2, 1));
            add(lblDomainMappings, GridBagUtils.captionConstraints(0, 2));
            add(new JScrollPane(tblDomainMappings), GridBagUtils
                    .clientAlignConstraints(0, 3, 2, 1));

            initialized = true;
        }

        return this;
    }

    public void handleResetToDefault() {
        GeneratorSql.getInstance().getDomainMapper().load();
    }

    public void handleSettingsTabCancel() {
        GeneratorSql.getInstance().getDomainMapper().load();
    }

    public void handleSettingsTabRefresh() {
        GeneratorSql.getInstance().getDomainMapper().load();
    }

    public void handleSettingsTabSave() {
        updateMappings();
        GeneratorSql.getInstance().getDomainMapper().save();
    }

    private void updateMappings() {
        if (previousSelected != null) {
            DomainMapper m = GeneratorSql.getInstance().getDomainMapper();
            m.clear(previousSelected.getClass());
            for (int i = 0; i < elements.size(); i++) {
                Object[] element = getElementForRow(i);
                String domain = (String) element[0];
                String datatype = (String) element[1];
                m.setDatatype(previousSelected.getClass(), domain, datatype);
            }
        }
    }
}
