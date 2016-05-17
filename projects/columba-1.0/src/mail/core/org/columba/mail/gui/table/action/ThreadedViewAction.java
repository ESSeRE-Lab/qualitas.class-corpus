//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.SelectionChangedEvent;
import org.columba.core.gui.action.AbstractSelectableAction;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class ThreadedViewAction extends AbstractSelectableAction implements
		ISelectionListener {
	/**
	 * Constructor for ThreadedViewAction.
	 * 
	 * @param frameMediator
	 */
	public ThreadedViewAction(IFrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"menu_view_viewthreaded"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_view_viewthreaded_tooltip").replaceAll("&",
				""));

		// shortcut key
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.CTRL_MASK));

		((MailFrameMediator) frameMediator).registerTreeSelectionListener(this);

		setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (!(frameMediator instanceof TableViewOwner)) {
			return;
		}

		JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();

		MailFolderCommandReference r = (MailFolderCommandReference) ((MailFrameMediator) frameMediator)
				.getTreeSelection();

		IMailbox folder = (IMailbox) r.getSourceFolder();

		boolean enableThreadedView = item.isSelected();

		/*
		 * folder.getConfiguration().set("property", "enable_threaded_view",
		 * enableThreadedView);
		 */
		updateTable(enableThreadedView);
	}

	protected void updateTable(boolean enableThreadedView) {
		if (!(frameMediator instanceof TableViewOwner)) {
			return;
		}

		((TableViewOwner) frameMediator).getTableController()
				.enableThreadedView(enableThreadedView, true);

	}

	/**
	 * @see org.columba.core.gui.util.ISelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		IMailFolder[] selection = ((TreeSelectionChangedEvent) e).getSelected();

		if (!(selection[0] instanceof IMailbox)) {
			return;
		}

		if (selection.length == 1) {
			XmlElement threadedview = ((MailFrameMediator) getFrameMediator())
					.getFolderOptionsController().getConfigNode(
							(IMailbox) selection[0], "ThreadedViewOptions");
			if (threadedview != null) {
				// *20040510, karlpeder* columns may be null (first time we
				// visit a folder!?)
				String attribute = threadedview.getAttribute("enabled");
				setState(Boolean.valueOf(attribute).booleanValue());
			}
		}
	}
}