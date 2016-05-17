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

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.objectweb.cjdbc.common.stream.encoding.HexaEncoding;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a UTFClient class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class UTFClient implements Runnable
{
  String          hexa;
  int             serverPort        = 7603;
  boolean         useWriteObject    = false;
  private int     loop              = 1;
  String          imageFile         = "/image/cinema.pdf";
  String          serverName        = "localhost";
  private boolean useBufferedStream = false;
  private boolean useGC             = false;
  private boolean useSameObject;
  private boolean useReset;

  /**
   * @return Returns the serverName.
   */
  public String getServerName()
  {
    return serverName;
  }

  /**
   * @param serverName The serverName to set.
   */
  public void setServerName(String serverName)
  {
    this.serverName = serverName;
  }

  /**
   * @return Returns the loop.
   */
  public int getLoop()
  {
    return loop;
  }

  /**
   * @param loop The loop to set.
   */
  public void setLoop(int loop)
  {
    this.loop = loop;
  }

  /**
   * @return Returns the serverPort.
   */
  public int getServerPort()
  {
    return serverPort;
  }

  /**
   * @param serverPort The serverPort to set.
   */
  public void setServerPort(int serverPort)
  {
    this.serverPort = serverPort;
  }

  /**
   * @return Returns the useWriteObject.
   */
  public boolean getUseWriteObject()
  {
    return useWriteObject;
  }

  /**
   * @param useWriteObject The useWriteObject to set.
   */
  public void setUseWriteObject(boolean useWriteObject)
  {
    this.useWriteObject = useWriteObject;
  }

  /**
   * @return Returns the imageFile.
   */
  public String getImageFile()
  {
    return imageFile;
  }

  /**
   * @param imageFile The imageFile to set.
   */
  public void setImageFile(String imageFile)
  {
    this.imageFile = imageFile;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {

      File image = new File(getClass().getResource(imageFile).getFile());
      hexa = HexaEncoding.data2hex(ScenarioUtility.readBinary(image));
      byte[] bytes = hexa.getBytes();
      Socket socket = new Socket(serverName, serverPort);
      ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

      out.writeBoolean(useBufferedStream);
      if (useBufferedStream)
        out = new ObjectOutputStream(socket.getOutputStream());

      out.writeBoolean(useWriteObject);
      out.writeBoolean(useGC);
      out.writeBoolean(false);
      out.writeInt(loop);
      out.flush();

      if (useWriteObject)
      {
        for (int i = 0; i < loop; i++)
        {
          System.out.println(i);
          if (!useSameObject)
            out.writeObject(HexaEncoding.data2hex(ScenarioUtility
                .readBinary(image)));
          else
            out.writeObject(hexa);
          out.flush();
          if (useGC)
            System.gc();
          if (useReset)
            out.reset();
        }
      }
      else
      {
        for (int i = 0; i < loop; i++)
        {
          System.out.println(i);
          out.writeInt(bytes.length);
          out.write(bytes);
          out.flush();
        }
      }

      socket.close();
    }
    catch (UnknownHostException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * @return Returns the hexa.
   */
  public String getHexa()
  {
    return hexa;
  }

  /**
   * @return Returns the useBufferedStream.
   */
  public boolean getUseBufferedStream()
  {
    return useBufferedStream;
  }

  /**
   * @param useBufferedStream The useBufferedStream to set.
   */
  public void setUseBufferedStream(boolean useBufferedStream)
  {
    this.useBufferedStream = useBufferedStream;
  }

  /**
   * Standalone application
   * 
   * @param args not needed, use the properties file instead
   * @throws Exception if fails
   */
  public static void main(String[] args) throws Exception
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

    // Start test
    long started = System.currentTimeMillis();
    long freeStart = Runtime.getRuntime().totalMemory()
        - Runtime.getRuntime().freeMemory();
    Thread client = new Thread(cutf);
    client.start();

    // Wait
    client.join();
    long end = System.currentTimeMillis();
    long freeEnd = Runtime.getRuntime().totalMemory()
        - Runtime.getRuntime().freeMemory();
    long usedMemory = (freeEnd - freeStart);
    long last = end - started;
    long time = last / 1000;
    float average1 = ((float) numberOfLoop / (float) time);
    float average2 = ((float) time / (float) numberOfLoop);

    System.out.println("The test lasted " + time + " s. (" + last
        + " ms.) for an average of " + average1
        + " loop(s) per second (or 1 loop takes :" + average2 + "seconds");
    System.out.println("Used memory was:" + usedMemory / 1024 / 1024 + " Mb, "
        + usedMemory + " bytes.");
  }

  /**
   * @param useReset
   */
  void setUseReset(boolean useReset)
  {
    this.useReset = useReset;
  }

  /**
   * @param useSameObject
   */
  void setUseSameObject(boolean useSameObject)
  {
    this.useSameObject = useSameObject;
  }

  /**
   * @param useGC
   */
  void setUseGC(boolean useGC)
  {
    this.useGC = useGC;

  }
}