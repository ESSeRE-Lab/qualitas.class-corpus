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
package org.columba.addressbook.model;


/**
 * @author fdietz
 *  
 */
public class ContactItem extends HeaderItem implements IContactItem {

	private Object uid;

	private String address;

	private String website;

	/**
	 *  
	 */
	public ContactItem() {
		super();

		setContact(true);
	}

	/**
	 * @param contact
	 */
	public ContactItem(IContact contact) {
		super();

		setDisplayName(contact.get(VCARD.DISPLAYNAME));
		setAddress(contact.get(VCARD.EMAIL, VCARD.EMAIL_TYPE_INTERNET));
		setWebsite(contact.get(VCARD.URL));

		setContact(true);
	}

	/**
	 * @return Returns the address.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            The address to set.
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return Returns the website.
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * @param website
	 *            The website to set.
	 */
	public void setWebsite(String website) {
		this.website = website;
	}

	/**
	 * @return Returns the uid.
	 */
	public Object getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            The uid to set.
	 */
	public void setUid(Object uid) {
		this.uid = uid;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		ContactItem item = new ContactItem();
		item.setDisplayName(getDisplayName());
		item.setHeader(getHeader());
		item.setAddress(getAddress());
		item.setWebsite(getWebsite());

		return item;
	}

}