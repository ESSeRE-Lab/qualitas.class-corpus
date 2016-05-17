/**
 * C-JDBC: Clustered JDBC. Copyright (C) 2002-2004 French National Institute For
 * Research In Computer Science And Control (INRIA). Contact:
 * c-jdbc@objectweb.org This library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA. Initial developer(s): Nicolas Modrzyk Contributor(s):
 * ______________________.
 */

package org.objectweb.cjdbc.scenario.standalone.socket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * This class defines a UTFServer class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class UTFServer implements Runnable
{
  int          serverPort = 7603;

  ServerSocket serverSocket;
  String       hexa;

  /**
   * Starts the UTFServer thread
   */
  public void run()
  {
    try
    {
      serverSocket = new ServerSocket(serverPort);
      Socket clientSocket = serverSocket.accept();

      ObjectInputStream in = new ObjectInputStream(clientSocket
          .getInputStream());
      boolean useBufferedStream = in.readBoolean();
      if (useBufferedStream)
        in = new ObjectInputStream(new BufferedInputStream(clientSocket
            .getInputStream()));

      boolean useWriteObject = in.readBoolean();
      boolean useGC = in.readBoolean();
      boolean useReset = in.readBoolean();
      int loop = in.readInt();

      System.out.println("Starting new test with:"
          + clientSocket.getInetAddress().getHostName());
      System.out.println("useWriteObject:" + useWriteObject);
      System.out.println("useGC:" + useGC);
      System.out.println("useReset:" + useReset);
      System.out.println("loop:" + loop);
      System.gc();
      long freeStart = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      System.out.println("Free Memory at start:" + (freeStart / 1024 / 1024)
          + " MBytes.");

      
      if (useWriteObject)
      {
        for (int i = 0; i < loop; i++)
        {
          hexa = (String) in.readObject();
          if (useGC)
            System.gc();
          if (useReset)
            in.reset();
        }
      }
      else
      {
        for (int i = 0; i < loop; i++)
        {

          int size = in.readInt();
          byte[] bytes = new byte[size];
          in.readFully(bytes);
          hexa = new String(bytes);
        }
      }

      long freeEnd = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      long usedMemory = (freeEnd - freeStart);
      System.out.println("Free Memory at end:" + freeEnd / 1024 / 1024
          + " MBytes");
      System.out.println("Used Memory:" + usedMemory / 1024 / 1024 + " MBytes");
      System.out.println("Ending test with:"
          + clientSocket.getInetAddress().getHostName());

    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally
    {
      try
      {
        serverSocket.close();
      }
      catch (IOException e1)
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
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
   * Standalone application
   * @param args not needed
   * @throws Exception if fails
   */
  public static void main(String[] args) throws Exception
  {
    UTFServer sutf = new UTFServer();
    while (true)
    {
      sutf.run();
    }
  }
}