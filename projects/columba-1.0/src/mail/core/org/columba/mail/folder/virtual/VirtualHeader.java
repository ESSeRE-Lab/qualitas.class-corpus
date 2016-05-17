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
package org.columba.mail.folder.virtual;

import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.message.ColumbaHeader;

/**
 * @author freddy
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class VirtualHeader extends ColumbaHeader {

	protected AbstractMessageFolder srcFolder;

	protected Object virtualUid;
	protected Object srcUid;
	
	public VirtualHeader(ColumbaHeader header, AbstractMessageFolder srcFolder,
			Object srcUid) {
		super(header);

		this.srcFolder = srcFolder;
		this.srcUid = srcUid;
	}

	/**
	 * Returns the srcFolder.
	 * 
	 * @return Folder
	 */
	public AbstractMessageFolder getSrcFolder() {
		return srcFolder;
	}

	/**
	 * Returns the srcUid.
	 * 
	 * @return Object
	 */
	public Object getVirtualUid() {
		return virtualUid;
	}

	/**
	 * @param srcUid
	 *            The srcUid to set.
	 */
	public void setVirtualUid(Object srcUid) {
		this.virtualUid = srcUid;
	}
	
	public Object getSrcUid() {
		return srcUid;
	}

	public void setSrcUid(Object uid) {
		srcUid = uid;
	}
}