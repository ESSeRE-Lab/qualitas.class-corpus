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
package org.columba.mail.command;

import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.composer.ComposerController;

/**
 * @author freddy
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class ComposerCommandReference extends MailFolderCommandReference {
	protected ComposerController composerController;

	protected boolean appendSignature = true;

	/**
	 * Constructor for ComposerCommandReference.
	 * 
	 * @param folder
	 */
	public ComposerCommandReference(ComposerController composerController,
			IMailbox folder) {
		super(folder);
		this.composerController = composerController;
	}

	/**
	 * Returns the composerController.
	 * 
	 * @return ComposerController
	 */
	public ComposerController getComposerController() {
		return composerController;
	}

	/**
	 * @return Returns the appendSignature.
	 */
	public boolean isAppendSignature() {
		return appendSignature;
	}

	/**
	 * @param appendSignature
	 *            The appendSignature to set.
	 */
	public void setAppendSignature(boolean appendSignature) {
		this.appendSignature = appendSignature;
	}
}
