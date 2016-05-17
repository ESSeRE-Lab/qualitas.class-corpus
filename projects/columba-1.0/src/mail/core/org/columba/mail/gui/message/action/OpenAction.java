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
package org.columba.mail.gui.message.action;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.message.URLObservable;
import org.columba.mail.gui.message.util.ColumbaURL;
import org.columba.mail.util.MailResourceLoader;

/**
 * Open link in external browser.
 * 
 * @author fdietz
 */
public class OpenAction extends AbstractColumbaAction implements Observer {
	ColumbaURL url = null;

	/**
	 *  
	 */
	public OpenAction(IFrameMediator controller) {
		super(controller, MailResourceLoader.getString("menu", "mainframe",
				"viewer_openlink"));

		setEnabled(false);

		putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("folder-open.png"));

		// listen for URL changes
		((MessageViewOwner) controller).getMessageController().addURLObserver(
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ColumbaDesktop.getInstance().browse(url.getRealURL());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1) {
		URLObservable o = (URLObservable) arg0;

		url = o.getUrl();

		if (url == null) {
			setEnabled(false);
		} else {
			setEnabled(!url.isMailTo() && ColumbaDesktop.getInstance().supportsBrowse());
		}
	}
}