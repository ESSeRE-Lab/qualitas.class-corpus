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

import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;

import javax.swing.UIManager;

import org.columba.core.gui.themes.plugin.AbstractThemePlugin;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class KunststoffLookAndFeelPlugin extends AbstractThemePlugin {

	/**
	 * 
	 */
	public KunststoffLookAndFeelPlugin() {
		super();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.themes.plugin.AbstractThemePlugin#setLookAndFeel()
	 */
	public void setLookAndFeel() throws Exception {

		UIManager.setLookAndFeel(
			new com.incors.plaf.kunststoff.KunststoffLookAndFeel());
		
		UIManager.getLookAndFeelDefaults().put("ClassLoader",getClass().getClassLoader());

	}

}
