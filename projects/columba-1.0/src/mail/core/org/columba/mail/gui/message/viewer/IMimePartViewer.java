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

import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;

/**
* Implementing classes should retrieve the necessary information from the
* selected message and change their appearance appropriately.
* <p>
* You can put every component in place you want.
* 
* @author fdietz
*/
public interface IMimePartViewer extends IViewer{

	/**
     * Use passed parameters to retrieve information from folder.
     * <p>
     * This method should be called from Command.execute() or a 
     * background thread.
     * <p>
     * 
     * @param folder			selected folder
     * @param uid				selected message
     * @param address			mimepart address
     * @param mediator			top-level mediator
     * @throws Exception
     */
    void view(IMailbox folder, Object uid, Integer[] address, MailFrameMediator mediator) throws Exception;
 
 
}
