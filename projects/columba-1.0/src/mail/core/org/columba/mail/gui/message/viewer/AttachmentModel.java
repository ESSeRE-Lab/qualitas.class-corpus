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
package org.columba.mail.gui.message.viewer;

import java.util.List;

import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.StreamableMimePart;

public class AttachmentModel {

	private IMailbox folder;

	private Object uid;

	private List displayedMimeParts;

	private MimeTree collection;

	public AttachmentModel() {

	}

	public synchronized void setFolder(IMailbox folder) {
		this.folder = folder;
	}

	public synchronized void setUid(Object uid) {
		this.uid = uid;
	}

	public IMailbox getFolder() {
		return folder;
	}

	public Object getUid() {
		return uid;
	}

	/**
	 * Returns the collection.
	 * 
	 * @return MimePartTree
	 */
	public MimeTree getCollection() {
		return collection;
	}

	/**
	 * Sets the collection.
	 * 
	 * @param collection
	 *            The collection to set
	 */
	public void setCollection(MimeTree collection) {
		this.collection = collection;

		// Get all MimeParts
		displayedMimeParts = collection.getAllLeafs();

		// Remove the BodyPart(s) if any
		StreamableMimePart bodyPart = (StreamableMimePart) collection
				.getFirstTextPart("plain");

		if (bodyPart != null) {
			MimePart bodyParent = bodyPart.getParent();

			if (bodyParent != null) {
				if (bodyParent.getHeader().getMimeType().getSubtype().equals(
						"alternative")) {
					List bodyParts = bodyParent.getChilds();
					displayedMimeParts.removeAll(bodyParts);
				} else {
					displayedMimeParts.remove(bodyPart);
				}
			} else {
				displayedMimeParts.remove(bodyPart);
			}
		}
	}

	/**
	 * Returns the displayedMimeParts.
	 * 
	 * @return List
	 */
	public List getDisplayedMimeParts() {
		return displayedMimeParts;
	}

	/**
	 * Sets the displayedMimeParts.
	 * 
	 * @param displayedMimeParts
	 *            The displayedMimeParts to set
	 */
	public void setDisplayedMimeParts(List displayedMimeParts) {
		this.displayedMimeParts = displayedMimeParts;
	}
}