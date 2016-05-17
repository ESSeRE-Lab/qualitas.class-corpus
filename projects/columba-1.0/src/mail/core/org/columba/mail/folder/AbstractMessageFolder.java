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

package org.columba.mail.folder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Logger;

import org.columba.api.command.IStatusObservable;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterList;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.event.FolderEvent;
import org.columba.mail.folder.event.IFolderListener;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.MailboxInfo;
import org.columba.ristretto.message.MimeHeader;

/**
 * Abstract Basic AbstractMessageFolder class. It is subclassed by every folder class
 * containing messages and therefore offering methods to alter the mailbox.
 * <p>
 * Folders are plugins and therefore dynamically created. This should make it
 * easy to write new folders in the future.
 * <p>
 * To make it very easy to add new local mailbox formats, we added a slightly
 * more complex class hierachy in org.columba.mail.folder,
 * org.columba.mail.folder.headercache. An implementation example can be found
 * in org.columba.mail.folder.mh.
 * <p>
 * Please note, that you only need to implement {@link DataStorageInstance}
 * which should be trivial in most cases. Then create a class extending
 * {@link AbstractLocalFolder}and plug your datastorage in this folder in overwriting
 * getDataStorageInstance() method.
 * <p>
 * Last, don't forget to register your folder plugin:
 * <p>
 * Add your folder to <code>org.columba.mail.plugin.folder.xml</code>. This
 * way you create an association of the folder name and the class which gets
 * loaded.
 * <p>
 * Edit your tree.xml file and replace the MH mailbox implementation with yours.
 * 
 * @author freddy
 * @created 19. Juni 2001
 */
public abstract class AbstractMessageFolder extends AbstractFolder implements
		IMailbox {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder");

	/**
	 * total/unread/recent count of messages in this folder
	 */
	protected MailboxInfo messageFolderInfo = new MailboxInfo();

	/**
	 * list of filters
	 */
	protected FilterList filterList;

	/**
	 * 
	 * set changed to true if the folder data changes.
	 */
	protected boolean changed = false;

	/**
	 * directory where this folders files are stored
	 */
	protected File directoryFile;

	/**
	 * The last selected uid for the current folder. This information is used to
	 * show the last selected message, if you switch to the current folder and
	 * the lastSelection field is set. If the lastSelection field is null, the
	 * first message in the table for this folder is shown. Have a look to
	 * org.columba.mail.gui.table.TableController#showHeaderList
	 */
	protected Object lastSelection;

	/**
	 * Status information updates are handled in using IStatusObservable.
	 * <p>
	 * Every command has to register its interest to this events before
	 * accessing the folder.
	 */
	protected IStatusObservable observable = new StatusObservableImpl();

	// implement your own search-engine here
	protected DefaultSearchEngine searchEngine;

	/**
	 * Standard constructor.
	 * 
	 * @param item
	 *            <class>FolderItem </class> contains information about the
	 *            folder
	 */
	public AbstractMessageFolder(FolderItem item, String path) {
		super(item);

		String dir = path + System.getProperty("file.separator") + getUid();

		if (DiskIO.ensureDirectory(dir)) {
			directoryFile = new File(dir);
		}

		loadMessageFolderInfo();
	}

	protected AbstractMessageFolder() {
		super();
	}

	/**
	 * @param type
	 */
	public AbstractMessageFolder(String name, String type, String path) {
		super(name, type);

		String dir = path + System.getProperty("file.separator") + getUid();

		if (DiskIO.ensureDirectory(dir)) {
			directoryFile = new File(dir);
		}

		loadMessageFolderInfo();
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a
	 * message addition.
	 */
	public void fireMessageAdded(Object uid) {
		getMessageFolderInfo().incExists();
		try {
			Flags flags = getFlags(uid);
			if (flags.getRecent()) {
				getMessageFolderInfo().incRecent();
			}
			if (!flags.getSeen()) {
				getMessageFolderInfo().incUnseen();
			}
		} catch (Exception e) {
		}
		setChanged(true);

		// update treenode
		fireFolderPropertyChanged();

		FolderEvent e = new FolderEvent(this, uid);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFolderListener.class) {
				((IFolderListener) listeners[i + 1]).messageAdded(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a
	 * message removal.
	 */
	public void fireMessageRemoved(Object uid, Flags flags) {
		getMessageFolderInfo().decExists();
		
		if( flags != null) {
			if (!flags.getSeen()) {
				getMessageFolderInfo().decUnseen();
			}
			if (flags.getRecent()) {
				getMessageFolderInfo().decRecent();
			}
		}

		try {
			getHeaderList().remove(uid);
		} catch (Exception e) {
		}
		setChanged(true);

		//      update treenode
		fireFolderPropertyChanged();

		FolderEvent e = new FolderEvent(this, uid);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFolderListener.class) {
				((IFolderListener) listeners[i + 1]).messageRemoved(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a
	 * message removal.
	 */
	public void fireMessageFlagChanged(Object uid, Flags oldFlags, int variant) {

		// @author fdietz
		// -> Moved code for updating mailfolderinfo to markMessage()
		// intentionally!
		//
		setChanged(true);

		FolderEvent e = new FolderEvent(this, uid, oldFlags, variant);
		
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFolderListener.class) {
				((IFolderListener) listeners[i + 1]).messageFlagChanged(e);
			}
		}
	}

	/**
	 * Returns the directory where the messages are saved
	 * 
	 * @return File the file representing the mailbox directory
	 */
	public File getDirectoryFile() {
		return directoryFile;
	}

	/**
	 * Call this method if folder data changed, so that we know if we have to
	 * save the header cache.
	 * 
	 * @param b
	 */
	public void setChanged(boolean b) {
		changed = b;
	}

	/**
	 * Change the <class>MessageFolderInfo </class>
	 * 
	 * @param i
	 *            the new messagefolderinfo
	 */
	public void setMessageFolderInfo(MailboxInfo i) {
		messageFolderInfo = i;
	}

	/**
	 * Check if folder was modified.
	 * 
	 * @return boolean True, if folder data changed. False, otherwise.
	 */
	protected boolean hasChanged() {
		return changed;
	}

	/**
	 * Method getMessageFolderInfo.
	 * 
	 * @return MessageFolderInfo
	 */
	public MailboxInfo getMessageFolderInfo() {
		return messageFolderInfo;
	}

	/**
	 * Method getFilterList.
	 * 
	 * @return FilterList
	 */
	public FilterList getFilterList() {
		return filterList;
	}

	

	/** ********************************** treenode implementation ********** */
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/**
	 * save messagefolderinfo to xml-configuration
	 *  
	 */
	protected void saveMessageFolderInfo() {
		MailboxInfo info = getMessageFolderInfo();

		IFolderItem item = getConfiguration();

		XmlElement property = item.getElement("property");

		property.addAttribute("exists", new Integer(info.getExists())
				.toString());
		property.addAttribute("unseen", new Integer(info.getUnseen())
				.toString());
		// on startup, there's shouldn't be any recent messages
		// -> we simply remember 0 recent messages here
		property.addAttribute("recent", "0");
		/*
		property.addAttribute("recent", new Integer(info.getRecent())
				.toString());
				*/

		if (info.getUidNext() != -1) {
			property.addAttribute("uidnext", new Integer(info.getUidNext())
					.toString());
			property.addAttribute("uidvalidity", new Integer(info
					.getUidValidity()).toString());
		}

	}

	/**
	 * 
	 * get messagefolderinfo from xml-configuration
	 *  
	 */
	protected void loadMessageFolderInfo() {
		XmlElement property = getConfiguration().getElement("property");

		if (property == null) {
			return;
		}

		MailboxInfo info = getMessageFolderInfo();

		String exists = property.getAttribute("exists");

		if (exists != null) {
			info.setExists(Integer.parseInt(exists));
		}

		String recent = property.getAttribute("recent");

		if (recent != null) {
			info.setRecent(Integer.parseInt(recent));
		}

		String unseen = property.getAttribute("unseen");

		if (unseen != null) {
			info.setUnseen(Integer.parseInt(unseen));
		}

		String uidnext = property.getAttribute("uidnext");

		if (uidnext != null) {
			info.setUidNext(Integer.parseInt(uidnext));
		}

		String uidvalidity = property.getAttribute("uidvalidty");

		if (uidvalidity != null) {
			info.setUidValidity(Integer.parseInt(uidvalidity));
		}

	}

	/**
	 * 
	 * use this method to save folder meta-data when closing Columba
	 *  
	 */
	public void save() throws Exception {
		saveMessageFolderInfo();
	}

	/**
	 * Returns the last selected Message for the current folder. If no message
	 * was selected, it returns null. The return-value is the uid of the last
	 * selected message.
	 */
	public Object getLastSelection() {
		return lastSelection;
	}

	/**
	 * Sets the last selection for the current folder. This should be the uid of
	 * the last selected Message for the current folder.
	 */
	public void setLastSelection(Object lastSel) {
		lastSelection = lastSel;
	}

	/**
	 * @return observable containing status information
	 */
	public IStatusObservable getObservable() {
		return observable;
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#supportsAddMessage()
	 */
	public boolean supportsAddMessage() {
		return true;
	}

	/**
	 * Returns true if this folder is an Inbox folder.
	 * 
	 * @return true if this folder is an Inbox folder.
	 */
	public boolean isInboxFolder() {
		return false;
	}

	/**
	 * Returns true if this folder is the Trash folder.
	 * 
	 * @return true if this folder is the Trash folder.
	 */
	public boolean isTrashFolder() {
		return false;
	}

	protected void updateMailFolderInfo(Flags flags, int variant)
			throws Exception {
		boolean updated = false;

		if (flags == null) {
			return;
		}

		switch (variant) {
		case MarkMessageCommand.MARK_AS_READ: {
			if (flags.getRecent()) {
				getMessageFolderInfo().decRecent();
				updated = true;
			}

			if (!flags.getSeen()) {
				getMessageFolderInfo().decUnseen();
				updated = true;
			}

			break;
		}

		case MarkMessageCommand.MARK_AS_UNREAD: {
			if (flags.getSeen()) {
				getMessageFolderInfo().incUnseen();
				updated = true;
			}

			break;
		}

		case MarkMessageCommand.MARK_AS_EXPUNGED: {
			if (!flags.getSeen()) {
				getMessageFolderInfo().decUnseen();
				updated = true;
			}

			if (flags.getRecent()) {
				getMessageFolderInfo().decRecent();
				updated = true;
			}

			break;
		}
		
		case MarkMessageCommand.MARK_AS_RECENT: {
			if (!flags.getRecent()) {
				getMessageFolderInfo().incRecent();
				updated = true;
			}

			break;
		}
		case MarkMessageCommand.MARK_AS_NOTRECENT: {
			if (flags.getRecent()) {
				getMessageFolderInfo().decRecent();
				updated = true;
			}

			break;
		}

		}
		
//      update treenode
		if ( updated )
			fireFolderPropertyChanged();
	}

	/**
	 * @param uid
	 * @param variant
	 * @param worker
	 * @throws Exception
	 */
	protected void markMessage(Object uid, int variant) throws Exception {
		Flags flags = getFlags(uid);
		
		updateMailFolderInfo(flags, variant);
		
		if (flags == null) {
			return;
		}
		Flags oldFlags = (Flags) flags.clone();

		switch (variant) {
		case MarkMessageCommand.MARK_AS_READ: {

			flags.setSeen(true);
			flags.setRecent(false);

			break;
		}

		case MarkMessageCommand.MARK_AS_UNREAD: {

			flags.setSeen(false);

			break;
		}

		case MarkMessageCommand.MARK_AS_FLAGGED: {
			flags.setFlagged(true);

			break;
		}

		case MarkMessageCommand.MARK_AS_UNFLAGGED: {
			flags.setFlagged(false);

			break;
		}

		case MarkMessageCommand.MARK_AS_EXPUNGED: {

			flags.setSeen(true);
			flags.setRecent(false);
			flags.setDeleted(true);

			break;
		}

		case MarkMessageCommand.MARK_AS_UNEXPUNGED: {
			flags.setDeleted(false);

			break;
		}

		case MarkMessageCommand.MARK_AS_ANSWERED: {
			flags.setAnswered(true);

			break;
		}
		case MarkMessageCommand.MARK_AS_UNANSWERED: {
			flags.setAnswered(false);

			break;
		}

		case MarkMessageCommand.MARK_AS_SPAM: {
			setAttribute(uid, "columba.spam", Boolean.TRUE);

			break;
		}

		case MarkMessageCommand.MARK_AS_NOTSPAM: {
			setAttribute(uid, "columba.spam", Boolean.FALSE);

			break;
		}
		case MarkMessageCommand.MARK_AS_DRAFT: {
			flags.setDraft(true);

			break;
		}
		case MarkMessageCommand.MARK_AS_NOTDRAFT: {
			flags.setDraft(false);

			break;
		}
		case MarkMessageCommand.MARK_AS_RECENT: {
			flags.setRecent(true);

			break;
		}
		case MarkMessageCommand.MARK_AS_NOTRECENT: {
			flags.setRecent(false);

			break;
		}
		}
		setChanged(true);
		
		fireMessageFlagChanged(uid, oldFlags, variant);
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#markMessage(java.lang.Object[],
	 *      int)
	 */
	public void markMessage(Object[] uids, int variant) throws Exception {
		for (int i = 0; i < uids.length; i++) {
			if (exists(uids[i])) {
				markMessage(uids[i], variant);

			}
		}
	}

	/** {@inheritDoc} */
	public void expungeFolder() throws Exception {

		// get list of all uids
		Object[] uids = getUids();

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];

			if (uid == null) {
				continue;
			}

			// if message with uid doesn't exist -> skip
			if (!exists(uid)) {
				LOG.info("uid " + uid + " doesn't exist");

				continue;
			}

			if (getFlags(uid).getDeleted()) {
				// move message to trash if marked as expunged
				LOG.info("removing uid=" + uid);

				// remove message
				removeMessage(uid);
			}
		}
	}

	/**
	 * Remove message from folder.
	 * <p>
	 * @author: fdietz
	 * This method was intentionally changed to public also it isn't 
	 * accessed from outside. This is why it isn't found in IMailbox.
	 * Only the VirtualFolder uses this public call.
	 * 
	 * 
	 * @param uid
	 *            UID identifying the message to remove
	 * @throws Exception
	 */
	public void removeMessage(Object uid) throws Exception {
		// notify listeners
		fireMessageRemoved(uid, getFlags(uid));

		// remove from header-list
		getHeaderList().remove(uid);
	}

	/** ****************************** IAttributeStorage *********************** */

	/**
	 * @return Returns the attributeStorage.
	 */
	//public abstract IHeaderListStorage getHeaderListStorage();

	/**
	 * @see org.columba.mail.folder.IMailbox#exists(java.lang.Object)
	 */
	public boolean exists(Object uid) throws Exception {
		return getHeaderList().exists(uid);
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#getUids()
	 */
	public Object[] getUids() throws Exception {
		return getHeaderList().getUids();
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#setAttribute(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setAttribute(Object uid, String key, Object value)
			throws Exception {
		getHeaderList().setAttribute(uid, key, value);
		//  set folder changed flag
		// -> if not, the header cache wouldn't notice that something
		// -> has changed. And wouldn't save the changes.
		setChanged(true);
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#getFlags(java.lang.Object)
	 */
	public Flags getFlags(Object uid) throws Exception {
		return getHeaderList().getFlags(uid);
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#getAttributes(java.lang.Object)
	 */
	public Attributes getAttributes(Object uid) throws Exception {
		return getHeaderList().getAttributes(uid);
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#getAttribute(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object getAttribute(Object uid, String key) throws Exception {
		return getHeaderList().getAttribute(uid, key);
	}

	/**
	 * @return
	 */
	public DefaultSearchEngine getSearchEngine() {
		return searchEngine;
	}

	/**
	 * @param filter
	 * @param uids
	 * @return @throws
	 *         Exception
	 */
	public Object[] searchMessages(Filter filter, Object[] uids)
			throws Exception {
		return getSearchEngine().searchMessages(filter, uids);
	}

	/**
	 * @param filter
	 * @return @throws
	 *         Exception
	 */
	public Object[] searchMessages(Filter filter) throws Exception {
		return getSearchEngine().searchMessages(filter);
	}

	/**
	 * Set new search engine
	 * 
	 * @see org.columba.mail.folder.search
	 * 
	 * @param engine
	 *            new search engine
	 */
	public void setSearchEngine(DefaultSearchEngine engine) {
		this.searchEngine = engine;
	}

	/**
	 * TODO (@author fdietz): move this out-of-folder!
	 * 
	 * @param header
	 * @param bodyStream
	 * @return
	 */
	protected InputStream decodeStream(MimeHeader header, InputStream bodyStream) {
		String charsetName = header.getContentParameter("charset");
		int encoding = header.getContentTransferEncoding();

		switch (encoding) {
		case MimeHeader.QUOTED_PRINTABLE: {
			bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

			break;
		}

		case MimeHeader.BASE64: {
			bodyStream = new Base64DecoderInputStream(bodyStream);

			break;
		}
		}

		if (charsetName != null) {
			Charset charset;

			try {
				charset = Charset.forName(charsetName);
			} catch (UnsupportedCharsetException e) {
				charset = Charset.forName(System.getProperty("file.encoding"));
			}

			bodyStream = new CharsetDecoderInputStream(bodyStream, charset);
		}

		return bodyStream;
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}


	/**
	 * @return Returns the headerList.
	 */
	public abstract IHeaderList getHeaderList() throws Exception;

}