/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.standalone.socket;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * This class defines a UTFWithSocketTest class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class UTFWithSocketTest extends NoTemplate
{
  
  
  /**
   * Test with repeated transmission of an image, to compare the speed 
   * between writeObject and writeBytes
   * @throws Exception if fails
   */
  public void testUTFOverSocketWithImage() throws Exception
  {
// Get test properties
    ResourceBundle rb = ResourceBundle.getBundle("socketTest");
    Enumeration enu = rb.getKeys();
    String key;
    while (enu.hasMoreElements())
    {
      key = (String) enu.nextElement();
      System.out.println("Using test property <" + key + "> with value <"
          + rb.getObject(key) + ">");
    }
    int serverPort = Integer.parseInt(rb.getString("serverPort"));
    String imageFile = rb.getString("imageFile");
    int numberOfLoop = Integer.parseInt(rb.getString("numberOfLoop"));
    String serverName = rb.getString("serverName");
    boolean useBufferedStream = new Boolean(rb.getString("useBufferedStream"))
        .booleanValue();
    boolean useWriteObject = new Boolean(rb.getString("useWriteObject"))
        .booleanValue();
    boolean useSameObject = new Boolean(rb.getString("useSameObject"))
        .booleanValue();
    boolean useGC = new Boolean(rb.getString("useGC")).booleanValue();
    boolean useReset = new Boolean(rb.getString("useReset")).booleanValue();

    // Set properties of the client
    UTFClient cutf = new UTFClient();
    cutf.setServerPort(serverPort);
    cutf.setLoop(numberOfLoop);
    cutf.setImageFile(imageFile);
    cutf.setServerName(serverName);
    cutf.setUseBufferedStream(useBufferedStream);
    cutf.setUseWriteObject(useWriteObject);
    cutf.setUseGC(useGC);
    cutf.setUseSameObject(useSameObject);
    cutf.setUseReset(useReset);

    // Set properties of the server
    UTFServer sutf = new UTFServer();
    sutf.setServerPort(serverPort);
    
    // Start test
    long started = System.currentTimeMillis();
    long freeStart = Runtime.getRuntime().freeMemory();
    Thread server = new Thread(sutf);
    Thread client = new Thread(cutf);
    server.start();
    client.start();
    
    // Wait
    server.join();
    client.join();
    long end = System.currentTimeMillis();
    long freeEnd = Runtime.getRuntime().freeMemory();
    long usedMemory = (freeEnd - freeStart);
    long last = end - started;
    long time = last/1000;
    float average = (numberOfLoop / time);
    
    System.out.println("The test lasted "+time+" s. ("+last+" ms.) for an average of "+average+" loop per second. ");
    System.out.println("Used memory was:"+usedMemory/1024/1024+" Mb, "+usedMemory+" bytes.");
    assertEquals(cutf.getHexa(),sutf.getHexa());
  }
}