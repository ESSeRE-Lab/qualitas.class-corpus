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
package org.columba.core.gui.profiles;

import java.io.File;

/**
 * Profile consisting of a name and a location pointing to the
 * configuration directory.
 * 
 * @author fdietz
 */
public class Profile {

	private String name;
	private File location;

	public Profile(String name, File location) {
		this.name = name;
		this.location = location;

	}

	/**
	 * @return Returns the location.
	 */
	public File getLocation() {
		return location;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
}