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
package org.columba.mail.folder.command;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;

/**
 * Copy a set of messages from a source to a destination folder.
 * <p>
 * A dialog asks the user the destination folder.
 * 
 * @author fdietz
 *  
 */
public class CopyMessageCommand extends Command {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.command");

	protected IMailbox destFolder;

	protected IMailFolderCommandReference r;
	
	/**
	 * Constructor for CopyMessageCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public CopyMessageCommand(ICommandReference reference) {
		super(reference);
	}

	protected void doExecute(IWorkerStatusController worker,
			String statusMessage, String errorRetryMessage,
			String errorIgnoreMessage, String errorCopyMessage,
			String errorTitle, String canceledMessage) throws Exception {
		// get references
		 r = (IMailFolderCommandReference) getReference();

		// get destination foldedr
		destFolder = (IMailbox) r.getDestinationFolder();

		Object[] uids = r.getUids();

		// get source folder
		IMailbox srcFolder = (IMailbox) r.getSourceFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// setting lastSelection for srcFolder to null
		srcFolder.setLastSelection(null);

		LOG.fine("src=" + srcFolder + " dest=" + destFolder);

		// update status message
		worker.setDisplayText(MessageFormat.format(MailResourceLoader
				.getString("statusbar", "message", statusMessage),
				new Object[] { destFolder.getName() }));

		// initialize progress bar with total number of messages
		worker.setProgressBarMaximum(uids.length);

		if (srcFolder.getRootFolder().equals(destFolder.getRootFolder())) {
			// folders have same root folder
			// -> for example: two IMAP folders on the same server
			// -----> this means we use server-side copying which
			// -----> is much faster than using inputstreams here
			//
			// also used for local folders, which saves some parsing work
			srcFolder.innerCopy(destFolder, uids);
		} else {
			// two different root folders
			// -> get inputstream from source-folder and add it to
			// -> destination-folder as inputstream
			// -----> moving of raw message source
			// (works also for copying from local to IMAP folders, etc.
			for (int j = 0; (j < uids.length) && !worker.cancelled(); j++) {
				if (!srcFolder.exists(uids[j])) {
					continue;
				}

				try {
					// add source to destination folder
					Attributes attributes = srcFolder.getAttributes(uids[j]);
					Flags flags = srcFolder.getFlags(uids[j]);
					InputStream messageSourceStream = srcFolder
							.getMessageSourceStream(uids[j]);
					destFolder.addMessage(messageSourceStream, attributes,
							flags);
					messageSourceStream.close();
				} catch (IOException ioe) {
					String[] options = new String[] {
							MailResourceLoader.getString("statusbar",
									"message", errorRetryMessage),
							MailResourceLoader.getString("statusbar",
									"message", errorIgnoreMessage),
							MailResourceLoader
									.getString("", "global", "cancel") };

					int result = JOptionPane.showOptionDialog(null,
							MailResourceLoader.getString("statusbar",
									"message", errorCopyMessage),
							MailResourceLoader.getString("statusbar",
									"message", errorTitle),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.ERROR_MESSAGE, null, options,
							options[0]);
					switch (result) {
					case JOptionPane.YES_OPTION:

						//retry copy
						j--;

						break;

					case JOptionPane.CANCEL_OPTION:
						worker.cancel();

					default:

						continue;
					}
				}

				// update progress bar
				worker.setProgressBarValue(j);
			}
		}

		//reset progress bar
		worker.setProgressBarValue(0);

		if (worker.cancelled()) {
			worker.setDisplayText(MailResourceLoader.getString("statusbar",
					"message", canceledMessage));
		} else {
			// We are done - clear the status message with a delay
			worker.clearDisplayTextWithDelay();
		}

	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		doExecute(worker, "copy_messages", "err_copy_messages_retry",
				"err_copy_messages_ignore", "err_copy_messages_msg",
				"err_copy_messages_title", "copy_messages_cancelled");
	}
}