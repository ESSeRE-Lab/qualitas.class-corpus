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
package org.columba.mail.gui.message.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.columba.core.io.StreamUtils;
import org.columba.core.logging.Logging;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.SecurityItem;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.viewer.EncryptionStatusViewer;
import org.columba.mail.gui.message.viewer.Rfc822MessageViewer;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.pgp.JSCFController;
import org.columba.mail.pgp.PGPPassChecker;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.parser.BodyParser;
import org.columba.ristretto.parser.HeaderParser;
import org.columba.ristretto.parser.ParserException;
import org.waffel.jscf.JSCFConnection;
import org.waffel.jscf.JSCFException;
import org.waffel.jscf.JSCFResultSet;
import org.waffel.jscf.JSCFStatement;

/**
 * Filter decrypting and verifying messages.
 * <p>
 * A {@link SecurityStatusEvent}is used to notify all listeners.
 * <p>
 * {@link EncryptionStatusViewer}is currently the only listener. In the
 * future a status icon will be added to the message header, too.
 * <p>
 * 
 * @author fdietz
 *  
 */
public class PGPMessageFilter extends AbstractFilter {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.mail.gui.message.filter");

	private MimeTree mimePartTree;

	private int pgpMode = EncryptionStatusViewer.NOOP;

	// true if we view an encrypted message
	private boolean encryptedMessage = false;

	private String pgpMessage = "";

	private InputStream decryptedStream;

	private ColumbaMessage message;

	private List listeners;

	public PGPMessageFilter(MailFrameMediator mediator, Rfc822MessageViewer messageController) {
		super(mediator, messageController);

		listeners = new ArrayList();
	}

	public void addSecurityStatusListener(SecurityStatusListener l) {
		listeners.add(l);
	}

	public void fireSecurityStatusEvent(SecurityStatusEvent ev) {
		final SecurityStatusEvent event = ev;
		
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			final SecurityStatusListener l = (SecurityStatusListener) it.next();
			Runnable doWorkRunnable = new Runnable() {
				public void run() {
					l.statusUpdate(event);
					
				}
			};
			SwingUtilities.invokeLater(doWorkRunnable);
			
		}
	}

	/**
	 * @see org.columba.mail.gui.message.filter.Filter#filter(org.columba.mail.folder.Folder,
	 *      java.lang.Object)
	 */
	public IMailFolderCommandReference filter(IMailbox folder, Object uid)
			throws Exception {

		mimePartTree = folder.getMimePartTree(uid);

		// Check if the message still exists
		// or has been moved by e.g. a filter
		if(mimePartTree == null ) return null;
		
		
		// TODO (@author waffel): encrypt AND sign dosN#t work. The message is always only
		// encrypted. We need a function that knows, here
		// is an encrypted AND signed Message. Thus first encyrpt and then
		// verifySign the message
		//      if this message is signed/encrypted we have to use
		// GnuPG to extract the decrypted bodypart
		// - multipart/encrypted
		// - multipart/signed
		MimeType firstPartMimeType = mimePartTree.getRootMimeNode().getHeader()
		.getMimeType();

		AccountItem defAccount = MailConfig.getInstance().getAccountList()
				.getDefaultAccount();

		boolean pgpActive = false;

		if (defAccount != null) {
			SecurityItem pgpItem = defAccount.getPGPItem();
			LOG.fine("pgp activated: " + pgpItem.get("enabled"));
			pgpActive = new Boolean((pgpItem.get("enabled"))).booleanValue();
		}

		IMailFolderCommandReference result = null;
		LOG.fine("pgp is true");
		if (firstPartMimeType.getSubtype().equals("signed")) {
			result = verify(folder, uid, pgpActive);

		} else if (firstPartMimeType.getSubtype().equals("encrypted")) {
			LOG.fine("Mimepart type encrypted found");
			result = decrypt(folder, uid, pgpActive);

		} else {
			pgpMode = EncryptionStatusViewer.NOOP;
		}

		//notify listeners
		fireSecurityStatusEvent(new SecurityStatusEvent(this, pgpMessage,
				pgpMode));

		return result;
	}

	/**
	 * Decrypt message.
	 * 
	 * @param folder
	 *            selected folder
	 * @param uid
	 *            selected message UID
	 * @throws Exception
	 * @throws IOException
	 */
	private IMailFolderCommandReference decrypt(IMailbox folder, Object uid,
			boolean pgpActive) throws Exception, IOException {
		InputStream decryptedStream = null;
		LOG.fine("start decrypting");
		if (!pgpActive) {
			pgpMessage = "";
			pgpMode = EncryptionStatusViewer.NO_KEY;
		} else {
			SecurityItem pgpItem = null;
			// we need the pgpItem, to extract the path to gpg
			pgpItem = MailConfig.getInstance().getAccountList().getDefaultAccount()
					.getPGPItem();
			// this is wrong! we need the default id.
			//pgpItem.set("id", new BasicHeader(header.getHeader()).getTo()[0]
			//        .getMailAddress());

			MimePart encryptedMultipart = mimePartTree.getRootMimeNode();

			encryptedMessage = true;

			// the first child must be the control part
			InputStream controlPart = folder.getMimePartBodyStream(uid,
					encryptedMultipart.getChild(0).getAddress());

			// the second child must be the encrypted message
			InputStream encryptedPart = folder.getMimePartBodyStream(uid,
					encryptedMultipart.getChild(1).getAddress());

			try {
				JSCFController controller = JSCFController.getInstance();
				JSCFConnection con = controller.getConnection();
				LOG.fine("new JSCConnection");
				JSCFStatement stmt = con.createStatement();
				LOG.fine("new Statement");
				PGPPassChecker passCheck = PGPPassChecker.getInstance();
				boolean check = passCheck.checkPassphrase(con);
				LOG.fine("after pass check, check is " + check);
				if (!check) {
					pgpMode = EncryptionStatusViewer.DECRYPTION_FAILURE;
					// TODO (@author fdietz): make i18n!
					pgpMessage = "wrong passphrase";
					return null;
				}
				LOG.fine("encrypted is != null?: " + (encryptedPart != null));
				JSCFResultSet res = stmt.executeDecrypt(encryptedPart);
				LOG.fine("after calling decrypting");
				if (res.isError()) {
					LOG.fine("the result set contains errors ");
					pgpMode = EncryptionStatusViewer.DECRYPTION_FAILURE;
					pgpMessage = StreamUtils.readCharacterStream(res.getErrorStream())
							.toString();
					LOG.fine("error message: " + pgpMessage);
					decryptedStream = res.getResultStream();
					//return null;
				} else {
					decryptedStream = res.getResultStream();
					pgpMode = EncryptionStatusViewer.DECRYPTION_SUCCESS;
				}
			} catch (JSCFException e) {
				e.printStackTrace();
				LOG.severe(e.getMessage());
				pgpMode = EncryptionStatusViewer.DECRYPTION_FAILURE;
				pgpMessage = e.getMessage();

				// just show the encrypted raw message
				decryptedStream = encryptedPart;
			}
		}
		try {
			LOG.fine("decrypted Stream is: " + decryptedStream);
			CharSequence decryptedBodyPart = "";
			// if the pgp mode is active we should get the decrypted part
			if (pgpActive) {
				// TODO (@author fdietz): should be removed if we only use Streams!
				decryptedBodyPart = StreamUtils.readCharacterStream(decryptedStream);
				// check if the returned String is has a length != 0
				if (decryptedBodyPart.length() == 0) {
					LOG
							.fine("decrypted body part has a 0 length ... fixing it");
					decryptedBodyPart = new StringBuffer(
							"Content-Type: text/plain; charset=\"ISO-8859-15\"\n\n");
				}
			}
			// else we set the body to the i18n String
			else {
				decryptedBodyPart = new StringBuffer(
						"Content-Type: text/plain; charset=\"ISO-8859-15\"\n\n"
								+ MailResourceLoader.getString("menu",
										"mainframe",
										"security_decrypt_encrypted") + "\n");
			}
			LOG.fine("the decrypted Body part: " + decryptedBodyPart);
			// construct new Message from decrypted string
			message = new ColumbaMessage(folder.getAllHeaderFields(uid));

			Source decryptedSource = new CharSequenceSource(decryptedBodyPart);
			MimeHeader mimeHeader = new MimeHeader(HeaderParser
					.parse(decryptedSource));
			mimePartTree = new MimeTree(BodyParser.parseMimePart(mimeHeader,
					decryptedSource));
			message.setMimePartTree(mimePartTree);

			InputStream messageSourceStream = folder
					.getMessageSourceStream(uid);
			message.setSource(new CharSequenceSource(StreamUtils
					.readCharacterStream(messageSourceStream)));
			messageSourceStream.close();

			encryptedMessage = true;

			// call AbstractFilter to do the tricky part
			return filter(folder, uid, message);
			//header = (ColumbaHeader) message.getHeaderInterface();
		} catch (ParserException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		}

		/*
		 * controlPart.close(); encryptedPart.close(); if (decryptedStream !=
		 * null) { decryptedStream.close(); }
		 */
		return null;
	}

	/**
	 * Verify message.
	 * 
	 * @param folder
	 *            selected folder
	 * @param uid
	 *            selected message UID
	 * @throws Exception
	 * @throws IOException
	 */
	private MailFolderCommandReference verify(IMailbox folder, Object uid,
			boolean pgpActive) throws Exception, IOException {
		if (!pgpActive) {
			pgpMessage = "";
			pgpMode = EncryptionStatusViewer.NO_KEY;
			return null;
		}
		MimePart signedMultipart = mimePartTree.getRootMimeNode();

		//          the first child must be the signed part
		InputStream signedPart = folder.getMimePartSourceStream(uid,
				signedMultipart.getChild(0).getAddress());

		// the second child must be the pgp-signature
		InputStream signature = folder.getMimePartBodyStream(uid,
				signedMultipart.getChild(1).getAddress());

		// Get the mailaddress and use it as the id
		Address fromAddress = new BasicHeader(folder.getHeaderFields(uid, new String[] { "From" })).getFrom();
		try {
			JSCFController controller = JSCFController.getInstance();
			JSCFConnection con = controller.getConnection();
			JSCFStatement stmt = con.createStatement();
			String micalg = signedMultipart.getHeader().getContentParameter(
					"micalg").substring(4);
			JSCFResultSet res = stmt.executeVerify(signedPart, signature,
					micalg);
			if (res.isError()) {
				pgpMode = EncryptionStatusViewer.VERIFICATION_FAILURE;
				pgpMessage = StreamUtils.readCharacterStream(res.getErrorStream())
						.toString();
			} else {
				pgpMode = EncryptionStatusViewer.VERIFICATION_SUCCESS;
				pgpMessage = StreamUtils.readCharacterStream(res.getResultStream())
						.toString();
			}

		} catch (JSCFException e) {

			if (Logging.DEBUG)
				e.printStackTrace();

			pgpMode = EncryptionStatusViewer.VERIFICATION_FAILURE;
			pgpMessage = e.getMessage();
			// something really got wrong here -> show error dialog
			//JOptionPane.showMessageDialog(null, e.getMessage());

			pgpMode = EncryptionStatusViewer.VERIFICATION_FAILURE;
		}

		signedPart.close();
		signature.close();

		return null;
	}

}