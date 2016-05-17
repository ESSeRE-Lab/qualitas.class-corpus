/*

   Derby - Class org.apache.derby.client.am.GetSystemPropertiesAction

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package org.apache.derby.client.am;


/**
 * Java 2 PrivilegedAction encapsulation of System getProperties processing
 */
public class GetSystemPropertiesAction implements java.security.PrivilegedAction {
    // System properties values, i.e. output from System.getProperties()
    private java.util.Properties systemProperties_ = null;

    //============================== Constructor ==============================

    //-------------------- GetSystemPropertiesAction --------------------
    /**
     * Constructor.
     */
    public GetSystemPropertiesAction() {
    }

    /**
     * Performs the privileged action of System.getProperties()
     */
    public Object run() {
        this.systemProperties_ = System.getProperties();
        return this.systemProperties_;
    }

    /**
     * Accessor for system properties (used after run() method invocation)
     */
    public java.util.Properties getSystemProperties() {
        return this.systemProperties_;
    }
}
