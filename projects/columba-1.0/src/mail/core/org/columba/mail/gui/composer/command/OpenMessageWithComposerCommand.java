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

package org.columba.mail.gui.composer.command;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.List;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.gui.frame.DefaultContainer;
import org.columba.core.io.StreamUtils;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.io.TempSourceFactory;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.Message;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.parser.MessageParser;

/**
 * Open message in composer.
 * 
 * @author fdietz
 */
public class OpenMessageWithComposerCommand extends Command {
	protected ComposerController controller;
	protected ComposerModel model;
	protected IMailbox folder;
	protected Object uid;

	/**
	 * Constructor for OpenMessageInComposerCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public OpenMessageWithComposerCommand(ICommandReference reference) {
		super(reference);
	}

	public void updateGUI() throws Exception {
		// open composer frame
		controller = new ComposerController();
		new DefaultContainer(controller);

		// apply model
		controller.setComposerModel(model);

		// model->view update
		controller.updateComponents(true);
	}

	public void execute(IWorkerStatusController worker) throws Exception {
		model = new ComposerModel();

		// get selected folder
		folder = (IMailbox) ((MailFolderCommandReference) getReference())
				.getSourceFolder();

		// get selected messages
		Object[] uids = ((MailFolderCommandReference) getReference()).getUids();

		// we only need the first message
		uid = uids[0];

		//TODO (@author fdietz): keep track of progress here

		InputStream messageSourceStream = folder.getMessageSourceStream(uid);
		Source tempSource = TempSourceFactory.createTempSource(
				messageSourceStream, -1);
		messageSourceStream.close();

		Message message = MessageParser.parse(tempSource);

		initHeader(message);
		
		// get message flags
		Flags flags = folder.getFlags(uid);
		model.getMessage().getHeader().setFlags(flags);

		// select the account this mail was received from
		Integer accountUid = (Integer) folder.getAttribute(uids[0],
				"columba.accountuid");
		AccountItem accountItem = MessageBuilderHelper
				.getAccountItem(accountUid);
		model.setAccountItem(accountItem);

		XmlElement html = MailConfig.getInstance().getMainFrameOptionsConfig()
				.getRoot().getElement("/options/html");

		boolean preferHtml = Boolean.valueOf(html.getAttribute("prefer"))
				.booleanValue();

		initBody(message, preferHtml);
		
		// store reference to source message
		model.setSourceReference(new MailFolderCommandReference(folder, new Object[] {uid}));
	}

	private void initBody(Message message, boolean preferHtml) throws Exception {
		MimeTree mimeTree = message.getMimePartTree();

		// Which Bodypart shall be shown? (html/plain)
		LocalMimePart bodyPart = null;

		if (preferHtml) {
			bodyPart = (LocalMimePart) mimeTree.getFirstTextPart("html");
		} else {
			bodyPart = (LocalMimePart) mimeTree.getFirstTextPart("plain");
		}

		if (bodyPart != null) {
			MimeHeader header = bodyPart.getHeader();
			if (header.getMimeType().getSubtype().equals("html")) {
				// html
				model.setHtml(true);
			} else {
				model.setHtml(false);
			}
			InputStream bodyStream = folder.getMimePartBodyStream(uid, bodyPart.getAddress());
			
	        // Do decoding stuff
	        switch( header.getContentTransferEncoding() ) {
	        	case MimeHeader.QUOTED_PRINTABLE : {
	        		bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);
	        		break;
	        	}
	        	
	        	case MimeHeader.BASE64 : {
	        		bodyStream = new Base64DecoderInputStream(bodyStream);
	        	}
	        }
	        String charset = header.getContentParameter("charset");
	        if (charset != null) {
	        	try {
	        		bodyStream = new CharsetDecoderInputStream(bodyStream, Charset.forName(charset));
	        		model.setCharset(Charset.forName(charset));
	        	} catch( UnsupportedCharsetException e ) {
	        		// Stick with the default charset
	        	}
	        }

			model.setBodyText(StreamUtils.readCharacterStream(
					bodyStream)
					.toString());

		}

		initAttachments(mimeTree, bodyPart);
	}

	private void initHeader(Message message) {
		Header header = message.getHeader();

		BasicHeader rfcHeader = new BasicHeader(header);

		// set subject
		model.setSubject(rfcHeader.getSubject());

		model.setTo(rfcHeader.getTo());
		
		model.setCc(rfcHeader.getCc());
		model.setBcc(rfcHeader.getBcc());

		// copy every headerfield the original message contains
		model.setHeader(header);
	}

	private void initAttachments(MimeTree collection, MimePart bodyPart) {
		// Get all MimeParts
		List displayedMimeParts = collection.getAllLeafs();

		if (bodyPart != null) {
			MimePart bodyParent = bodyPart.getParent();

			// remove the bodypart from the mimeparts
			// that are added to the attachment viewer
			if (bodyParent != null && bodyParent.getHeader().getMimeType().getSubtype().equals(
			"alternative")) {
				List bodyParts = bodyParent.getChilds();
				displayedMimeParts.removeAll(bodyParts);
			}  else {
				displayedMimeParts.remove(bodyPart);
			}

			Iterator it = displayedMimeParts.iterator();

			while (it.hasNext()) {
				model.addMimePart((StreamableMimePart) it.next());
			}
		}
	}
}