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
package org.columba.addressbook.gui.autocomplete;

/**
 * JCombox includes autocomplete feature.
 * <p>
 * This class automatically initializes the data for the autocomplete feature.
 * 
 * @author fdietz
 */
public class DefaultAddressComboBox extends BasicAddressAutocompleteComboBox {

	/**
	 * Default constructor
	 * 
	 * @param includeGroup
	 *            include group items, if true. Don't, otherwise.
	 */
	public DefaultAddressComboBox(boolean includeGroup) {
		super();

		initData(includeGroup);

		// initialize completer
		addCompleter();
	}

	/**
	 * Constructor
	 * 
	 * @param folderUid
	 *            uid of folder
	 * @param includeGroup
	 *            include group items, if true. Don't, otherwise.
	 */
	public DefaultAddressComboBox(int folderUid, boolean includeGroup) {
		super();

		AddressCollector.getInstance().clear();

		AddressCollector.getInstance().addAllContacts(folderUid, includeGroup);

		// initialize completer
		addCompleter();
	}

	/**
	 * Add data from the Personal Addressbook and Collected Addresses
	 * 
	 */
	private void initData(boolean includeGroup) {
		AddressCollector.getInstance().clear();

		AddressCollector.getInstance().addAllContacts(101, includeGroup);
		AddressCollector.getInstance().addAllContacts(102, includeGroup);

	}
}