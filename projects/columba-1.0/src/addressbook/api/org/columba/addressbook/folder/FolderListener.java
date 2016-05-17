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
package org.columba.addressbook.folder;

import java.util.EventListener;

/**
 * 
 * Clients can attach implementations of this interface to a folder in order to
 * get notified on folder changes.
 * 
 * @author fdietz
 */
public interface FolderListener extends EventListener{

	/**
	 * Called when a item has been added to a folder.
	 */
	void itemAdded(IFolderEvent e);

	/**
	 * Called when a item has been removed from a folder.
	 */
	void itemRemoved(IFolderEvent e);

	/**
	 * Called when a item has been changed.
	 * 
	 * @param e
	 */
	void itemChanged(IFolderEvent e);

}