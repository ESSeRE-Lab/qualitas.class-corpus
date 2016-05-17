/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.common.jmx.mbeans;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.ControllerException;

/**
 * JMX Interface of the C-JDBC Controller.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public interface ControllerMBean
{

  //
  // Virtual databases management
  //

  /**
   * Registers one or several virtual databases in the controller. The
   * description of each Virtual Database must contain the definition of the
   * backends and components (cache, scheduler, load balancer) to use.
   * <p>
   * This function expects the content of an XML file conforming to the C-JDBC
   * DTD to be given as a single <code>String</code> object.
   * 
   * @param xml XML code to parse
   * @exception ControllerException if an error occurs while interpreting XML
   */
  void addVirtualDatabases(String xml) throws ControllerException;

  /**
   * Returns the names of currently available virtual databases.
   * 
   * @return ArrayList of <code>String</code> objects.
   */
  ArrayList getVirtualDatabaseNames();

  /**
   * Tests if a <code>VirtualDatabase</code> of a given name exists in this
   * controller.
   * 
   * @param name the virtual database name
   * @return <code>true</code> if the virtual database exists
   */
  boolean hasVirtualDatabase(String name);

  /**
   * Prevent the controller from accessing a virtual database thereafter
   * 
   * @param virtualname the virtual database name to remove
   * @return description message
   * @throws Exception if fails
   */
  String removeVirtualDatabase(String virtualname) throws Exception;

  //
  // Controller operations
  //

  /**
   * Adds a driver jar file sent in its binary form in the drivers directory of
   * the controller.
   * 
   * @param bytes the data in a byte array
   * @throws Exception if fails
   */
  void addDriver(byte[] bytes) throws Exception;

  /**
   * Generate a log report on the controller now
   * 
   * @return the content of the logreport
   * @throws Exception if fails
   */
  String generateLogReport() throws Exception;

  /**
   * Generate a report on the controller now
   * 
   * @return the content of the report
   * @throws Exception if fails
   */
  String generateReport() throws Exception;

  /**
   * Get the configuration of the logging now.
   * 
   * @return a <code>String</code> representing the logging configuration
   * @throws Exception if the logging configuration can not be returned
   */
  String getLoggingConfiguration() throws Exception;
  
  /**
   * Save current configuration of the controller to a default file location.
   * 
   * @return status message
   * @throws Exception if fails
   */
  String saveConfiguration() throws Exception;

  /**
   * Turns the controller down by using default shutdown level
   * 
   * @param level Smart,Fast or Immediate.
   * @throws ControllerException if unknown level or other error occurs.
   */
  void shutdown(int level) throws ControllerException;

  //
  // Controller information
  //

  /**
   * Get the controller socket backlog size.
   * 
   * @return the backlog size
   */
  int getBacklogSize();

  /**
   * Gets the controller name.
   * 
   * @return a <code>String</code> value containing the controller name.
   */
  String getControllerName();

  /**
   * Gets the JMX name of the controller.
   * 
   * @return a <code>String</code> value containing the jmx name of the
   *         controller
   */
  String getJmxName();

  /**
   * Return this controller port number
   * 
   * @return a <code>int</code> containing the port code number
   */
  int getPortNumber();

  /**
   * Gets the controller version.
   * 
   * @return a <code>String</code> value containing the version number
   * @throws RemoteException if an error occurs
   */
  String getVersionNumber() throws RemoteException;

  /**
   * Return the xml version of the controller.xml file without doc type
   * declaration, just data. The content is formatted using the controller xsl
   * stylesheet.
   * 
   * @return controller xml data
   */
  String getXml();

  /**
   * Is the controller shutting down ?
   * 
   * @return <tt>true</tt> if the controller is no more accepting connection
   */
  boolean isShuttingDown();

  /**
   * Set the controller socket backlog size.
   * 
   * @param size backlog size
   */
  void setBacklogSize(int size);

  // 
  // Logging system
  //

  /**
   * Refreshs the logging system configuration by re-reading the
   * <code>log4j.properties</code> file.
   * 
   * @exception ControllerException if the <code>log4j.properties</code> file
   *              cannot be found in classpath
   */
  void refreshLogConfiguration() throws ControllerException;

  /**
   * Update the log4j configuration file with the given content Also call
   * <code>refreshLogConfiguration</code> method
   * 
   * @param newConfiguration the content of the new log4j configuration
   * @throws IOException if cannot access the log4j file
   * @throws ControllerException if could not refresh the logs
   */
  void updateLogConfigurationFile(String newConfiguration) throws IOException,
      ControllerException;

  /**
   * Retrieve the content of the log4j configuration file
   * 
   * @return <code>String</code>
   * @throws IOException if IO problems
   */
  String viewLogConfigurationFile() throws IOException;

}