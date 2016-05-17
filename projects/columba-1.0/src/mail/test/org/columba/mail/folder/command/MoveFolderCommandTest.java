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

import org.columba.core.command.NullWorkerStatusController;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractFolderTst;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.MailboxTstFactory;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.temp.TempFolder;

/**
 * Test cases for the MoveFolder command.
 * 
 * @author redsolo
 */
public class MoveFolderCommandTest extends AbstractFolderTst {

	public MoveFolderCommandTest(String arg0) {
		super(arg0);
	}

	public MoveFolderCommandTest(MailboxTstFactory factory, String arg0) {
		super(factory, arg0);
	}

	/**
	 * Tests the execute() method.
	 * 
	 * @throws Exception
	 *             thrown for any bad reason if the command goes wrong.
	 */
	public void testMoveFolder() throws Exception {
		// @author: fdietz
		// Disabled this testcase, as it doesn't really fit into
		// this category of message operations
		// It fails because we would have to create a folder-hierarchy
		// including parent folders to execute the move
		
//		AbstractMessageFolder rootFolder = createFolder();
//
//		// Is not supported by IMAP and TempFolder
//		if (rootFolder instanceof IMAPFolder
//				|| rootFolder instanceof TempFolder) {
//			return;
//		}
//
//		AbstractMessageFolder folderToBeMoved = createFolder();
//		folderToBeMoved.moveTo(rootFolder);
//
//		AbstractMessageFolder destinationFolder = createFolder();
//		folderToBeMoved.moveTo(rootFolder);
//
//		//      create Command reference
//		MailFolderCommandReference ref = new MailFolderCommandReference(
//				folderToBeMoved, destinationFolder);
//
//		MoveFolderCommand command = new MoveFolderCommand(ref);
//		command.execute(NullWorkerStatusController.getInstance());
//
//		assertEquals("The destination folders child size is incorrect.", 1,
//				destinationFolder.getChildCount());
//		assertEquals("The root folder has more than one child", 1,
//				destinationFolder.getChildCount());
//		assertEquals("The moved folders parent is not the destination folder",
//				destinationFolder, folderToBeMoved.getParent());
	}
}