/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.jdbcapi.JDBCDriversPropertyTest

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.derbyTesting.functionTests.tests.jdbcapi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Run an autoload test with the system property
 * jdbc.drivers being set. This test is only intended
 * to be run in its own vm as a single test run.
 * It inherits from TestCase so as to not have any
 * chance of loading DriverManager before setting
 * jdbc.drivers. 
 */
abstract class JDBCDriversPropertyTest extends TestCase {
    
    final static Test getSuite(String jdbcDrivers) throws Exception {
        
        TestSuite suite = new TestSuite("jdbc.drivers="+jdbcDrivers);
        
        System.setProperty("jdbc.drivers", jdbcDrivers);
        
        suite.addTest(getAutoLoadSuite());
            
        return suite;
    }
    
    /**
     * Load the class and run its suite method through reflection
     * so that DriverManger is not loaded indirectly before
     * jdbc.drivers is set.
     */
    private static Test getAutoLoadSuite()
       throws Exception
    {
        Class alt = Class.forName(
           "org.apache.derbyTesting.functionTests.tests.jdbcapi.AutoloadTest");
        
        Method suiteMethod = alt.getMethod("suite", null);
        
        return (Test) suiteMethod.invoke(null, null);
    }
    
    JDBCDriversPropertyTest() {
        super();
     }
}
