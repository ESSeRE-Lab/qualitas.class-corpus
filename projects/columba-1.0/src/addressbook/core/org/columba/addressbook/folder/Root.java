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
package org.columba.addressbook.folder;

import org.columba.addressbook.config.FolderItem;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.xml.XmlElement;


/**
 * Top-level root folder of JTree.
 * 
 * @author fdietz
 */
public class Root extends AddressbookTreeNode {
    FolderItem item;

    public Root(XmlElement node) {
        super(new FolderItem(node));
    }

    /**
 * @see org.columba.modules.mail.folder.FolderTreeNode#instanceNewChildNode(AdapterNode, FolderItem)
 */
    public Class getDefaultChild() {
        return null;
    }

    public void createChildren(IWorkerStatusController c) {
    }

    /* (non-Javadoc)
 * @see org.columba.addressbook.gui.tree.AddressbookTreeNode#getName()
 */
    public String getName() {
        return "Root";
    }
}
