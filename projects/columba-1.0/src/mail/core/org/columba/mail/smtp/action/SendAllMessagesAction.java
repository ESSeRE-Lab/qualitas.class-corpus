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
package org.columba.mail.smtp.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.connectionstate.ConnectionStateImpl;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.outbox.OutboxFolder;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.smtp.command.SendAllMessagesCommand;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 * 
 * This action is responsible for starting the command which does the actual
 * work. It is visually represented with a menuentry and a toolbar.
 * 
 */
public class SendAllMessagesAction extends AbstractColumbaAction {
	/**
	 * @param controller
	 */
	public SendAllMessagesAction(IFrameMediator controller) {
		super(controller, MailResourceLoader.getString("menu", "mainframe",
				"menu_file_sendunsentmessages"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_file_sendunsentmessages_tooltip")
				.replaceAll("&", ""));

		// icon
		putValue(LARGE_ICON, ImageLoader.getImageIcon("send-24.png"));

		// shortcut key
		// no shortcut here, because F10 conflicts with system accelerator key
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F10,
		// 0));
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		setEnabled(false);
		// check if we are online
		if (ConnectionStateImpl.getInstance().isOnline() == false) {
			// offline -> go online
			ConnectionStateImpl.getInstance().setOnline(true);
		}

		// get outbox folder
		OutboxFolder folder = (OutboxFolder) FolderTreeModel.getInstance()
				.getFolder(103);

		// create referenc
		MailFolderCommandReference r = new MailFolderCommandReference(folder);

		// start command
		SendAllMessagesCommand c = new SendAllMessagesCommand(this, r);

		CommandProcessor.getInstance().addOp(c);
	}
}