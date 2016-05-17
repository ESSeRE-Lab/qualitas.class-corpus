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
package org.columba.chat;

import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.columba.api.exception.PluginHandlerNotFoundException;
import org.columba.core.component.IComponentPlugin;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ActionExtensionHandler;
import org.jivesoftware.smack.XMPPConnection;

/**
 * @author fdietz
 * 
 */
public class AlturaComponent implements IComponentPlugin {

	public static XMPPConnection connection;

	/**
	 * 
	 */
	public AlturaComponent() {
		super();

	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#init()
	 */
	public void init() {
		try {
			InputStream is = this.getClass().getResourceAsStream(
					"/org/columba/chat/action/action.xml");

			((ActionExtensionHandler) PluginManager.getInstance().getHandler(
					ActionExtensionHandler.NAME)).loadExtensionsFromStream(is);

		} catch (PluginHandlerNotFoundException ex) {
		}

	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#postStartup()
	 */
	public void postStartup() {

	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#registerCommandLineArguments()
	 */
	public void registerCommandLineArguments() {

	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#handleCommandLineParameters(org.apache.commons.cli.CommandLine)
	 */
	public void handleCommandLineParameters(CommandLine commandLine) {

	}

}