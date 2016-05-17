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
package org.columba.mail.spam.rules;


/**
 * @author fdietz
 *
 */
public abstract class AbstractRule implements Rule {

    /**
     * Rule didn't find any hints that this message is spam
     * -> use a neutral 0.5 value
     */
    public static float NEARLY_ZERO= 0.5f;
    
    /**
     * Rule found hints that this message is spam
     * -> use a maximum 0.9 value
     */
    public static float MAX_PROBABILITY= 0.9f;
    
    private String name;
    
    public AbstractRule(String name) {
        this.name = name;
    }
    /**
     * @see org.columba.mail.spam.rules.Rule#getName()
     */
    public String getName() {
       return name;
    }
}
