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
package org.columba.chat.jabber;

import java.util.HashMap;
import java.util.Map;

import org.columba.chat.api.IBuddyStatus;

/**
 * Manages a list of buddies.
 * 
 * @author fdietz
 */
public class BuddyList {

	private Map map;

	private static BuddyList instance = new BuddyList();

	public BuddyList() {
		map = new HashMap();
	}

	public static BuddyList getInstance() {
		return instance;
	}

	public void add(String jabberId, IBuddyStatus buddy) {
		map.put(jabberId, buddy);
	}

	public boolean exists(String jabberId) {
		return map.containsKey(jabberId);
	}

	public BuddyStatus getBuddy(String jabberId) {
		if (map.containsKey(jabberId))
			return (BuddyStatus) map.get(jabberId);

		return null;
	}
}