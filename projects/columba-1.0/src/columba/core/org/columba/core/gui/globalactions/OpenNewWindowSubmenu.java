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
package org.columba.core.gui.globalactions;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.menu.IMenu;
import org.columba.core.resourceloader.GlobalResourceLoader;


/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenNewWindowSubmenu extends IMenu {
    /**
 * @param controller
 * @param caption
 */
    public OpenNewWindowSubmenu(IFrameMediator controller, String caption,String id) {
        super(controller,
            GlobalResourceLoader.getString("global", "global",
                "menu_file_opennewwindow"),id);
    }
}
