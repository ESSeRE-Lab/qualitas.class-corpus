/* $Id: SelectCodeCreatorDialog.java 188 2010-01-13 17:41:24Z linus $
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

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.argouml.i18n.Translator;
import org.argouml.language.sql.GeneratorSql;
import org.argouml.language.sql.SqlCodeCreator;
import org.argouml.util.ArgoDialog;

/**
 * Small dialog for selecting a {@link SqlCodeCreator} for creating DDL
 * statements.
 * 
 * @author drahmann
 */
public final class SelectCodeCreatorDialog extends ArgoDialog {
    private JLabel lblSelect;

    private JScrollPane spList;

    private JTable tblCreators;

    private static boolean executed = false;

    private static final Logger LOG = Logger.getLogger(SelectCodeCreatorDialog.class);

    /**
     * Shows the dialog for selecting a code creator for generating proper DDL
     * statements.
     * 
     * @return <code>true</code>, if the user selected OK, <code>false</code>
     *         otherwise.
     */
    public static boolean execute() {

    	// TODO: This appears to be a flag to make sure that we only ask the
    	// user once, but it's not really going to work as expected since the
    	// method will return before the user has had a chance to make a 
    	// selection.
    	if (!executed) {
			SelectCodeCreatorDialog d = new SelectCodeCreatorDialog();
			d.setVisible(true);
		}

        return executed;
    }

    /**
     * Creates a new dialog for selecting a code creator.
     * 
     */
    private SelectCodeCreatorDialog() {
        super(Translator.localize("argouml-sql.select-dialog.title"),
                OK_CANCEL_OPTION, true);

        setPreferredSize(new Dimension(400, 300));

        GridBagLayout l = new GridBagLayout();
        l.rowWeights = new double[] { 0, 1 };
        l.columnWeights = new double[] { 1 };

        JPanel content = new JPanel();
        content.setLayout(l);

        lblSelect = new JLabel(Translator
                .localize("argouml-sql.select-dialog.label-select")
                + ":");
        tblCreators = new JTable(new TableModelCodeCreators());
        spList = new JScrollPane(tblCreators);
        getOkButton().setEnabled(false);

        content.add(lblSelect, GridBagUtils.captionConstraints(0, 0,
                GridBagUtils.LEFT));
        content.add(spList, GridBagUtils.clientAlignConstraints(0, 1));
        setContent(content);
        tblCreators.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (tblCreators.getSelectedRowCount() > 0) {
							getOkButton().setEnabled(true);
						} else {
							getOkButton().setEnabled(false);
						}
					}
        		});
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            try {
                int index = tblCreators.getSelectedRow();
                if (index >= 0 && index < tblCreators.getRowCount()) {
					SqlCodeCreator scc = (SqlCodeCreator) tblCreators
							.getModel().getValueAt(index, -1);
					GeneratorSql.getInstance().setSqlCodeCreator(scc);
					executed = true;
				}
            } catch (Exception exc) {
            	// TODO: We probably shouldn't be doing any special exception
            	// handling here.  Let the default error handlers work.
                LOG.error("Exception", exc);
                String message = Translator
                        .localize("argouml-sql.exceptions.no_sqlcodecreator");
                ExceptionDialog ed = new ExceptionDialog(ProjectBrowser
                        .getInstance(), message, exc);
                ed.setModal(true);
                ed.setVisible(true);
            }
        }
        dispose();
    }
}
