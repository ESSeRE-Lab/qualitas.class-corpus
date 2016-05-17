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
package org.columba.mail.gui.table;

import java.util.Observable;

/**
 * This is the model which handles the selected sorting column and the sorting
 * order (ascending/descending) of the table
 * <p>
 * The sorting state submenu registers interest on this observable to change its
 * selection state.
 * 
 * @author fdietz
 */
public class SortingStateObservable extends Observable {
	private String column;

	private boolean order;

	/**
	 *  
	 */
	public SortingStateObservable() {
		super();
	}

	/**
	 * @return
	 */
	public String getColumn() {
		return column;
	}

	/**
	 * @return
	 */
	public boolean isOrder() {
		return order;
	}

	/**
	 * @param string
	 */
	public void setSortingState(String string, boolean order) {
		column = string;
		this.order = order;

		setChanged();

		notifyObservers();
	}
}