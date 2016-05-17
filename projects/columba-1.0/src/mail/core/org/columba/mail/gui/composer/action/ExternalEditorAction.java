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
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.base.SwingWorker;
import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.util.ExternalEditor;
import org.columba.mail.util.MailResourceLoader;

/**
 * Edit message body text in external application.
 * 
 * @author fdietz
 */
public class ExternalEditorAction extends AbstractColumbaAction {
	public ExternalEditorAction(IFrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "composer",
				"menu_edit_extern_edit"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"composer", "menu_edit_extern_edit").replaceAll("&", ""));

		setEnabled(ColumbaDesktop.getInstance().supportsOpen());
	}

	/**
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		final ComposerController composerController = (ComposerController) getFrameMediator();

		composerController.getView().getFrame().setEnabled(false);

		composerController.getEditorController().setViewEnabled(false);

		final SwingWorker worker = new SwingWorker() {
			public Object construct() {

				ExternalEditor Ed = new ExternalEditor();

				try {

					Ed.startExternalEditor(composerController
							.getEditorController());
				} catch (IOException e) {
					e.printStackTrace();
				}

				return Ed;
			}

			// Runs on the event-dispatching thread.
			public void finished() {
				composerController.getView().getFrame().setEnabled(true);

				composerController.getEditorController().setViewEnabled(true);

			}
		};

		worker.start(); // required for SwingWorker 3
	}
}
