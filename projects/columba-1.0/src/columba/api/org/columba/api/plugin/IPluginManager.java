// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.api.plugin;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.columba.api.exception.PluginHandlerNotFoundException;

/**
 * Plugin manager is a singleton registry for all plugins and all
 * extension handlers.
 * 
 * @author fdietz
 *
 */
public interface IPluginManager {

	/**
	 * Add extension handler to manager.
	 * 
	 * @param id		handler id
	 * @param handler	extension handler
	 */
	public void addHandler(String id, IExtensionHandler handler);
	
	/**
	 * Retrieve extension handler.
	 * 
	 * @param id		extension handler id
	 * @return			extension handler
	 * @throws PluginHandlerNotFoundException
	 */
	public IExtensionHandler getHandler(String id) throws PluginHandlerNotFoundException;
	
	/**
	 * Add plugin to plugin manager.
	 * 
	 * @param folder	directory containing folder
	 * @return			plugin id
	 */
	public String addPlugin(File folder);
	
	/**
	 * Get plugin config file (config.xml).
	 * 
	 * @param id		plugin id
	 * @return			plugin config file
	 */
	public File getPluginConfigFile(String id);
	
	/**
	 * Get plugin metadata
	 * 
	 * @param id		plugin id
	 * @return			plugin metadata
	 */
	public PluginMetadata getPluginMetadata(String id);
	
	/**
	 * Get URL pointing to the Readme file shipped with the plugin.
	 * 
	 * @param id		plugin id
	 * @return			URL to readme file
	 */
	public URL getInfoURL(String id);
	
	/**
	 * Get array of available plugin ids.
	 * 
	 * @return	array of plugin ids
	 */
	public String[] getPluginIds();
	
	/**
	 * Get enumeration of plugin metadata.
	 * 
	 * @return		plugin metadata enumeration
	 */
	public Enumeration getPluginMetadataEnumeration();
	
	/**
	 * initialize all core plugins in "/plugin" folder. 
	 */
	public void initCorePlugins();
	
	/**
	 * initialize all other plugins in "/plugin" folder.
	 *
	 */
	public void initExternalPlugins();
	
	/**
	 * Add handlers from xml resource.
	 * 
	 * @param xmlResource	xml resource
	 */
	public void addHandlers(String xmlResource);
}
