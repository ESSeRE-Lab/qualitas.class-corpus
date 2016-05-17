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
package org.columba.mail.pop3.command;

import java.io.IOException;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Action;

import org.columba.api.command.IWorkerStatusChangeListener;
import org.columba.api.command.IWorkerStatusController;
import org.columba.api.command.WorkerStatusChangedEvent;
import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.CommandProcessor;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.logging.Logging;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.command.POP3CommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.pop3.POP3Server;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author freddy
 */
public class FetchNewMessagesCommand extends Command {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.pop3.command");

	POP3Server server;

	int totalMessageCount;

	int newMessageCount;

	Action action;

	/**
	 * Constructor for FetchNewMessages.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public FetchNewMessagesCommand(Action action,
			DefaultCommandReference reference) {
		super(reference);

		POP3CommandReference r = (POP3CommandReference) getReference();

		server = r.getServer();

		priority = Command.DAEMON_PRIORITY;

		this.action = action;
	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		POP3CommandReference r = (POP3CommandReference) getReference();

		server = r.getServer();

		// register interest on status bar information
		((StatusObservableImpl) server.getObservable()).setWorker(worker);

		log(MailResourceLoader.getString("statusbar", "message",
				"authenticating"));

		try {
			// login and get # of messages on server
			totalMessageCount = server.getMessageCount();

			if (worker.cancelled())
				throw new CommandCancelledException();

			// synchronize local UID list with server UID list
			List newMessagesUidList = synchronize();

			if (worker.cancelled())
				throw new CommandCancelledException();

			if (Logging.DEBUG) {
				LOG.fine(newMessagesUidList.toString());
			}

			if (worker.cancelled())
				throw new CommandCancelledException();
			// only download new messages
			downloadNewMessages(newMessagesUidList, worker);

			// Delete old message from server if the feature is enabled
			server.cleanUpServer();

			// logout cleanly
			logout();

			// display downloaded message count in statusbar
			if (newMessageCount == 0) {
				log(MailResourceLoader.getString("statusbar", "message",
						"no_new_messages"));
			} else {
				log(MessageFormat.format(MailResourceLoader.getString(
						"statusbar", "message", "fetched_count"),
						new Object[] { new Integer(newMessageCount) }));
			}
		} catch (CommandCancelledException e) {
			server.logout();

			// clear statusbar message
			server.getObservable().clearMessage();
		} catch (Exception e) {
			// clear statusbar message
			server.getObservable().clearMessage();
			throw e;
		}
		/*
		 * catch (IOException e) { String name = e.getClass().getName();
		 * JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
		 * name.substring(name.lastIndexOf(".")), JOptionPane.ERROR_MESSAGE); //
		 * clear statusbar message server.getObservable().clearMessage(); }
		 */
		finally {
			/*
			 * // always enable the menuitem again
			 * r[0].getPOP3ServerController().enableActions(true);
			 */
		}
	}

	protected void log(String message) {
		server.getObservable().setMessage(
				server.getFolderName() + ": " + message);
	}

	public void downloadMessage(Object serverUID, IWorkerStatusController worker)
			throws Exception {
		// server message numbers start with 1
		// whereas List numbers start with 0
		// -> always increase fetch number
		IWorkerStatusChangeListener listener = new IWorkerStatusChangeListener() {
			public void workerStatusChanged(WorkerStatusChangedEvent e) {
				if (e.getSource().cancelled()) {
					try {
						server.dropConnection();
					} catch (IOException e1) {
					}
				}

			}
		};

		// important for cancel
		worker.addWorkerStatusChangeListener(listener);

		// download message
		ColumbaMessage message;
		try {
			message = server.getMessage(serverUID, worker);
		} catch (SocketException e) {
			if (!worker.cancelled())
				throw e;
			else
				throw new CommandCancelledException();
		}
		// not needed anymore
		worker.removeWorkerStatusChangeListener(listener);

		if (message == null) {
			LOG.severe("Message with UID=" + serverUID
					+ " isn't on the server.");

			return;
		}

		message.getHeader().getFlags().setSeen(false);

		// get inbox-folder from pop3-server preferences
		IMailbox inboxFolder = server.getFolder();

		// start command which adds message to folder
		// and calls apply-filter on this specific message
		IMailFolderCommandReference r = new MailFolderCommandReference(
				inboxFolder, message);

		CommandProcessor.getInstance().addOp(new AddPOP3MessageCommand(r));
	}

	protected int calculateTotalSize(List uidList) throws Exception {
		int totalSize = 0;

		Iterator it = uidList.iterator();

		while (it.hasNext()) {
			totalSize += server.getMessageSize(it.next());
		}

		return totalSize;
	}

	public void downloadNewMessages(List newMessagesUIDList,
			IWorkerStatusController worker) throws Exception {
		LOG.fine("need to fetch " + newMessagesUIDList.size() + " messages.");

		int totalSize = calculateTotalSize(newMessagesUIDList);

		worker.setProgressBarMaximum(totalSize);
		worker.setProgressBarValue(0);

		newMessageCount = newMessagesUIDList.size();

		for (int i = 0; i < newMessageCount; i++) {

			if (worker.cancelled())
				throw new CommandCancelledException();

			// which UID should be downloaded next
			Object serverUID = newMessagesUIDList.get(i);

			LOG.fine("fetch message with UID=" + serverUID);

			log(MessageFormat.format(MailResourceLoader.getString("statusbar",
					"message", "fetch_messages"), new Object[] {
					new Integer(i + 1), new Integer(newMessageCount) }));

			int size = server.getMessageSize(serverUID);

			if (server.getAccountItem().getPopItem().getBoolean("enable_limit")) {
				// check if message isn't too big to download
				int maxSize = server.getAccountItem().getPopItem().getInteger(
						"limit");

				// if message-size is bigger skip download of this message
				if (size > maxSize) {
					LOG.fine("skipping download of message, too big");

					continue;
				}
			}

			// now download the message
			downloadMessage(serverUID, worker);

			if (!server.getAccountItem().getPopItem().getBoolean(
					"leave_messages_on_server")) {
				// delete message from server
				server.deleteMessage(serverUID);

				LOG.fine("deleted message with uid=" + serverUID);
			}
		}
	}

	public List synchronize() throws Exception {
		log(MailResourceLoader.getString("statusbar", "message",
				"fetch_uid_list"));

		LOG.fine("synchronize local UID-list with remote UID-list");

		// synchronize local UID-list with server
		List newMessagesUIDList = server.synchronize();

		return newMessagesUIDList;
	}

	public void logout() throws Exception {
		server.logout();

		LOG.fine("logout");

		log(MailResourceLoader.getString("statusbar", "message", "logout"));

		if (newMessageCount == 0) {
			log(MailResourceLoader.getString("statusbar", "message",
					"no_new_messages"));
		}
	}

	/**
	 * @see org.columba.api.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		if (action != null) {
			action.setEnabled(true);
		}
	}
}