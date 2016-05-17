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
package org.columba.addressbook.gui.action;

import java.awt.event.ActionEvent;

import org.columba.addressbook.gui.dialog.importfilter.ImportWizardLauncher;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * @author frd
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class AddressbookImportAction extends AbstractColumbaAction {
	public AddressbookImportAction(IFrameMediator frameController) {
		super(frameController, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_utilities_addressbook"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_utilities_addressbook").replaceAll("&", ""));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		new ImportWizardLauncher().launchWizard();
	}
}