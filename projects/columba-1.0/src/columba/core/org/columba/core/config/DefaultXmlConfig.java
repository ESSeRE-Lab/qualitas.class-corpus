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
package org.columba.core.config;

import java.io.File;
import java.net.MalformedURLException;

import org.columba.core.xml.XmlIO;


/**
 * XML config file.
 * 
 * @author fdietz
 */
public class DefaultXmlConfig extends XmlIO {
	
	/**
	 * Default constructor.
	 * 
	 * @param file
	 */
    public DefaultXmlConfig(File file) {
        try {
            setURL(file.toURL());
        } catch (MalformedURLException mue) {
        }
    }

    /**
     * Overwrite method to set default settings.
     * 
     * @see org.columba.core.xml.XmlIO#load()
     */
    public boolean load() {
        boolean result = super.load();

        return result;
    }
}
