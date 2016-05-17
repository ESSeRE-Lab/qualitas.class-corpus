// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.command;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;

/**
 * Mark selected messages with specific variant.
 * <p>
 * 
 * Variant can be: - read/unread - flagged/unflagged - expunged/unexpunged -
 * answered
 * 
 * @author fdietz
 */
public class MarkMessageCommand extends Command {

	public final static int MARK_AS_READ = 1;

	public final static int MARK_AS_UNREAD = -1;

	public final static int MARK_AS_FLAGGED = 2;

	public final static int MARK_AS_UNFLAGGED = -2;

	public final static int MARK_AS_EXPUNGED = 3;

	public final static int MARK_AS_UNEXPUNGED = -3;

	public final static int MARK_AS_ANSWERED = 4;

	public final static int MARK_AS_UNANSWERED = -4;

	public final static int MARK_AS_SPAM = 5;

	public final static int MARK_AS_NOTSPAM = -5;

	public final static int MARK_AS_DRAFT = 6;

	public final static int MARK_AS_NOTDRAFT = -6;
	
	public final static int MARK_AS_RECENT = 7;
	
	public final static int MARK_AS_NOTRECENT = -7;

	private IWorkerStatusController worker;

	/**
	 * Constructor for MarkMessageCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public MarkMessageCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		this.worker = worker;

		/*
		 * // use wrapper class for easier handling of references array adapter =
		 * new FolderCommandAdapter( (MailFolderCommandReference[])
		 * getReferences()); // get array of source references
		 * MailFolderCommandReference[] r = adapter.getSourceFolderReferences();
		 */
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		// get array of message UIDs
		Object[] uids = r.getUids();

		// get source folder
		IMailbox srcFolder = (IMailbox) r.getSourceFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// which kind of mark?
		int markVariant = r.getMarkVariant();

		// saving last selected message to the folder
		srcFolder.setLastSelection(uids[0]);

		// mark message
		srcFolder.markMessage(uids, markVariant);

	}

}