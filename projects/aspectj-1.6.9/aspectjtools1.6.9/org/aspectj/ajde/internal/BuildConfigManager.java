/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.ajde.internal;

import java.util.List;

import org.aspectj.ajde.ui.BuildConfigModel;

/** 
 * @author	Mik Kersten
 */
public interface BuildConfigManager {
	
	public static final String CONFIG_FILE_SUFFIX = ".lst";
	
	public static final String DEFAULT_CONFIG_LABEL = "<all project files>";
	
	/**
	 * Returns the currently active build configuration file.  The current active 
	 * build configuration file that is set in this class is used for building and 
	 * for updating the structure model.
	 * 
	 * @return	full path to the file
	 */
	public String getActiveConfigFile();
	
	/**
	 * Sets the currently active build configuration file.
	 * 
	 * @param	full path to the file
	 */
	public void setActiveConfigFile(String currConfigFilePath);

	/**
	 * Add a listner that will be notified of build configuration change events
	 */
    public void addListener(BuildConfigListener configurationListener);

	/**
	 * Remove a configuration listener.
	 */
    public void removeListener(BuildConfigListener configurationListener);
     
   	/**
   	 * Build a model for the corresponding configuration file.
   	 * 
   	 * @param	full path to the file
   	 */  
	public BuildConfigModel buildModel(String configFilePath);
	
	/**
	 * Save the given configuration model to the file that it was generated from.
	 */
	public void writeModel(BuildConfigModel model);
	
	/**
	 * Write a list of source files into a configuration file.  File paths will be
	 * written relative to the path of the configuration file.
	 */ 
	public void writePaths(String configFilePath, List paths);
	
	/**
	 * Add files to a configuration.
	 * 
	 * @param configFilePath	full path to the configuration file
	 * @param files			list of full paths to the files to be added
	 */
	public void addFilesToConfig(String configFilePath, List files);

	/**
	 * Remove files from a configuration.
	 * 
	 * @param configFilePath	full path to the configuration file
	 * @param files			list of full paths to the files to be removed
	 */  
	public void removeFilesFromConfig(String configFilePath, List files);
	
	
	/**
	 * @return list (of Strings) of all build configuration files
	 * found so far
	 */
	public List /*String*/ getAllBuildConfigFiles();
	
}


