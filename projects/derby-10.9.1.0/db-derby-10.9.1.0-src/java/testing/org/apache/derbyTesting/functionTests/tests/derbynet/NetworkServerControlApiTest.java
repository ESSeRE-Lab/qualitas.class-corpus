/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.derbynet.NetworkServerControlApiTest

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

import org.apache.derby.drda.NetworkServerControl;
import org.apache.derbyTesting.functionTests.tests.lang.SecurityPolicyReloadingTest;
import org.apache.derbyTesting.functionTests.tests.lang.SimpleTest;
import org.apache.derbyTesting.functionTests.util.TestUtil;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.Derby;
import org.apache.derbyTesting.junit.NetworkServerTestSetup;
import org.apache.derbyTesting.junit.SecurityManagerSetup;
import org.apache.derbyTesting.junit.SupportFilesSetup;
import org.apache.derbyTesting.junit.SystemPropertyTestSetup;
import org.apache.derbyTesting.junit.TestConfiguration;


import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

public class NetworkServerControlApiTest extends BaseJDBCTestCase {

    private static String POLICY_FILE_NAME="functionTests/tests/derbynet/NetworkServerControlApiTest.policy";
    private static String TARGET_POLICY_FILE_NAME="server.policy";
    
    public NetworkServerControlApiTest(String name) {
        super(name);
       
    }

    /** Test NetworkServerControl API.
     *  Right now it tests only the trace command for DERBY-3110.
     *  TODO: Add tests for other API calls.
     */
    
    /**
     *   Test other commands. These should all give a helpful error and the
     *   usage message
     */
    public void test_01_WrongUsage() throws Exception
    {
        final String nsc = "org.apache.derby.drda.NetworkServerControl";
        // we'll assume that we get the full message if we get 'Usage'
        // because sometimes, the message gets returned with carriage return,
        // and sometimes it doesn't, checking for two different parts...
        final String usage = "Usage: ";

        // no arguments
        String[] cmd = new String[] {nsc};
        assertExecJavaCmdAsExpected(new String[] 
            {"No command given.", usage}, cmd, 1);

        // some option but no command
        cmd = new String[] {nsc, "-h", "localhost"};
        assertExecJavaCmdAsExpected(new String[] 
            {"No command given.", usage}, cmd, 1);

        // unknown command
        cmd = new String[] {nsc, "unknowncmd"};
        assertExecJavaCmdAsExpected(new String[] 
            {"Command unknowncmd is unknown.", usage}, cmd, 1);

        // unknown option
        cmd = new String[] {nsc, "-unknownarg"};
        assertExecJavaCmdAsExpected(new String[] 
            {"Argument -unknownarg is unknown.", usage}, cmd, 1);

        // wrong number of arguments
        cmd = new String[] {nsc, "ping", "arg1"};
        assertExecJavaCmdAsExpected(new String[] 
            {"Invalid number of arguments for command ping.", usage}, cmd, 1);
    }
    
     /** 
     * @throws Exception
     */
    public void test_02_TraceCommands() throws Exception
    {
        NetworkServerControl nsctrl = NetworkServerTestSetup.getNetworkServerControl();
        String derbySystemHome = getSystemProperty("derby.system.home");
        nsctrl.setTraceDirectory(derbySystemHome);
       
        nsctrl.trace(true);
        nsctrl.ping();
        assertTrue(fileExists(derbySystemHome+"/Server3.trace"));
        nsctrl.trace(false);
        
        // now try on a directory where we don't have permission
        // this won't actually cause a failure until we turn on tracing.
        // assume we don't have permission to write to root.
        nsctrl.setTraceDirectory("/");
        
        // attempt to turn on tracing to location where we don't have permisson
        try {
            nsctrl.trace(true);
            fail("Should have gotten an exception turning on tracing");
        } catch (Exception e) {
            // expected exception
        }
        // make sure we can still ping
        nsctrl.ping();
    
                        
    }
    
    /**
     * Test tracing with system properties if we have no permission
     * to write to the trace directory. Make sure we can still 
     * get a connection.  Trace directory set to "/" in test setup.
     * 
     */
    public void xtestTraceSystemPropertiesNoPermission() throws SQLException{
        // our connection should go through fine and there should be an
        // exception in the derby.log.
        //access denied (java.io.FilePermission \\ read). I verified 
        // this manually when creating this fixture but do not know 
        // how to check in the test.
        assertEquals(getSystemProperty("derby.drda.traceAll"),"true");
        assertEquals(getSystemProperty("derby.drda.traceDirectory"),"/");
        Connection conn = getConnection();
        assertFalse(conn.getMetaData().isReadOnly());
    }
    
    /**
     * Test tracing with system properties when we have permissions
     * to write to the trace directory. 
     * Check that the tracing file is there.
     * 
     */
    public void xtestTraceSystemPropertiesHasPermission() throws SQLException{
        String derbysystemhome = getSystemProperty("derby.system.home");
        assertEquals(getSystemProperty("derby.drda.traceAll"),"true");
        assertEquals(getSystemProperty("derby.drda.traceDirectory"),derbysystemhome + "/trace");
        Connection conn = getConnection();
        assertFalse(conn.getMetaData().isReadOnly());
        assertTrue(fileExists(derbysystemhome+"/trace/Server1.trace"));
    }
    
    
    /**
     * Test NetworkServerControl ping command.
     * @throws Exception
     */
    public void test_03_Ping() throws Exception
    {
        String currentHost = TestConfiguration.getCurrent().getHostName();
        
        NetworkServerControl nsctrl = NetworkServerTestSetup.getNetworkServerControl();
        nsctrl.ping();
        
        // Note:Cannot test ping with unknown host because it fails in
        // InetAddress.getByName()
        
        nsctrl = new NetworkServerControl(privInetAddressGetByName(currentHost), 9393);
        try {        
        	nsctrl.ping();
        	fail("Should not have been able to ping on port 9393");
        }catch (Exception e){
        	// expected exception
        }
    }
    
        /**
         * Wraps InitAddress.getByName in privilege block.
         * 
         * @param host  host to resolve
         * @return InetAddress of host
         * @throws UnknownHostException
         */
        private InetAddress privInetAddressGetByName(final String host) throws UnknownHostException
        {
            InetAddress inetAddr = null;
            try {
                inetAddr = (InetAddress) AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws UnknownHostException {
                            return InetAddress.getByName(host);
                        }
                    });
            } catch (PrivilegedActionException pe) {
                Exception e = pe.getException();
                if (e instanceof UnknownHostException)
                    throw (UnknownHostException) e;
                else
                    throw (SecurityException) e;
            }
        return inetAddr;
            
        }
        
        
    
    
    private boolean fileExists(String filename) {
        final File file = new File(filename);
        try {
            return ((Boolean)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws SecurityException {
                        return new Boolean(file.exists());
                    }
                })).booleanValue();
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
        
    }
    
    /**
     * Construct the name of the server policy file.
     */
    private String makeServerPolicyName()
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
            System.out.println( "Unexpected exception caught by makeServerPolicyName(): " + e );

            return null;
        }
    }
    
    
    /**
     * Add decorators to a test run. Context is established in the reverse order
     * that decorators are declared here. That is, decorators compose in reverse
     * order. The order of the setup methods is:
     *
     * <ul>
     * <li>Copy security policy to visible location.</li>
     * <li>Install a security manager.</li>
     * <li>Run the tests.</li>
     * </ul>
     */
    private static Test decorateTest()
    {
        
        String serverPolicyName = new NetworkServerControlApiTest("test").makeServerPolicyName();

        // Use a fixed ordering of the test cases. Some of the test cases set
        // properties that will be seen by subsequent test cases, but only if
        // they run in one particular order. Also, some test cases depend on
        // the exact connection number, which can only be guaranteed if we
        // know exactly how many connections have been established so far, and
        // then we need a stable ordering of the test cases.
        Test test = TestConfiguration.clientServerDecorator(
                TestConfiguration.orderedSuite(
                        NetworkServerControlApiTest.class));
        //
        // Install a security manager using the initial policy file.
        //
        test = new SecurityManagerSetup( test,serverPolicyName );
        
        
        //
        // Copy over the policy file we want to use.
        //
        test = new SupportFilesSetup
            (
             test,
             null,
             new String[] { POLICY_FILE_NAME },
             null,
             new String[] { TARGET_POLICY_FILE_NAME}
             );

       
        return test;
    }
    
    public static Test suite()
    {
        
        TestSuite suite = new TestSuite("NetworkServerControlApiTest");
        
        // Need derbynet.jar in the classpath!
        if (!Derby.hasServer())
            return suite;
        suite.addTest(decorateTest());
        
        suite = decorateSystemPropertyTests(suite);
                    
        return suite;
    }

    private static TestSuite decorateSystemPropertyTests(TestSuite suite) {
        Properties traceProps = new Properties();
        traceProps.put("derby.drda.traceDirectory","/");
        traceProps.put("derby.drda.traceAll","true");
        suite.addTest(new SystemPropertyTestSetup(TestConfiguration.clientServerDecorator(
                new NetworkServerControlApiTest("xtestTraceSystemPropertiesNoPermission")),
                    traceProps));
        
        Properties traceProps2 = new Properties();
        
        traceProps2.put("derby.drda.traceDirectory",getSystemProperty("derby.system.home") + "/trace");
        traceProps2.put("derby.drda.traceAll","true");
        suite.addTest(new SystemPropertyTestSetup(TestConfiguration.clientServerDecorator(
                new NetworkServerControlApiTest("xtestTraceSystemPropertiesHasPermission")),
                    traceProps2));
        
        return suite;
    }

     // test fixtures from maxthreads
    public void test_04_MaxThreads_0() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] maxthreadsCmd1 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "maxthreads", "0","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        // test maxthreads 0
        assertExecJavaCmdAsExpected(new String[]
                {"Max threads changed to 0."}, maxthreadsCmd1, 0);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 0, maxValue);
    }

    public void test_05_MaxThreads_Neg1() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] maxthreadsCmd2 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "maxthreads", "-1", "-h", "localhost", "-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        String host = TestUtil.getHostName();
        maxthreadsCmd2[4] = host;
        assertExecJavaCmdAsExpected(new String[]{"Max threads changed to 0."}, maxthreadsCmd2, 0);
        //test maxthreads -1
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 0, maxValue);
    }

    /**
     * Calling with -12 should fail.
     * @throws Exception
     */
    public void test_06_MaxThreads_Neg12() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),
                    TestConfiguration.getCurrent().getPort());
        String[] maxthreadsCmd3 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "maxthreads", "-12","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        //test maxthreads -12
        assertExecJavaCmdAsExpected(new String[]{
                "Invalid value, -12, for maxthreads.",
                "Usage: NetworkServerControl <commands>",
                "Commands:",
                "start [-h <host>] [-p <portnumber>] [-noSecurityManager] [-ssl <sslmode>]",
                "shutdown [-h <host>][-p <portnumber>] [-ssl <sslmode>] [-user <username>] [-password <password>]",
                "ping [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "sysinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "runtimeinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "logconnections {on|off} [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "maxthreads <max>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "timeslice <milliseconds>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "trace {on|off} [-s <session id>][-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "tracedirectory <traceDirectory>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
        }, maxthreadsCmd3, 1);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 0, maxValue);
    }

    public void test_07_MaxThreads_2147483647() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] maxthreadsCmd4 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "maxthreads", "2147483647","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Max threads changed to 2147483647."}, maxthreadsCmd4, 0);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 2147483647, maxValue);
    }

    public void test_08_MaxThreads_9000() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] maxthreadsCmd5 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "maxthreads", "9000","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Max threads changed to 9000."}, maxthreadsCmd5, 0);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 9000, maxValue);
    }

    /**
     * Calling with 'a' causes a NFE which results in an error.
     * @throws Exception
     */
    public void test_09_MaxThreads_Invalid() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] maxthreadsCmd5 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "maxthreads", "10000","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Max threads changed to 10000."}, maxthreadsCmd5, 0);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 10000, maxValue);

        String[] maxthreadsCmd6 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "maxthreads", "a"};
        assertExecJavaCmdAsExpected(new String[]{"Invalid value, a, for maxthreads.",
                "Usage: NetworkServerControl <commands>",
                "Commands:",
                "start [-h <host>] [-p <portnumber>] [-noSecurityManager] [-ssl <sslmode>]",
                "shutdown [-h <host>][-p <portnumber>] [-ssl <sslmode>] [-user <username>] [-password <password>]",
                "ping [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "sysinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "runtimeinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "logconnections {on|off} [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "maxthreads <max>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "timeslice <milliseconds>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "trace {on|off} [-s <session id>][-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "tracedirectory <traceDirectory>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",}, maxthreadsCmd6, 1);


        maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 10000, maxValue);
    }

    public void test_10_MaxThreadsCallable_0() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        server.setMaxThreads(0);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 0, maxValue);
    }

    public void test_11_MaxThreadsCallable_Neg1() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        server.setMaxThreads(-1);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 0, maxValue);
    }

    /**
     * Test should throw an exception.
     * @throws Exception
     */
    public void test_12_MaxThreadsCallable_Neg12() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        try {
            server.setMaxThreads(-2);
            fail("Should have thrown an exception with 'DRDA_InvalidValue.U:Invalid value, -2, for maxthreads.'");
        } catch (Exception e) {
            assertEquals("DRDA_InvalidValue.U:Invalid value, -2, for maxthreads.", e.getMessage());
        }
    }

    public void test_13_MaxThreadsCallable_2147483647() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        server.setMaxThreads(2147483647);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 2147483647, maxValue);
    }

    public void test_14_MaxThreadsCallable_9000() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        server.setMaxThreads(9000);
        int maxValue = server.getMaxThreads();
        assertEquals("Fail! Max threads value incorrect!", 9000, maxValue);
    }

      // timeslice test fixtures
    public void test_15_TimeSlice_0() throws Exception {
        int value = 0;
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] timesliceCmd1 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "timeslice", "0","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Time slice changed to 0."}, timesliceCmd1, 0);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_16_TimeSlice_Neg1() throws Exception {
        int value = 0;
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] timesliceCmd2 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "timeslice", "-1", "-h", "localhost", "-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        String host = TestUtil.getHostName();
        timesliceCmd2[4] = host;
        assertExecJavaCmdAsExpected(new String[]{"Time slice changed to 0."}, timesliceCmd2, 0);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_17_TimeSlice_Neg12() throws Exception {
        int value = 0;
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] timesliceCmd3 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "timeslice", "-12","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Invalid value, -12, for timeslice.",
                "Usage: NetworkServerControl <commands> ",
                "Commands:",
                "start [-h <host>] [-p <portnumber>] [-noSecurityManager] [-ssl <sslmode>]",
                "shutdown [-h <host>][-p <portnumber>] [-ssl <sslmode>] [-user <username>] [-password <password>]",
                "ping [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "sysinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "runtimeinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "logconnections {on|off} [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "maxthreads <max>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "timeslice <milliseconds>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "trace {on|off} [-s <session id>][-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "tracedirectory <traceDirectory>[-h <host>][-p <portnumber>] [-ssl <sslmode>]"}, timesliceCmd3, 1);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_18_TimeSlice_2147483647() throws Exception {
        int value = 2147483647;
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] timesliceCmd4 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "timeslice", "2147483647","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Time slice changed to 2147483647."}, timesliceCmd4, 0);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_19_TimeSlice_9000() throws Exception {
        int value = 9000;
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] timesliceCmd5 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "timeslice", "9000","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Time slice changed to 9000."}, timesliceCmd5, 0);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_20_TimeSlice_a() throws Exception {
        int value = 8000;
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        String[] timesliceCmd5 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "timeslice", "8000","-p", String.valueOf(TestConfiguration.getCurrent().getPort())};
        assertExecJavaCmdAsExpected(new String[]{"Time slice changed to 8000."}, timesliceCmd5, 0);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
        String[] timesliceCmd6 = new String[]{"org.apache.derby.drda.NetworkServerControl",
                "timeslice", "a"};
        assertExecJavaCmdAsExpected(new String[]{"Invalid value, a, for timeslice.",
                "Usage: NetworkServerControl <commands> ",
                "Commands:",
                "start [-h <host>] [-p <portnumber>] [-noSecurityManager] [-ssl <sslmode>]",
                "shutdown [-h <host>][-p <portnumber>] [-ssl <sslmode>] [-user <username>] [-password <password>]",
                "ping [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "sysinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "runtimeinfo [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "logconnections {on|off} [-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "maxthreads <max>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "timeslice <milliseconds>[-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "trace {on|off} [-s <session id>][-h <host>][-p <portnumber>] [-ssl <sslmode>]",
                "tracedirectory <traceDirectory>[-h <host>][-p <portnumber>] [-ssl <sslmode>]"}, timesliceCmd6, 1);
        timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_21_TimeSliceCallable_0() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        int value = 0;
        server.setTimeSlice(0);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_22_TimeSliceCallable_Neg1() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        int value = 0;
        server.setTimeSlice(-1);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_23_TimeSliceCallable_Neg2() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        int value = 0;
        try {
            server.setTimeSlice(-2);
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("Invalid value") != -1); 
        }
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_24_TimeSliceCallable_2147483647() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        int value = 2147483647;
        server.setTimeSlice(2147483647);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }

    public void test_25_TimeSliceCallable_9000() throws Exception {
        NetworkServerControl server = new NetworkServerControl(InetAddress.getLocalHost(),TestConfiguration.getCurrent().getPort());
        int value = 9000;
        server.setTimeSlice(9000);
        int timeSliceValue = server.getTimeSlice();
        assertEquals(value, timeSliceValue);
    }
}
