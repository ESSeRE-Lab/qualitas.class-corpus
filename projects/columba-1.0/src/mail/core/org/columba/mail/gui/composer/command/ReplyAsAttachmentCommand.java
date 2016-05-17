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
package org.columba.mail.gui.composer.command;

import java.io.InputStream;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.ristretto.message.InputStreamMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeType;

/**
 * Reply to message, while keeping the original message as attachment. In
 * comparison to quoting the bodytext inline.
 * 
 * @author fdietz
 */
public class ReplyAsAttachmentCommand extends ReplyCommand {

	/**
	 * Constructor for ReplyCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ReplyAsAttachmentCommand(ICommandReference reference) {
		super(reference);
	}

	public void execute(IWorkerStatusController worker) throws Exception {
		// create composer model
		model = new ComposerModel();

		// get selected folder
		IMailbox folder = (IMailbox) ((MailFolderCommandReference) getReference())
				.getSourceFolder();

		// get first selected message
		Object[] uids = ((MailFolderCommandReference) getReference()).getUids();

		//      ->set source reference in composermodel
		// when replying this is the original sender's message
		// you selected and replied to
		MailFolderCommandReference ref = new MailFolderCommandReference(folder, uids);
		model.setSourceReference(ref);

		// setup to, references and account
		initHeader(folder, uids);

		// initialize MimeHeader as RFC822-compliant-message
		MimeHeader mimeHeader = new MimeHeader();
		mimeHeader.setMimeType(new MimeType("message", "rfc822"));
		mimeHeader.setContentDescription((String)folder.getAttribute(uids[0],"columba.subject"));

		// add mimepart to model

		InputStream messageSourceStream = folder
				.getMessageSourceStream(uids[0]);
		model.addMimePart(new InputStreamMimePart(mimeHeader,
				messageSourceStream));
		messageSourceStream.close();
	}
}