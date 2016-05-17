/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.derbynet.GetCurrentPropertiesTest

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
package org.apache.derbyTesting.functionTests.tests.derbynet;

import java.io.File;
import java.util.Properties;
import java.util.Enumeration;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import junit.framework.Test;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derbyTesting.junit.NetworkServerTestSetup;
import org.apache.derbyTesting.junit.TestConfiguration;
import org.apache.derbyTesting.junit.SecurityManagerSetup;
import org.apache.derbyTesting.junit.SupportFilesSetup;

/**
 * This tests getCurrentProperties
 * 
 */
public class GetCurrentPropertiesTest extends BaseJDBCTestCase {
    // create own policy file
    private static String POLICY_FILE_NAME = 
        "functionTests/tests/derbynet/GetCurrentPropertiesTest.policy";
    private static String TARGET_POLICY_FILE_NAME = "server.policy";

    public GetCurrentPropertiesTest(String name) {
        super(name);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static Test suite()
    {
        // Use a fixed order for the test cases so that we know the exact
        // order in which they run. Some of them depend on the connection
        // number having a specific value, which can only be guaranteed if
        // we know exactly how many connections have been opened, hence the
        // need for a fixed order. Some also depend on seeing property values
        // set by the previous test case.
        Test test =
                TestConfiguration.orderedSuite(GetCurrentPropertiesTest.class);
        test = TestConfiguration.clientServerDecorator(test);
        // Install a security manager using the special policy file.
        test = decorateWithPolicy(test);
        // return suite; to ensure that nothing interferes with setting of
        // properties, wrap in singleUseDatabaseDecorator 
        return TestConfiguration.singleUseDatabaseDecorator(test);
    }
    /**
     * Construct the name of the server policy file.
     */
    private static String makeServerPolicyName()
    {
        try {
            
            String  userDir = getSystemProperty( "user.dir" );
            String  fileName = userDir + File.separator + SupportFilesSetup.EXTINOUT + File.separator + TARGET_POLICY_FILE_NAME;
            File      file = new File( fileName );
            String  urlString = file.toURL().toExternalForm();

            return urlString;
        }
        catch (Exception e)
        {
            fail("Exception in REading Server policy file",e);
            return null;
        }
    }
    // grant ALL FILES execute, and getPolicy permissions,
    // as well as write for the trace files.
    private static Test decorateWithPolicy(Test test) {
        String serverPolicyName = makeServerPolicyName(); 
        //
        // Install a security manager using the initial policy file.
        //
        test = new SecurityManagerSetup(test,serverPolicyName );
        // Copy over the policy file we want to use.
        //
        test = new SupportFilesSetup(
                test, null, new String[] {POLICY_FILE_NAME},
                null, new String[] {TARGET_POLICY_FILE_NAME}
        );
        return test;
        
    }
    /**
     * Testing the properties before connecting to a database
     * 
     * @throws Exception
     */
    public void test_01_propertiesBeforeConnection() throws Exception {
        Properties p = null;
        String  userDir = getSystemProperty( "user.dir" );
        String traceDir = userDir + File.separator + "system";
        Properties expectedValues = new Properties();
        expectedValues.setProperty("derby.drda.traceDirectory",traceDir);
        expectedValues.setProperty("derby.drda.maxThreads","0");
        expectedValues.setProperty("derby.drda.sslMode","off");
        expectedValues.setProperty("derby.drda.keepAlive","true");
        expectedValues.setProperty("derby.drda.minThreads","0");
        expectedValues.setProperty("derby.drda.portNumber",TestConfiguration.getCurrent().getPort()+"");
        expectedValues.setProperty("derby.drda.logConnections","false");
        expectedValues.setProperty("derby.drda.timeSlice","0");
        expectedValues.setProperty("derby.drda.startNetworkServer","false");
        expectedValues.setProperty("derby.drda.host","127.0.0.1");
        expectedValues.setProperty("derby.drda.traceAll","false");
        p = NetworkServerTestSetup.getNetworkServerControl().getCurrentProperties();

        Enumeration expectedProps = expectedValues.propertyNames();
        for ( expectedProps = expectedValues.propertyNames(); expectedProps.hasMoreElements();) {
            String propName = (String)expectedProps.nextElement();
            String propVal = (String)p.get(propName);
            //for debug
            println(expectedValues.getProperty(propName));
            println(propVal);
            assertEquals(expectedValues.getProperty(propName), propVal);

        }
    }
    /**
     * Testing the properties after connecting to a database
     * 
     * @throws Exception
     */
    public void test_02_propertiesAfterConnection() throws Exception {
        Properties p = null;
        String  userDir = getSystemProperty( "user.dir" );
        String traceDir = userDir + File.separator + "system";
        Properties expectedValues = new Properties();
        expectedValues.setProperty("derby.drda.traceDirectory",traceDir);
        expectedValues.setProperty("derby.drda.maxThreads","0");
        expectedValues.setProperty("derby.drda.sslMode","off");
        expectedValues.setProperty("derby.drda.trace.4","true");
        expectedValues.setProperty("derby.drda.keepAlive","true");
        expectedValues.setProperty("derby.drda.minThreads","0");
        expectedValues.setProperty("derby.drda.portNumber",TestConfiguration.getCurrent().getPort()+"");
        expectedValues.setProperty("derby.drda.logConnections","true");
        expectedValues.setProperty("derby.drda.timeSlice","0");
        expectedValues.setProperty("derby.drda.startNetworkServer","false");
        expectedValues.setProperty("derby.drda.host","127.0.0.1");
        expectedValues.setProperty("derby.drda.traceAll","false");  
        getConnection().setAutoCommit(false);
        NetworkServerControl nsctrl = NetworkServerTestSetup.getNetworkServerControl();
        nsctrl.trace(4,true);
        nsctrl.logConnections(true);
        p = NetworkServerTestSetup.getNetworkServerControl().getCurrentProperties();
        Enumeration expectedProps = expectedValues.propertyNames();
        for ( expectedProps = expectedValues.propertyNames(); expectedProps.hasMoreElements();) {
            String propName = (String) expectedProps.nextElement();
            String propVal = (String)p.get(propName);
            //for debug
            println(expectedValues.getProperty(propName));
            println(propVal);
            assertEquals(expectedValues.getProperty(propName), propVal);

        }
    } 
    /**
     * Testing the properties after setting the trace dir and tracing on
     * 
     * @throws Exception
     */
    public void test_03_propertiesTraceOn() throws Exception {
        Properties p = null;

        NetworkServerControl nsctrl = NetworkServerTestSetup.getNetworkServerControl();
        nsctrl.trace(true);
        String derbySystemHome = getSystemProperty("derby.system.home");
        nsctrl.setTraceDirectory(derbySystemHome);
        Properties expectedValues = new Properties();
        expectedValues.setProperty("derby.drda.traceDirectory",derbySystemHome);
        expectedValues.setProperty("derby.drda.maxThreads","0");
        expectedValues.setProperty("derby.drda.sslMode","off");
        expectedValues.setProperty("derby.drda.keepAlive","true");
        expectedValues.setProperty("derby.drda.minThreads","0");
        expectedValues.setProperty("derby.drda.portNumber",TestConfiguration.getCurrent().getPort()+"");
        expectedValues.setProperty("derby.drda.logConnections","true");
        expectedValues.setProperty("derby.drda.timeSlice","0");
        expectedValues.setProperty("derby.drda.startNetworkServer","false");
        expectedValues.setProperty("derby.drda.host","127.0.0.1");
        expectedValues.setProperty("derby.drda.traceAll","true");
        p = NetworkServerTestSetup.getNetworkServerControl().getCurrentProperties();
        Enumeration expectedProps = expectedValues.propertyNames();
        for ( expectedProps = expectedValues.propertyNames(); expectedProps.hasMoreElements();) {
            String propName = (String) expectedProps.nextElement();
            String propVal = (String)p.get(propName);
            //for debug
            println(expectedValues.getProperty(propName));
            println(propVal);
            assertEquals(expectedValues.getProperty(propName), propVal);


        }
    }
}
