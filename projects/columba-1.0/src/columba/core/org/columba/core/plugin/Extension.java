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
package org.columba.core.plugin;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.columba.api.exception.PluginException;
import org.columba.api.plugin.ExtensionMetadata;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionInterface;
import org.columba.api.plugin.PluginMetadata;
import org.columba.core.logging.Logging;

/**
 * An extension providing the metadata of an extension and the runtime context
 * for instanciation.
 * 
 * @author fdietz
 */
public class Extension implements IExtension {

	private static final String FILE_PLUGIN_JAR = "plugin.jar";

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.core.plugin");

	private ExtensionMetadata metadata;

	private PluginMetadata pluginMetadata;

	private boolean internalPlugin;

	private IExtensionInterface cachedInstance;

	/**
	 * @param metadata
	 */
	public Extension(ExtensionMetadata metadata) {
		this.metadata = metadata;

		internalPlugin = true;
	}

	/**
	 * @param pluginMetadata
	 * @param metadata
	 */
	public Extension(PluginMetadata pluginMetadata, ExtensionMetadata metadata) {
		this.metadata = metadata;
		this.pluginMetadata = pluginMetadata;

		internalPlugin = false;
	}

	/**
	 * @see org.columba.api.plugin.IExtension#getMetadata()
	 */
	public ExtensionMetadata getMetadata() {
		return metadata;
	}

	/**
	 * @see org.columba.api.plugin.IExtension#instanciateExtension(java.lang.Object[])
	 */
	public IExtensionInterface instanciateExtension(Object[] arguments)
			throws PluginException {

		String id = null;
		File pluginDirectory = null;

		String className = metadata.getClassname();

		if (pluginMetadata != null) {
			id = pluginMetadata.getId();
			// if external plugin, we need the directory of it
			pluginDirectory = pluginMetadata.getDirectory();
		}

		IExtensionInterface plugin = null;

		// if available, load cached instance
		if ((metadata.isSingleton()) && (cachedInstance != null)) {
			plugin = cachedInstance;

		} else {

			try {
				if (isInternal())
					if (className.endsWith(".groovy")) {
						// use Groovy classloader
						plugin = instanciateGroovyClass(arguments,
								pluginDirectory, className);
					} else {
						// use default Java classlodaer
						plugin = new DefaultPluginLoader().loadPlugin(id,
								className, arguments);
					}
				else {
					// external plugin
					// -> use external classloader
					if (className.endsWith(".groovy")) {
						// use Groovy classloader, wrapped in external URL
						// classloader
						// TODO: still needs to be wrapped in external URL
						// classloader,
						// nevertheless, it seems to work this way - but don't
						// know why?!
						plugin = instanciateGroovyClass(arguments,
								pluginDirectory, className);
					} else {
						// use external Java URL classloader
						plugin = instanciateExternalJavaClass(arguments,
								pluginDirectory, className);
					}

				}

				// remember instance
				if (metadata.isSingleton())
					cachedInstance = plugin;

			} catch (Exception e) {
				handleException(e);
			} catch (Error e) {
				handleException(e);
			}

		}
		return plugin;
	}

	/**
	 * @param e
	 */
	private void handleException(Throwable e) {
		if (e.getCause() != null) {
			LOG.severe(e.getCause().getMessage());
			if (Logging.DEBUG)
				e.getCause().printStackTrace();
		} else {
			LOG.severe(e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		}
	}

	/**
	 * Instanciate external Java class which is specified using the
	 * <code>plugin.xml</code> descriptor file.
	 * 
	 * @param arguments
	 *            class constructor arguments
	 * @param pluginDirectory
	 *            plugin directory
	 * @param className
	 *            extension classname
	 * @return extension instance
	 * @throws Exception
	 */
	private IExtensionInterface instanciateExternalJavaClass(
			Object[] arguments, File pluginDirectory, String className)
			throws Exception {
		IExtensionInterface plugin;
		URL[] urls = null;

		// all Java plugins package their class-files in "plugin.jar"
		String jarFilename = Extension.FILE_PLUGIN_JAR;

		try {
			urls = getURLs(pluginDirectory, jarFilename);
		} catch (MalformedURLException e) {
			// should never happen
			e.printStackTrace();
		}

		ExternalClassLoader loader = new ExternalClassLoader(urls);

		plugin = (IExtensionInterface) loader.instanciate(className, arguments);

		return plugin;
	}

	/**
	 * Instaciate groovy script class.
	 * 
	 * @param arguments
	 *            class arguments
	 * @param pluginDirectory
	 *            plugin directory
	 * @param className
	 *            groovy classname
	 * @return extension interface
	 * @throws Exception
	 */
	private IExtensionInterface instanciateGroovyClass(Object[] arguments,
			File pluginDirectory, String className) throws Exception {
		IExtensionInterface plugin;
		GroovyClassLoader gcl = new GroovyClassLoader(getClass()
				.getClassLoader());

		if (!className.endsWith(".groovy"))
			className = className + ".groovy";

		File groovyFile = new File(pluginDirectory, className);

		Class clazz = gcl.parseClass(groovyFile);
		Constructor constr = ClassLoaderHelper
				.findConstructor(arguments, clazz);

		Object object = null;
		if (constr != null)
			object = constr.newInstance(arguments);
		else
			object = clazz.newInstance();

		plugin = (IExtensionInterface) object;
		return plugin;
	}

	/**
	 * @see org.columba.api.plugin.IExtension#isInternal()
	 */
	public boolean isInternal() {
		return internalPlugin;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("id=" + metadata.getId());
		buf.append(" class=" + metadata.getClassname());

		return buf.toString();
	}

	/**
	 * Generate array of all URLs which are used to prefill the URLClassloader.
	 * <p>
	 * All jar-files in /lib directory are automatically added.
	 * 
	 * @param file
	 *            plugin directory
	 * @param jarFile
	 *            jar-file containing the extension code
	 * @return URL array
	 * @throws MalformedURLException
	 */
	private URL[] getURLs(File file, String jarFile)
			throws MalformedURLException {
		List urlList = new Vector();

		// plugin-directory
		String path = file.getPath();

		if (jarFile != null) {
			URL jarURL = new File(file, jarFile).toURL();
			urlList.add(jarURL);
		}

		URL newURL = new File(path).toURL();
		urlList.add(newURL);

		// we add every jar-file in /lib, too
		// plugin-directory

		File lib = new File(file, "lib");

		if (lib.exists()) {
			File[] libList = lib.listFiles();

			for (int i = 0; i < libList.length; i++) {
				File f = libList[i];

				if (f.getName().endsWith(".jar")) {
					// jar-file found
					urlList.add(f.toURL());
				} else if (f.isDirectory()) {
					urlList.add(f.toURL());
				}
			}
		}

		URL[] url = new URL[urlList.size()];

		for (int i = 0; i < urlList.size(); i++) {
			url[i] = (URL) urlList.get(i);
		}

		if (Logging.DEBUG) {
			for (int i = 0; i < url.length; i++) {
				LOG.finest("url[" + i + "]=" + url[i]);
			}
		}

		return url;
	}

}
