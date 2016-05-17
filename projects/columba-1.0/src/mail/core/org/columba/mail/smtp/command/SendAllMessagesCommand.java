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
package org.columba.mail.smtp.command;

import java.util.List;
import java.util.Vector;

import javax.swing.Action;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.command.Worker;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.folder.outbox.OutboxFolder;
import org.columba.mail.folder.outbox.SendListManager;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.smtp.SMTPServer;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 * 
 * Send all messages in folder Outbox
 * 
 */
public class SendAllMessagesCommand extends Command {
	protected SendListManager sendListManager = new SendListManager();

	protected OutboxFolder outboxFolder;

	private Action action;

	public SendAllMessagesCommand(Action action, ICommandReference reference) {
		super(reference);

		this.action = action;
	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		// display status message
		worker.setDisplayText(MailResourceLoader.getString("statusbar",
				"message", "send_message"));

		// get Outbox folder from reference
		outboxFolder = (OutboxFolder) r.getSourceFolder();

		// get UID list of messages
		Object[] uids = outboxFolder.getUids();

		// save every message in a list
		for (int i = 0; i < uids.length; i++) {
			if (outboxFolder.exists(uids[i]) == true) {
				SendableMessage message = outboxFolder
						.getSendableMessage(uids[i]);
				sendListManager.add(message);
			}
		}

		int actAccountUid = -1;
		List sentList = new Vector();

		SMTPServer smtpServer = null;
		IMailbox sentFolder = null;

		// send all messages
		while (sendListManager.hasMoreMessages()) {
			SendableMessage message = sendListManager.getNextMessage();

			// get account information from message
			if (message.getAccountUid() != actAccountUid) {
				actAccountUid = message.getAccountUid();

				AccountItem accountItem = MailConfig.getInstance()
						.getAccountList().uidGet(actAccountUid);

				if (accountItem == null) {
					// use the default account
					accountItem = MailConfig.getInstance().getAccountList()
							.getDefaultAccount();

					if (accountItem == null)
						continue; // skip message if there's no account
					// available to send it
				}

				// Sent folder
				sentFolder = (IMailbox) FolderTreeModel.getInstance()
						.getFolder(
								Integer.parseInt(accountItem
										.getSpecialFoldersItem().get("sent")));

				// open connection to SMTP server
				smtpServer = new SMTPServer(accountItem);
			}
			smtpServer.sendMessage(message, worker);

			sentList.add(message.getHeader().get("columba.uid"));
		}

		// we are done - clear status text with a delay
		// (if this is not done, the initial text will stay in
		// case no messages were sent)
		worker.clearDisplayTextWithDelay();

		// move all successfully send messages to the Sent folder
		if (sentList.size() > 0) {
			moveToSentFolder(sentList, sentFolder);
			sentList.clear();
		}
	}

	/**
	 * 
	 * Move all send messages to the Sent folder
	 * 
	 * @param v
	 *            list of SendableMessage objects
	 * 
	 * @param sentFolder
	 *            Sent folder
	 */
	protected void moveToSentFolder(List v, IMailbox sentFolder) {
		IMailFolderCommandReference r = new MailFolderCommandReference(
				outboxFolder, sentFolder, v.toArray());

		// start move command
		MoveMessageCommand c = new MoveMessageCommand(r);

		CommandProcessor.getInstance().addOp(c);
	}

	/**
	 * @see org.columba.api.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		if (action != null)
			action.setEnabled(true);
	}
}