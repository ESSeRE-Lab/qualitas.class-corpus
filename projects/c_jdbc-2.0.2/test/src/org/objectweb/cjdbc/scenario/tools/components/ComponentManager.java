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

package org.objectweb.cjdbc.scenario.tools.components;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class defines a ComponentManager
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class ComponentManager implements ComponentManagerInterface
{
  protected Hashtable processes = new Hashtable();
  
  
  /**
   * Instaciate a process managed by this component manager on the given port
   * This used the default configuration file returned by the 
   * <code>getDefaultConfigurationFile</code> method
   * @param port port to start the process on 
   * @return a reference to the newly started component
   * @throws Exception if fails
   */
  public ComponentInterface instanciateProcess(String port) throws Exception
  {
    return instanciateProcess(port,getDefaultConfigurationFile());
  }
  
  /**
   * Instaciate a process managed by this component manager on the given port
   * 
   * @param port port to start the process on
   * @param configurationFile used to instanciate the process 
   * @return a reference to the newly started component
   * @throws Exception if fails
   */
  public abstract ComponentInterface instanciateProcess(String port,String configurationFile) throws Exception;
  
  /**
   * The default configuration file to use with this component manager.
   * This should be specific to each component manager
   * 
   * @return the default filename
   */
  public abstract String getDefaultConfigurationFile();
  
  
  
  /**
   * Starts component on the given port with the given database
   * 
   * @param port to run hsql on
   * @param database to load component with
   * @return created process
   * @throws Exception if fails to create process
   */
  public ComponentInterface startComponent(String port, String database) throws Exception
  {
    return start(port, database);
  }

  /**
   * Starts component on the given port
   * 
   * @param port to run component on
   * @return created process
   * @throws Exception if fails to create process
   */
  public ComponentInterface startComponent(String port) throws Exception
  {
    return start(port);
  }

  /**
   * Check if can open a connection on localhost on the given port
   * 
   * @param port to open a socket for check
   * @throws Exception if fails
   */
  public void waitForStarted(String port) throws Exception
  {
    int retry = 10;
    while (!isStarted(port))
    {
      retry--;
      if (retry == 0)
        throw new IOException("I think the component is not started");
      synchronized (this)
      {
        this.wait(1000);
      }
    }
  }

  /**
   * Wait for the component to stop
   * 
   * @param port to check the connection on
   * @throws Exception if fails
   */
  public void waitForStopped(String port) throws Exception
  {
    int retry = 5;
    while (isStarted(port))
    {
      retry--;
      if (retry == 0)
        throw new IOException("I think the component is still started");
      synchronized (this)
      {
        this.wait(2000);
      }
    }
  }

  /**
   * Check if can open a connection on localhost on the given port
   * 
   * @param port to open a socket for check
   * @return true if can connect, false if exception or can't connect
   */
  public boolean isStarted(String port)
  {
    try
    {
      Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(),
          Integer.parseInt(port));
      socket.close();
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  /**
   * fill the database with raidb1 default configuration
   * 
   * @param port of the database
   * @throws Exception if fails
   */
  public void loaddatabase(String port) throws Exception
  {
    ((ComponentInterface) processes.get(port)).loadDatabase();
  }
  
  /**
   * Load the database with a given input file
   * 
   * @param port of the database to load
   * @param templateName name of the file to load(NO PATH!)
   * @throws Exception if fails
   */
  public void loaddatabase(String port, String templateName) throws Exception
  {
    ((ComponentInterface) processes.get(port)).loadDatabase(templateName);
  }
  
  /**
   * Load the database with a given input file
   * 
   * @param port of the database to load
   * @param templateName name of the file to load(NO PATH!)
   * @param target the target database to load
   * @throws Exception if fails
   */
  public void loaddatabase(String port, String templateName,String target) throws Exception
  {
    ((ComponentInterface) processes.get(port)).loadDatabase(templateName,target);
  }
  
  /**
   * Stop Hsql. Destroy the process so it looks like a failure.
   * 
   * @param process to stop
   */
  public void stop(ComponentInterface process)
  {
    // Kill the process and remove files
    if (process != null)
    {
      processes.remove(process.getPort());
      process.release();
    }
    try
    {
      waitForStopped(process.getPort());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Stop a component from its unique port number
   * 
   * @param componentOnPort port number of the component to stop
   */
  public void stop(String componentOnPort)
  {
    stop((ComponentInterface)processes.get(componentOnPort));
  }
  
  /**
   * Same as stop(String) 
   * 
   * @param port port number
   */
  public void stop(int port)
  {
    stop(""+port);
  }
  
  /**
   * Stops all process contained in this manager
   */
  public void stopAll()
  {
   this.release(); 
  }
  
  /**
   * Starts database component on the given port with the given database
   * 
   * @param port to run component on
   * @param database to load component with
   * @return created process
   * @throws Exception if fails to create process
   */
  public ComponentInterface start(String port, String database) throws Exception
  {
    ComponentInterface hs = instanciateProcess(port, database);
    waitForStarted(port);
    processes.put(port, hs);
    return hs;
  }
  
  /**
   * 
   * Starts database component on the given port with the default database
   * 
   * @param port to run component on
   * @return created process
   * @throws Exception if fails to create process
   */
  public ComponentInterface start(String port) throws Exception
  {
    return start(port,getDefaultConfigurationFile());
  }
  

  /**
   * release files locked by manager
   */
  public void release()
  {
    Iterator iter = processes.keySet().iterator();
    ComponentInterface component = null;
    while (iter.hasNext())
    {
      try
      {
        component = ((ComponentInterface) processes.get(iter.next()));
        component.release();
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentManagerInterface#simulateFailure(int, long, boolean)
   */
  public void simulateFailure(final int port, final long wait, boolean rand)
  {
    simulateFailure(""+port,wait,rand);    
  }
  
  /**
   * @see org.objectweb.cjdbc.scenario.tools.components.ComponentManagerInterface#simulateFailure(java.lang.String, long, boolean)
   */
  public void simulateFailure(final String port, final long wait, final boolean rand)
  {
    Thread t = new Thread()
    {
      public void run()
      {
        synchronized(this)
        {
          try
          {
            wait(wait);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
          ((ComponentInterface)processes.get(port)).release();
        }
      }
    };
    t.start();
  }
}