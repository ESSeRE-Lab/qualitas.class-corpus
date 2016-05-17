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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools.components;

/**
 * This class defines a ComponentManagerInterface. This class is used to start,
 * stop, management configuration of classes that implements the
 * <code>ComponentInterface</code> interface. <br>
 * Typically, this will be used to start database backends and controller, 
 * load database configuration (virtual or not) and simulate failure by 
 * stopping the component straight away or after a given time.<br>
 * 
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public interface ComponentManagerInterface
{
  /**
   * Instaciate a process managed by this component manager on the given port
   * This used the default configuration file returned by the
   * <code>getDefaultConfigurationFile</code> method
   * 
   * @param port port to start the process on
   * @return a reference to the newly started component
   * @throws Exception if fails
   */
  ComponentInterface instanciateProcess(String port) throws Exception;

  /**
   * Instaciate a process managed by this component manager on the given port
   * 
   * @param port port to start the process on
   * @param configurationFile used to instanciate the process
   * @return a reference to the newly started component
   * @throws Exception if fails
   */
  ComponentInterface instanciateProcess(String port, String configurationFile)
      throws Exception;

  /**
   * The default configuration file to use with this component manager. This
   * should be specific to each component manager
   * 
   * @return the default filename
   */
  String getDefaultConfigurationFile();

  /**
   * Starts component on the given port with the given database
   * 
   * @param port to run hsql on
   * @param database to load component with
   * @return created process
   * @throws Exception if fails to create process
   */
  ComponentInterface startComponent(String port, String database)
      throws Exception;

  /**
   * Starts component on the given port
   * 
   * @param port to run component on
   * @return created process
   * @throws Exception if fails to create process
   */
  ComponentInterface startComponent(String port) throws Exception;

  /**
   * Check if can open a connection on localhost on the given port
   * 
   * @param port to open a socket for check
   * @throws Exception if fails
   */
  void waitForStarted(String port) throws Exception;

  /**
   * Wait for the component to stop
   * 
   * @param port to check the connection on
   * @throws Exception if fails
   */
  void waitForStopped(String port) throws Exception;

  /**
   * Check if can open a connection on localhost on the given port
   * 
   * @param port to open a socket for check
   * @return true if can connect, false if exception or can't connect
   */
  boolean isStarted(String port);

  /**
   * fill the database with raidb1 default configuration
   * 
   * @param port of the database
   * @throws Exception if fails
   */
  void loaddatabase(String port) throws Exception;

  /**
   * Load the database with a given input file
   * 
   * @param port of the database to load
   * @param templateName name of the file to load(NO PATH!)
   * @throws Exception if fails
   */
  void loaddatabase(String port, String templateName) throws Exception;

  /**
   * Stop Hsql. Destroy the process so it looks like a failure.
   * 
   * @param process to stop
   */
  void stop(ComponentInterface process);

  /**
   * Stop a component from its unique port number
   * 
   * @param componentOnPort port number of the component to stop
   */
  void stop(String componentOnPort);

  /**
   * Same as stop(String)
   * 
   * @param port port number
   */
  void stop(int port);

  /**
   * Simulate a failure of the component by stopping it after the given time.
   * 
   * @param port the port of the component to stop
   * @param wait the wait time before stopping it
   * @param rand should we use a random time, if so, the previous argument is
   *          used as a range
   */
  void simulateFailure(String port, long wait, boolean rand);

  /**
   * Simulate a failure of the component by stopping it after the given time.
   * 
   * @param port the port of the component to stop
   * @param wait the wait time before stopping it
   * @param rand should we use a random time, if so, the previous argument is
   *          used as a range
   */
  void simulateFailure(int port, long wait, boolean rand);

  /**
   * Stops all process contained in this manager
   */
  void stopAll();

  /**
   * Starts database component on the given port with the given database
   * 
   * @param port to run component on
   * @param database to load component with
   * @return created process
   * @throws Exception if fails to create process
   */
  ComponentInterface start(String port, String database) throws Exception;

  /**
   * Starts database component on the given port with the default configuration
   * 
   * @param port to run component on
   * @return created process
   * @throws Exception if fails to create process
   */
  ComponentInterface start(String port) throws Exception;

  /**
   * release files and processes owned by manager
   */
  void release();
}