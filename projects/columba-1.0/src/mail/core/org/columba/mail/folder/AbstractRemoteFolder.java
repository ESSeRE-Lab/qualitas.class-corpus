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
package org.columba.mail.folder;

import org.columba.core.filter.FilterList;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;


/**
 * Top-level folder of every IMAP account.
 * <p>
 * Only purpose of this folder is to allow for a better structure
 * of the folder hierachy, where local and remote folders are
 * very easy to distinct.
 *
 *  @author fdietz
 */
public abstract class AbstractRemoteFolder extends AbstractMessageFolder {
    //protected RemoteSearchEngine searchEngine;

    /**
 * Constructs a AbstractRemoteFolder.
 * @param item information about the folder.
 */
    public AbstractRemoteFolder(FolderItem item, String path) {
        super(item, path);

        // TODO (@author fdietz): move this to AbstractMessageFolder constructor
        XmlElement filterListElement = node.getElement(FilterList.XML_NAME);

        if (filterListElement == null) {
            filterListElement = new XmlElement(FilterList.XML_NAME);
            getConfiguration().getRoot().addElement(filterListElement);
        }

        filterList = new FilterList(filterListElement);
    }

    /**
 * Constructs a Remote Folder.
 * @param name the name of the folder.
 * @param type the type of a folder.
 */
    public AbstractRemoteFolder(String name, String type, String path) {
        super(name, type, path);
    }
}
